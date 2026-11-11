package com.dudu.run.simulation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Explicit user-triggered POI queries, using the existing client map Key and platform TLS. */
final class PlaceSearch {
    static final class Place {
        final String title,address;final LatLng point;
        Place(String title,String address,double lat,double lon){this.title=title;this.address=address;point=new LatLng(lat,lon);}
    }
    private PlaceSearch(){}
    static List<Place> find(Context context,String keyword,String city,LatLng center)throws Exception{
        String key=MapKeySettings.serviceKey(context);
        if(key.trim().isEmpty())throw new IOException("应用尚未配置腾讯地图 Key");
        Uri.Builder uri=Uri.parse("https://apis.map.qq.com/ws/place/v1/suggestion").buildUpon()
                .appendQueryParameter("key",key).appendQueryParameter("keyword",keyword)
                .appendQueryParameter("page_size","20").appendQueryParameter("page_index","1");
        if(!city.isEmpty()&&!"全国".equals(city))uri.appendQueryParameter("region",city).appendQueryParameter("region_fix","1");
        if(center!=null)uri.appendQueryParameter("location",String.format(Locale.US,"%.6f,%.6f",center.latitude,center.longitude));
        HttpURLConnection connection=(HttpURLConnection)new URL(uri.build().toString()).openConnection();
        connection.setConnectTimeout(10000);connection.setReadTimeout(10000);connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept","application/json");
        try{
            if(connection.getResponseCode()!=200)throw new IOException("搜索服务暂不可用（HTTP "+connection.getResponseCode()+"）");
            ByteArrayOutputStream data=new ByteArrayOutputStream();
            try(InputStream input=connection.getInputStream()){
                byte[] buffer=new byte[4096];int n;
                while((n=input.read(buffer))!=-1){
                    if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();
                    if(data.size()+n>1024*1024)throw new IOException("搜索响应过大");data.write(buffer,0,n);
                }
            }
            JSONObject response=new JSONObject(new String(data.toByteArray(),StandardCharsets.UTF_8));
            int code=response.optInt("status",-1);
            if(code!=0){
                if(code==199)throw new IOException("搜索 Key 尚未开启 WebService API，请在腾讯控制台启用（199）");
                if(code==110||code==112)throw new IOException("搜索请求来源未获授权，请检查 Key 的域名/IP 限制（"+code+"）");
                if(code==111)throw new IOException("Key 要求签名校验，当前搜索请求未配置此方式（111）");
                if(code==120)throw new IOException("搜索请求过于频繁，请稍后重试（120）");
                if(code==121)throw new IOException("今日搜索额度已用完，请在控制台查看关键词输入提示的日配额（121）");
                throw new IOException("地点搜索失败（"+code+"）："+response.optString("message","服务异常").replace(key,"[Key]"));
            }
            ArrayList<Place> places=new ArrayList<>();JSONArray rows=response.optJSONArray("data");
            if(rows!=null)for(int i=0;i<rows.length();i++){
                JSONObject row=rows.optJSONObject(i);if(row==null)continue;
                JSONObject location=row.optJSONObject("location");if(location==null)continue;
                double lat=location.optDouble("lat",Double.NaN),lon=location.optDouble("lng",Double.NaN);
                if(Double.isNaN(lat)||Double.isNaN(lon)||Math.abs(lat)>90||Math.abs(lon)>180)continue;
                String address=row.optString("address","");
                if(address.isEmpty())address=row.optString("province","")+row.optString("city","")+row.optString("district","");
                places.add(new Place(row.optString("title","未命名地点"),address,lat,lon));
            }
            return places;
        }finally{connection.disconnect();}
    }
}
