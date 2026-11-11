package com.dudu.run.ui;

import android.graphics.*;
import android.os.*;
import android.os.Process;
import android.view.PixelCopy;
import android.view.Surface;

/** One asynchronous GPU copy shared by both backdrops. No View reads or UI-thread bitmap copies. */
@android.annotation.TargetApi(31)
public final class MapGlassSampler {
    public interface Callback { void sampled(Bitmap top, Bitmap bottom, long durationMillis); }

    public static final class Region {
        final int x, y, width, height;
        public Region(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
    }

    private final HandlerThread thread = new HandlerThread("RunMapGlass", Process.THREAD_PRIORITY_BACKGROUND);
    private final Handler worker;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Frame[] frames = {new Frame(), new Frame(), new Frame()};
    private Bitmap capture;
    private Surface source;
    private SurfaceTexture sourceTexture;
    private int frameIndex;
    private boolean busy;
    private volatile boolean closed;

    public MapGlassSampler() { thread.start(); worker = new Handler(thread.getLooper()); }

    /** Main-thread entry; back-pressure prevents a queue of stale screenshots. */
    public boolean request(SurfaceTexture texture, int width, int height, Region[] regions, Callback callback) {
        if (closed || busy || texture == null || width <= 0 || height <= 0 || regions == null || regions.length != 2) return false;
        for (Region region : regions) if (region == null || region.width <= 0 || region.height <= 0) return false;
        busy = true;
        Region[] geometry = regions.clone();
        worker.post(() -> copy(texture, width, height, geometry, callback));
        return true;
    }

    private void copy(SurfaceTexture texture, int width, int height, Region[] regions, Callback callback) {
        if (closed) return;
        long started = SystemClock.uptimeMillis();
        try {
            if (sourceTexture != texture || source == null || !source.isValid()) {
                if (source != null) source.release();
                sourceTexture = texture; source = new Surface(texture);
            }
            int sampleWidth = Math.min(256, width);
            float scale = sampleWidth / (float)width;
            int sampleHeight = Math.max(1, Math.round(height * scale));
            if (capture == null || capture.getWidth() != sampleWidth || capture.getHeight() != sampleHeight)
                capture = Bitmap.createBitmap(sampleWidth, sampleHeight, Bitmap.Config.ARGB_8888);
            PixelCopy.request(source, capture, status -> {
                if (closed) return;
                try {
                    if (status != PixelCopy.SUCCESS) { deliver(null, callback, started); return; }
                    Frame frame = frames[frameIndex]; frameIndex = (frameIndex + 1) % frames.length;
                    for (int i = 0; i < regions.length; i++) {
                        Region region = regions[i];
                        int w = Math.max(1, Math.round(region.width * scale));
                        int h = Math.max(1, Math.round(region.height * scale));
                        if (frame.bitmaps[i] == null || frame.bitmaps[i].getWidth() != w || frame.bitmaps[i].getHeight() != h) {
                            frame.bitmaps[i] = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            frame.canvases[i] = new Canvas(frame.bitmaps[i]);
                        }
                        frame.bitmaps[i].eraseColor(Color.TRANSPARENT);
                        frame.canvases[i].drawBitmap(capture, -region.x * scale, -region.y * scale, paint);
                    }
                    deliver(frame, callback, started);
                } catch (RuntimeException invalidSurface) { deliver(null, callback, started); }
            }, worker);
        } catch (RuntimeException invalidSurface) { deliver(null, callback, started); }
    }

    private void deliver(Frame frame, Callback callback, long started) {
        long duration = SystemClock.uptimeMillis() - started;
        main.post(() -> {
            busy = false;
            if (!closed) callback.sampled(frame == null ? null : frame.bitmaps[0], frame == null ? null : frame.bitmaps[1], duration);
        });
    }

    public void close() {
        if (closed) return;
        closed = true;
        worker.post(() -> {
            if (source != null) { source.release(); source = null; }
            sourceTexture = null; capture = null;
            for (Frame frame : frames) for (int i = 0; i < frame.bitmaps.length; i++) {
                frame.bitmaps[i] = null; frame.canvases[i] = null;
            }
            // Never release the SDK-owned SurfaceTexture or recycle a displayed bitmap.
            thread.quitSafely();
        });
    }

    private static final class Frame {
        final Bitmap[] bitmaps = new Bitmap[2];
        final Canvas[] canvases = new Canvas[2];
    }
}
