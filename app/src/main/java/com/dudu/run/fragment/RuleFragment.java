package com.dudu.run.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.dudu.run.R;
public class RuleFragment extends com.dudu.run.fragment.a {
    private boolean f1606d = false;
    private boolean f1607e = false;

    @BindView(R.id.layout_error)
    View layout_error;

    @BindView(R.id.tv_error)
    TextView tv_error;

    @BindView(R.id.webview)
    WebView webview;

    class a implements View.OnLongClickListener {
        a(RuleFragment ruleFragment) {
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            return true;
        }
    }

    class b extends WebViewClient {
        b() {
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            RuleFragment.this.f1606d = false;
            RuleFragment ruleFragment = RuleFragment.this;
            ruleFragment.layout_error.setVisibility(ruleFragment.f1607e ? 0 : 8);
            RuleFragment ruleFragment2 = RuleFragment.this;
            ruleFragment2.tv_error.setText(ruleFragment2.f1607e ? "加载失败，点击重试" : "加载中...");
        }

        @Override // android.webkit.WebViewClient
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            super.onPageStarted(webView, str, bitmap);
            RuleFragment.this.f1607e = false;
            RuleFragment.this.f1606d = true;
            RuleFragment.this.layout_error.setVisibility(0);
            RuleFragment.this.tv_error.setText("加载中...");
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            super.onReceivedError(webView, webResourceRequest, webResourceError);
            RuleFragment.this.f1607e = true;
        }
    }

    private void s() {
        this.webview.getSettings().setJavaScriptEnabled(false);
        this.webview.getSettings().setSupportZoom(false);
        this.webview.getSettings().setDisplayZoomControls(false);
        this.webview.getSettings().setCacheMode(2);
        this.webview.setWebChromeClient(new WebChromeClient());
        this.webview.setOnLongClickListener(new a(this));
        this.webview.setWebViewClient(new b());
        this.webview.loadUrl("http://sports2.sdut.edu.cn/rule.html");
    }

    @OnClick({R.id.tv_error})
    public void clickViewReload() {
        if (this.f1606d) {
            return;
        }
        this.webview.reload();
    }

    @Override // com.dudu.run.fragment.a
    int o() {
        return R.layout.fragment_rule;
    }

    @Override // com.dudu.legacy.androidx.fragment.app.Fragment
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        com.dudu.run.ui.RunnerUi ui=new com.dudu.run.ui.RunnerUi(getActivity());
        getView().setBackgroundColor(ui.page);
        android.widget.LinearLayout header=(android.widget.LinearLayout)((android.view.ViewGroup)getView()).getChildAt(0);
        header.removeAllViews(); header.setBackgroundColor(ui.page);
        com.google.android.material.appbar.MaterialToolbar toolbar=new com.google.android.material.appbar.MaterialToolbar(new com.dudu.run.ui.NativeMaterial(getActivity()).context());
        toolbar.setTitle("跑步规则"); toolbar.setTitleTextColor(ui.ink);
        toolbar.setNavigationIcon(R.drawable.ic_demo_arrow_left); toolbar.setNavigationIconTint(ui.accentText);
        toolbar.setNavigationContentDescription("返回"); toolbar.setNavigationOnClickListener(v->getActivity().finish());
        header.addView(toolbar,new android.widget.LinearLayout.LayoutParams(-1,ui.dp(56)));
        layout_error.setBackgroundColor(ui.page); tv_error.setTextColor(ui.muted); webview.setBackgroundColor(ui.page);
        if(android.os.Build.VERSION.SDK_INT>=29) webview.getSettings().setForceDark(ui.dark ? android.webkit.WebSettings.FORCE_DARK_ON : android.webkit.WebSettings.FORCE_DARK_OFF);
        s();
    }
}
