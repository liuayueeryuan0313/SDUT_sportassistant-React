package com.dudu.run.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.dudu.run.simulation.ConfigurableMapView;
import com.dudu.run.simulation.CoordinateTransform;
import com.dudu.run.simulation.SimulationEngine;
import com.dudu.run.utils.PrivacyConsent;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Physical foreground location and a read-only map, independent of run initialization. */
public final class DashboardLocation implements LocationListener {
    private final Activity host;
    private final FrameLayout container;
    private final TextView label;
    private final LocationManager locations;
    private final ExecutorService addressWorker = Executors.newSingleThreadExecutor();
    private ConfigurableMapView mapView;
    private TencentMap map;
    private Marker marker;
    private boolean active, closed, addressBusy, visible, permissionRequested, mapFailed;
    private long latestFix;
    private Location addressLocation;
    private Location latestLocation;
    private long addressTime;
    private int generation;

    public DashboardLocation(Activity host, FrameLayout container, TextView label) {
        this.host = host;
        this.container = container;
        this.label = label;
        locations = (LocationManager) host.getSystemService(Context.LOCATION_SERVICE);
        label.setText("正在定位…");
        label.setOnClickListener(view -> {
            permissionRequested = false;
            resume();
        });
    }

    private boolean permitted() {
        return Build.VERSION.SDK_INT < 23
                || host.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || host.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void resume() {
        visible = true;
        if (closed || active || !PrivacyConsent.isAgreed(host)) return;
        if (!permitted()) {
            label.setText("请允许定位权限");
            if (permissionRequested) return;
            permissionRequested = true;
            com.dudu.run.permission.b.a(host, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    new com.dudu.run.permission.c() {
                        @Override public void b() { if (!closed && visible) resume(); }
                    });
            return;
        }
        active = true;
        generation++;
        mapFailed = false;
        if (mapView != null) { mapView.onStart(); mapView.onResume(); }
        boolean enabled = false;
        if (locations != null) {
            for (String provider : new String[]{LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, "fused"}) {
                try {
                    if (!locations.isProviderEnabled(provider)) continue;
                    enabled = true;
                    Location cached = locations.getLastKnownLocation(provider);
                    if (cached != null) onLocationChanged(cached);
                    locations.requestLocationUpdates(provider, 3000, 5, this, Looper.getMainLooper());
                } catch (RuntimeException unavailable) {
                    // Other enabled providers can still deliver a physical location.
                }
            }
        }
        if (!enabled) label.setText("请开启系统定位");
        else if (latestLocation == null) label.setText("正在定位…");
    }

    @Override public void onLocationChanged(Location raw) {
        if (!active || closed || raw == null) return;
        long age = SystemClock.elapsedRealtimeNanos() - raw.getElapsedRealtimeNanos();
        if (age < 0 || age > 120000000000L || raw.getElapsedRealtimeNanos() < latestFix || !raw.hasAccuracy()
                || raw.getAccuracy() <= 0 || Float.isNaN(raw.getAccuracy()) || Float.isInfinite(raw.getAccuracy())) return;
        SimulationEngine.Point point;
        try {
            point = CoordinateTransform.fromWgs84(new SimulationEngine.Point(raw.getLatitude(), raw.getLongitude(), 0));
        } catch (IllegalArgumentException invalid) { return; }
        latestFix = raw.getElapsedRealtimeNanos();
        latestLocation = new Location(raw);
        LatLng coordinate = new LatLng(point.latitude, point.longitude);
        if (mapView == null && !mapFailed) createMap();
        if (map != null) {
            if (marker == null) marker = map.addMarker(new MarkerOptions().position(coordinate));
            else marker.setPosition(coordinate);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, raw.getAccuracy() > 200 ? 13 : 16));
            mapView.setVisibility(View.VISIBLE);
        }
        long now = SystemClock.elapsedRealtime();
        if (addressLocation == null || raw.distanceTo(addressLocation) > 100 || now - addressTime > 60000) {
            String prefix = raw.getAccuracy() > 200 ? "大致位置 · " : "";
            label.setText(prefix + String.format(Locale.CHINA, "%.5f, %.5f", raw.getLatitude(), raw.getLongitude()));
            if (!addressBusy && Geocoder.isPresent()) resolveAddress(raw);
        }
    }

    private void createMap() {
        try {
            mapView = new ConfigurableMapView(host);
            mapView.setVisibility(View.INVISIBLE);
            map = mapView.getMap();
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            container.addView(mapView, 0, new FrameLayout.LayoutParams(-1, -1));
            mapView.onStart();
            mapView.onResume();
        } catch (RuntimeException unavailable) {
            mapFailed = true;
            map = null;
            if (mapView != null) {
                container.removeView(mapView);
                mapView.onDestroy();
                mapView = null;
            }
        }
    }

    private void resolveAddress(Location raw) {
        addressBusy = true;
        int requestedGeneration = generation;
        Location requested = new Location(raw);
        addressWorker.execute(() -> {
            String name = null;
            try {
                List<Address> results = new Geocoder(host.getApplicationContext(), Locale.getDefault())
                        .getFromLocation(requested.getLatitude(), requested.getLongitude(), 1);
                if (results != null && !results.isEmpty()) {
                    Address address = results.get(0);
                    name = address.getThoroughfare();
                    if (name == null || name.isEmpty()) name = address.getFeatureName();
                    if (name == null || name.isEmpty()) name = address.getSubLocality();
                }
            } catch (Exception unavailable) {
                // Coordinates remain visible when reverse geocoding is unavailable.
            }
            String place = name;
            host.runOnUiThread(() -> {
                addressBusy = false;
                if (closed || !active || requestedGeneration != generation) return;
                if (latestLocation != null && requested.distanceTo(latestLocation) > 100) return;
                addressLocation = requested;
                addressTime = SystemClock.elapsedRealtime();
                if (place != null && !place.isEmpty()) {
                    label.setText((requested.getAccuracy() > 200 ? "大致位置 · " : "") + place);
                }
            });
        });
    }

    public void pause() {
        visible = false;
        if (!active) return;
        active = false;
        generation++;
        if (locations != null) {
            try { locations.removeUpdates(this); } catch (SecurityException revoked) { }
        }
        if (mapView != null) { mapView.onPause(); mapView.onStop(); }
    }

    public void close() {
        pause();
        closed = true;
        addressWorker.shutdownNow();
        if (mapView != null) { mapView.onDestroy(); mapView = null; map = null; }
    }

    @Override public void onProviderEnabled(String provider) {
        if (active) { pause(); resume(); }
    }
    @Override public void onProviderDisabled(String provider) {
        if (active) { pause(); resume(); }
    }
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
