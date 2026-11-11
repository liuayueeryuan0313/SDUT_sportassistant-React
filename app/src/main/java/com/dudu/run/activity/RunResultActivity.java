package com.dudu.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.dudu.run.ui.*;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

/** Blank result-wait screen. Back leaves immediately; the session has a fixed deadline. */
public final class RunResultActivity extends Activity implements RunResultSession.Listener {
    private RunResultSession session;
    private boolean resumed,leaving;
    public static void open(Activity source){
        try{
            RunResultSession job=RunResultSession.prepare(source);
            source.startActivity(intent(source,job.runId,job.localOnly));source.finish();
        }catch(RuntimeException error){com.dudu.run.utils.r.a(source,error.getMessage());}
    }
    public static void resume(Activity source,long id){
        resume(source,id,com.dudu.run.simulation.OfflineTestSession.active());
    }
    public static void resume(Activity source,long id,boolean local){
        source.startActivity(intent(source,id,local));source.finish();
    }
    private static Intent intent(Activity source,long id,boolean local){
        return new Intent(source,RunResultActivity.class).putExtra("completed_run_id",id).putExtra("local_only",local);
    }
    @Override protected void onCreate(Bundle saved){
        super.onCreate(saved);
        RunnerUi ui=new RunnerUi(this);NativeMaterial material=new NativeMaterial(this);ui.applySystemBars(this);
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(ui.page);
        LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.VERTICAL);center.setGravity(Gravity.CENTER);
        center.setPadding(ui.dp(24),ui.dp(24),ui.dp(24),ui.dp(24));
        CircularProgressIndicator progress=new CircularProgressIndicator(material.context());
        progress.setIndicatorSize(ui.dp(40));progress.setTrackThickness(ui.dp(3));
        progress.setIndicatorColor(ui.accent);progress.setTrackColor(ui.tone);progress.setIndeterminate(true);
        center.addView(progress,new LinearLayout.LayoutParams(ui.dp(48),ui.dp(48)));
        MaterialTextView label=new MaterialTextView(material.context());label.setText("正在获取结果");
        label.setTextColor(ui.ink);label.setTextSize(15);label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textPosition=new LinearLayout.LayoutParams(-2,-2);textPosition.topMargin=ui.dp(12);center.addView(label,textPosition);
        root.addView(center,new FrameLayout.LayoutParams(-2,-2,Gravity.CENTER));setContentView(root);
        long id=getIntent().getLongExtra("completed_run_id",0);
        if(id<=0){exit();return;}
        session=RunResultSession.resolve(this,id,getIntent().getBooleanExtra("local_only",false));
        PredictiveBack.confirmBeforeLeaving(this,this::exit);session.start();
    }
    @Override protected void onResume(){super.onResume();resumed=true;if(session!=null)session.attach(this);}
    @Override protected void onPause(){resumed=false;if(session!=null)session.detach(this);super.onPause();}
    @Override public void onBackPressed(){exit();}
    private void exit(){
        if(leaving)return;leaving=true;if(session!=null){session.detach(this);session.leave();}finish();
    }
    @Override public void changed(RunResultSession job){
        if(!resumed||leaving||isFinishing()||isDestroyed())return;
        if(job.state==RunResultSession.READY){
            leaving=true;job.detach(this);
            NativeNavigation.launch(this,new Intent(this,RecordDetailActivity.class)
                    .putExtra("recordId",job.recordId).putExtra("completed_run_id",job.runId).putExtra("local_only",job.localOnly));
            finish();
        }else if(job.state!=RunResultSession.WAITING){com.dudu.run.utils.r.a(this,job.message);exit();}
    }
    @Override protected void onDestroy(){if(session!=null)session.detach(this);super.onDestroy();}
}
