package com.dudu.run.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/** Local run-only counter. Paused time and the device's lifetime total are excluded. */
public final class RunSteps implements SensorEventListener {
    private final Context app;
    private final SensorManager sensors;
    private Sensor sensor;
    private boolean listening,known;
    private long steps,baseline=-1,lastSimulation=-1,startedNanos;

    public RunSteps(Context context) {
        app=context.getApplicationContext();
        sensors=(SensorManager)app.getSystemService(Context.SENSOR_SERVICE);
    }
    private static Sensor available(SensorManager sensors) {
        if(sensors==null)return null;
        Sensor detector=sensors.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        return detector!=null?detector:sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }
    public static void requestPermission(Activity activity) {
        if(Build.VERSION.SDK_INT<29||activity.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)==PackageManager.PERMISSION_GRANTED)return;
        if(available((SensorManager)activity.getSystemService(Context.SENSOR_SERVICE))==null)return;
        android.content.SharedPreferences prefs=activity.getSharedPreferences("local_run_steps",0);
        if(prefs.getBoolean("permission_requested",false))return;
        // This optional permission never blocks location recording or starting the run.
        try{
            activity.requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},741);
            prefs.edit().putBoolean("permission_requested",true).apply();
        }catch(RuntimeException unavailable){}
    }
    public void startDevice() {
        if(listening)return;
        if(Build.VERSION.SDK_INT>=29&&app.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED)return;
        sensor=available(sensors);if(sensor==null)return;
        baseline=-1;startedNanos=SystemClock.elapsedRealtimeNanos();
        try{
            listening=sensors.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL,new Handler(Looper.getMainLooper()));
            if(listening)known=true;
        }catch(RuntimeException unavailable){listening=false;}
    }
    public void simulation(long total) {
        total=Math.max(0,total);
        if(lastSimulation<0)lastSimulation=total;
        else if(total>=lastSimulation){steps+=total-lastSimulation;lastSimulation=total;}
        else lastSimulation=total;
        known=true;
    }
    public void pause() {
        listening=false;if(sensors!=null)sensors.unregisterListener(this);baseline=-1;
    }
    public void reset() {pause();steps=0;known=false;lastSimulation=-1;}
    public void restore(long value,boolean available) {reset();steps=Math.max(0,value);known=available;}
    public long count() {return steps;}
    public boolean known() {return known;}
    @Override public void onSensorChanged(SensorEvent event) {
        if(!listening||event.timestamp<startedNanos||event.values.length==0)return;
        if(sensor.getType()==Sensor.TYPE_STEP_DETECTOR){if(event.values[0]==1f)steps++;return;}
        float value=event.values[0];if(Float.isNaN(value)||Float.isInfinite(value)||value<0)return;
        long total=(long)value;
        if(baseline>=0&&total>=baseline)steps+=total-baseline;
        baseline=total;
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy) {}
}
