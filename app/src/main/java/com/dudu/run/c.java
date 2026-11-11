package com.dudu.run;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.dudu.run.activity.SportMapActivity;
import com.dudu.run.bean.CurrentSportDataInfo;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.utils.Accompaniment;
import com.dudu.run.utils.p;
import com.dudu.run.utils.s;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.tencent.mapsdk.internal.y;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import io.reactivex.f;
import io.reactivex.g;
import io.reactivex.h;
import io.reactivex.j;
import java.util.ArrayList;
import java.util.List;
public class c implements TencentLocationListener {
    private static c z;
    private Context f1591d;
    private TencentLocationManager f1592e;
    private b f1593f;
    private HandlerC0034c h;
    private long i;
    private long m;
    private float n;
    private int o;
    private int u;
    private int v;
    private boolean g = false;
    private List<LatLngInfo> p = new ArrayList();
    private List<RandomPoint> q = new ArrayList();
    private int r = 0;
    private boolean s = false;
    private int t = 1;
    private boolean w = false;
    private boolean x = false;
    private boolean y = false;
    private boolean simulationSource, simulationSubscribed;
    private LatLng lastCheckpointLocation;
    private int gpsLevel;
    private long gpsFixElapsed;
    private final Runnable expireGps=()->publishGpsSignal(0);
    private com.dudu.run.ui.RunSteps localSteps;
    private final Handler initializationHandler=new Handler(Looper.getMainLooper());
    private com.dudu.run.simulation.SystemLocationFallback initializationFallback;
    private boolean initializationLocationPending;

    private void startInitializationFallback(){
        if(initializationFallback!=null)initializationFallback.close();
        initializationFallback=new com.dudu.run.simulation.SystemLocationFallback(f1591d,location->{
            if(initializationLocationPending&&!simulationSource&&!com.dudu.run.simulation.SimulationController.get().enabled()&&i==0&&f1593f!=null){
                initializationLocationPending=false;
                processLocation(location,0);
            }
        });
        initializationFallback.start();
    }
    class a implements j<Object> {
        a(c cVar) {
        }

        @Override // io.reactivex.j
        public void onComplete() {
            com.dudu.run.utils.j.a("saveSportCacheData() onComplete()");
        }

        @Override // io.reactivex.j
        public void onError(Throwable th) {
        }

        @Override // io.reactivex.j
        public void onNext(Object obj) {
            com.dudu.run.utils.j.a("saveSportCacheData() onNext()");
        }

        @Override // io.reactivex.j
        public void onSubscribe(io.reactivex.n.b bVar) {
        }
    }
    public interface b {
        default void initializationLocationError(int code,String message) {}
        default void gpsSignalChanged(int level) {}
        void H(RandomPoint randomPoint);

        void L(Location location, int i);

        void h(int i, float f2);

        void w(LatLngInfo latLngInfo, LatLngInfo latLngInfo2);
    }
    private class HandlerC0034c extends Handler {
        public HandlerC0034c(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i != 0) {
                if (i != 1) {
                    return;
                }
                if (c.this.n > 100.0f) {
                    c.this.C();
                }
                sendEmptyMessageDelayed(1, 30000L);
                return;
            }
            c.b(c.this);
            if (c.this.f1593f != null) {
                c.this.f1593f.h(c.this.o, c.this.n);
            }
            if (System.currentTimeMillis() - c.this.i >= 3300000 && !c.this.x) {
                Accompaniment.a().g(R.raw.run_time_almost_1hour);
                c.this.x = true;
            }
            sendEmptyMessageDelayed(0, 1000L);
        }
    }

    private c() {
    }
    public void C() {
        f.c(new h() { // from class: com.dudu.run.a
            @Override // io.reactivex.h
            public final void a(g gVar) throws Exception {
                c.this.A(gVar);
            }
        }).d(io.reactivex.m.b.a.a()).g(io.reactivex.t.a.a()).a(new a(this));
    }

    static /* synthetic */ int b(c cVar) {
        int i = cVar.o;
        cVar.o = i + 1;
        return i;
    }

    private Notification i() {
        Intent intent = new Intent(this.f1591d, (Class<?>) SportMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activity = PendingIntent.getActivity(this.f1591d, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        String packageName = this.f1591d.getPackageName();
        String string = this.f1591d.getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) this.f1591d.getSystemService("notification");
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(new NotificationChannel(packageName, string, 3));
            return new Notification.Builder(this.f1591d, packageName).setChannelId(packageName).setSmallIcon(R.mipmap.ic_location_n).setContentTitle(string).setContentText("跑步中").setContentIntent(activity).build();
        }
        com.dudu.legacy.androidx.core.app.f.c cVar = new com.dudu.legacy.androidx.core.app.f.c(this.f1591d, null);
        cVar.f(string);
        cVar.g(R.mipmap.ic_location_n);
        cVar.e("跑步中");
        cVar.d(activity);
        return cVar.a();
    }

    private void j(LatLng latLng) {
        List<RandomPoint> list = this.q;
        if (list == null || list.isEmpty() || this.r == this.q.size()) {
            return;
        }
        com.dudu.run.simulation.SimulationEngine.Point current=new com.dudu.run.simulation.SimulationEngine.Point(latLng.latitude,latLng.longitude,0);
        com.dudu.run.simulation.SimulationEngine.Point previous=null;
        if(!this.s&&lastCheckpointLocation!=null&&TencentLocationUtils.distanceBetween(latLng.latitude,latLng.longitude,lastCheckpointLocation.latitude,lastCheckpointLocation.longitude)<=50)
            previous=new com.dudu.run.simulation.SimulationEngine.Point(lastCheckpointLocation.latitude,lastCheckpointLocation.longitude,0);
        lastCheckpointLocation=latLng;
        for (RandomPoint randomPoint : this.q) {
            if (!randomPoint.c() && com.dudu.run.simulation.CheckpointGeometry.near(new com.dudu.run.simulation.SimulationEngine.Point(randomPoint.a(),randomPoint.b(),0),previous,current)) {
                randomPoint.d(true);
                this.r++;
                b bVar = this.f1593f;
                if (bVar != null) {
                    bVar.H(randomPoint);
                }
                Accompaniment.a().g(R.raw.sign_success);
                com.dudu.run.utils.s.a(this.f1591d);
            }
        }
    }

    private CurrentSportDataInfo l() {
        if (!this.g) {
            return null;
        }
        CurrentSportDataInfo currentSportDataInfo = new CurrentSportDataInfo();
        currentSportDataInfo.simulated = this.simulationSource;
        currentSportDataInfo.localSteps = localSteps.count();
        currentSportDataInfo.localStepsKnown = localSteps.known();
        currentSportDataInfo.o(this.i);
        currentSportDataInfo.p(this.n);
        currentSportDataInfo.q(this.o);
        currentSportDataInfo.r(this.p);
        currentSportDataInfo.l(this.q);
        currentSportDataInfo.m(this.r);
        currentSportDataInfo.j(this.t);
        currentSportDataInfo.k(this.u);
        currentSportDataInfo.n(this.v);
        return currentSportDataInfo;
    }

    public static c n() {
        if (z == null) {
            z = new c();
        }
        return z;
    }
    public /* synthetic */ void A(g gVar) throws Exception {
        com.dudu.run.utils.p.f(this.f1591d, l());
        gVar.onNext(new Object());
        gVar.onComplete();
    }

    public void B() {
        localSteps.reset();
        lastCheckpointLocation=null;
        this.g = false;
        this.simulationSource = false;
        this.simulationSubscribed = false;
        this.n = 0.0f;
        this.o = 0;
        this.i = 0L;
        this.m = 0L;
        this.w = false;
        this.x = false;
        this.v = 0;
        this.p.clear();
        this.q.clear();
        this.r = 0;
        this.s = false;
        this.h.removeMessages(0);
        this.h.removeMessages(1);
        com.dudu.run.utils.p.d(this.f1591d);
    }

    public void D(int i) {
        this.t = i;
    }

    public void E(CurrentSportDataInfo currentSportDataInfo) {
        if (currentSportDataInfo == null) {
            return;
        }
        this.simulationSource = currentSportDataInfo.simulated;
        localSteps.restore(currentSportDataInfo.localSteps,currentSportDataInfo.localStepsKnown);
        this.i = currentSportDataInfo.f();
        this.n = currentSportDataInfo.g();
        this.o = currentSportDataInfo.h();
        if (currentSportDataInfo.i() != null) {
            this.p.clear();
            this.p.addAll(currentSportDataInfo.i());
        }
        if (currentSportDataInfo.c() != null) {
            this.q.clear();
            this.q.addAll(currentSportDataInfo.c());
        }
        this.r = currentSportDataInfo.d();
        this.t = currentSportDataInfo.a();
        this.u = currentSportDataInfo.b();
        this.v = currentSportDataInfo.e();
    }

    public void F(int i) {
        this.u = i;
    }

    public void G(List<RandomPoint> list) {
        this.q.clear();
        if (list != null) {
            this.q.addAll(list);
        }
    }

    public void H(b bVar) {
        this.f1593f = bVar;
        if(bVar!=null)bVar.gpsSignalChanged(gpsSignalLevel());
    }

    public boolean isSimulationSource() { return this.simulationSource; }

    /** Select actual device callbacks only before creating a new record. */
    public void useDeviceSourceForNewRun() {
        if (this.i != 0) throw new IllegalStateException("已有跑步进行中，不能切换数据来源");
        if (com.dudu.run.simulation.SimulationController.get().enabled())
            throw new IllegalStateException("请先停止模拟，再使用设备定位");
        K();
        this.simulationSource = false;
        this.simulationSubscribed = false;
    }

    public void acceptSimulation(Location location) {
        if (this.simulationSubscribed && this.simulationSource && location != null) {
            if(this.g)captureSimulationSteps();
            processLocation(location, 0);
        }
    }

    public void I(boolean z2) {
        com.dudu.run.simulation.SimulationController simulation = com.dudu.run.simulation.SimulationController.get();
        if (this.simulationSource && this.i == 0 && !simulation.enabled()) this.simulationSource = false;
        if ((this.simulationSource && this.i != 0) || simulation.enabled()) {
            K();
            this.simulationSource = true;
            this.simulationSubscribed = true;
            final Location location = simulation.location();
            new Handler(Looper.getMainLooper()).post(() -> acceptSimulation(location));
            return;
        }
        // Do not make a working system provider wait eight seconds for the SDK.
        initializationLocationPending=!z2&&this.i==0&&this.f1593f!=null;
        if(initializationLocationPending)startInitializationFallback();
        int result;
        try{
            if(this.f1592e==null)this.f1592e=TencentLocationManager.getInstance(this.f1591d);
            this.f1592e.setCoordinateType(1);
            this.f1592e.setMockEnable(true);
            result=this.f1592e.requestLocationUpdates(TencentLocationRequest.create().setInterval(1000L).setAllowDirection(true).setLocMode(10), this, this.f1591d.getMainLooper());
        }catch(RuntimeException failure){handleDeviceLocationError(-1,failure.getClass().getSimpleName());return;}
        android.util.Log.i("RunInit","location_request_result="+result);
        if(result!=0){handleDeviceLocationError(result,"定位请求未启动");return;}
        this.y = z2;
        if (z2) {
            this.f1592e.enableForegroundLocation(20010, i());
        }
    }

    public void J() {
        com.dudu.run.simulation.SimulationController simulation = com.dudu.run.simulation.SimulationController.get();
        if (this.simulationSource && (!simulation.enabled() || simulation.sample() == null || simulation.sample().finished)) {
            com.dudu.run.utils.r.a(this.f1591d, "模拟已停止或路线已完成；请结束当前跑步后重新启用模拟");
            return;
        }
        if (this.g) {
            return;
        }
        this.s = true;
        this.g = true;
        if (this.simulationSource) com.dudu.run.simulation.SimulationController.get().recordingState(true);
        if (this.i == 0) {
            this.i = System.currentTimeMillis();
            this.w = false;
            this.x = false;
        }
        if(this.simulationSource)captureSimulationSteps();else localSteps.startDevice();
        b bVar = this.f1593f;
        if (bVar != null) {
            bVar.h(this.o, this.n);
        }
        this.h.removeMessages(0);
        this.h.sendEmptyMessageDelayed(0, 1000L);
        this.h.removeMessages(1);
        this.h.sendEmptyMessageDelayed(1, 30000L);
        // The new record exists, but drawing must not consume active running time.
        if(this.simulationSource&&simulation.waitingForRoute())L();
    }

    public void K() {
        initializationHandler.removeCallbacks(expireGps);publishGpsSignal(0);
        initializationLocationPending=false;
        if(initializationFallback!=null){initializationFallback.close();initializationFallback=null;}
        this.simulationSubscribed = false;
        TencentLocationManager tencentLocationManager = this.f1592e;
        if (tencentLocationManager != null) {
            if (this.y) {
                tencentLocationManager.disableForegroundLocation(true);
            }
            this.f1592e.removeUpdates(this);
        }
    }

    public void L() {
        if(this.simulationSource&&this.g)captureSimulationSteps();
        localSteps.pause();
        lastCheckpointLocation=null;
        this.g = false;
        if (this.simulationSource) com.dudu.run.simulation.SimulationController.get().recordingState(false);
        this.h.removeMessages(0);
        this.h.removeMessages(1);
        this.m = System.currentTimeMillis();
    }

    private void captureSimulationSteps() {
        com.dudu.run.simulation.SimulationEngine.Sample sample=com.dudu.run.simulation.SimulationController.get().sample();
        if(sample!=null)localSteps.simulation(sample.steps);
    }
    public long recordedSteps() {return localSteps.count();}
    public boolean hasRecordedSteps() {return localSteps.known();}
    public void refreshDeviceStepCounter() {if(this.g&&!this.simulationSource)localSteps.startDevice();}

    public int k() {
        return this.t;
    }

    public long m() {
        return this.m;
    }

    public List<RandomPoint> o() {
        return this.q;
    }

    @Override // com.tencent.map.geolocation.TencentLocationListener
    public void onLocationChanged(TencentLocation tencentLocation, int error, String message) {
        if (this.simulationSource || com.dudu.run.simulation.SimulationController.get().enabled())return;
        if(error!=0||tencentLocation==null){publishGpsSignal(0);handleDeviceLocationError(error==0?-2:error,message);return;}
        android.util.Log.i("RunInit","location_callback_success accuracy="+tencentLocation.getAccuracy());
        Location location = new Location(tencentLocation.getProvider());
        location.setLatitude(tencentLocation.getLatitude()); location.setLongitude(tencentLocation.getLongitude());
        location.setAltitude(tencentLocation.getAltitude()); location.setAccuracy(tencentLocation.getAccuracy());
        location.setSpeed(tencentLocation.getSpeed()); location.setBearing(tencentLocation.getBearing());
        location.setTime(tencentLocation.getTime());
        initializationLocationPending=false;
        if(initializationFallback!=null){initializationFallback.close();initializationFallback=null;}
        processLocation(location, tencentLocation.getGPSRssi());
    }

    private void handleDeviceLocationError(int code,String message){
        // A Tencent error must not tear down an independent system request.
        // The existing initialization deadline still limits the entire wait.
        if(initializationLocationPending){
            android.util.Log.w("RunInit","tencent_location_error="+code+" waiting_for_system_location");
            return;
        }
        reportInitializationLocationError(code,message);
    }

    private void reportInitializationLocationError(int code,String message){
        String safe=message==null?"无详细原因":message.replaceAll("[A-Z0-9]{5}(?:-[A-Z0-9]{5}){5}","[Key]");
        if(safe.length()>160)safe=safe.substring(0,160);
        android.util.Log.w("RunInit","location_error="+code+" reason="+safe);
        if(this.f1593f!=null)this.f1593f.initializationLocationError(code,safe);
    }
    private void processLocation(Location location, int gpsRssi) {
        initializationHandler.removeCallbacks(expireGps);
        if(location.getTime()>0&&System.currentTimeMillis()-location.getTime()>15000)gpsRssi=0;
        gpsRssi=gpsRssi>=1&&gpsRssi<=3?gpsRssi:0;
        publishGpsSignal(gpsRssi);
        if(gpsRssi>0){gpsFixElapsed=android.os.SystemClock.elapsedRealtime();initializationHandler.postDelayed(expireGps,15000);}
        if (this.f1593f != null) this.f1593f.L(location, gpsRssi);
        if (this.g) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            j(latLng);
            LatLngInfo latLngInfo = new LatLngInfo();
            latLngInfo.latLng = latLng;
            if (this.s) {
                com.dudu.run.utils.j.c("开始启动跑步");
                latLngInfo.isStartPosition = true;
                this.s = false;
            }
            if (this.p.size() <= 0) {
                latLngInfo.isStartPosition = true;
                this.p.add(latLngInfo);
                b bVar = this.f1593f;
                if (bVar != null) {
                    bVar.w(latLngInfo, null);
                    return;
                }
                return;
            }
            List<LatLngInfo> list = this.p;
            LatLngInfo latLngInfo2 = list.get(list.size() - 1);
            LatLng latLng2 = latLngInfo2.latLng;
            double d2 = latLng2.latitude;
            double d3 = latLng.latitude;
            if (d2 == d3 && latLng2.longitude == latLng.longitude) {
                return;
            }
            double dDistanceBetween = TencentLocationUtils.distanceBetween(d3, latLng.longitude, d2, latLng2.longitude);
            this.p.add(latLngInfo);
            if (!latLngInfo.isStartPosition) {
                this.n = (float) (((double) this.n) + dDistanceBetween);
                com.dudu.run.utils.j.b("mine", "总距离：" + this.n + " betweenDis=" + dDistanceBetween);
                if (this.n >= this.u) {
                    if (this.v == 0) {
                        this.v = this.o;
                    }
                    if (!this.w) {
                        Accompaniment.a().g(R.raw.over_max_distance);
                        this.w = true;
                    }
                }
            }
            if (this.f1593f != null) this.f1593f.w(latLngInfo, latLngInfo2);
        }
    }

    @Override // com.tencent.map.geolocation.TencentLocationListener
    public void onStatusUpdate(String provider, int status, String description) {
        if(simulationSource)return;
        if(status==TencentLocationListener.STATUS_LOCATION_SWITCH_OFF||(TencentLocationListener.GPS.equals(provider)&&(status==TencentLocationListener.STATUS_DISABLED
                ||status==TencentLocationListener.STATUS_DENIED||status==TencentLocationListener.STATUS_UNKNOWN
                ||status==TencentLocationListener.STATUS_GPS_UNAVAILABLE))){
            initializationHandler.removeCallbacks(expireGps);publishGpsSignal(0);
        }
    }
    public int gpsSignalLevel(){return gpsLevel>0&&android.os.SystemClock.elapsedRealtime()-gpsFixElapsed<15000?gpsLevel:0;}
    private void publishGpsSignal(int level){
        if(Looper.myLooper()!=Looper.getMainLooper()){initializationHandler.post(()->publishGpsSignal(level));return;}
        if(gpsLevel==level)return;gpsLevel=level;
        if(f1593f!=null)f1593f.gpsSignalChanged(level);
    }

    public int p() {
        return (int) (this.o / (this.n / 1000.0f));
    }

    public long q() {
        return this.i;
    }

    public float r() {
        return this.n;
    }

    public int s() {
        return this.o;
    }

    public List<LatLngInfo> t() {
        return this.p;
    }

    public int u() {
        List<RandomPoint> list = this.q;
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return this.q.size() - this.r;
    }

    public float v() {
        return Math.min(this.n, this.u);
    }

    public int w() {
        int i = this.v;
        return i == 0 ? this.o : i;
    }

    public void x(Context context) {
        this.f1591d = context;
        this.localSteps = new com.dudu.run.ui.RunSteps(context);
        this.h = new HandlerC0034c(context.getMainLooper());
    }

    public boolean y() {
        return this.g;
    }
}
