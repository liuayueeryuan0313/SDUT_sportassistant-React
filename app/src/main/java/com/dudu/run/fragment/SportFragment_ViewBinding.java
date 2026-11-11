package com.dudu.run.fragment;

import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.dudu.run.R;
public class SportFragment_ViewBinding implements Unbinder {
    private SportFragment a;
    private View b;

    class a extends DebouncingOnClickListener {
        final /* synthetic */ SportFragment f1616d;

        a(SportFragment_ViewBinding sportFragment_ViewBinding, SportFragment sportFragment) {
            this.f1616d = sportFragment;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1616d.clickViewRun(view);
        }
    }

    public SportFragment_ViewBinding(SportFragment sportFragment, View view) {
        this.a = sportFragment;
        sportFragment.tv_my_count = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_my_count, "field 'tv_my_count'", TextView.class);
        sportFragment.tv_run_length = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_run_length, "field 'tv_run_length'", TextView.class);
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.btn_run, "method 'clickViewRun'");
        this.b = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new a(this, sportFragment));
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        SportFragment sportFragment = this.a;
        if (sportFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        sportFragment.tv_my_count = null;
        sportFragment.tv_run_length = null;
        this.b.setOnClickListener(null);
        this.b = null;
    }
}
