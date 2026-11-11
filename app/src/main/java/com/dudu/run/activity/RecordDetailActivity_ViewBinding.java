package com.dudu.run.activity;

import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.dudu.run.R;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;
public class RecordDetailActivity_ViewBinding implements Unbinder {
    private RecordDetailActivity a;

    public RecordDetailActivity_ViewBinding(RecordDetailActivity recordDetailActivity, View view) {
        this.a = recordDetailActivity;
        recordDetailActivity.tv_title = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_title, "field 'tv_title'", TextView.class);
        recordDetailActivity.mapView = (TextureMapView) Utils.findRequiredViewAsType(view, R.id.mapView, "field 'mapView'", TextureMapView.class);
        recordDetailActivity.tv_total_distance = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_total_distance, "field 'tv_total_distance'", TextView.class);
        recordDetailActivity.tv_start_time = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_start_time, "field 'tv_start_time'", TextView.class);
        recordDetailActivity.tv_sport_speed = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_speed, "field 'tv_sport_speed'", TextView.class);
        recordDetailActivity.tv_sport_time = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_time, "field 'tv_sport_time'", TextView.class);
        recordDetailActivity.tv_sport_cal = (TextView) Utils.findRequiredViewAsType(view, R.id.tv_sport_cal, "field 'tv_sport_cal'", TextView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        RecordDetailActivity recordDetailActivity = this.a;
        if (recordDetailActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.a = null;
        recordDetailActivity.tv_title = null;
        recordDetailActivity.mapView = null;
        recordDetailActivity.tv_total_distance = null;
        recordDetailActivity.tv_start_time = null;
        recordDetailActivity.tv_sport_speed = null;
        recordDetailActivity.tv_sport_time = null;
        recordDetailActivity.tv_sport_cal = null;
    }
}
