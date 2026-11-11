package com.dudu.run.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public final class TrackArtwork extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF lane = new RectF();
    private final Path lap = new Path();

    public TrackArtwork(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        if (width == 0 || height == 0) return;
        RunnerUi ui = new RunnerUi(getContext());
        canvas.save();
        float scale = Math.max(width / 220, height * 1.26f / 160);
        canvas.translate((width - 220 * scale) / 2, (height - 160 * scale) / 2);
        canvas.scale(scale, scale);
        canvas.rotate(-24, 110, 80);
        paint.setColor(ui.accentText);
        paint.setAlpha(59);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        for (int i = 0; i < 3; i++) {
            float inset = i * 10f;
            lane.set(15 + inset, 25 + inset, 205 - inset, 135 - inset);
            canvas.drawRoundRect(lane, lane.height() / 2, lane.height() / 2, paint);
        }
        paint.setAlpha(191);
        paint.setStrokeWidth(2.5f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        lap.reset();
        lap.moveTo(73, 130);
        lap.lineTo(150, 130);
        lane.set(100, 30, 200, 130);
        lap.arcTo(lane, 90, -90, false);
        canvas.drawPath(lap, paint);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(200, 80, 4, paint);
        canvas.restore();
    }
}
