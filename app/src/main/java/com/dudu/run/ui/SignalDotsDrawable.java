package com.dudu.run.ui;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Visual mapping of SDK signal grades, not five measured grades. */
public final class SignalDotsDrawable extends Drawable {
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final float density;private int count,color;
 public SignalDotsDrawable(float density,int color){this.density=density;this.color=color;}
 public void level(int level){count=level==3?5:level==2?3:level==1?1:0;invalidateSelf();}
 @Override public void draw(Canvas canvas){Rect b=getBounds();float radius=2*density,gap=6*density,left=b.exactCenterX()-2*gap;
  paint.setColor(color);paint.setStrokeWidth(density*.8f);for(int i=0;i<5;i++){paint.setStyle(i<count?Paint.Style.FILL:Paint.Style.STROKE);canvas.drawCircle(left+i*gap,b.exactCenterY(),radius,paint);}}
 @Override public int getIntrinsicWidth(){return Math.round(30*density);}@Override public int getIntrinsicHeight(){return Math.round(16*density);}
 @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}@Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
