package com.dudu.run.fragment;

import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.dudu.run.R;
public class RuleFragment_ViewBinding implements Unbinder {
    private RuleFragment a;
    private View b;

    class a extends DebouncingOnClickListener {
        final /* synthetic */ RuleFragment f1608d;

        a(RuleFragment_ViewBinding ruleFragment_ViewBinding, RuleFragment ruleFragment) {
            this.f1608d = ruleFragment;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1608d.clickViewReload();
        }
    }

    public RuleFragment_ViewBinding(RuleFragment ruleFragment, View view) {
        this.a = ruleFragment;
        ruleFragment.webview = (WebView) Utils.findRequiredViewAsType(view, R.id.webview, "field 'webview'", WebView.class);
        ruleFragment.layout_error = Utils.findRequiredView(view, R.id.layout_error, "field 'layout_error'");
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.tv_error, "field 'tv_error' and method 'clickViewReload'");
        ruleFragment.tv_error = (TextView) Utils.castView(viewFindRequiredView, R.id.tv_error, "field 'tv_error'", TextView.class);
        this.b = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new a(this, ruleFragment));
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        RuleFragment ruleFragment = this.a;
        if (ruleFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        ruleFragment.webview = null;
        ruleFragment.layout_error = null;
        ruleFragment.tv_error = null;
        this.b.setOnClickListener(null);
        this.b = null;
    }
}
