package com.dudu.run.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public final class InitialAvatar extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String initial;

    public InitialAvatar(int color) {
        String name = com.dudu.run.b.c().o();
        name = name == null ? "" : name.trim();
        initial = name.isEmpty() ? "跑" : new String(Character.toChars(name.codePointAt(0)));
        paint.setColor(color);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override public void draw(Canvas canvas) {
        paint.setTextSize(getBounds().height() * .42f);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(initial, getBounds().exactCenterX(),
                getBounds().exactCenterY() - (metrics.ascent + metrics.descent) / 2, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
