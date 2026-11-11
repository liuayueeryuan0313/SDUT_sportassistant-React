package com.dudu.run.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public final class RoutePreview extends View {
    private final RunnerUi ui;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private double[] coordinates = new double[0];
    private boolean[] starts = new boolean[0];

    public RoutePreview(Context context) {
        this(context, false);
    }

    public RoutePreview(Context context, boolean recordCard) {
        super(context);
        ui = new RunnerUi(context);
        setBackground(ui.background(recordCard ? ui.map : ui.tone, recordCard ? 10 : 14));
        setClipToOutline(true);
        setContentDescription("路线预览");
    }

    public void setCoordinates(double[] points) {
        coordinates = points;
        starts = new boolean[0];
        invalidate();
    }

    public void setTrack(java.util.List<com.dudu.run.bean.LatLngInfo> track) {
        java.util.ArrayList<Double> values = new java.util.ArrayList<>();
        java.util.ArrayList<Boolean> segments = new java.util.ArrayList<>();
        boolean gap = true;
        for (com.dudu.run.bean.LatLngInfo point : track) {
            if (point == null || point.latLng == null
                    || Double.isNaN(point.latLng.latitude) || Double.isNaN(point.latLng.longitude)
                    || Math.abs(point.latLng.latitude) > 90 || Math.abs(point.latLng.longitude) > 180) {
                gap = true;
                continue;
            }
            values.add(point.latLng.latitude);
            values.add(point.latLng.longitude);
            segments.add(gap || point.isStartPosition);
            gap = false;
        }
        coordinates = new double[values.size()];
        for (int i = 0; i < coordinates.length; i++) coordinates[i] = values.get(i);
        starts = new boolean[segments.size()];
        for (int i = 0; i < starts.length; i++) starts[i] = segments.get(i);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        paint.setColor(ui.accent);
        paint.setStrokeWidth(ui.dp(1));
        paint.setAlpha(45);
        for (int x = ui.dp(18); x < width; x += ui.dp(24)) canvas.drawLine(x, 0, x, height, paint);
        for (int y = ui.dp(14); y < height; y += ui.dp(24)) canvas.drawLine(0, y, width, y, paint);
        if (coordinates.length < 4) return;
        double minLat = 90, maxLat = -90, minLon = 180, maxLon = -180;
        for (int i = 0; i < coordinates.length; i += 2) {
            minLat = Math.min(minLat, coordinates[i]);
            maxLat = Math.max(maxLat, coordinates[i]);
            minLon = Math.min(minLon, coordinates[i + 1]);
            maxLon = Math.max(maxLon, coordinates[i + 1]);
        }
        double cos = Math.max(.001, Math.cos(Math.toRadians((minLat + maxLat) / 2)));
        double scale = Math.max(0, Math.min((width - ui.dp(20)) / Math.max((maxLon - minLon) * cos, .000001),
                (height - ui.dp(20)) / Math.max(maxLat - minLat, .000001)));
        path.reset();
        for (int i = 0; i < coordinates.length; i += 2) {
            float x = (float) (width / 2 + (coordinates[i + 1] - (minLon + maxLon) / 2) * cos * scale);
            float y = (float) (height / 2 - (coordinates[i] - (minLat + maxLat) / 2) * scale);
            if (i == 0 || (starts.length > i / 2 && starts[i / 2])) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ui.dp(2.5f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
