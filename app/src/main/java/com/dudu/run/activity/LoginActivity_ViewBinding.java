package com.dudu.run.activity;

import android.view.View;
import android.widget.EditText;
import com.google.android.material.button.MaterialButton;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.dudu.run.R;
public class LoginActivity_ViewBinding implements Unbinder {
    private LoginActivity a;
    private View b;
    private View f1579c;
    private View f1580d;

    class a extends DebouncingOnClickListener {
        final /* synthetic */ LoginActivity f1581d;

        a(LoginActivity_ViewBinding loginActivity_ViewBinding, LoginActivity loginActivity) {
            this.f1581d = loginActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1581d.onViewClick(view);
        }
    }

    class b extends DebouncingOnClickListener {
        final /* synthetic */ LoginActivity f1582d;

        b(LoginActivity_ViewBinding loginActivity_ViewBinding, LoginActivity loginActivity) {
            this.f1582d = loginActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1582d.onViewClick(view);
        }
    }

    class c extends DebouncingOnClickListener {
        final /* synthetic */ LoginActivity f1583d;

        c(LoginActivity_ViewBinding loginActivity_ViewBinding, LoginActivity loginActivity) {
            this.f1583d = loginActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1583d.onViewLogin(view);
        }
    }

    public LoginActivity_ViewBinding(LoginActivity loginActivity, View view) {
        this.a = loginActivity;
        loginActivity.et_username = (EditText) Utils.findRequiredViewAsType(view, R.id.et_username, "field 'et_username'", EditText.class);
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.ib_username_clear, "field 'ib_username_clear' and method 'onViewClick'");
        loginActivity.ib_username_clear = (MaterialButton) Utils.castView(viewFindRequiredView, R.id.ib_username_clear, "field 'ib_username_clear'", MaterialButton.class);
        this.b = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new a(this, loginActivity));
        loginActivity.et_psd = (EditText) Utils.findRequiredViewAsType(view, R.id.et_psd, "field 'et_psd'", EditText.class);
        View viewFindRequiredView2 = Utils.findRequiredView(view, R.id.ib_psd_visible, "field 'ib_psd_visible' and method 'onViewClick'");
        loginActivity.ib_psd_visible = (MaterialButton) Utils.castView(viewFindRequiredView2, R.id.ib_psd_visible, "field 'ib_psd_visible'", MaterialButton.class);
        this.f1579c = viewFindRequiredView2;
        viewFindRequiredView2.setOnClickListener(new b(this, loginActivity));
        View viewFindRequiredView3 = Utils.findRequiredView(view, R.id.btn_login, "method 'onViewLogin'");
        this.f1580d = viewFindRequiredView3;
        viewFindRequiredView3.setOnClickListener(new c(this, loginActivity));
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        LoginActivity loginActivity = this.a;
        if (loginActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        loginActivity.et_username = null;
        loginActivity.ib_username_clear = null;
        loginActivity.et_psd = null;
        loginActivity.ib_psd_visible = null;
        this.b.setOnClickListener(null);
        this.b = null;
        this.f1579c.setOnClickListener(null);
        this.f1579c = null;
        this.f1580d.setOnClickListener(null);
        this.f1580d = null;
    }
}
