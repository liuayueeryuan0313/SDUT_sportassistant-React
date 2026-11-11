package com.dudu.run.activity;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.dudu.run.R;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.RecordDetailResponseData;
import com.dudu.run.e.j;
import com.dudu.run.utils.o;
import com.dudu.run.utils.r;
import com.google.gson.e;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;
import java.util.List;
public class RecordDetailActivity extends BaseActivity implements j {
    private com.dudu.run.ui.RunnerUi palette;
    private java.util.List<LatLngInfo> displayedTrack;
    private TencentMap B;
    private int C;
    private int dataGeneration;

    @BindView(R.id.mapView)
    TextureMapView mapView;

    @BindView(R.id.tv_sport_cal)
    TextView tv_sport_cal;

    @BindView(R.id.tv_sport_speed)
    TextView tv_sport_speed;

    @BindView(R.id.tv_sport_time)
    TextView tv_sport_time;

    @BindView(R.id.tv_start_time)
    TextView tv_start_time;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.tv_total_distance)
    TextView tv_total_distance;

    public static List<LatLngInfo> parseTrack(String track) {
        List<LatLngInfo> raw=new e().j(track, new com.google.gson.t.a<List<LatLngInfo>>() {}.e());
        List<LatLngInfo> valid=new java.util.ArrayList<>();boolean gap=true;
        if(raw!=null)for(LatLngInfo point:raw){
            if(point==null||point.latLng==null||Double.isNaN(point.latLng.latitude)||Double.isNaN(point.latLng.longitude)
                    ||Math.abs(point.latLng.latitude)>90||Math.abs(point.latLng.longitude)>180){gap=true;continue;}
            LatLngInfo copy=new LatLngInfo();copy.latLng=point.latLng;copy.isStartPosition=gap||point.isStartPosition;
            valid.add(copy);gap=false;
        }
        return valid;
    }

    private void E0() {
        this.tv_sport_speed.setText(o.e(0.0f, 0));
    }

    private void F0() {
        this.B.getUiSettings().setMyLocationButtonEnabled(false);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(0);
        myLocationStyle.fillColor(Color.argb(0, 0, 0, 0));
        myLocationStyle.icon(BitmapDescriptorFactory.fromView(View.inflate(this, R.layout.view_location, null)));
        myLocationStyle.myLocationType(0);
        this.B.setMyLocationStyle(myLocationStyle);
    }

    private void G0(List<LatLngInfo> list) {
        if (list == null || list.isEmpty()) return;
        B.clear();
        PolylineOptions segment=new PolylineOptions().width(12f).color(palette.accent);
        int segmentSize=0;
        LatLngInfo first=null,last=null;
        for(LatLngInfo point:list){
            if(point==null||point.latLng==null)continue;
            if(first==null){first=point;segment.add(point.latLng);segmentSize=1;}
            else if(point.isStartPosition){
                if(segmentSize>1)B.addPolyline(segment);
                B.addPolyline(new PolylineOptions().add(last.latLng,point.latLng).width(12f).lineType(2).color(palette.muted));
                segment=new PolylineOptions().width(12f).color(palette.accent).add(point.latLng);segmentSize=1;
            }else{segment.add(point.latLng);segmentSize++;}
            last=point;
        }
        if(segmentSize>1)B.addPolyline(segment);
        if(first!=null){
            B.addMarker(new MarkerOptions().position(first.latLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_track_start))));
            if(last!=first)B.addMarker(new MarkerOptions().position(last.latLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_track_end))));
        }
        displayedTrack=list;fitTrack();
    }

    private void fitTrack() {
        if (displayedTrack == null || displayedTrack.isEmpty() || B == null) return;
        com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds.Builder bounds = new com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds.Builder();
        int count=0;
        for (LatLngInfo point : displayedTrack) if (point != null && point.latLng != null) {
            bounds.include(point.latLng); count++;
        }
        if (count > 1) try { B.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), palette.dp(32))); }
        catch (IllegalStateException ignored) { }
    }

    private void styleDetail() {
        palette = new com.dudu.run.ui.RunnerUi(this);
        tv_title.setText("运动轨迹");
        findViewById(R.id.runner_record_detail_root).setBackgroundColor(palette.map);
        for (int id : new int[]{R.id.runner_record_title_capsule, R.id.runner_record_stats}) {
            com.google.android.material.card.MaterialCardView card=findViewById(id);
            card.setCardBackgroundColor(palette.surface); card.setStrokeColor(palette.outline); card.setStrokeWidth(palette.dp(1));
        }
        for (TextView value : new TextView[]{tv_title, tv_sport_speed, tv_sport_time, tv_sport_cal}) value.setTextColor(palette.ink);
        tv_total_distance.setTextColor(palette.accentText); tv_start_time.setTextColor(palette.muted);
        for (int id : new int[]{R.id.runner_record_unit,R.id.runner_record_pace_label,R.id.runner_record_time_label,R.id.runner_record_cal_label})
            ((TextView)findViewById(id)).setTextColor(palette.muted);
        findViewById(R.id.runner_record_divider).setBackgroundColor(palette.outline);
        android.widget.FrameLayout back=findViewById(R.id.runner_record_back);
        back.addView(new com.dudu.run.ui.NativeMaterial(this).iconButton(R.drawable.ic_demo_arrow_left,"返回",this::finish),new android.widget.FrameLayout.LayoutParams(-1,-1));
        View stats=findViewById(R.id.runner_record_stats), header=findViewById(R.id.runner_record_header);
        stats.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)-> {
            if (B != null) { B.setPadding(palette.dp(16),header.getBottom()+palette.dp(16),palette.dp(16),stats.getHeight()+palette.dp(32)); fitTrack(); }
        });
    }

    private void H0(RecordDetailResponseData recordDetailResponseData, List<LatLngInfo> preparedTrack) {
        if (recordDetailResponseData == null) {
            return;
        }
        this.tv_total_distance.setText(o.b(recordDetailResponseData.c()));
        this.tv_start_time.setText(recordDetailResponseData.a());
        this.tv_sport_speed.setText(o.e(recordDetailResponseData.c(), recordDetailResponseData.d()));
        this.tv_sport_time.setText(o.c((long) recordDetailResponseData.d()));
        this.tv_sport_cal.setText(o.a(60.0d, recordDetailResponseData.c()));
        if(preparedTrack!=null)G0(preparedTrack);
    }

    @Override // com.dudu.run.e.j
    public void A(RecordDetailResponseData recordDetailResponseData) {
        if (isFinishing()) {
            return;
        }
        int token=++dataGeneration;
        com.dudu.run.ui.RunnerWork.IO.execute(()->{
            List<LatLngInfo> parsed=null;
            try{if(recordDetailResponseData!=null&&!TextUtils.isEmpty(recordDetailResponseData.b()))parsed=parseTrack(recordDetailResponseData.b());}
            catch(RuntimeException ignored){}
            List<LatLngInfo> prepared=parsed;
            runOnUiThread(()->{if(token==dataGeneration&&!isFinishing()&&!isDestroyed())H0(recordDetailResponseData,prepared);});
        });
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.C = getIntent().getIntExtra("recordId", 0);
        styleDetail();
        this.B = this.mapView.getMap();
        this.B.setMapType(palette.dark ? TencentMap.MAP_TYPE_DARK : TencentMap.MAP_TYPE_NORMAL);
        F0();
        E0();
        long completed=getIntent().getLongExtra("completed_run_id",0);
        com.dudu.run.ui.RunResultSession ready=com.dudu.run.ui.RunResultSession.ready(completed,this.C);
        if(ready!=null)H0(ready.detail,ready.track);
        else if(getIntent().getBooleanExtra("local_only",false)){
            // A restored local result must never call the remote record endpoint.
            RunResultActivity.resume(this,completed,true);
        }else{
            int token=dataGeneration;
            // Keep request setup/signing outside the entry transition's frames.
            mapView.postDelayed(()->{
                if(token!=dataGeneration||isFinishing()||isDestroyed())return;
                com.dudu.run.ui.RunnerWork.IO.execute(()->{
                    try{new com.dudu.run.e.s.e(this).a(this.C);}
                    catch(RuntimeException error){runOnUiThread(()->{
                        if(token==dataGeneration&&!isFinishing()&&!isDestroyed())q("记录加载失败，请稍后重试");
                    });}
                });
            },360);
        }
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.run.permission.PermissionActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        dataGeneration++;
        super.onDestroy();
        this.mapView.onDestroy();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        this.mapView.onPause();
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
        this.mapView.onRestart();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        this.mapView.onResume();
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        this.mapView.onStart();
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.mapView.onStop();
    }

    @Override // com.dudu.run.e.j
    public void q(String str) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        r.a(this, str);
    }

    @Override // com.dudu.run.activity.BaseActivity
    int u0() {
        return R.layout.runner_record_detail;
    }

    @Override // com.dudu.run.e.j
    public void x() {
    }
}
