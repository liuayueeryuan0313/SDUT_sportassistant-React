package com.dudu.run.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.dudu.run.bean.*;
import com.dudu.run.simulation.OfflineTestSession;
import com.dudu.run.simulation.SimulationController;
import java.lang.ref.WeakReference;
import java.util.*;
import org.json.*;

/** One upload per run; UI cancellation never retries or cancels an ambiguous upload. */
public final class RunResultSession {
    public static final int WAITING=0,READY=1,FAILED=2,TIMED_OUT=3;
    public interface Listener { void changed(RunResultSession session); }
    private static RunResultSession current;
    private static long recordsVersion;
    public static long recordsVersion(){return recordsVersion;}
    public final long runId;
    public final boolean localOnly;
    public volatile int state=WAITING;
    public int recordId;
    public String message="";
    public RecordDetailResponseData detail;
    public List<LatLngInfo> track;
    private final Context app;
    private final String owner,beginTime;
    private final Handler main=new Handler(Looper.getMainLooper());
    private WeakReference<Listener> listener=new WeakReference<>(null);
    private boolean started,uploaded,rejected,recordsChanged;
    private volatile boolean possiblySubmitted;
    private volatile boolean durable;
    private boolean abandoned;
    private int generation;
    private final int campus,seconds;
    private final long endedAt;
    private final float meters;
    private final long steps;
    private final boolean stepsKnown;
    private long deadline;
    private List<LatLngInfo> submission;
    private final Runnable timeout=()->finish(TIMED_OUT,"结果获取超时，已退出。可稍后在跑步记录查看。");
    private final Runnable retry=this::lookup;

    private static String owner(){return String.valueOf(com.dudu.run.b.c().n());}
    private static SharedPreferences store(Context c){return c.getSharedPreferences("pending_run_result",0);}
    private static boolean marked(Context c,long id){return store(c).getLong("run",0)==id&&owner().equals(store(c).getString("owner",""));}
    public static boolean hasPending(Context c,long id){
        return id!=0&&((current!=null&&current.runId==id&&current.owner.equals(owner())&&!current.rejected
                &&(current.state==WAITING||current.possiblySubmitted))||marked(c,id));
    }
    public static RunResultSession prepare(Context context){
        com.dudu.run.c manager=com.dudu.run.c.n();long id=manager.q();
        if(id==0)throw new IllegalStateException("尚未开始跑步");
        if(current!=null&&current.runId==id&&current.owner.equals(owner())&&current.state==WAITING)return current;
        boolean recovery=marked(context,id)||(current!=null&&current.runId==id&&current.owner.equals(owner())&&current.possiblySubmitted);
        if(manager.y()||manager.m()<=0)manager.L();manager.K();
        RunResultSession previous=current;
        RunResultSession next=new RunResultSession(context,id,OfflineTestSession.active(),!recovery);
        if(previous!=null&&previous.runId==id&&previous.owner.equals(owner()))next.durable=previous.durable;
        return current=next;
    }
    public static RunResultSession resolve(Context context,long id,boolean local){
        if(current!=null&&current.runId==id&&current.owner.equals(owner())&&(current.state==WAITING||current.state==READY))return current;
        // Restoring an Activity/process is a read-only lookup, never another upload.
        RunResultSession previous=current;
        RunResultSession next=new RunResultSession(context,id,local,false);
        if(previous!=null&&previous.runId==id&&previous.owner.equals(owner()))next.durable=previous.durable;
        return current=next;
    }
    public static RunResultSession ready(long id,int recordId){
        return current!=null&&current.runId==id&&current.recordId==recordId&&current.owner.equals(owner())&&current.state==READY?current:null;
    }
    private RunResultSession(Context context,long id,boolean local,boolean submit){
        app=context.getApplicationContext();runId=id;localOnly=local;owner=owner();beginTime=com.dudu.run.utils.e.b(id);
        com.dudu.run.c manager=com.dudu.run.c.n();
        campus=manager.k();seconds=manager.s();meters=manager.r();endedAt=manager.m();
        steps=manager.recordedSteps();stepsKnown=manager.hasRecordedSteps();
        uploaded=!submit;possiblySubmitted=!submit;
        if(submit&&!local){
            submission=new ArrayList<>();
            for(LatLngInfo point:manager.t()){
                LatLngInfo copy=new LatLngInfo();copy.latLng=point.latLng;copy.isStartPosition=point.isStartPosition;submission.add(copy);
            }
        }
    }
    public void start(){
        if(started)return;started=true;deadline=android.os.SystemClock.elapsedRealtime()+30000;main.postDelayed(timeout,30000);
        if(localOnly){main.postDelayed(this::localResult,32);return;}
        if(uploaded){
            RunnerWork.IO.execute(()->{
                try{
                    JSONObject saved=new JSONObject(new String(backupFile().readFully(),java.nio.charset.StandardCharsets.UTF_8));
                    if(saved.optLong("runId")==runId&&owner.equals(saved.optString("owner"))){
                        LocalRunStats.record(app,owner,runId,saved.optLong("steps",0),saved.optBoolean("stepsKnown",false),(float)saved.optDouble("meters",0));
                        durable=true;main.post(this::releaseAbandonedRun);
                    }
                }catch(Exception ignored){}
            });
            return;
        }
        RunnerWork.IO.execute(()->{
            if(!ownerMatches()){main.post(()->finish(FAILED,"登录账号已变化，请重新查看跑步记录"));return;}
            try{
                Map<String,Object> backup=new HashMap<>();backup.put("runId",runId);backup.put("owner",owner);
                backup.put("campus",campus);backup.put("endTime",endedAt);backup.put("meters",meters);
                backup.put("seconds",seconds);backup.put("track",submission);
                backup.put("steps",steps);backup.put("stepsKnown",stepsKnown);
                android.util.AtomicFile file=backupFile();java.io.FileOutputStream output=null;
                try{output=file.startWrite();output.write(new com.google.gson.e().r(backup).getBytes(java.nio.charset.StandardCharsets.UTF_8));file.finishWrite(output);}
                catch(Exception error){if(output!=null)file.failWrite(output);throw error;}
                LocalRunStats.record(app,owner,runId,steps,stepsKnown,meters);
                durable=true;main.post(this::releaseAbandonedRun);
            }catch(Exception error){main.post(()->finish(FAILED,"无法保存本次运动，请稍后重试"));return;}
            if(state!=WAITING)return;
            if(!ownerMatches()){main.post(()->finish(FAILED,"登录账号已变化，请重新查看跑步记录"));return;}
            if(!store(app).edit().putLong("run",runId).putString("owner",owner).commit()){
                main.post(()->finish(FAILED,"无法保存提交状态，请重试"));return;
            }
            possiblySubmitted=true;
            try{
                new com.dudu.run.e.s.h(new com.dudu.run.e.p(){
                    @Override public void g(){}
                    @Override public void f(){main.post(()->{
                        uploaded=true;clearAcknowledgedRun();
                        if(state==WAITING&&listener.get()!=null)lookup();
                    });}
                    @Override public void m(int code,String error){main.post(()->{
                        if(code!=-1){rejected=true;possiblySubmitted=false;
                            if(marked(app,runId))store(app).edit().remove("run").remove("owner").apply();}
                        finish(FAILED,error==null||error.trim().isEmpty()?"提交未完成，请稍后查看跑步记录":error);
                    });}
                }).a(campus,runId,endedAt,meters,seconds,submission);
            }catch(RuntimeException error){main.post(()->finish(FAILED,"提交未完成，请稍后查看跑步记录"));}
            finally{submission=null;}
        });
    }
    public void attach(Listener target){
        listener=new WeakReference<>(target);
        if(state==WAITING&&started&&android.os.SystemClock.elapsedRealtime()>=deadline){
            finish(TIMED_OUT,"结果获取超时，已退出。可稍后在跑步记录查看。");return;
        }
        publish();
        if(state==WAITING&&uploaded&&!localOnly)lookup();
    }
    public void detach(Listener target){
        if(listener.get()==target){listener.clear();generation++;main.removeCallbacks(retry);}
    }
    public void leave(){abandoned=true;releaseAbandonedRun();}
    private android.util.AtomicFile backupFile() throws java.io.IOException{
        java.io.File directory=new java.io.File(app.getFilesDir(),"pending-run-results");
        if(!directory.isDirectory()&&!directory.mkdirs())throw new java.io.IOException("Cannot store run result");
        return new android.util.AtomicFile(new java.io.File(directory,runId+".json"));
    }
    private void releaseAbandonedRun(){
        com.dudu.run.c manager=com.dudu.run.c.n();
        if(abandoned&&durable&&ownerMatches()&&manager.q()==runId){
            SimulationController.get(app).stop();manager.K();manager.B();
        }
    }
    private boolean ownerMatches(){return owner.equals(owner());}
    private boolean accepts(int token){return state==WAITING&&generation==token&&ownerMatches()&&listener.get()!=null;}
    private void lookup(){
        main.removeCallbacks(retry);
        if(state!=WAITING||!uploaded||listener.get()==null)return;
        if(!ownerMatches()){finish(FAILED,"登录账号已变化，请重新查看跑步记录");return;}
        final int token=++generation;
        RunnerWork.IO.execute(()->{
            try{new com.dudu.run.e.s.f(new com.dudu.run.e.l(){
                @Override public void v(int page){}
                @Override public void t(String error){main.post(()->{if(accepts(token))scheduleRetry();});}
                @Override public void N(int page,List<RecordItem> rows){main.post(()->{
                    if(!accepts(token))return;
                    if(rows!=null)for(RecordItem row:rows)if(row!=null&&row.b()>0&&sameBegin(row.a(),beginTime)){
                        loadDetail(row.b(),token);return;
                    }
                    scheduleRetry();
                });}
            }).a(1,20);}catch(RuntimeException error){main.post(()->{if(accepts(token))scheduleRetry();});}
        });
    }
    private void loadDetail(int id,int token){
        RunnerWork.IO.execute(()->{
            try{new com.dudu.run.e.s.e(new com.dudu.run.e.j(){
                @Override public void x(){}
                @Override public void q(String error){main.post(()->{if(accepts(token))scheduleRetry();});}
                @Override public void A(RecordDetailResponseData data){
                    RunnerWork.IO.execute(()->{
                        try{
                            if(data==null||!sameBegin(data.a(),beginTime)||data.b()==null||data.b().isEmpty()){
                                main.post(()->{if(accepts(token))scheduleRetry();});return;
                            }
                            List<LatLngInfo> parsed=com.dudu.run.activity.RecordDetailActivity.parseTrack(data.b());
                            if(parsed==null||parsed.isEmpty()){main.post(()->{if(accepts(token))scheduleRetry();});return;}
                            main.post(()->{
                                if(!accepts(token))return;
                                recordId=id;detail=data;track=parsed;clearAcknowledgedRun();finish(READY,"");
                            });
                        }catch(RuntimeException error){main.post(()->{if(accepts(token))scheduleRetry();});}
                    });
                }
            }).a(id);}catch(RuntimeException error){main.post(()->{if(accepts(token))scheduleRetry();});}
        });
    }
    private void scheduleRetry(){if(state==WAITING){main.removeCallbacks(retry);main.postDelayed(retry,1500);}}
    private static boolean sameBegin(String date,String expected){
        if(date==null)return false;String normalized=date.trim().replace('T',' ');
        return normalized.length()>=19&&normalized.substring(0,19).equals(expected);
    }
    private void clearAcknowledgedRun(){
        if(!ownerMatches())return;
        if(!localOnly&&!recordsChanged){recordsChanged=true;recordsVersion++;}
        if(!localOnly)RunnerWork.IO.execute(()->{try{backupFile().delete();}catch(java.io.IOException ignored){}});
        com.dudu.run.c manager=com.dudu.run.c.n();
        if(manager.q()==runId){SimulationController.get(app).stop();manager.K();manager.B();}
        else if(manager.q()==0){
            CurrentSportDataInfo cached=com.dudu.run.utils.p.c(app);
            if(cached!=null&&cached.f()==runId)com.dudu.run.utils.p.d(app);
        }
    }
    private void localResult(){
        if(state!=WAITING)return;
        try{
            if(!uploaded&&com.dudu.run.c.n().q()==runId){OfflineTestSession.saveRun();durable=true;}
            uploaded=true;
            RunnerWork.IO.execute(()->{
                try{
                    JSONArray rows=OfflineTestSession.records();JSONObject saved=null;
                    for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).optLong("startedAt")==runId){saved=rows.getJSONObject(i);break;}
                    if(saved==null)throw new IllegalStateException("未找到本次本机记录");
                    JSONArray raw=saved.getJSONArray("track");List<LatLngInfo> parsed=new ArrayList<>();
                    for(int n=0;n<raw.length();n++){
                        JSONArray point=raw.getJSONArray(n);LatLngInfo p=new LatLngInfo();
                        p.latLng=new com.tencent.tencentmap.mapsdk.maps.model.LatLng(point.getDouble(0),point.getDouble(1));
                        p.isStartPosition=point.optBoolean(2);parsed.add(p);
                    }
                    Map<String,Object> values=new HashMap<>();values.put("beginTime",beginTime);
                    values.put("totalLength",saved.getDouble("meters"));values.put("totalTime",saved.getInt("seconds"));
                    com.google.gson.e gson=new com.google.gson.e();values.put("markList",gson.r(parsed));
                    RecordDetailResponseData data=gson.j(gson.r(values),RecordDetailResponseData.class);
                    main.post(()->{if(state==WAITING){detail=data;track=parsed;recordId=0;finish(READY,"");}});
                }catch(Exception error){main.post(()->finish(FAILED,"本机记录读取失败，请从测试模式查看"));}
            });
        }catch(Exception error){finish(FAILED,"本机保存失败，请重试");}
    }
    private void finish(int result,String text){
        if(state!=WAITING)return;
        state=result;message=text;generation++;main.removeCallbacks(timeout);main.removeCallbacks(retry);publish();
    }
    private void publish(){Listener target=listener.get();if(target!=null)target.changed(this);}
}
