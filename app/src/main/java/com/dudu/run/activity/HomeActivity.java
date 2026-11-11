package com.dudu.run.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.Gravity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.transition.Visibility;
import com.dudu.legacy.androidx.fragment.app.Fragment;
import com.dudu.legacy.androidx.fragment.app.j;
import butterknife.BindView;
import butterknife.OnClick;
import com.dudu.run.R;
import com.dudu.run.bean.ConfigResponseData;
import com.dudu.run.bean.CurrentSportDataInfo;
import com.dudu.run.e.c;
import com.dudu.run.e.d;
import com.dudu.run.fragment.MineFragment;
import com.dudu.run.fragment.RuleFragment;
import com.dudu.run.fragment.SportFragment;
import com.dudu.run.fragment.RecordListFragment;
import com.dudu.run.utils.p;
import com.dudu.run.utils.r;
import com.dudu.run.ui.RunnerUi;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.util.DateUtils;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;
public class HomeActivity extends BaseActivity implements d {
    public static final String OPEN_TAB = "runner_open_tab";
    private RunnerUi runnerUi;
    private String dashboardLocationName;
    private Fragment B;
    private Fragment C;
    private Fragment D;
    private Fragment E;
    private Fragment records;
    private int F = 1;
    private com.dudu.run.e.c G;

    @BindView(R.id.layout_tabs)
    LinearLayout layout_tabs;

    class a implements DialogInterface.OnClickListener {
        a() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            p.d(HomeActivity.this);
        }
    }

    class b implements DialogInterface.OnClickListener {
        final /* synthetic */ CurrentSportDataInfo f1571d;

        b(CurrentSportDataInfo currentSportDataInfo) {
            this.f1571d = currentSportDataInfo;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            com.dudu.run.c.n().E(this.f1571d);
            Intent intent = new Intent(HomeActivity.this, (Class<?>) SportMapActivity.class);
            intent.putExtra("isNeedCountDownView", true);
            HomeActivity.this.startActivity(intent);
        }
    }

    private Fragment E0(int i) {
        if (i == 3) return records;
        if (i == 0) {
            return this.B;
        }
        if (i == 1) {
            return this.C;
        }
        if (i != 2) {
            return null;
        }
        return this.D;
    }

    private Fragment F0(String str) {
        Fragment fragmentD = U().d(str);
        if (fragmentD != null) {
            return fragmentD;
        }
        return null;
    }

    private void G0(j jVar) {
        if (records != null) jVar.j(records);
        Fragment fragment = this.B;
        if (fragment != null) {
            jVar.j(fragment);
        }
        Fragment fragment2 = this.C;
        if (fragment2 != null) {
            jVar.j(fragment2);
        }
        Fragment fragment3 = this.D;
        if (fragment3 != null) {
            jVar.j(fragment3);
        }
        jVar.f();
    }

    private void H0(int i) {
        runnerUi.styleDock(layout_tabs, i == 1 ? R.id.layout_tab_sport : i == 2 ? R.id.layout_tab_mine
                : i == 3 ? R.id.layout_tab_rule : View.NO_ID);
        updateSportTab(false);
        updateRecordTab(records instanceof RecordListFragment && ((RecordListFragment) records).isScrolled());
    }

    public void updateSportTab(boolean scrolled) {
        com.google.android.material.button.MaterialButton sport = findViewById(R.id.layout_tab_sport);
        boolean top = F == 1 && scrolled;
        sport.setIconResource(top ? R.drawable.ic_demo_circle_arrow_up : R.drawable.ic_runner_run);
        sport.setContentDescription(top ? "运动，回到顶部" : "运动");
    }

    public void updateRecordTab(boolean scrolled) {
        com.google.android.material.button.MaterialButton record = findViewById(R.id.layout_tab_rule);
        boolean top = F == 3 && scrolled;
        record.setIconResource(top ? R.drawable.ic_demo_circle_arrow_up : R.drawable.ic_demo_clipboard_list);
        record.setContentDescription(top ? "记录，回到顶部" : "记录");
    }

    @Override // com.dudu.run.e.d
    public void B(String str) {
    }

    @Override // com.dudu.run.e.d
    public void C(String str) {
        if (isFinishing()) {
            return;
        }
        r.a(getApplication(), str);
    }

    @Override // com.dudu.run.e.d
    public void D(String str) {
        if (isFinishing()) {
            return;
        }
        r.a(getApplication(), str);
    }

    public void D0() {
        if (isFinishing() || com.dudu.run.c.n().y()) {
            return;
        }
        CurrentSportDataInfo currentSportDataInfoC = p.c(this);
        if (currentSportDataInfoC == null) {
            com.dudu.run.utils.j.b("mine", "没有缓存数据");
            return;
        }
        if(com.dudu.run.ui.RunResultSession.hasPending(this,currentSportDataInfoC.f()))return;
        if (System.currentTimeMillis() - currentSportDataInfoC.f() > DateUtils.ONE_HOUR) {
            com.dudu.run.utils.j.b("mine", "开始跑步时间到现在超过1小时，扔掉");
            p.d(this);
            return;
        }
        androidx.appcompat.app.AlertDialog dialog = new com.dudu.run.ui.NativeMaterial(this).dialog()
                .setTitle("恢复跑步")
                .setMessage("检测到上次跑步异常中断，是否恢复？")
                .setPositiveButton("恢复跑步", new b(currentSportDataInfoC))
                .setNegativeButton("取消", new a()).create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override // com.dudu.run.e.d
    public void E(ConfigResponseData configResponseData) {
        if (isFinishing()) {
            return;
        }
        com.dudu.run.b.c().s(configResponseData.i(), configResponseData.j(), configResponseData.c(), configResponseData.b(), configResponseData.d(), configResponseData.a(), configResponseData.g(), configResponseData.f(), configResponseData.h(), configResponseData.e());
        Fragment fragment = this.C;
        if (fragment != null) {
            ((SportFragment) fragment).B();
        }
        D0();
    }

    @Override // com.dudu.run.e.d
    public void G() {
    }

    private int tabGeneration;
    public void I0(int i) {
        if (i == 0) {
            com.dudu.run.ui.NativeNavigation.launch(this, new Intent(this, RuleActivity.class));
            return;
        }
        if (i != 1 && i != 2 && i != 3) return;
        ViewGroup content = findViewById(R.id.container);
        androidx.transition.TransitionManager.endTransitions(content);
        int previous = F;
        View oldView = E == null ? null : E.getView();
        F = i; H0(i);
        if (i == 1 && C == null) C = new SportFragment();
        if (i == 2 && D == null) D = new MineFragment();
        if (i == 3 && records == null) records = new RecordListFragment();
        Fragment target = i == 1 ? C : i == 2 ? D : records;
        j transaction = U().a();
        for (Fragment page : new Fragment[]{B, C, D, records}) {
            if (page != null && page != target) transaction.j(page);
        }
        // A tag is assigned when add() is queued, before isAdded() becomes true.
        if (!target.isAdded() && target.getTag() == null) transaction.b(R.id.container, target,
                i == 1 ? "tag_home_sport" : i == 2 ? "tag_home_mine" : "tag_home_records");
        transaction.l(target);
        transaction.f();
        E = target;
        int generation = ++tabGeneration;
        // Fragment transactions are asynchronous. Capture the two real view
        // states after the commit has executed, rather than before it exists.
        content.post(() -> {
            if (generation != tabGeneration || isFinishing() || target.getView() == null) return;
            if (i == 1) ((SportFragment)target).resetToTop();
            if (i == 3) ((RecordListFragment)target).resetToTop();
            com.dudu.run.ui.PageMotion.tabs(content, oldView, target.getView(), dockPosition(i) > dockPosition(previous));
        });
    }

    private static int dockPosition(int tab) {
        return tab == 1 ? 0 : tab == 3 ? 1 : 2;
    }

    @OnClick({R.id.layout_tab_mine})
    public void clickViewTabMine(View view) {
        I0(2);
    }

    @OnClick({R.id.layout_tab_rule})
    public void clickViewTabRule(View view) {
        if (F == 3 && records instanceof RecordListFragment) ((RecordListFragment) records).returnToTop();
        else I0(3);
    }

    @OnClick({R.id.layout_tab_sport})
    public void clickViewTabSport(View view) {
        if (F == 1 && C instanceof SportFragment) ((SportFragment) C).returnToTop();
        else I0(1);
    }

    @Override // com.dudu.run.e.d
    public void j(int i) {
        if (isFinishing()) {
            return;
        }
        com.dudu.run.b.c().t(i);
        Fragment fragment = this.C;
        if (fragment != null) {
            ((SportFragment) fragment).z(i);
        }
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        runnerUi = new RunnerUi(this);
        dashboardLocationName = RunnerUi.preferences(this).getString("location_name", "");
        runnerUi.applySystemBars(this);
        findViewById(R.id.runner_home_root).setBackgroundColor(runnerUi.page);
        com.dudu.run.utils.PrivacyConsent.restoreIfAgreed(this);
        this.G = new com.dudu.run.e.s.b(this);
        if (bundle != null) {
            this.F = bundle.getInt("currentIndex", 1);
            if (this.F == 0) this.F = 2;
            this.B = F0("tag_home_rule");
            this.C = F0("tag_home_sport");
            this.D = F0("tag_home_mine");
            this.records = F0("tag_home_records");
            this.E = E0(this.F);
        }
        I0(getIntent().getIntExtra(OPEN_TAB, this.F));
        getIntent().removeExtra(OPEN_TAB);
        this.G.b();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(OPEN_TAB)) I0(intent.getIntExtra(OPEN_TAB, 1));
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("currentIndex", F);
        super.onSaveInstanceState(state);
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.run.permission.PermissionActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mine", "HomeActivity onDestroy()");
    }

    @Override // com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        if (!runnerUi.hasSamePalette(new RunnerUi(this))
                || !dashboardLocationName.equals(RunnerUi.preferences(this).getString("location_name", ""))) {
            recreate();
            return;
        }
        this.G.a();
        this.G.c();
    }

    @Override // com.dudu.run.e.d
    public void p(float f2) {
        if (isFinishing()) {
            return;
        }
        com.dudu.run.b.c().u(f2);
        Fragment fragment = this.C;
        if (fragment != null) {
            ((SportFragment) fragment).A(f2);
        }
    }

    @Override // com.dudu.run.activity.BaseActivity
    int u0() {
        return R.layout.activity_home;
    }

    @Override // com.dudu.run.e.d
    public void y() {
    }

    @Override // com.dudu.run.e.d
    public void z() {
    }
}
