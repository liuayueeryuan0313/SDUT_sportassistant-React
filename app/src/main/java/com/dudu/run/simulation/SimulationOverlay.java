package com.dudu.run.simulation;

import android.app.*;
import android.content.Intent;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.dudu.run.activity.SportMapActivity;
import java.util.Locale;

/** An activity-owned capsule which expands into a floating control card. */
public final class SimulationOverlay implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityResumed(Activity activity) {attach(activity);}
    public static void attach(Activity activity) {
        if (!(activity instanceof SportMapActivity)||activity.isFinishing()||com.dudu.run.c.n().q()==0) return;
        if (!SimulationController.get(activity).enabled() && !com.dudu.run.c.n().isSimulationSource()) return;
        View countdown=activity.findViewById(com.dudu.run.R.id.layout_countdown);
        if(countdown!=null&&countdown.getVisibility()==View.VISIBLE)return;
        FrameLayout root=activity.findViewById(android.R.id.content);
        if(root!=null && root.findViewWithTag("motion_panel")==null) new Panel(activity,root);
    }
    private static final class Panel extends MaterialCardView implements SimulationController.Listener {
        private final FrameLayout parent;
        private final SimulationController controller;
        private final MaterialButton title,pause;
        private final TextView state,coordinates;
        private final TextView[] values=new TextView[4];
        private final LinearLayout body,content;
        private final RunnerUi ui;
        private boolean expanded;
        private int dp(int n) { return SimulationUi.dp(getContext(),n); }
        Panel(Activity activity,FrameLayout parent) {
            super(new NativeMaterial(activity).context()); this.parent=parent; controller=SimulationController.get(activity);
            NativeMaterial material=new NativeMaterial(activity);ui=new RunnerUi(activity);
            setTag("motion_panel");setRadius(dp(24));setCardElevation(0);
            setCardBackgroundColor(ui.surface);setStrokeColor(ui.outline);setStrokeWidth(dp(1));
            setPreventCornerOverlap(false);setUseCompatPadding(false);
            content=new LinearLayout(material.context());content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(0,0,0,0);addView(content);
            title=material.tonalButton("运动模拟",null);
            title.setContentDescription("展开运动模拟控制面板");
            title.setTextSize(13);title.setInsetTop(0);title.setInsetBottom(0);
            content.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
            body=new LinearLayout(material.context());body.setOrientation(LinearLayout.VERTICAL);body.setVisibility(GONE);content.addView(body);
            state=ui.text("未启用",12,ui.accentText,true);body.addView(state);
            SimulationUi.gap(body,6);
            for(int row=0;row<2;row++) {
                LinearLayout stats=new LinearLayout(material.context());stats.setOrientation(LinearLayout.HORIZONTAL);
                String[] captions={"速度 · km/h","步频 · 步/分","高度 · m","模拟里程"};
                for(int col=0;col<2;col++) {
                    int i=row*2+col;
                    MaterialCardView cell=new MaterialCardView(material.context());
                    cell.setRadius(dp(14));cell.setCardElevation(0);
                    int[] darkColors={0xff4a3b50,0xff493d55,0xff3d4658,0xff3b4c49};
                    int[] lightColors={0xfff0e4f3,0xffeee6f5,0xffe2eaf7,0xffe1f1e8};
                    cell.setCardBackgroundColor(androidx.core.graphics.ColorUtils.blendARGB(ui.dark?darkColors[i]:lightColors[i],ui.accent,ui.dark?.10f:.06f));
                    LinearLayout column=new LinearLayout(material.context());column.setOrientation(LinearLayout.VERTICAL);
                    column.setPadding(dp(8),dp(5),dp(6),dp(5));
                    LinearLayout caption=new LinearLayout(material.context());caption.setGravity(Gravity.CENTER_VERTICAL);
                    int[] icons={com.dudu.run.R.drawable.ic_simulation_speed,com.dudu.run.R.drawable.ic_demo_footprints,com.dudu.run.R.drawable.ic_simulation_altitude,com.dudu.run.R.drawable.ic_demo_route};
                    caption.addView(ui.icon(icons[i],ui.accentText,14));TextView label=ui.text(captions[i],10,ui.muted,false);
                    LinearLayout.LayoutParams labelPosition=new LinearLayout.LayoutParams(-1,-2);labelPosition.setMarginStart(dp(4));caption.addView(label,labelPosition);column.addView(caption);
                    values[i]=ui.text("—",19,ui.ink,true);LinearLayout.LayoutParams valuePosition=new LinearLayout.LayoutParams(-1,-2);valuePosition.topMargin=dp(4);column.addView(values[i],valuePosition);cell.addView(column);
                    LinearLayout.LayoutParams position=new LinearLayout.LayoutParams(0,dp(72),1);
                    if(col>0)position.setMarginStart(dp(6));stats.addView(cell,position);
                }
                body.addView(stats);SimulationUi.gap(body,6);
            }
            coordinates=ui.text("请先设置路线",10,ui.muted,false);body.addView(coordinates);
            SimulationUi.gap(body,8);
            MaterialButton route=material.button("画线 / 修改本次路线",()->activity.startActivity(new Intent(activity,SimulationActivity.class)));
            route.setTextSize(12);body.addView(route,new LinearLayout.LayoutParams(-1,dp(48)));
            SimulationUi.gap(body,6);
            MaterialButton speed=material.button("直接调整速度",()->QuickSpeedDialog.show(activity));
            speed.setTextSize(12);body.addView(speed,new LinearLayout.LayoutParams(-1,dp(48)));
            SimulationUi.gap(body,6);
            LinearLayout actions=new LinearLayout(material.context());actions.setOrientation(LinearLayout.HORIZONTAL);
            pause=material.tonalButton("继续",()->{if(controller.running())controller.pause();else controller.resume();});
            pause.setTextSize(12);actions.addView(pause,new LinearLayout.LayoutParams(0,dp(48),1));
            MaterialButton stop=material.tonalButton("停止",()->controller.stop());stop.setTextSize(12);
            LinearLayout.LayoutParams stopPosition=new LinearLayout.LayoutParams(0,dp(48),1);stopPosition.setMarginStart(dp(6));
            actions.addView(stop,stopPosition);body.addView(actions);
            FrameLayout.LayoutParams position=new FrameLayout.LayoutParams(dp(148),-2,Gravity.TOP|Gravity.RIGHT);
            position.topMargin=dp(12); position.rightMargin=dp(16); parent.addView(this,position);
            title.setOnClickListener(v->{
                expanded=!expanded;body.setVisibility(expanded?VISIBLE:GONE);
                FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)getLayoutParams();
                lp.width=expanded?Math.min(dp(280),parent.getWidth()-dp(32)):dp(148);
                setLayoutParams(lp);onSimulationChanged();
            });
            controller.addListener(this);onSimulationChanged();
        }
        @Override public void onSimulationChanged() {
            title.setText(expanded?(OfflineTestSession.active()?"离线测试 · 收起":"运动模拟 · 收起"):(OfflineTestSession.active()?"离线测试  ›":"运动模拟  ›"));
            if(controller.waitingForRoute())title.setText(expanded?"等待画线 · 收起":"等待画线  ›");
            title.setTextColor(ui.accentText);
            MaterialButton header=title;
            android.graphics.drawable.GradientDrawable dot=new android.graphics.drawable.GradientDrawable();
            dot.setShape(android.graphics.drawable.GradientDrawable.OVAL);dot.setColor(android.graphics.Color.WHITE);dot.setSize(dp(5),dp(5));
            header.setIcon(dot);header.setIconSize(dp(5));header.setIconPadding(dp(6));
            header.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            header.setIconTint(android.content.res.ColorStateList.valueOf(controller.running()?0xff39c868:0xffff525e));
            content.setPadding(expanded?dp(10):0,expanded?dp(4):0,expanded?dp(10):0,expanded?dp(10):0);
            setCardBackgroundColor(expanded?ui.surface:android.graphics.Color.TRANSPARENT);
            setStrokeWidth(expanded?dp(1):0);
            state.setText(controller.state()); pause.setText(controller.running()?"暂停模拟":"继续模拟");
            SimulationEngine.Sample s=controller.sample();
            if(s==null) return;
            values[0].setText(String.format(Locale.CHINA,"%.1f",controller.running()?s.speedKmh:0));
            values[1].setText(String.format(Locale.CHINA,"%.0f",controller.running()?s.cadence:0));
            values[2].setText(String.format(Locale.CHINA,"%.1f",s.point.altitude));
            values[3].setText(s.distanceMeters>=1000?String.format(Locale.CHINA,"%.2f km",s.distanceMeters/1000):
                    String.format(Locale.CHINA,"%.0f m",s.distanceMeters));
            coordinates.setText(String.format(Locale.CHINA,"%.6f, %.6f  ·  %d 步",s.point.latitude,s.point.longitude,s.steps));
        }
        @Override protected void onDetachedFromWindow() {controller.removeListener(this);super.onDetachedFromWindow();}
    }
    @Override public void onActivityPaused(Activity activity) {
        View root=activity.findViewById(android.R.id.content);
        if(root instanceof FrameLayout) {View panel=root.findViewWithTag("motion_panel");if(panel!=null)((FrameLayout)root).removeView(panel);}
    }
    @Override public void onActivityCreated(Activity a,Bundle b) {}
    @Override public void onActivityStarted(Activity a) {}
    @Override public void onActivityStopped(Activity a) {}
    @Override public void onActivitySaveInstanceState(Activity a,Bundle b) {}
    @Override public void onActivityDestroyed(Activity a) {}
}

