package com.dudu.run.widget;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.dudu.run.ui.NativeMaterial;
import com.google.android.material.button.MaterialButton;
/** Material button; the original 100-step hold threshold and scheduling are preserved. */
public class LongClickProgressView extends com.dudu.run.ui.MotionMaterialButton {
 public interface c {void a(float value);void onCancel();void onFinish();}
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path clip=new Path();
 private float progress;private boolean holding,completed;private c listener;private int fillColor=0xffff3c4a;
 private final Runnable advance=new Runnable(){public void run(){
  if(!holding)return;setProgress(progress+1);if(listener!=null)listener.a(progress);
  if(progress<100)handler.postDelayed(this,1);
  else{holding=false;completed=true;setPressed(false);if(listener!=null)listener.onFinish();}
 }};
 private final Runnable rewind=new Runnable(){public void run(){
  setProgress(Math.max(0,progress-(progress<10?2:7)));if(listener!=null)listener.a(progress);
  if(progress>0)handler.postDelayed(this,10);else if(listener!=null)listener.onCancel();
 }};
 public LongClickProgressView(Context c){this(c,null);}public LongClickProgressView(Context c,AttributeSet a){this(c,a,0);}
 public LongClickProgressView(Context c,AttributeSet a,int style){
  super(new NativeMaterial(c).context(),a);setText("长按暂停");setIconResource(com.dudu.run.R.drawable.ic_run_pause);setIconSize((int)(getResources().getDisplayMetrics().density*18));setIconTint(ColorStateList.valueOf(Color.WHITE));setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);setTextColor(Color.WHITE);setTextSize(16);setAllCaps(false);
  setBackgroundTintList(ColorStateList.valueOf(0xff923740));
  setInsetTop(0);setInsetBottom(0);setMinHeight(0);setMinimumHeight(0);setContentDescription("长按暂停跑步");
 }
 @Override protected void onDraw(Canvas canvas){
  if(progress>0){int save=canvas.save();clip.reset();clip.addRoundRect(new RectF(0,0,getWidth(),getHeight()),getHeight()/2f,getHeight()/2f,Path.Direction.CW);
   canvas.clipPath(clip);fill.setColor(fillColor);canvas.drawRect(0,0,getWidth()*progress/100f,getHeight(),fill);canvas.restoreToCount(save);}
  super.onDraw(canvas);
 }
 @Override public boolean onTouchEvent(MotionEvent e){if(!isEnabled())return false;switch(e.getActionMasked()){
  case MotionEvent.ACTION_DOWN:handler.removeCallbacks(rewind);handler.removeCallbacks(advance);completed=false;holding=true;setPressed(true);getParent().requestDisallowInterceptTouchEvent(true);handler.post(advance);return true;
  case MotionEvent.ACTION_MOVE:if(e.getX()<0||e.getY()<0||e.getX()>getWidth()||e.getY()>getHeight())cancelHold();return true;
  case MotionEvent.ACTION_UP:if(completed)performClick();else cancelHold();return true;
  case MotionEvent.ACTION_CANCEL:cancelHold();return true;default:return true;}}
 private void cancelHold(){if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);holding=false;handler.removeCallbacks(advance);setPressed(false);handler.removeCallbacks(rewind);handler.post(rewind);}
 public void resetHold(){holding=false;completed=false;handler.removeCallbacksAndMessages(null);setProgress(0);setPressed(false);}
 @Override public boolean performClick(){return super.performClick();}
 @Override protected void onDetachedFromWindow(){resetHold();super.onDetachedFromWindow();}
 @Override protected void onVisibilityChanged(android.view.View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(handler!=null&&visibility!=VISIBLE)resetHold();}
 public void setOnLongClickStateListener(c target){listener=target;}public void setProgress(float value){progress=Math.max(0,Math.min(100,value));invalidate();}
 public void setCenterColor(int color){setBackgroundTintList(ColorStateList.valueOf(color));}public void setRingColor(int color){fillColor=color;invalidate();}
 public void setRingWidth(float width){}public void setStartAngle(float angle){}
}
