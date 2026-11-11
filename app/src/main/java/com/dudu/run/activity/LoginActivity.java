package com.dudu.run.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.dudu.run.R;
import com.dudu.run.bean.LoginResponseData;
import com.dudu.run.e.e;
import com.dudu.run.e.f;
import com.dudu.run.e.s.c;
import com.dudu.run.utils.l;
import com.dudu.run.utils.r;
import com.dudu.run.simulation.OfflineTestActivity;
import com.dudu.run.ui.RunnerUi;
public class LoginActivity extends BaseActivity implements f {
    private boolean B = false;
    private e C;

    @BindView(R.id.et_psd)
    EditText et_psd;

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.ib_psd_visible)
    MaterialButton ib_psd_visible;

    @BindView(R.id.ib_username_clear)
    MaterialButton ib_username_clear;

    class a implements TextWatcher {
        a() {
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            LoginActivity.this.ib_username_clear.setVisibility(charSequence.length() > 0 ? 0 : 8);
        }
    }

    private void D0() {
        this.et_username.addTextChangedListener(new a());
    }

    @Override // com.dudu.run.e.f
    public void J(String str) {
        if (isFinishing()) {
            return;
        }
        r.a(this, str);
        s0();
    }

    @Override // com.dudu.run.e.f
    public void i() {
        z0("登录中");
    }

    @Override // com.dudu.run.activity.BaseActivity, com.dudu.legacy.androidx.appcompat.app.AppCompatActivity, com.dudu.legacy.androidx.fragment.app.FragmentActivity, com.dudu.legacy.androidx.activity.ComponentActivity, com.dudu.legacy.androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.C = new com.dudu.run.e.s.c(this);
        D0();
        styleLogin();
        findViewById(R.id.offline_test_entry).setOnClickListener(view -> OfflineTestActivity.open(this, false));
        et_psd.setOnEditorActionListener((view, action, event) -> {
            if (action != android.view.inputmethod.EditorInfo.IME_ACTION_DONE) return false;
            onViewLogin(view);
            return true;
        });
    }

    private void styleLogin() {
        RunnerUi ui = new RunnerUi(this);
        ui.applySystemBars(this);
        findViewById(R.id.runner_login_scroll).setBackgroundColor(ui.page);
        View banner = findViewById(R.id.runner_login_banner);
        banner.setBackground(ui.background(ui.tone, 25));
        banner.setClipToOutline(true);
        ImageView mark = findViewById(R.id.runner_login_mark);
        mark.setBackground(ui.background(ui.accent, 14));
        mark.setColorFilter(ui.onAccent);
        for (int id : new int[]{R.id.runner_login_brand, R.id.runner_login_heading,
                R.id.runner_login_account_label, R.id.runner_login_password_label}) {
            TextView label = findViewById(id);
            label.setTextColor(ui.ink);
            label.getPaint().setFakeBoldText(true);
        }
        ((TextView) findViewById(R.id.runner_login_category)).setTextColor(ui.muted);
        ((TextView) findViewById(R.id.runner_login_intro)).setTextColor(ui.muted);
        findViewById(R.id.runner_login_account).setBackground(ui.outlined(ui.surface, 16));
        findViewById(R.id.runner_login_password).setBackground(ui.outlined(ui.surface, 16));
        ((ImageView) findViewById(R.id.runner_login_id_icon)).setColorFilter(ui.accentText);
        ((ImageView) findViewById(R.id.runner_login_lock_icon)).setColorFilter(ui.accentText);
        for (EditText field : new EditText[]{et_username, et_psd}) {
            field.setTextColor(ui.ink);
            field.setHintTextColor(ui.muted);
        }
        ib_psd_visible.setIconTint(android.content.res.ColorStateList.valueOf(ui.accentText));
        ib_psd_visible.setIconResource(R.drawable.ic_demo_eye);
        ib_username_clear.setIconTint(android.content.res.ColorStateList.valueOf(ui.accentText));
        ib_username_clear.setIconResource(R.mipmap.icon_et_del);
        TextView login = findViewById(R.id.btn_login);
        login.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ui.accent));
        login.setTextColor(ui.onAccent);
        TextView demo = findViewById(R.id.offline_test_entry);
        demo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ui.page));
        demo.setTextColor(ui.accentText);
        android.graphics.drawable.Drawable arrow = getDrawable(R.drawable.ic_demo_arrow_right).mutate();
        arrow.setTint(ui.accentText);
        arrow.setBounds(0, 0, ui.dp(15), ui.dp(15));
        demo.setCompoundDrawablesRelative(null, null, arrow, null);
        demo.setCompoundDrawablePadding(ui.dp(6));
        ((TextView) findViewById(R.id.runner_login_demo_note)).setTextColor(ui.muted);
        TextView footer = findViewById(R.id.runner_login_footer);
        footer.setText(RunnerUi.version(this) + "\n请勿分发使用");
        footer.setTextColor(ui.muted);
    }

    @OnClick({R.id.ib_username_clear, R.id.ib_psd_visible})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ib_psd_visible /* 2131230919 */:
                if (!TextUtils.isEmpty(this.et_psd.getText())) {
                    if (this.B) {
                        this.et_psd.setInputType(129);
                        this.ib_psd_visible.setIconResource(R.drawable.ic_demo_eye);
                    } else {
                        this.et_psd.setInputType(144);
                        this.ib_psd_visible.setIconResource(R.drawable.ic_demo_eye_off);
                    }
                    this.B = !this.B;
                    this.ib_psd_visible.setContentDescription(this.B ? "隐藏密码" : "显示密码");
                    EditText editText = this.et_psd;
                    editText.setSelection(editText.getText().length());
                    break;
                }
                break;
            case R.id.ib_username_clear /* 2131230920 */:
                this.et_username.setText((CharSequence) null);
                break;
        }
    }

    @OnClick({R.id.btn_login})
    public void onViewLogin(View view) {
        if (l.b(this)) {
            String strTrim = this.et_username.getText().toString().trim();
            if (TextUtils.isEmpty(strTrim)) {
                r.a(this, "请输入学号");
                return;
            }
            String strTrim2 = this.et_psd.getText().toString().trim();
            if (TextUtils.isEmpty(strTrim2)) {
                r.a(this, "请输入密码");
            } else {
                this.C.a(strTrim, strTrim2);
            }
        }
    }

    @Override // com.dudu.run.e.f
    public void s(LoginResponseData loginResponseData) {
        if (isFinishing()) {
            return;
        }
        s0();
        com.dudu.run.b.c().v(loginResponseData.a(), 1, loginResponseData.d(), loginResponseData.b(), loginResponseData.e(), loginResponseData.f(), loginResponseData.c());
        startActivity(new Intent(this, (Class<?>) HomeActivity.class));
        r.a(this, "登录成功");
        finish();
    }

    @Override // com.dudu.run.activity.BaseActivity
    int u0() {
        return R.layout.activity_login;
    }
}
