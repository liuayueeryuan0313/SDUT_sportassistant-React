package com.dudu.run.simulation;

import android.app.*;
import com.dudu.run.ui.NativeMaterial;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

/** No remote account, login token or server records are used by this screen. */
public final class OfflineTestActivity extends Activity {
    private static final String RETURN_TO_PROFILE = "return_to_profile";
    private LinearLayout page,history;
    private TextView summary;
    private MapExplorer startLocator;
    public static void open(Activity host, boolean returnToProfile) {
        try {
            OfflineTestSession.enter(host);
            host.startActivity(new Intent(host, OfflineTestActivity.class)
                    .putExtra(RETURN_TO_PROFILE, returnToProfile));
        } catch (IllegalStateException error) {
            Toast.makeText(host, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveTest() {
        OfflineTestSession.leave();
        if (!getIntent().getBooleanExtra(RETURN_TO_PROFILE, false)) {
            startActivity(new Intent(this, com.dudu.run.activity.LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        finish();
    }
    private int dp(float n){return SimulationUi.dp(this,n);}
    private void error(Exception e){new NativeMaterial(this).dialog().setTitle("离线测试").setMessage(e.getMessage()).setPositiveButton("知道了",null).show();}
    private void action(Runnable callback){try{callback.run();}catch(Exception e){error(e);}}
    private TextView button(String text,String tag,Runnable callback){
        TextView view=SimulationUi.button(this,text,SimulationUi.NAVY,Color.WHITE,()->action(callback));
        view.setTag(tag);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.topMargin=dp(10);page.addView(view,p);return view;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        if(!OfflineTestSession.active()){finish();return;}
        com.dudu.run.ui.PredictiveBack.confirmBeforeLeaving(this, this::onBackPressed);
        getWindow().setStatusBarColor(SimulationUi.PALE);getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(SimulationUi.PALE);
        page=SimulationUi.column(this);page.setPadding(dp(22),dp(24),dp(22),dp(24));scroll.addView(page);setContentView(scroll);
        page.addView(SimulationUi.text(this,"离线测试用户",25,SimulationUi.NAVY,true));SimulationUi.gap(page,8);
        page.addView(SimulationUi.text(this,"账户  "+OfflineTestSession.ACCOUNT+"  ·  免密码",14,SimulationUi.INK,true));
        SimulationUi.gap(page,12);
        TextView note=SimulationUi.text(this,"仅本机测试 · 跑步记录不会上传\n地图、搜索、道路吸附需要联网；吸附时路线点会发给腾讯地图。断网可关闭吸附，使用手绘、坐标、GPX 或示例路线。",13,SimulationUi.MUTED,false);page.addView(note);
        summary=SimulationUi.text(this,"",15,SimulationUi.TEAL,true);SimulationUi.gap(page,14);page.addView(summary);
        startLocator=new MapExplorer(this,null,new MapExplorer.Target(){
            @Override public void beforeMove(){}
            @Override public boolean append(com.tencent.tencentmap.mapsdk.maps.model.LatLng point){return false;}
            @Override public void located(com.tencent.tencentmap.mapsdk.maps.model.LatLng point){action(()->{
                if(com.dudu.run.c.n().q()!=0)return;
                SimulationEngine.Config parameters=SimulationController.get(OfflineTestActivity.this).load();
                double height=parameters==null?30:parameters.points.get(0).altitude;
                OfflineTestSession.startWaitingRun(OfflineTestActivity.this,new SimulationEngine.Point(point.latitude,point.longitude,height));
            });}
        });
        button("开始 / 返回测试跑步","offline_run",()->startLocalRun());
        button("地图选点 / 手绘路线","offline_edit",()->{
            if(com.dudu.run.c.n().q()==0)startLocalRun();
            else startActivity(new Intent(this,SimulationActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });
        button("地图 Key 设置","offline_keys",()->MapKeySettings.show(this));
        button("示例路线测试","offline_demo",()->{startLocator.cancelLocation();OfflineTestSession.startRun(this,OfflineTestSession.demo());});
        button("退出离线测试","offline_exit",()->new NativeMaterial(this).dialog().setTitle("退出离线测试")
                .setMessage("未保存的测试跑步将被丢弃，已保存的本机记录会保留。")
                .setNegativeButton("取消",null).setPositiveButton("退出",(d,w)->leaveTest()).show());
        SimulationUi.gap(page,24);page.addView(SimulationUi.text(this,"本机测试记录（最近 20 次）",18,SimulationUi.NAVY,true));
        history=SimulationUi.column(this);page.addView(history);
        TextView clear=SimulationUi.button(this,"清空本机测试记录",Color.WHITE,SimulationUi.RED,()->new NativeMaterial(this).dialog().setTitle("清空测试记录？")
                .setMessage("只删除本机的离线测试记录。") .setNegativeButton("取消",null).setPositiveButton("清空",(d,w)->{try{OfflineTestSession.clearRecords();refresh();}catch(Exception e){error(e);}}).show());
        LinearLayout.LayoutParams clearLayout=new LinearLayout.LayoutParams(-1,dp(48));clearLayout.topMargin=dp(12);page.addView(clear,clearLayout);
    }
    private void startLocalRun(){
        if(com.dudu.run.c.n().q()!=0){
            startActivity(new Intent(this,com.dudu.run.activity.SportMapActivity.class));return;
        }
        summary.setText("正在获取当前位置；倒计时后进入工作台画线");startLocator.locate();
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){
        super.onRequestPermissionsResult(code,permissions,results);if(startLocator!=null)startLocator.permissionResult(code);
    }
    @Override protected void onStop(){if(startLocator!=null)startLocator.cancelLocation();super.onStop();}
    @Override protected void onDestroy(){if(startLocator!=null)startLocator.close();super.onDestroy();}
    @Override protected void onResume(){super.onResume();if(!OfflineTestSession.active()){finish();return;}refresh();}
    private void refresh(){
        if(summary==null)return;
        try{
            SimulationEngine.Config config=SimulationController.get(this).load();
            summary.setText(config==null?"开始测试后定位、倒计时，再进入工作台画线":String.format(Locale.CHINA,"已保存参数 · %.1f km/h · 每次新跑步重新画线",config.speedKmh));
            history.removeAllViews();JSONArray records=OfflineTestSession.records();
            if(records.length()==0){SimulationUi.gap(history,12);history.addView(SimulationUi.text(this,"暂无记录；结束测试跑步后可保存在这里。",13,SimulationUi.MUTED,false));}
            for(int i=0;i<records.length();i++){
                JSONObject record=records.getJSONObject(i);
                String date=new SimpleDateFormat("MM-dd HH:mm",Locale.CHINA).format(new Date(record.getLong("savedAt")));
                String label=String.format(Locale.CHINA,"%s   %.3f km · %d 秒 · %d 轨迹点",date,record.getDouble("meters")/1000,record.getInt("seconds"),record.getJSONArray("track").length());
                TextView item=SimulationUi.button(this,label,Color.WHITE,SimulationUi.INK,()->showRecord(record));item.setSingleLine(false);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(8);history.addView(item,p);
            }
        }catch(Exception e){error(e);}
    }
    private void showRecord(JSONObject record){
        try{
            LinearLayout content=SimulationUi.column(this);content.setPadding(dp(18),dp(12),dp(18),dp(12));
            content.addView(SimulationUi.text(this,String.format(Locale.CHINA,"%.3f km · %d 秒\n本机测试记录 · 未上传",record.getDouble("meters")/1000,record.getInt("seconds")),14,SimulationUi.INK,false));
            content.addView(new RoutePreview(record.getJSONArray("track")),new LinearLayout.LayoutParams(-1,dp(260)));
            new NativeMaterial(this).dialog().setTitle("离线轨迹预览").setView(content).setPositiveButton("关闭",null).show();
        }catch(Exception e){error(e);}
    }
    @Override public void onBackPressed(){findViewByTag("offline_exit").performClick();}
    private View findViewByTag(String tag){return page.findViewWithTag(tag);}
    private final class RoutePreview extends View{
        final JSONArray rows;final Paint paint=new Paint(3);
        RoutePreview(JSONArray rows){super(OfflineTestActivity.this);this.rows=rows;}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);canvas.drawColor(Color.WHITE);
            if(rows.length()==0){paint.setColor(SimulationUi.MUTED);paint.setTextSize(dp(14));canvas.drawText("本次没有记录到轨迹点",dp(16),dp(80),paint);return;}
            try{
                double minX=180,maxX=-180,minY=90,maxY=-90;
                for(int i=0;i<rows.length();i++){JSONArray r=rows.getJSONArray(i);double y=r.getDouble(0),x=r.getDouble(1);minX=Math.min(x,minX);maxX=Math.max(x,maxX);minY=Math.min(y,minY);maxY=Math.max(y,maxY);}
                double cos=Math.cos((minY+maxY)*Math.PI/360),dx=Math.max((maxX-minX)*cos,.000001),dy=Math.max(maxY-minY,.000001);
                double scale=Math.min((getWidth()-dp(40))/dx,(getHeight()-dp(40))/dy);Path path=new Path();
                float firstX=0,firstY=0,lastX=0,lastY=0;
                for(int i=0;i<rows.length();i++){JSONArray r=rows.getJSONArray(i);float x=(float)(getWidth()/2.0+(r.getDouble(1)-(minX+maxX)/2)*cos*scale);float y=(float)(getHeight()/2.0-(r.getDouble(0)-(minY+maxY)/2)*scale);if(i==0||r.optBoolean(2,false)){path.moveTo(x,y);}else path.lineTo(x,y);if(i==0){firstX=x;firstY=y;}lastX=x;lastY=y;}
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(3));paint.setColor(SimulationUi.TEAL);canvas.drawPath(path,paint);
                paint.setStyle(Paint.Style.FILL);paint.setColor(0xff4CB85B);canvas.drawCircle(firstX,firstY,dp(5),paint);paint.setColor(SimulationUi.RED);canvas.drawCircle(lastX,lastY,dp(5),paint);
            }catch(Exception ignored){}
        }
    }
}
