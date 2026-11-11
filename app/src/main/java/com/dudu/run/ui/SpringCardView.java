package com.dudu.run.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.card.MaterialCardView;

/** Official Material card with AndroidX spring feedback; native click/ripple handling stays intact. */
public final class SpringCardView extends MaterialCardView {
    private final SpringAnimation pressX,pressY;
    public SpringCardView(Context context){
        super(context);
        pressX=new SpringAnimation(this,SpringAnimation.SCALE_X,1f);
        pressY=new SpringAnimation(this,SpringAnimation.SCALE_Y,1f);
        pressX.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM).setDampingRatio(.7f);
        pressY.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM).setDampingRatio(.7f);
    }
    @Override public void setPressed(boolean pressed){
        boolean changed=pressed!=isPressed();
        super.setPressed(pressed);
        if(!changed||pressX==null||pressY==null)return;
        if(!isAttachedToWindow()||!isLaidOut()||(Build.VERSION.SDK_INT>=26&&!ValueAnimator.areAnimatorsEnabled())){
            resetSpring();return;
        }
        pressX.animateToFinalPosition(pressed?.98f:1f);
        pressY.animateToFinalPosition(pressed?.98f:1f);
    }
    private void resetSpring(){
        pressX.cancel();pressY.cancel();setScaleX(1f);setScaleY(1f);
    }
    @Override protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();resetSpring();
    }
}
