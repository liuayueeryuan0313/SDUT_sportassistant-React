package com.dudu.run.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.dudu.legacy.androidx.fragment.app.Fragment;
import com.dudu.legacy.androidx.fragment.app.j;
import butterknife.ButterKnife;
import com.dudu.run.R;
import com.dudu.run.bean.CheckAppUpdateResponseData;
import com.dudu.run.permission.PermissionActivity;
import com.dudu.run.utils.g;
import com.dudu.run.utils.r;
import java.io.File;
public abstract class BaseActivity extends PermissionActivity {
    private int A = 0;
    private android.app.Dialog z;
    private android.widget.TextView materialProgressMessage;

    class a implements DialogInterface.OnClickListener {
        a() {
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            BaseActivity.this.finish();
        }
    }

    class b implements DialogInterface.OnClickListener {
        final /* synthetic */ CheckAppUpdateResponseData f1566d;

        b(CheckAppUpdateResponseData checkAppUpdateResponseData) {
            this.f1566d = checkAppUpdateResponseData;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            BaseActivity.this.t0(this.f1566d.a(), this.f1566d.b());
        }
    }

    class c implements g.e {

        class a implements Runnable {
            a() {
            }

            @Override // java.lang.Runnable
            public void run() {
                r.a(BaseActivity.this.getApplication(), "下载出错");
            }
        }

        c() {
        }

        @Override // com.dudu.run.utils.g.e
        public void a(int i) {
            if (BaseActivity.this.A == i) {
                return;
            }
            BaseActivity.this.A = i;
            BaseActivity.this.C0("下载进度: " + i + "%");
        }

        @Override // com.dudu.run.utils.g.e
        public void b(Exception exc) {
            if (BaseActivity.this.isFinishing()) {
                return;
            }
            BaseActivity.this.s0();
            BaseActivity.this.runOnUiThread(new a());
        }

        @Override // com.dudu.run.utils.g.e
        public void c(File file) {
            if (BaseActivity.this.isFinishing()) {
                return;
            }
            BaseActivity.this.s0();
            com.dudu.run.utils.b.c(BaseActivity.this.getApplication(), file.getPath());
        }
    }

    private void y0() {
        if (isTaskRoot()) {
            return;
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent.hasCategory("android.intent.category.LAUNCHER") && "android.intent.action.MAIN".equals(action)) {
            finish();
        }
    }

    public void A0(CheckAppUpdateResponseData checkAppUpdateResponseData) {
        // App update prompts are disabled for this maintained build.
    }

    public Fragment B0(int i, Fragment fragment, Fragment fragment2, String str) {
        j jVarA = U().a();
        if (fragment2.isAdded()) {
            jVarA.j(fragment);
            jVarA.l(fragment2);
        } else {
            if (fragment != null) {
                jVarA.j(fragment);
            }
            jVarA.b(i, fragment2, str);
        }
        jVarA.f();
        return fragment2;
    }

    public void C0(String str) {
        android.app.Dialog progressDialog = this.z;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        if (this.z instanceof ProgressDialog) ((ProgressDialog) this.z).setMessage(str);
        else if (materialProgressMessage != null) materialProgressMessage.setText(str);
    }

    public void clickViewBack(View view) {
        finish();
    }

    @Override public void finish() {
        if (!com.dudu.run.ui.PageMotion.finish(this, () -> super.finish())) super.finish();
    }

    @Override // com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        y0();
        setContentView(u0());
        if (x0()) {
            w0();
        }
        ButterKnife.bind(this);
        if (this instanceof SportMapActivity || this instanceof SplashActivity) {
            com.dudu.run.ui.PredictiveBack.confirmBeforeLeaving(this, this::onBackPressed);
        }
    }

    @Override // com.dudu.run.permission.PermissionActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    public void s0() {
        android.app.Dialog progressDialog = this.z;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        this.z.cancel();
    }

    public void t0(String str, int i) {
        if (TextUtils.isEmpty(str)) {
            r.a(this, "下载地址异常");
            return;
        }
        File file = new File(getExternalFilesDir(null), "DownloadApk");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.A = 0;
        String str2 = "common_update_" + i + ".apk";
        File file2 = new File(file, str2);
        if (file2.exists()) {
            com.dudu.run.utils.b.c(getApplication(), file2.getPath());
        } else {
            z0("下载中，请稍候...");
            g.b().a(str, file.getAbsolutePath(), str2, new c());
        }
    }

    abstract int u0();

    boolean v0() {
        return false;
    }

    protected void w0() {
        new com.dudu.run.ui.RunnerUi(this).applySystemBars(this);
    }

    protected boolean x0() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public void z0(String str) {
        if (isFinishing()) {
            return;
        }
        android.app.Dialog progressDialog = this.z;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.z.cancel();
        }
        if (!(this instanceof SportMapActivity)) {
            com.dudu.run.ui.NativeMaterial material = new com.dudu.run.ui.NativeMaterial(this);
            com.dudu.run.ui.RunnerUi ui = new com.dudu.run.ui.RunnerUi(this);
            android.widget.LinearLayout row = new android.widget.LinearLayout(material.context());
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(ui.dp(24), ui.dp(20), ui.dp(24), ui.dp(20));
            com.google.android.material.progressindicator.CircularProgressIndicator indicator = new com.google.android.material.progressindicator.CircularProgressIndicator(material.context());
            indicator.setIndicatorSize(ui.dp(32)); indicator.setTrackThickness(ui.dp(3)); indicator.setIndeterminate(true);
            row.addView(indicator, new android.widget.LinearLayout.LayoutParams(ui.dp(40), ui.dp(40)));
            materialProgressMessage = ui.text(str == null ? "" : str, 16, ui.ink, false);
            android.widget.LinearLayout.LayoutParams label = new android.widget.LinearLayout.LayoutParams(-2, -2);
            label.setMarginStart(ui.dp(20)); row.addView(materialProgressMessage, label);
            this.z = material.dialog().setView(row).create();
            this.z.setCancelable(false); this.z.setCanceledOnTouchOutside(false); this.z.show();
            return;
        }
        ProgressDialog progressDialog2 = new ProgressDialog(this);
        this.z = progressDialog2;
        if (str != null) {
            progressDialog2.setMessage(str);
        }
        this.z.setCancelable(false);
        this.z.setCanceledOnTouchOutside(false);
        this.z.show();
    }
}
