package com.dudu.run.utils;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Application;
import android.os.Bundle;
import com.dudu.run.ui.NativeMaterial;

/** Process-only notice: one acceptance per app process, never persisted across cold starts. */
public final class StartupDisclaimer implements Application.ActivityLifecycleCallbacks {
    public static final String MESSAGE = "运动助手\n\n"
            +"本应用为非官方修改版本，仅供个人学习、功能体验及授权测试使用。\n\n"
            +"模拟位置、路线和运动数据不代表真实运动或正式成绩。请遵守适用法律法规及相关平台、学校的使用规则，不得冒用他人账号或用于违规用途。\n\n"
            +"请自行评估使用风险。本声明不替代原应用的用户协议及隐私政策，也不免除依法应承担的责任。";
    private static final StartupDisclaimer INSTANCE=new StartupDisclaimer();
    private boolean required=true;
    private AlertDialog dialog;
    private Activity owner;
    private Runnable continuation;

    private StartupDisclaimer() {}
    public static void install(Application application){application.registerActivityLifecycleCallbacks(INSTANCE);}
    public static void beforeStartup(Activity activity,Runnable next){INSTANCE.show(activity,next);}

    private void clear(){dialog=null;owner=null;continuation=null;}
    private void show(Activity activity,Runnable next){
        if(activity.isFinishing()||activity.isDestroyed())return;
        if(!required){if(next!=null)next.run();return;}
        if(dialog!=null){
            if(owner==activity){if(next!=null)continuation=next;return;}
            dialog.dismiss();clear();
        }
        owner=activity;continuation=next;
        NativeMaterial material = new NativeMaterial(activity);
        dialog=material.dialog()
            .setTitle("免责声明")
            .setMessage(MESSAGE)
            .setPositiveButton("同意并继续",(d,which)->{
                required=false;Runnable after=continuation;clear();
                if(after!=null&&!activity.isFinishing()&&!activity.isDestroyed())after.run();
            })
            .setNegativeButton("退出",(d,which)->{clear();required=true;activity.finishAffinity();})
            .create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        material.showStartupNotice(dialog);
    }
    @Override public void onActivityStarted(Activity activity){}
    @Override public void onActivityResumed(Activity activity){if(required)show(activity,null);}
    // Locking the screen, opening Settings, and ordinary backgrounding do not revoke acceptance.
    @Override public void onActivityStopped(Activity activity){}
    @Override public void onActivityDestroyed(Activity activity){
        if(owner==activity){if(dialog!=null)dialog.dismiss();clear();}
    }
    @Override public void onActivityCreated(Activity activity,Bundle state){}
    @Override public void onActivityPaused(Activity activity){}
    @Override public void onActivitySaveInstanceState(Activity activity,Bundle state){}
}
