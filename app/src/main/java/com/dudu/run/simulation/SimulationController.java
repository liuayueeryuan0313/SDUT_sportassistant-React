package com.dudu.run.simulation;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.location.Location;
import com.dudu.run.c;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public final class SimulationController {
    public interface Listener { void onSimulationChanged(); }
    private static SimulationController instance;
    private final Context context;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Set<Listener> listeners=new LinkedHashSet<>();
    private SimulationEngine engine;
    private boolean enabled,running,serviceActive,recordingPausedByPanel,waitingForRoute;
    private long boundRunId,editSequence;
    private SimulationEngine.Config activeConfig;
    private RouteEdit activeEdit;
    public static final class RouteEdit {
        public final long runId,token;
        public final SimulationEngine.Point anchor;
        public final SimulationEngine.Config parameters;
        public final java.util.List<SimulationEngine.Point> remaining;
        private final boolean resumeOnCancel;
        private RouteEdit(long runId,long token,SimulationEngine.Point anchor,SimulationEngine.Config parameters,java.util.List<SimulationEngine.Point> remaining,boolean resumeOnCancel){
            this.runId=runId;this.token=token;this.anchor=anchor;this.parameters=parameters;this.remaining=remaining;this.resumeOnCancel=resumeOnCancel;
        }
    }
    private long lastTick;
    private String message="未启用";
    private SimulationController(Context context) { this.context=context.getApplicationContext(); }
    public static SimulationController get(Context context) {
        if(instance==null) instance=new SimulationController(context);
        return instance;
    }
    public static SimulationController get() {
        if(instance==null) throw new IllegalStateException("SimulationController not initialized");
        return instance;
    }
    public boolean enabled() { return enabled; }
    public boolean running() { return enabled&&running; }
    public SimulationEngine.Sample sample() { return engine==null?null:engine.current(); }
    public String state() { return message; }
    public void addListener(Listener listener) { listeners.add(listener); }
    public void removeListener(Listener listener) { listeners.remove(listener); }
    private void notifyChanged() {
        for(Listener listener:new ArrayList<>(listeners)) listener.onSimulationChanged();
    }
    public void releaseEndedRun(){if(boundRunId!=0&&c.n().q()==0)stop();}
    public boolean waitingForRoute(){return enabled&&waitingForRoute;}
    public boolean isCurrentEdit(RouteEdit edit){return enabled&&edit!=null&&activeEdit==edit&&c.n().q()==edit.runId;}
    public void start(SimulationEngine.Config config) {
        if(c.n().q()!=0)throw new IllegalStateException("请在工作台把新路线应用到本次跑步");
        RunRouteDraft.clear();
        save(config);activate(config,false);
    }
    /** Called only after the original run-eligibility and checkpoint requests have succeeded. */
    public void prepareForRun(SimulationEngine.Point origin){
        if(c.n().q()!=0)throw new IllegalStateException("已有跑步记录，请返回当前跑步");
        SimulationEngine.Config old=load();
        SimulationEngine.Config hold=new SimulationEngine.Config(java.util.Collections.singletonList(origin),
                old==null?10:old.speedKmh,old==null?.8:old.speedVariationKmh,old==null?164:old.cadence,
                old==null?6:old.cadenceVariation,old==null?1.5:old.altitudeVariation,
                old==null?SimulationEngine.Config.DEFAULT_ROUTE_OFFSET_METERS:old.routeOffsetMeters,false,true);
        RunRouteDraft.clear();
        activate(hold,true); // Do not replace the last saved route with a temporary fixed point.
    }
    private void activate(SimulationEngine.Config config,boolean wait){
        engine=new SimulationEngine(config,System.nanoTime());activeConfig=config;
        enabled=true;running=false;waitingForRoute=wait;activeEdit=null;boundRunId=0;
        recordingPausedByPanel=false;message=wait?"等待开始跑步后画线":"已就绪，等待开始跑步";
        c.n().K();
        Intent intent=new Intent(context,SimulationService.class);
        try {
            if(Build.VERSION.SDK_INT>=26)context.startForegroundService(intent);else context.startService(intent);
        }catch(RuntimeException error){enabled=false;running=false;waitingForRoute=false;message="后台服务启动失败";notifyChanged();throw error;}
        notifyChanged();
    }
    public RouteEdit beginRouteEdit(){
        long runId=c.n().q();
        if(runId==0)throw new IllegalStateException("请先开始跑步并获取打卡点");
        if(!enabled||engine==null||!c.n().isSimulationSource())
            throw new IllegalStateException("本次模拟未运行，请先结束当前记录后重新开始");
        if(boundRunId!=0&&boundRunId!=runId)throw new IllegalStateException("跑步记录已变化，请重新打开工作台");
        if(isCurrentEdit(activeEdit))return activeEdit;
        boundRunId=runId;
        activeEdit=new RouteEdit(runId,++editSequence,engine.current().point,activeConfig,engine.remainingRoute(),c.n().y());
        if(c.n().y())c.n().L();
        engine.pause();running=false;lastTick=SystemClock.elapsedRealtime();
        message="画线中 · 计时与位置均暂停";notifyChanged();return activeEdit;
    }
    public void cancelRouteEdit(RouteEdit edit){
        if(!isCurrentEdit(edit))return;
        activeEdit=null;running=false;lastTick=SystemClock.elapsedRealtime();
        if(edit.resumeOnCancel&&!waitingForRoute&&!sample().finished)c.n().J();
        message=waitingForRoute?"等待画线 · 计时已暂停":running?"正在模拟":"跑步已暂停";notifyChanged();
    }
    public SimulationEngine.Config currentParameters(){return activeConfig==null?load():activeConfig;}
    public void updateSpeed(double speedKmh,double variation){
        if(activeEdit!=null)throw new IllegalStateException("请在工作台修改参数并完成绘制");
        SimulationEngine.Config old=currentParameters();
        SimulationEngine.Config updated=new SimulationEngine.Config(old.points,speedKmh,variation,old.cadence,old.cadenceVariation,
            old.altitudeVariation,old.routeOffsetMeters,old.loop,old.fixed);
        if(enabled&&engine!=null){engine.updateSpeed(speedKmh,variation);activeConfig=updated;}
        SimulationPreferences.store(context).edit().putFloat("speed",(float)speedKmh).putFloat("variation",(float)variation).apply();
        notifyChanged();
    }
    public void applyToRun(RouteEdit edit,SimulationEngine.Config draft){
        if(!isCurrentEdit(edit)||!c.n().isSimulationSource())throw new IllegalStateException("本次跑步或工作台已变化，请重新打开工作台");
        if(System.currentTimeMillis()-edit.runId>3600000L)throw new IllegalStateException("距开始跑步超过1小时，请按原流程结束本次跑步");
        if(draft.fixed)throw new IllegalArgumentException("请关闭定点模式并画出路线");
        ArrayList<SimulationEngine.Point> points=new ArrayList<>(draft.points);
        if(SimulationEngine.distance(edit.anchor,points.get(0))<=.2)points.set(0,edit.anchor);else points.add(0,edit.anchor);
        SimulationEngine.Config anchored=new SimulationEngine.Config(points,draft.speedKmh,draft.speedVariationKmh,
                draft.cadence,draft.cadenceVariation,draft.altitudeVariation,draft.routeOffsetMeters,false,false);
        SimulationEngine next=engine.continueOn(anchored,System.nanoTime(),c.n().s(),c.n().r());
        save(anchored);engine=next;activeConfig=anchored;waitingForRoute=false;activeEdit=null;RunRouteDraft.clear();
        recordingPausedByPanel=false;lastTick=SystemClock.elapsedRealtime();running=c.n().y();message="正在模拟";
        c.n().I(true); // Change only the location subscription, preserving the original recorder and checkpoint list.
        if(!c.n().y())c.n().J();
        notifyChanged();
    }
    public void pause() {
        if(!enabled) return;
        recordingPausedByPanel=c.n().isSimulationSource()&&c.n().y();
        if(recordingPausedByPanel) c.n().L();
        running=false; engine.pause(); message="已暂停"; notifyChanged();
    }
    public void resume() {
        if(!enabled || sample().finished) return;
        if(waitingForRoute||activeEdit!=null){message="请在工作台画线后应用路线";notifyChanged();return;}
        if(c.n().q()==0){message="已就绪，请在跑步页开始记录";notifyChanged();return;}
        running=true; lastTick=SystemClock.elapsedRealtime(); message="正在模拟";
        recordingPausedByPanel=false;
        if(!c.n().y())c.n().J();
        notifyChanged();
    }
    public void recordingState(boolean active) {
        if(!enabled)return;
        if(active&&c.n().q()!=0)boundRunId=c.n().q();
        running=active&&!waitingForRoute&&activeEdit==null&&!sample().finished;
        if(!running&&engine!=null)engine.pause();lastTick=SystemClock.elapsedRealtime();
        message=activeEdit!=null?(active?"画线中 · 计时与位置均暂停":"画线中 · 本次跑步已暂停"):waitingForRoute?"等待画线 · 计时已暂停":sample().finished?"路线已完成":active?"正在模拟":"跑步已暂停";
        notifyChanged();
    }
    public void stop() {
        if(c.n().isSimulationSource()&&c.n().y()) c.n().L();
        RunRouteDraft.clear();
        enabled=false;running=false;waitingForRoute=false;activeEdit=null;boundRunId=0;recordingPausedByPanel=false;message="已停止";
        handler.removeCallbacks(ticker); context.stopService(new Intent(context,SimulationService.class));
        notifyChanged();
    }
    void serviceStarted() {
        serviceActive=true; handler.removeCallbacks(ticker); lastTick=SystemClock.elapsedRealtime();
        handler.post(ticker);
    }
    void serviceStopped() {
        serviceActive=false; handler.removeCallbacks(ticker);
        if(enabled) {
            if(c.n().isSimulationSource()&&c.n().y()) c.n().L();
            enabled=false;running=false;waitingForRoute=false;activeEdit=null;message="服务已停止，请手动重新启用"; notifyChanged();
        }
    }
    private final Runnable ticker=new Runnable() {
        @Override public void run() {
            if(!enabled || !serviceActive) return;
            if(boundRunId!=0&&c.n().q()!=boundRunId){stop();return;}
            if(boundRunId==0&&c.n().q()!=0)boundRunId=c.n().q();
            long now=SystemClock.elapsedRealtime();
            double dt=(now-lastTick)/1000.0; lastTick=now;
            // Never synthesize a catch-up jump after a prolonged OS scheduling gap.
            if(dt>5&&running) { pause(); message="系统延迟超过 5 秒，已暂停"; }
            if(running&&!waitingForRoute&&activeEdit==null&&c.n().isSimulationSource()&&c.n().y()) {
                engine.advance(Math.max(0,Math.min(5,dt)));
                if(engine.current().finished) {
                    running=false; message="路线已完成";
                    if(c.n().isSimulationSource()&&c.n().y()) {
                        c.n().acceptSimulation(location()); // Deliver the final endpoint before pausing.
                        c.n().L();
                    }
                }
            }
            if(c.n().isSimulationSource()) c.n().acceptSimulation(location());
            notifyChanged(); handler.postDelayed(this,1000);
        }
    };
    public Location location() {
        if(engine==null) return null;
        SimulationEngine.Sample s=sample();
        Location result=new Location("simulation");
        result.setLatitude(s.point.latitude); result.setLongitude(s.point.longitude);
        result.setAltitude(s.point.altitude); result.setSpeed((float)(running?s.speedKmh/3.6:0));
        result.setBearing((float)s.bearing); result.setAccuracy(5);
        result.setTime(System.currentTimeMillis());
        result.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        if(Build.VERSION.SDK_INT>=31) result.setMock(true);
        return result;
    }
    public String summary() {
        SimulationEngine.Sample s=sample();
        if(s==null) return message;
        return String.format(Locale.CHINA,"%s\n%.1f km/h · %.0f 步/分 · %d 步\n高度 %.1f m · %.0f m\n%.6f, %.6f",
                message,running?s.speedKmh:0,running?s.cadence:0,s.steps,s.point.altitude,
                s.distanceMeters,s.point.latitude,s.point.longitude);
    }
    public void save(SimulationEngine.Config config) { SimulationPreferences.save(context,config); }
    /** Parameter carrier only; the singleton coordinate is a placeholder, never a saved route. */
    public SimulationEngine.Config load() { return SimulationPreferences.load(context); }
}
