package com.dudu.run.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.dudu.run.R;
import com.dudu.run.widget.LongClickProgressView;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;
public class SportMapActivity_ViewBinding implements Unbinder {
    private SportMapActivity a;
    private View b;
    private View f1587c;

    class a extends DebouncingOnClickListener {
        final /* synthetic */ SportMapActivity f1588d;

        a(SportMapActivity_ViewBinding sportMapActivity_ViewBinding, SportMapActivity sportMapActivity) {
            this.f1588d = sportMapActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1588d.clickViewRunGo(view);
        }
    }

    class b extends DebouncingOnClickListener {
        final /* synthetic */ SportMapActivity f1589d;

        b(SportMapActivity_ViewBinding sportMapActivity_ViewBinding, SportMapActivity sportMapActivity) {
            this.f1589d = sportMapActivity;
        }

        @Override // butterknife.internal.DebouncingOnClickListener
        public void doClick(View view) {
            this.f1589d.clickViewRunFinish(view);
        }
    }

    public SportMapActivity_ViewBinding(SportMapActivity sportMapActivity, View view) {
        this.a = sportMapActivity;
        sportMapActivity.layout_countdown = Utils.findRequiredView(view, R.id.layout_countdown, "field 'layout_countdown'");
        sportMapActivity.tv_countdown = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_countdown, "field 'tv_countdown'", TextView.class);
        sportMapActivity.mapView = (TextureMapView) Utils.findRequiredViewAsType(view, R.id.mapView, "field 'mapView'", TextureMapView.class);
        sportMapActivity.tv_total_distance = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_total_distance, "field 'tv_total_distance'", TextView.class);
        sportMapActivity.tv_location_accuracy = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_location_accuracy, "field 'tv_location_accuracy'", TextView.class);
        sportMapActivity.tv_sport_speed = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_speed, "field 'tv_sport_speed'", TextView.class);
        sportMapActivity.tv_sport_time = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_time, "field 'tv_sport_time'", TextView.class);
        sportMapActivity.tv_sport_cal = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_cal, "field 'tv_sport_cal'", TextView.class);
        sportMapActivity.iv_gps_status = (ImageView) Utils.findRequiredViewAsType(view, R.id.iv_gps_status, "field 'iv_gps_status'", ImageView.class);
        sportMapActivity.tv_std_distance = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_std_distance, "field 'tv_std_distance'", TextView.class);
        sportMapActivity.tv_std_speed = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_std_speed, "field 'tv_std_speed'", TextView.class);
        sportMapActivity.tv_unpassed_random_count = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_unpassed_random_count, "field 'tv_unpassed_random_count'", TextView.class);
        sportMapActivity.layout_status_pause = Utils.findRequiredView(view, R.id.layout_status_pause, "field 'layout_status_pause'");
        sportMapActivity.layout_status_go = Utils.findRequiredView(view, R.id.layout_status_go, "field 'layout_status_go'");
        sportMapActivity.btn_long_click_finish = (LongClickProgressView) Utils.findRequiredViewAsType(view, R.id.btn_long_click_finish, "field 'btn_long_click_finish'", LongClickProgressView.class);
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.btn_run_go, "method 'clickViewRunGo'");
        this.b = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new a(this, sportMapActivity));
        View viewFindRequiredView2 = Utils.findRequiredView(view, R.id.btn_run_finish, "method 'clickViewRunFinish'");
        this.f1587c = viewFindRequiredView2;
        viewFindRequiredView2.setOnClickListener(new b(this, sportMapActivity));
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        SportMapActivity sportMapActivity = this.a;
        if (sportMapActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        sportMapActivity.layout_countdown = null;
        sportMapActivity.tv_countdown = null;
        sportMapActivity.mapView = null;
        sportMapActivity.tv_total_distance = null;
        sportMapActivity.tv_location_accuracy = null;
        sportMapActivity.tv_sport_speed = null;
        sportMapActivity.tv_sport_time = null;
        sportMapActivity.tv_sport_cal = null;
        sportMapActivity.iv_gps_status = null;
        sportMapActivity.tv_std_distance = null;
        sportMapActivity.tv_std_speed = null;
        sportMapActivity.tv_unpassed_random_count = null;
        sportMapActivity.layout_status_pause = null;
        sportMapActivity.layout_status_go = null;
        sportMapActivity.btn_long_click_finish = null;
        this.b.setOnClickListener(null);
        this.b = null;
        this.f1587c.setOnClickListener(null);
        this.f1587c = null;
    }
}
