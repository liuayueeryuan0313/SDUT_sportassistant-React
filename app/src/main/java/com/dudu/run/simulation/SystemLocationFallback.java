package com.dudu.run.simulation;

import android.content.Context;
import android.location.*;
import android.os.*;
import java.util.ArrayList;
import java.util.List;

/** Fresh physical startup locations, independent of the vendor SDK callback path. */
public final class SystemLocationFallback implements LocationListener {
    public interface Callback{void located(Location location);}
    private final LocationManager manager;
    private final Callback callback;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<CancellationSignal> currentRequests=new ArrayList<>();
    private boolean closed,started;
    public SystemLocationFallback(Context context,Callback callback){manager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);this.callback=callback;}
    public void start(){
        if(closed||started||manager==null)return;
        started=true;
        Location cached=null;
        for(String provider:new String[]{LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER,"fused"}){
            try{
                if(manager.isProviderEnabled(provider)){
                    Location candidate=manager.getLastKnownLocation(provider);
                    if(usable(candidate)&&(cached==null||candidate.getElapsedRealtimeNanos()>cached.getElapsedRealtimeNanos()
                            ||(candidate.getElapsedRealtimeNanos()==cached.getElapsedRealtimeNanos()&&candidate.getAccuracy()<cached.getAccuracy())))
                        cached=new Location(candidate);
                    manager.requestLocationUpdates(provider,1000,0,this,Looper.getMainLooper());
                    android.util.Log.i("RunInit","system_location_requested provider="+provider);
                }
            }catch(RuntimeException error){android.util.Log.w("RunInit","system_location_unavailable provider="+provider+" type="+error.getClass().getSimpleName());}
            if(Build.VERSION.SDK_INT>=30&&!closed){
                try{
                    if(manager.isProviderEnabled(provider)){
                        CancellationSignal cancellation=new CancellationSignal();
                        currentRequests.add(cancellation);
                        // Current-location requests can return a fresh stationary fix even
                        // when the update listener has not seen a new position yet.
                        Api30.request(manager,provider,cancellation,handler,this);
                    }
                }catch(RuntimeException error){android.util.Log.w("RunInit","system_current_location_unavailable provider="+provider+" type="+error.getClass().getSimpleName());}
            }
        }
        // Never deliver inline: the owner must finish registering the SDK listener
        // before a successful cached fix tears both paths down.
        if(cached!=null){Location fresh=cached;handler.post(()->onLocationChanged(fresh));}
    }
    private static boolean usable(Location raw){
        if(raw==null)return false;
        long age=SystemClock.elapsedRealtimeNanos()-raw.getElapsedRealtimeNanos();
        double latitude=raw.getLatitude(),longitude=raw.getLongitude();
        return age>=0&&age<=10000000000L&&raw.hasAccuracy()&&!Float.isNaN(raw.getAccuracy())
                &&raw.getAccuracy()>0&&raw.getAccuracy()<=50
                &&!Double.isNaN(latitude)&&!Double.isNaN(longitude)
                &&latitude>=-90&&latitude<=90&&longitude>=-180&&longitude<=180;
    }
    @Override public void onLocationChanged(Location raw){
        if(closed||!usable(raw))return;
        long age=SystemClock.elapsedRealtimeNanos()-raw.getElapsedRealtimeNanos();
        double height=raw.hasAltitude()?raw.getAltitude():30;
        if(Double.isNaN(height)||Double.isInfinite(height)||height< -500||height>9000)height=30;
        try{
            SimulationEngine.Point gcj=CoordinateTransform.fromWgs84(new SimulationEngine.Point(raw.getLatitude(),raw.getLongitude(),height));
            Location converted=new Location(raw);converted.setLatitude(gcj.latitude);converted.setLongitude(gcj.longitude);
            android.util.Log.i("RunInit","system_location_success provider="+raw.getProvider()+" accuracy="+raw.getAccuracy()+" age_ms="+age/1000000);
            close();callback.located(converted);
        }catch(IllegalArgumentException invalid){android.util.Log.w("RunInit","system_location_invalid_coordinates");}
    }
    public void close(){
        if(closed)return;
        closed=true;handler.removeCallbacksAndMessages(null);
        for(CancellationSignal request:currentRequests)request.cancel();
        currentRequests.clear();
        if(manager!=null)try{manager.removeUpdates(this);}catch(RuntimeException ignored){}
    }
    @android.annotation.TargetApi(30)
    private static final class Api30{
        static void request(LocationManager manager,String provider,CancellationSignal cancellation,Handler handler,SystemLocationFallback listener){
            manager.getCurrentLocation(provider,cancellation,command->handler.post(command),listener::onLocationChanged);
        }
    }
    @Override public void onProviderEnabled(String provider){}
    @Override public void onProviderDisabled(String provider){}
    @Override public void onStatusChanged(String provider,int status,Bundle extras){}
}
