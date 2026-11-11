package com.dudu.run.activity;

import android.view.View;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.dudu.run.R;
public class HomeActivity_ViewBinding implements Unbinder {
    private HomeActivity a;
    private View b;
    private View f1573c;
    private View f1574d;

    class a extends DebouncingOnClickListener {
        final /* synthetic */ HomeActivity f1575d;

        a(HomeActivity_ViewBinding homeActivity_ViewBinding, HomeActivity homeActivity) {
            this.f1575d = homeActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1575d.clickViewTabRule(view);
        }
    }

    class b extends DebouncingOnClickListener {
        final /* synthetic */ HomeActivity f1576d;

        b(HomeActivity_ViewBinding homeActivity_ViewBinding, HomeActivity homeActivity) {
            this.f1576d = homeActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1576d.clickViewTabSport(view);
        }
    }

    class c extends DebouncingOnClickListener {
        final /* synthetic */ HomeActivity f1577d;

        c(HomeActivity_ViewBinding homeActivity_ViewBinding, HomeActivity homeActivity) {
            this.f1577d = homeActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1577d.clickViewTabMine(view);
        }
    }

    public HomeActivity_ViewBinding(HomeActivity homeActivity, View view) {
        this.a = homeActivity;
        homeActivity.layout_tabs = (LinearLayout) Utils.findRequiredViewAsType(view, R.id.layout_tabs, "field 'layout_tabs'", LinearLayout.class);
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.layout_tab_rule, "method 'clickViewTabRule'");
        this.b = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new a(this, homeActivity));
        View viewFindRequiredView2 = Utils.findRequiredView(view, R.id.layout_tab_sport, "method 'clickViewTabSport'");
        this.f1573c = viewFindRequiredView2;
        viewFindRequiredView2.setOnClickListener(new b(this, homeActivity));
        View viewFindRequiredView3 = Utils.findRequiredView(view, R.id.layout_tab_mine, "method 'clickViewTabMine'");
        this.f1574d = viewFindRequiredView3;
        viewFindRequiredView3.setOnClickListener(new c(this, homeActivity));
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        HomeActivity homeActivity = this.a;
        if (homeActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        homeActivity.layout_tabs = null;
        this.b.setOnClickListener(null);
        this.b = null;
        this.f1573c.setOnClickListener(null);
        this.f1573c = null;
        this.f1574d.setOnClickListener(null);
        this.f1574d = null;
    }
}
