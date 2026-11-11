package com.dudu.run.ui;

import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.ImageView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;

/** Event-driven backdrop; map copies run off the UI thread and blur effects are cached. */
public final class RunMapGlass {
    private static final long FRAME_INTERVAL_MS = 42;
    private final RunScreenLayout root;
    private final ViewGroup mapView;
    private final TencentMap map;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView[] images;
    private final int[] effectWidths = new int[2], effectHeights = new int[2];
    private final MapGlassSampler sampler;
    private TextureView texture;
    private boolean active, closed, scheduled, inFlight, dirty;
    private int generation, settleFrames, sourceRetries;
    private long nextFrameAt;
    private final Runnable frame = this::captureFrame;
    private final TencentMap.OnMapLoadedCallback loaded = () -> invalidate(2);
    private final View.OnLayoutChangeListener layout = (v,l,t,r,b,ol,ot,or,ob) -> {
        resize(); invalidate(1);
    };
    private final View.OnLayoutChangeListener imageLayout = (v,l,t,r,b,ol,ot,or,ob) -> {
        configureEffects(); invalidate(1);
    };

    public RunMapGlass(RunScreenLayout root, ViewGroup mapView, TencentMap map) {
        this.root = root; this.mapView = mapView; this.map = map;
        images = new ImageView[]{root.topGlass, root.bottomGlass};
        sampler = Build.VERSION.SDK_INT >= 31 ? new MapGlassSampler() : null;
        root.addOnLayoutChangeListener(layout);
        root.topCard.addOnLayoutChangeListener(layout);
        root.bottomCard.addOnLayoutChangeListener(layout);
        for (ImageView image : images) image.addOnLayoutChangeListener(imageLayout);
        map.addOnMapLoadedCallback(loaded);
        map.setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override public void onCameraChange(CameraPosition position) { invalidate(0); }
            @Override public void onCameraChangeFinished(CameraPosition position) { invalidate(2); }
        });
    }

    private int dp(int n) { return Math.round(n * root.getResources().getDisplayMetrics().density); }

    private void resize() {
        int[] heights = {root.topCard.getBottom() + dp(56), root.bottomCard.getHeight() + dp(80)};
        for (int i = 0; i < images.length; i++) {
            ViewGroup.LayoutParams params = images[i].getLayoutParams();
            if (heights[i] > 0 && params.height != heights[i]) {
                params.height = heights[i]; images[i].setLayoutParams(params);
            }
        }
        configureEffects();
    }

    private void configureEffects() {
        if (sampler == null) return;
        for (int i = 0; i < images.length; i++) {
            ImageView image = images[i];
            int width = image.getWidth(), height = image.getHeight();
            if (width <= 0 || height <= 0 || (effectWidths[i] == width && effectHeights[i] == height)) continue;
            Api31.apply(image, dp(8), i == 1);
            effectWidths[i] = width; effectHeights[i] = height;
        }
    }

    public void resume() {
        if (closed) return;
        active = true; nextFrameAt = 0; resize(); invalidate(2);
    }

    public void pause() {
        active = false; generation++; dirty = false; settleFrames = 0;
        handler.removeCallbacks(frame); scheduled = false;
    }

    /** Markers and route segments can change without camera motion. */
    public void invalidate() { invalidate(1); }

    private void invalidate(int settle) {
        if (!active || closed || sampler == null) return;
        dirty = true; sourceRetries = 0; settleFrames = Math.max(settleFrames, settle);
        schedule();
    }

    private void schedule() {
        if (!active || closed || sampler == null || scheduled || inFlight || !dirty) return;
        scheduled = true;
        handler.postDelayed(frame, Math.max(0, nextFrameAt - SystemClock.uptimeMillis()));
    }

    private static TextureView findTexture(View view) {
        if (view instanceof TextureView) return (TextureView)view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
            TextureView child = findTexture(((ViewGroup)view).getChildAt(i));
            if (child != null) return child;
        }
        return null;
    }

    private void captureFrame() {
        scheduled = false;
        if (!active || closed || inFlight || !dirty) return;
        if (texture == null || !texture.isAttachedToWindow() || !texture.isAvailable()) texture = findTexture(mapView);
        if (texture == null || !texture.isAvailable() || texture.getWidth() <= 0 || texture.getHeight() <= 0) {
            // Bound initial retries; map and layout callbacks can retry a new surface.
            if (++sourceRetries <= 3) { nextFrameAt = SystemClock.uptimeMillis() + 120; schedule(); }
            return;
        }
        int[] origin = new int[2]; texture.getLocationOnScreen(origin);
        MapGlassSampler.Region[] regions = new MapGlassSampler.Region[2];
        for (int i = 0; i < images.length; i++) {
            ImageView image = images[i];
            if (image.getWidth() <= 0 || image.getHeight() <= 0) return;
            int[] at = new int[2]; image.getLocationOnScreen(at);
            regions[i] = new MapGlassSampler.Region(at[0] - origin[0], at[1] - origin[1], image.getWidth(), image.getHeight());
        }
        final int epoch = generation;
        dirty = false; inFlight = true;
        boolean accepted = sampler.request(texture.getSurfaceTexture(), texture.getWidth(), texture.getHeight(), regions,
                (top, bottom, duration) -> {
                    inFlight = false;
                    if (closed) return;
                    if (active && epoch == generation) {
                        if (top != null && bottom != null) {
                            images[0].setImageBitmap(top); images[1].setImageBitmap(bottom);
                            if (settleFrames > 0) { settleFrames--; dirty = true; }
                        } else {
                            // Keep the last good backdrop instead of clearing it.
                            settleFrames = 0;
                        }
                        nextFrameAt = Math.max(nextFrameAt, SystemClock.uptimeMillis()
                                + Math.max(0, Math.min(100, duration * 2) - FRAME_INTERVAL_MS));
                    }
                    schedule();
                });
        nextFrameAt = SystemClock.uptimeMillis() + FRAME_INTERVAL_MS;
        if (!accepted) { inFlight = false; dirty = true; schedule(); }
    }

    public void close() {
        if (closed) return;
        closed = true; pause();
        root.removeOnLayoutChangeListener(layout);
        root.topCard.removeOnLayoutChangeListener(layout);
        root.bottomCard.removeOnLayoutChangeListener(layout);
        map.removeOnMapLoadedCallback(loaded); map.setOnCameraChangeListener(null);
        for (ImageView image : images) {
            image.removeOnLayoutChangeListener(imageLayout); image.setImageDrawable(null);
            if (Build.VERSION.SDK_INT >= 31) image.setRenderEffect(null);
        }
        if (sampler != null) sampler.close();
        texture = null;
    }

    @android.annotation.TargetApi(31)
    private static final class Api31 {
        static void apply(ImageView view, float radius, boolean bottom) {
            RenderEffect blur = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP);
            int[] colors = bottom ? new int[]{Color.TRANSPARENT, Color.WHITE, Color.WHITE}
                    : new int[]{Color.WHITE, Color.WHITE, Color.TRANSPARENT};
            float[] stops = bottom ? new float[]{0, .35f, 1} : new float[]{0, .65f, 1};
            Shader fade = new LinearGradient(0, 0, 0, view.getHeight(), colors, stops, Shader.TileMode.CLAMP);
            view.setRenderEffect(RenderEffect.createBlendModeEffect(blur, RenderEffect.createShaderEffect(fade), BlendMode.DST_IN));
        }
    }
}
