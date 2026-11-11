package com.dudu.run.fragment;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.dudu.run.R;
import com.dudu.run.activity.BaseActivity;
import com.dudu.run.activity.HomeActivity;
import com.dudu.run.activity.SportMapActivity;
import com.dudu.run.bean.LatLngInfo;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.e.q;
import com.dudu.run.e.r;
import com.dudu.run.utils.i;
import com.dudu.run.utils.l;
import com.dudu.run.ui.SportDashboard;
import com.dudu.run.ui.RunnerUi;
import java.util.List;
public class SportFragment extends com.dudu.run.fragment.a implements r, com.dudu.run.c.b {
    private SportDashboard dashboard;
    private d f1609d;
    private q f1610e;

    @BindView(R.id.tv_my_count)
    TextView tv_my_count;

    @BindView(R.id.tv_run_length)
    TextView tv_run_length;
    protected String[] f1611f = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private int m = 1;
    private Location pendingRunOrigin;
    private int initializationStage;
    private boolean automaticRun=true;
    private void startInitializationStage(int stage,String label){
        initializationStage=stage;f1609d.removeMessages(1);
        android.util.Log.i("RunInit","stage="+stage+" "+label);
        if(getActivity()!=null&&!getActivity().isFinishing())((BaseActivity)getActivity()).z0(label);
        f1609d.sendEmptyMessageDelayed(1,stage==2?25000:40000);
    }
    private void finishInitialization(){
        android.util.Log.i("RunInit","stage_finished="+initializationStage);
        initializationStage=0;f1609d.removeMessages(1);
    }
    private void initializationTimeout(){
        int stage=initializationStage;if(stage==0)return;
        if(stage==2){com.dudu.run.c.n().K();com.dudu.run.c.n().H(null);}
        finishInitialization();
        if(getActivity()==null||isDetached()||getActivity().isFinishing())return;
        ((BaseActivity)getActivity()).s0();
        String name=stage==1?"跑步资格检查":stage==2?"设备定位":stage==3?"校区查询":"打卡点获取";
        android.util.Log.w("RunInit","timeout stage="+stage);
        com.dudu.run.utils.r.a(getActivity(),name+"超时，请检查后重试");
    }
    @Override public void initializationLocationError(int code,String message){
        if(initializationStage!=2)return;
        com.dudu.run.c.n().K();finishInitialization();
        if(getActivity()!=null&&!isDetached()&&!getActivity().isFinishing()){
            ((BaseActivity)getActivity()).s0();
            com.dudu.run.utils.r.a(getActivity(),"定位失败（"+code+"）："+message);
        }
    }

    class a extends com.dudu.run.permission.c {
        a() {
        }

        @Override // com.dudu.run.permission.c
        public void b() {
            SportFragment.this.f1610e.c();
        }
    }

    private class d extends Handler {
        public d(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if(message.what==1)SportFragment.this.initializationTimeout();
        }
    }

    private void x() {
        if (dashboard != null) dashboard.renderStats(false);
    }

    private void y(double latitude, double longitude) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        // The homepage selection was frozen when the user confirmed this run.
        com.dudu.run.c.n().D(m);
        com.dudu.run.c.n().F(com.dudu.run.b.c().e());
        this.f1610e.a(m, latitude, longitude);
    }

    public void A(float f2) {
        if (dashboard != null) dashboard.renderStats(true);
    }

    public void B() {
        if (dashboard != null) dashboard.renderStats(false);
    }

    @Override // com.dudu.run.c.b
    public void H(RandomPoint randomPoint) {
    }

    @Override // com.dudu.run.c.b
    public void L(Location location, int i) {
        if(location==null||initializationStage!=2)return;
        pendingRunOrigin=new Location(location);
        com.dudu.run.c.n().K();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        startInitializationStage(3,"正在查询所在校区…");
        this.f1610e.b(location.getLatitude(), location.getLongitude());
    }

    @OnClick({R.id.btn_run})
    public void clickViewRun(View view) {
        chooseRunMode(true);
    }

    private void chooseRunMode(boolean automatic) {
        if (getActivity() == null || initializationStage != 0) return;
        if (com.dudu.run.c.n().q() != 0) {
            if (automatic != com.dudu.run.c.n().isSimulationSource()) {
                new com.dudu.run.ui.NativeMaterial(getActivity()).dialog().setTitle("已有跑步进行中")
                        .setMessage("请先结束本次跑步，再切换跑步方式。")
                        .setNegativeButton("取消", null).setPositiveButton("返回本次跑步", (d,w) ->
                            startActivity(new Intent(getActivity(), SportMapActivity.class))).show();
            } else startActivity(new Intent(getActivity(), SportMapActivity.class));
            return;
        }
        if (!automatic && com.dudu.run.simulation.OfflineTestSession.active()) {
            com.dudu.run.utils.r.a(getActivity(), "请先退出测试模式，再开始真实跑步");
            return;
        }
        beginRunInitialization(automatic);
    }

    private void beginRunInitialization(boolean automatic) {
        if (getActivity() == null || initializationStage != 0) return;
        if (com.dudu.run.c.n().q()!=0) {
            com.dudu.run.utils.r.a(getActivity(), "已有跑步进行中，请返回本次跑步");return;
        }
        automaticRun=automatic;
        m=SportDashboard.preferredCampus(getActivity(), 2);
        pendingRunOrigin=null;
        // Both preparations need a fresh device origin. Only the automatic
        // branch enables simulation after eligibility and checkpoints succeed.
        com.dudu.run.simulation.SimulationController.get(getActivity()).stop();
        com.dudu.run.c.n().useDeviceSourceForNewRun();
        if (!com.dudu.run.utils.i.a(getActivity())) {
            com.dudu.run.utils.i.b(getActivity());
            com.dudu.run.utils.r.a(getActivity(), "请先开启位置定位");
        } else if (l.b(getActivity())) {
            com.dudu.run.permission.b.a(getActivity(), this.f1611f, new a());
        }
    }

    @Override // com.dudu.run.e.r
    public void d(String str) {
        if(initializationStage!=3)return;
        finishInitialization();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        ((BaseActivity) getActivity()).s0();
        com.dudu.run.utils.r.a(getActivity(), str);
    }

    @Override // com.dudu.run.e.r
    public void f() {startInitializationStage(4,"正在获取本次打卡点…");}

    @Override // com.dudu.run.e.r
    public void g(int i, double d2, double d3) {
        if(initializationStage!=3)return;
        finishInitialization();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        y(d2, d3);
    }

    @Override // com.dudu.run.c.b
    public void h(int i, float f2) {
    }

    @Override // com.dudu.run.e.r
    public void i(String str) {
        if(initializationStage!=4)return;
        finishInitialization();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        ((BaseActivity) getActivity()).s0();
        com.dudu.run.utils.r.a(getActivity(), str);
    }

    @Override // com.dudu.run.e.r
    public void j() {
    }

    @Override // com.dudu.run.e.r
    public void k() {
        if(initializationStage!=1)return;
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        com.dudu.run.simulation.SimulationController.get(getActivity()).releaseEndedRun();
        startInitializationStage(2,"正在获取设备位置…");
        com.dudu.run.c.n().H(this);
        com.dudu.run.c.n().I(false);
    }

    @Override // com.dudu.run.e.r
    public void l(String str) {
        if(initializationStage!=1)return;
        finishInitialization();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        ((BaseActivity) getActivity()).s0();
        com.dudu.run.utils.r.a(getActivity(), str);
    }

    @Override // com.dudu.run.e.r
    public void m(List<RandomPoint> list) {
        if(initializationStage!=4)return;
        finishInitialization();
        if (getActivity() == null || isDetached() || getActivity().isFinishing()) {
            return;
        }
        ((BaseActivity) getActivity()).s0();
        try{
            if(com.dudu.run.c.n().q()!=0)throw new IllegalStateException("已有跑步进行中，请返回本次跑步");
            if(pendingRunOrigin==null)throw new IllegalStateException("未取得跑步起点，请重新定位后开始");
            if(automaticRun){
                double height=pendingRunOrigin.hasAltitude()?pendingRunOrigin.getAltitude():30;
                if(Double.isNaN(height)||Double.isInfinite(height)||height< -500||height>9000)height=30;
                com.dudu.run.simulation.SimulationController.get(getActivity()).prepareForRun(
                    new com.dudu.run.simulation.SimulationEngine.Point(pendingRunOrigin.getLatitude(),pendingRunOrigin.getLongitude(),height));
            }else{
                com.dudu.run.simulation.SimulationController.get(getActivity()).stop();
                com.dudu.run.c.n().useDeviceSourceForNewRun();
            }
        }catch(Exception error){com.dudu.run.utils.r.a(getActivity(),error.getMessage());return;}
        com.dudu.run.c.n().G(list);
        Intent intent = new Intent(getActivity(), (Class<?>) SportMapActivity.class);
        intent.putExtra("isNeedCountDownView", true);
        startActivity(intent);
    }

    @Override // com.dudu.run.e.r
    public void n() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        startInitializationStage(1,"正在检查跑步资格…");
    }

    @Override // com.dudu.run.fragment.a
    int o() {
        return R.layout.fragment_sport;
    }

    @Override void prepare(View root) {
        dashboard = new SportDashboard((HomeActivity) getActivity(), (ScrollView) root);
        root.findViewById(R.id.btn_run_real).setOnClickListener(v -> chooseRunMode(false));
    }

    public void returnToTop() {
        if (dashboard != null) dashboard.toTop();
    }

    public void resetToTop() {
        if (dashboard != null) dashboard.resetToTop();
    }

    public boolean isScrolled() {
        return dashboard != null && dashboard.scrolled();
    }

    @Override public void onResume() {
        super.onResume();
        if (dashboard != null) {
            dashboard.updateLocalLabels();
            dashboard.loadRecent();
            if (!isHidden()) dashboard.resumeLocation();
        }
    }

    @Override public void onPause() {
        if (dashboard != null) dashboard.pauseLocation();
        super.onPause();
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (dashboard == null) return;
        if (hidden) dashboard.pauseLocation();
        else if (isResumed()) {
            dashboard.resumeLocation();dashboard.updateLocalLabels();dashboard.loadRecent();
        }
    }

    @Override public void onDestroyView() {
        if (dashboard != null) dashboard.closeLocation();
        dashboard = null;
        super.onDestroyView();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        x();
    }

    @Override // com.dudu.legacy.androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.f1610e = new com.dudu.run.e.s.i(this);
        this.f1609d = new d(getActivity().getMainLooper());
    }

    @Override public void onDestroy(){
        if(initializationStage==2){com.dudu.run.c.n().K();com.dudu.run.c.n().H(null);}
        if(f1609d!=null)f1609d.removeCallbacksAndMessages(null);
        initializationStage=0;super.onDestroy();
    }
    @Override // com.dudu.run.c.b
    public void w(LatLngInfo latLngInfo, LatLngInfo latLngInfo2) {
    }

    public void z(int i) {
        if (dashboard != null) dashboard.renderStats(false);
    }
}
