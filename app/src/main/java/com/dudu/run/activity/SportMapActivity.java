package com.dudu.run.activity;

import androidx.appcompat.app.AlertDialog;
import com.dudu.run.ui.RunScreenLayout;
import com.dudu.run.ui.RunMapGlass;
import com.dudu.run.ui.RunnerUi;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.dudu.run.R;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.utils.Accompaniment;
import com.dudu.run.utils.m;
import com.dudu.run.utils.r;
import com.dudu.run.widget.LongClickProgressView;
import com.tencent.map.geolocation.util.DateUtils;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MapRouteSectionWithName;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SportMapActivity extends BaseActivity implements LocationSource, com.dudu.run.c.b, com.dudu.run.utils.d.b, com.dudu.run.simulation.SimulationController.Listener {
    private LocationSource.OnLocationChangedListener B;
    private TencentMap C;
    private boolean simulatedCameraCentered;
    private RunScreenLayout runScreen;
    private RunMapGlass glass;
    private boolean screenResumed;
    private boolean pendingRouteEditor,routeEditorOpened;
    private m D;
    private boolean E = false;
    private Map<RandomPoint, Marker> F = new HashMap();
    private StringBuffer G = new StringBuffer();
    private int H = 0;

    @BindView(R.id.btn_long_click_finish)
    LongClickProgressView btn_long_click_finish;

    @BindView(R.id.iv_gps_status)
    ImageView iv_gps_status;

    @BindView(R.id.layout_countdown)
    View layout_countdown;

    @BindView(R.id.layout_status_go)
    View layout_status_go;

    @BindView(R.id.layout_status_pause)
    View layout_status_pause;

    @BindView(R.id.mapView)
    TextureMapView mapView;

    @BindView(R.id.tv_countdown)
    TextView tv_countdown;

    @BindView(R.id.tv_location_accuracy)
    TextView tv_location_accuracy;

    @BindView(R.id.tv_sport_cal)
    TextView tv_sport_cal;

    @BindView(R.id.tv_sport_speed)
    TextView tv_sport_speed;

    @BindView(R.id.tv_sport_time)
    TextView tv_sport_time;

    @BindView(R.id.tv_std_distance)
    TextView tv_std_distance;

    @BindView(R.id.tv_std_speed)
    TextView tv_std_speed;

    @BindView(R.id.tv_total_distance)
    TextView tv_total_distance;

    @BindView(R.id.tv_unpassed_random_count)
    TextView tv_unpassed_random_count;

    class a implements LongClickProgressView.c {
        a() {
        }

        @Override // com.dudu.run.widget.LongClickProgressView.c
        public void a(float f2) {
        }

        @Override // com.dudu.run.widget.LongClickProgressView.c
        public void onCancel() {
        }

        @Override // com.dudu.run.widget.LongClickProgressView.c
        public void onFinish() {
            com.dudu.run.c.n().L();
            SportMapActivity.this.O0();
        }
    }

    class b implements DialogInterface.OnClickListener {
        b(SportMapActivity sportMapActivity) {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
        }
    }

    class c implements DialogInterface.OnClickListener {
        c() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            com.dudu.run.c.n().K();
            com.dudu.run.c.n().B();
            SportMapActivity.this.finish();
        }
    }

    private void E0() {
        this.btn_long_click_finish.setOnLongClickStateListener(new a());
    }

    private boolean F0() {
        this.H = 0;
        StringBuffer stringBuffer = this.G;
        stringBuffer.delete(0, stringBuffer.length());
        if (com.dudu.run.c.n().r() < com.dudu.run.b.c().g()) {
            this.H++;
            this.G.append(this.H + ". 跑步距离不达标\n");
        }
        int iH = com.dudu.run.b.c().h();
        int iF = com.dudu.run.b.c().f();
        int iP = com.dudu.run.c.n().p();
        if (iP >= iH || iP <= iF) {
            this.H++;
            this.G.append(this.H + ". 平均配速不达标\n");
        }
        int iU = com.dudu.run.c.n().u();
        if (iU > 0) {
            this.H++;
            this.G.append(this.H + ". 有" + iU + "个随机点未通过\n");
        }
        return this.H == 0;
    }

    private void G0(LatLngInfo latLngInfo, LatLngInfo latLngInfo2) {
        if (latLngInfo.isStartPosition) {
            this.C.addPolyline(new PolylineOptions().add(latLngInfo2.latLng, latLngInfo.latLng).width(15.0f).lineType(1).color(new RunnerUi(this).muted));
        } else {
            this.C.addPolyline(new PolylineOptions().add(latLngInfo2.latLng, latLngInfo.latLng).width(15.0f).color(new RunnerUi(this).accent));
        }
    }

    private void H0() {
        m mVar = new m();
        this.D = mVar;
        mVar.a(4);
    }

    private void I0() {
        List<LatLngInfo> listT = com.dudu.run.c.n().t();
        for (int i = 0; i < listT.size(); i++) {
            if (i != 0) {
                G0(listT.get(i), listT.get(i - 1));
            }
        }
    }

    private void J0() {
        if (com.dudu.run.simulation.OfflineTestSession.active()) {
            this.tv_std_distance.setText("离线测试 · 不上传");
            this.tv_std_speed.setText("账户：offline-test");
            this.tv_location_accuracy.setVisibility(8);
            ((TextView)findViewById(R.id.btn_run_finish)).setText("保存测试");
            return;
        }
        this.tv_std_distance.setText("达标公里:" + com.dudu.run.utils.o.b(com.dudu.run.b.c().g()) + "~" + com.dudu.run.utils.o.b(com.dudu.run.b.c().e()));
        this.tv_std_speed.setText("达标速度:" + com.dudu.run.utils.o.f(com.dudu.run.b.c().f()) + "~" + com.dudu.run.utils.o.f(com.dudu.run.b.c().h()));
        this.tv_sport_speed.setText(com.dudu.run.utils.o.e(0.0f, 0));
        this.tv_location_accuracy.setVisibility(8);
    }

    private void K0() {
        this.C.getUiSettings().setMyLocationButtonEnabled(true);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(0);
        myLocationStyle.fillColor(Color.argb(0, 0, 0, 0));
        myLocationStyle.icon(BitmapDescriptorFactory.fromView(View.inflate(this, R.layout.view_location, null)));
        myLocationStyle.myLocationType(0);
        this.C.setMyLocationStyle(myLocationStyle);
        this.C.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
        this.C.setLocationSource(this);
        this.C.setMyLocationEnabled(true);
    }

    private void L0() {
        new com.dudu.run.ui.NativeMaterial(this).dialog().setTitle("提示").setMessage("有条件未满足，跑步记录不会上传:\n" + ((Object) this.G)).setPositiveButton("放弃并退出", new c()).setNegativeButton("取消", new b(this)).show();
    }

    private void N0() {
        this.F.clear();
        updateCheckpoints();
        List<RandomPoint> listO = com.dudu.run.c.n().o();
        if (listO == null || listO.isEmpty()) {
            return;
        }
        for (RandomPoint randomPoint : listO) {
            this.F.put(randomPoint, this.C.addMarker(new MarkerOptions().position(new LatLng(randomPoint.a(), randomPoint.b())).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), randomPoint.c() ? R.mipmap.ic_point_yes : R.mipmap.ic_point_no)))));
        }
        updateCheckpoints();
    }
    public void O0() {
        if(screenResumed)com.dudu.run.simulation.SimulationOverlay.attach(this);
        updateSteps();
        if (com.dudu.run.c.n().y()) {
            this.layout_status_pause.setVisibility(0);
            this.layout_status_go.setVisibility(8);
        } else {
            this.layout_status_pause.setVisibility(8);
            this.layout_status_go.setVisibility(0);
        }
    }

    @Override // com.dudu.run.c.b
    public void H(RandomPoint randomPoint) {
        if (isFinishing()) {
            return;
        }
        try {
            Marker marker = this.F.get(randomPoint);
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), randomPoint.c() ? R.mipmap.ic_point_yes : R.mipmap.ic_point_no)));
            }
            updateCheckpoints();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    @Override // com.dudu.run.utils.d.b
    public void I() {
    }

    @Override // com.dudu.run.c.b
    public void L(Location location, int i) {
        if (location == null) return;
        if (com.dudu.run.c.n().isSimulationSource() && !simulatedCameraCentered && this.C != null) {
            simulatedCameraCentered = true;
            this.C.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
        }
        LocationSource.OnLocationChangedListener onLocationChangedListener = this.B;
        if (onLocationChangedListener != null) {
            onLocationChangedListener.onLocationChanged(location);
        }
        if (isFinishing()) {
            return;
        }
        gpsSignalChanged(i);
        if (glass != null) glass.invalidate();
    }

    @Override public void gpsSignalChanged(int level){
        if(runScreen==null||isFinishing()||isDestroyed())return;
        runScreen.signal.level(level);
        iv_gps_status.setContentDescription("GPS 信号"+(level==3?"强":level==2?"中":level==1?"弱":"未知或不可用"));
    }
    private void updateCheckpoints(){
        int total=com.dudu.run.c.n().o().size();
        tv_unpassed_random_count.setText((total-com.dudu.run.c.n().u())+"/"+total);
    }
    private void updateSteps(){
        if(runScreen!=null)runScreen.steps.setText(com.dudu.run.c.n().hasRecordedSteps()?String.format(java.util.Locale.CHINA,"%,d 步",com.dudu.run.c.n().recordedSteps()):"—");
    }

    @Override // com.dudu.run.utils.d.b
    public void M() {
        com.dudu.run.c.n().J();
        O0();
        pendingRouteEditor=com.dudu.run.simulation.SimulationController.get().waitingForRoute();
        if(pendingRouteEditor){this.tv_countdown.setText("准备画线");openPendingRouteEditor();}
        else this.layout_countdown.setVisibility(View.GONE);
    }

    private void openPendingRouteEditor(){
        if(!pendingRouteEditor||routeEditorOpened||!hasWindowFocus()||isFinishing())return;
        pendingRouteEditor=false;routeEditorOpened=true;
        // Keep the opaque countdown cover until the editor has taken over. No intermediate map flash.
        startActivity(new android.content.Intent(this,com.dudu.run.simulation.SimulationActivity.class)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        overridePendingTransition(0,0);
    }
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)openPendingRouteEditor();}

    @Override // com.tencent.tencentmap.mapsdk.maps.LocationSource
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        this.B = onLocationChangedListener;
    }

    @OnClick({R.id.btn_run_finish})
    public void clickViewRunFinish(View view) {
        if (!com.dudu.run.simulation.OfflineTestSession.active() && !F0()) {
            L0();return;
        }
        RunResultActivity.open(this);
    }

    @OnClick({R.id.btn_run_go})
    public void clickViewRunGo(View view) {
        if (System.currentTimeMillis() - com.dudu.run.c.n().q() > DateUtils.ONE_HOUR) {
            r.a(this, "距开始跑步时间超过1小时，无法继续，请结束跑步");
        } else {
            com.dudu.run.c.n().J();
            O0();
        }
    }

    @Override // com.tencent.tencentmap.mapsdk.maps.LocationSource
    public void deactivate() {
    }

    @Override // com.dudu.run.c.b
    public void h(int i, float f2) {
        updateSteps();
        String strB = com.dudu.run.utils.o.b(f2);
        String strE = com.dudu.run.utils.o.e(f2, i);
        String strC = com.dudu.run.utils.o.c(i);
        String strA = com.dudu.run.utils.o.a(60.0d, f2);
        this.tv_total_distance.setText(strB+" km");
        this.tv_sport_speed.setText(strE+" /km");
        this.tv_sport_time.setText(strC);
        this.tv_sport_cal.setText(strA);
    }

    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants) {
        if(code==741){com.dudu.run.c.n().refreshDeviceStepCounter();return;}
        super.onRequestPermissionsResult(code,permissions,grants);
    }

    @Override // com.dudu.legacy.androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (com.dudu.run.ui.RunResultSession.hasPending(this,com.dudu.run.c.n().q())) {
            RunResultActivity.resume(this,com.dudu.run.c.n().q());return;
        }
        if (com.dudu.run.simulation.OfflineTestSession.active() && !com.dudu.run.simulation.SimulationController.get().enabled()) {
            startActivity(new android.content.Intent(this,com.dudu.run.simulation.OfflineTestActivity.class)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();return;
        }
        this.E = getIntent().getBooleanExtra("isNeedCountDownView", false)&&com.dudu.run.c.n().q()==0;
        if(bundle!=null){pendingRouteEditor=bundle.getBoolean("pendingRouteEditor");routeEditorOpened=bundle.getBoolean("routeEditorOpened");}
        getWindow().addFlags(MapRouteSectionWithName.kMaxRoadNameLength);
        runScreen=(RunScreenLayout)mapView.getParent();
        this.C = this.mapView.getMap();
        glass=new RunMapGlass(runScreen,mapView,C);
        this.C.setMapType(new RunnerUi(this).dark?TencentMap.MAP_TYPE_DARK:TencentMap.MAP_TYPE_NORMAL);
        J0();
        K0();
        H0();
        com.dudu.run.c.n().H(this);
        com.dudu.run.c.n().I(true);
        if(!com.dudu.run.c.n().isSimulationSource())com.dudu.run.ui.RunSteps.requestPermission(this);
        O0();
        com.dudu.run.simulation.SimulationController.get().addListener(this);
        E0();
        I0();
        N0();
        h(com.dudu.run.c.n().s(),com.dudu.run.c.n().r());
        if (!this.E) {
            this.layout_countdown.setVisibility(pendingRouteEditor?View.VISIBLE:View.GONE);
        } else {
            this.layout_countdown.setVisibility(0);
            com.dudu.run.utils.d.d(this.tv_countdown, this);
        }
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.run.permission.PermissionActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        if(glass!=null)glass.close();
        super.onDestroy();
        com.dudu.run.simulation.SimulationController.get().removeListener(this);
        com.dudu.run.c.n().H(null);
        this.B = null;
        this.mapView.onDestroy();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        screenResumed=false;if(glass!=null)glass.pause();
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
        screenResumed=true;if(glass!=null)glass.resume();
        gpsSignalChanged(com.dudu.run.c.n().gpsSignalLevel());
        if(routeEditorOpened)this.layout_countdown.setVisibility(View.GONE);
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("pendingRouteEditor",pendingRouteEditor);bundle.putBoolean("routeEditorOpened",routeEditorOpened);
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

    @Override
    public void onSimulationChanged() {
        if (!isFinishing()) {O0();updateSteps();}
    }

    @Override // com.dudu.run.utils.d.b
    public void start() {
        Accompaniment.a().g(R.raw.count_down);
    }

    @Override // com.dudu.run.activity.BaseActivity
    int u0() {
        return R.layout.activity_sport_map;
    }

    @Override // com.dudu.run.activity.BaseActivity
    boolean v0() {
        return true;
    }

    @Override // com.dudu.run.c.b
    public void w(LatLngInfo latLngInfo, LatLngInfo latLngInfo2) {
        if (latLngInfo2 == null) {
            return;
        }
        G0(latLngInfo, latLngInfo2);
        if (glass != null) glass.invalidate();
    }
}
