package com.dudu.run.simulation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Walking polylines only. Bounded user-edit-triggered requests; no retry or chord fallback. */
final class RoadSnapper {
    private final Map<String,List<SimulationEngine.Point>> cache=new LinkedHashMap<String,List<SimulationEngine.Point>>(32,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<String,List<SimulationEngine.Point>> e){return size()>100;}
    };
    private volatile HttpURLConnection active;
    void cancel(){HttpURLConnection c=active;if(c!=null)c.disconnect();}
    List<SimulationEngine.Point> snap(Context context,List<SimulationEngine.Point> source)throws Exception{
        List<Integer> guides=RoadRouteGeometry.guides(source);
        String key=MapKeySettings.serviceKey(context);
        if(key.trim().isEmpty())throw new IOException("尚未配置地图 Key，已保留原线");
        long deadline=android.os.SystemClock.elapsedRealtime()+45000;
        ArrayList<SimulationEngine.Point> result=new ArrayList<>();
        for(int i=1;i<guides.size();i++){
            check(deadline);
            int first=guides.get(i-1),last=guides.get(i);
            SimulationEngine.Point a=source.get(first),b=source.get(last);
            String id=coordinate(a)+">"+coordinate(b)+":"+a.altitude;
            List<SimulationEngine.Point> leg=cache.get(id);
            if(leg==null){leg=request(key,a,b,deadline);check(deadline);cache.put(id,leg);}
            RoadRouteGeometry.validateLeg(source.subList(first,last+1),leg);
            RoadRouteGeometry.appendLeg(result,leg);
        }
        if(result.size()<2)throw new IOException("未找到步行道路，已保留原线");
        // Preserve the held run origin. A large off-road connector is not silently invented.
        if(SimulationEngine.distance(source.get(0),result.get(0))>8)
            throw new IOException("起点距可用道路超过 8 米，不能自动连接；请关闭吸附后手动画出连接段");
        if(SimulationEngine.distance(source.get(0),result.get(0))>.02)result.add(0,source.get(0));
        else result.set(0,source.get(0));
        if(result.size()>5000)throw new IOException("道路点过多，已保留原线");
        check(deadline);return result;
    }
    private static String coordinate(SimulationEngine.Point p){return String.format(Locale.US,"%.7f,%.7f",p.latitude,p.longitude);}
    private static void check(long deadline)throws IOException{
        if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("已取消道路吸附");
        if(android.os.SystemClock.elapsedRealtime()>deadline)throw new java.net.SocketTimeoutException("道路吸附超时，已保留原线");
    }
    private List<SimulationEngine.Point> request(String key,SimulationEngine.Point a,SimulationEngine.Point b,long deadline)throws Exception{
        Uri uri=Uri.parse("https://apis.map.qq.com/ws/direction/v1/walking/").buildUpon().appendQueryParameter("key",key)
            .appendQueryParameter("from",coordinate(a)).appendQueryParameter("to",coordinate(b)).build();
        HttpURLConnection connection=(HttpURLConnection)new URL(uri.toString()).openConnection();active=connection;
        connection.setConnectTimeout(8000);connection.setReadTimeout(8000);connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept","application/json");
        try{
            check(deadline);
            if(connection.getResponseCode()!=200)throw new IOException("步行道路服务暂不可用，已保留原线");
            ByteArrayOutputStream data=new ByteArrayOutputStream();
            try(InputStream input=connection.getInputStream()){
                byte[] buffer=new byte[4096];int n;
                while((n=input.read(buffer))!=-1){check(deadline);if(data.size()+n>1024*1024)throw new IOException("道路响应过大，已保留原线");data.write(buffer,0,n);}
            }
            JSONObject response=new JSONObject(new String(data.toByteArray(),StandardCharsets.UTF_8));int code=response.optInt("status",-1);
            if(code!=0){
                if(code==121)throw new IOException("今日步行路线额度已用完，已保留原线；可关闭道路吸附继续手绘");
                if(code==120)throw new IOException("道路请求过于频繁，请稍后重试；已保留原线");
                if(code==199)throw new IOException("Key 未开启步行路线所需 WebService API，已保留原线");
                throw new IOException("道路吸附失败（"+code+"），请检查地图服务权限或配额；已保留原线");
            }
            JSONArray routes=response.getJSONObject("result").getJSONArray("routes");
            if(routes.length()==0)throw new IOException("附近没有可用的步行道路，已保留原线");
            JSONArray encoded=routes.getJSONObject(0).getJSONArray("polyline");
            if(encoded.length()<4||encoded.length()%2!=0||encoded.length()>10000)throw new IOException("道路点数据不完整，已保留原线");
            ArrayList<SimulationEngine.Point> points=new ArrayList<>();double lat=0,lon=0;
            for(int i=0;i<encoded.length();i+=2){
                lat=i==0?encoded.getDouble(i):lat+encoded.getDouble(i)/1000000;
                lon=i==0?encoded.getDouble(i+1):lon+encoded.getDouble(i+1)/1000000;
                SimulationEngine.Point p=new SimulationEngine.Point(lat,lon,a.altitude);
                if(points.isEmpty()||SimulationEngine.distance(points.get(points.size()-1),p)>.0001)points.add(p);
            }
            return points;
        }finally{if(active==connection)active=null;connection.disconnect();}
    }
}
