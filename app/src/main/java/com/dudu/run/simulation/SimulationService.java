package com.dudu.run.simulation;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.*;
import com.dudu.run.R;

public final class SimulationService extends Service {
    private PowerManager.WakeLock wakeLock;
    private static final String CHANNEL="motion_simulation";
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)
            manager.createNotificationChannel(new NotificationChannel(CHANNEL,"运动模拟",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,201,new Intent(this,SimulationActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop=PendingIntent.getService(this,202,new Intent(this,SimulationService.class).setAction("stop"),
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        Notification notification=builder.setSmallIcon(R.mipmap.ic_location_n).setContentTitle("运动模拟运行中")
                .setContentText("点击打开控制面板；停止后不自动重启").setContentIntent(open)
                .setOngoing(true).addAction(0,"停止模拟",stop).build();
        if(Build.VERSION.SDK_INT>=34) startForeground(2201,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(2201,notification);
        PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
        wakeLock=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getPackageName()+":simulation");
        wakeLock.acquire(2*60*60*1000L);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "stop".equals(intent.getAction())) {
            SimulationController.get(this).stop(); stopSelf(); return START_NOT_STICKY;
        }
        if(!SimulationController.get(this).enabled()) { stopSelf(); return START_NOT_STICKY; }
        SimulationController.get(this).serviceStarted();
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() {
        SimulationController.get(this).serviceStopped();
        if(wakeLock!=null && wakeLock.isHeld()) wakeLock.release();
        stopForeground(true); super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
