package com.dudu.run.activity;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.utils.PrivacyConsent;
import android.os.Bundle;
import com.dudu.run.R;
public class SplashActivity extends BaseActivity {

    private void beginStartup() {
        G0(); // No artificial splash delay after the required startup notices.
    }

    private void confirmPrivacy() {
        if (PrivacyConsent.isAgreed(this)) {
            PrivacyConsent.apply(this);
            beginStartup();
            return;
        }
        NativeMaterial material = new NativeMaterial(this);
        AlertDialog dialog = material.dialog()
                .setTitle("隐私提示")
                .setMessage("运动地图和轨迹功能需要使用腾讯地图与定位服务，处理设备定位信息；应用还使用友盟进行统计和崩溃日志采集。同意后将初始化这些服务。")
                .setPositiveButton("同意并继续", (d, which) -> {
                    PrivacyConsent.agree(this);
                    beginStartup();
                })
                .setNegativeButton("退出", (d, which) -> finish())
                .create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        material.showStartupNotice(dialog);
    }
    public void G0() {
        if (com.dudu.run.simulation.OfflineTestSession.active()) {
            startActivity(new Intent(this, com.dudu.run.simulation.OfflineTestActivity.class));
        } else if (com.dudu.run.b.c().q()) {
            startActivity(new Intent(this, (Class<?>) HomeActivity.class));
        } else {
            startActivity(new Intent(this, (Class<?>) LoginActivity.class));
        }
        finish();
    }

    @Override // com.dudu.legacy.androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        com.dudu.run.utils.StartupDisclaimer.beforeStartup(this, this::confirmPrivacy);
    }

    @Override // com.dudu.run.activity.BaseActivity
    int u0() {
        return R.layout.activity_splash;
    }

    @Override // com.dudu.run.activity.BaseActivity
    boolean v0() {
        return true;
    }
}
