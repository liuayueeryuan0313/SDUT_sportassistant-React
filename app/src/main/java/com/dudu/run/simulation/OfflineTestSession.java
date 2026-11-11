package com.dudu.run.simulation;

import android.app.Activity;
import android.content.*;
import android.util.AtomicFile;
import com.dudu.run.c;
import com.dudu.run.bean.LatLngInfo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Local test identity and data store. Never changes the real login preferences. */
public final class OfflineTestSession {
    public static final String ACCOUNT="offline-test";
    private static Context app;
    private OfflineTestSession() {}
    public static void init(Context context){app=context.getApplicationContext();}
    public static boolean active(){return app!=null&&app.getSharedPreferences("offline_test_session",0).getBoolean("active",false);}
    public static void enter(Context context){
        init(context);
        if(active())return;
        if(c.n().q()!=0)throw new IllegalStateException("请先结束当前跑步，再进入离线测试");
        // Switch cache namespace before resetting the test recorder.
        app.getSharedPreferences("offline_test_session",0).edit().putBoolean("active",true).commit();
        SimulationController.get(app).stop();c.n().K();c.n().B();
    }
    public static void leave(){
        SimulationController.get(app).stop();c.n().L();c.n().K();c.n().B();
        app.getSharedPreferences("offline_test_session",0).edit().putBoolean("active",false).commit();
    }
    public static void startRun(Activity activity,SimulationEngine.Config config){
        if(!active())throw new IllegalStateException("请从登录页进入离线测试");
        if(c.n().q()!=0){
            if(!SimulationController.get(app).enabled())throw new IllegalStateException("本次模拟已停止，请先保存或放弃当前测试");
            activity.startActivity(new Intent(activity,com.dudu.run.activity.SportMapActivity.class));return;
        }
        if(config==null)throw new IllegalStateException("请先绘制路线，或载入示例路线");
        c.n().K();c.n().B();
        SimulationController.get(app).start(config);
        c.n().D(1);c.n().F(1000000);c.n().G(Collections.emptyList());
        activity.startActivity(new Intent(activity,com.dudu.run.activity.SportMapActivity.class)
                .putExtra("isNeedCountDownView",true));
    }
    public static void startWaitingRun(Activity activity,SimulationEngine.Point origin){
        if(!active())throw new IllegalStateException("请先进入离线测试");
        if(c.n().q()!=0)throw new IllegalStateException("请返回当前测试跑步");
        c.n().K();c.n().B();SimulationController.get(app).prepareForRun(origin);
        c.n().D(1);c.n().F(1000000);c.n().G(Collections.emptyList());
        activity.startActivity(new Intent(activity,com.dudu.run.activity.SportMapActivity.class).putExtra("isNeedCountDownView",true));
    }
    public static SimulationEngine.Config demo(){
        if(c.n().q()!=0)throw new IllegalStateException("请先结束当前测试");
        SimulationEngine.Config saved=SimulationController.get(app).load();
        List<SimulationEngine.Point> points=new ArrayList<>();
        for(int i=0;i<60;i++){
            double t=i*Math.PI*2/60;
            points.add(new SimulationEngine.Point(36.0+.0007*Math.cos(t),117.0+.0012*Math.sin(t),saved.points.get(0).altitude));
        }
        return new SimulationEngine.Config(points,saved.speedKmh,saved.speedVariationKmh,saved.cadence,saved.cadenceVariation,
                saved.altitudeVariation,saved.routeOffsetMeters,false,false);
    }
    private static AtomicFile store(){return new AtomicFile(new File(app.getFilesDir(),"offline_test_runs.json"));}
    public static synchronized JSONArray records() throws Exception{
        AtomicFile file=store();
        try{return new JSONArray(new String(file.readFully(),StandardCharsets.UTF_8));}
        catch(FileNotFoundException missing){return new JSONArray();}
    }
    private static void write(JSONArray records) throws Exception{
        AtomicFile file=store();FileOutputStream output=null;
        try{output=file.startWrite();output.write(records.toString().getBytes(StandardCharsets.UTF_8));file.finishWrite(output);}
        catch(Exception error){if(output!=null)file.failWrite(output);throw error;}
    }
    public static synchronized void saveRun() throws Exception{
        if(!active())throw new IllegalStateException("离线测试未启用");
        if(c.n().q()==0)throw new IllegalStateException("尚未开始测试跑步");
        c.n().L();
        JSONObject record=new JSONObject();record.put("id",UUID.randomUUID().toString());record.put("account",ACCOUNT);
        record.put("localOnly",true);record.put("startedAt",c.n().q());record.put("savedAt",System.currentTimeMillis());
        record.put("seconds",c.n().s());record.put("meters",c.n().r());
        JSONArray track=new JSONArray();
        for(LatLngInfo point:c.n().t()){
            JSONArray row=new JSONArray();row.put(point.latLng.latitude);row.put(point.latLng.longitude);row.put(point.isStartPosition);track.put(row);
        }
        record.put("track",track);
        JSONArray old=records(),next=new JSONArray();next.put(record);
        for(int i=0;i<Math.min(19,old.length());i++)next.put(old.get(i));
        write(next); // Only discard the current run after the atomic save succeeds.
        SimulationController.get(app).stop();c.n().K();c.n().B();
    }
    public static synchronized void clearRecords() throws Exception{write(new JSONArray());}
}
