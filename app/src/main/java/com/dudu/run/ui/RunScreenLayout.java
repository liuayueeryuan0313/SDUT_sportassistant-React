package com.dudu.run.ui;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.dudu.run.R;
import com.dudu.run.simulation.ConfigurableMapView;
import com.dudu.run.widget.LongClickProgressView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

/** Live run screen, preserving every recorder binding and its action IDs. */
public final class RunScreenLayout extends FrameLayout {
 public final ImageView topGlass,bottomGlass;
 public final MaterialCardView topCard,bottomCard;
 public final TextView steps;
 public final SignalDotsDrawable signal;
 private final RunnerUi ui;private final NativeMaterial material;
 public RunScreenLayout(Context c,AttributeSet a){
  super(c,a);ui=new RunnerUi(c);material=new NativeMaterial(c);setBackgroundColor(ui.page);
  ConfigurableMapView map=new ConfigurableMapView(c);map.setId(R.id.mapView);addView(map,new FrameLayout.LayoutParams(-1,-1));
  topGlass=glass();bottomGlass=glass();addView(topGlass,new FrameLayout.LayoutParams(-1,dp(210),Gravity.TOP));addView(bottomGlass,new FrameLayout.LayoutParams(-1,dp(230),Gravity.BOTTOM));
  MaterialButton title=material.tonalButton("跑步",()->{
   material.dialog().setTitle("本次跑步规则").setMessage(((TextView)findViewById(R.id.tv_std_distance)).getText()+"\n"+((TextView)findViewById(R.id.tv_std_speed)).getText()).setPositiveButton("知道了",null).show();
  });title.setIconResource(R.drawable.ic_runner_run);title.setIconSize(dp(18));title.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
  FrameLayout.LayoutParams heading=new FrameLayout.LayoutParams(dp(104),dp(48),Gravity.TOP|Gravity.START);heading.setMargins(dp(16),dp(12),0,0);addView(title,heading);
  topCard=card();LinearLayout info=column();info.setPadding(dp(14),dp(12),dp(14),dp(12));topCard.addView(info);
  LinearLayout first=row();LinearLayout gps=row();gps.addView(ui.text("GPS",12,ui.ink,false));
  ImageView dots=new ImageView(material.context());dots.setId(R.id.iv_gps_status);signal=new SignalDotsDrawable(getResources().getDisplayMetrics().density,ui.ink);dots.setImageDrawable(signal);dots.setContentDescription("GPS 信号未知");
  LinearLayout.LayoutParams dotsPosition=new LinearLayout.LayoutParams(dp(32),dp(20));dotsPosition.setMarginStart(dp(5));gps.addView(dots,dotsPosition);first.addView(gps,new LinearLayout.LayoutParams(0,-2,1));
  LinearLayout points=row();points.addView(ui.text("已过打卡点 ",12,ui.muted,false));points.addView(text(R.id.tv_unpassed_random_count,"0/0",13,true));first.addView(points,new LinearLayout.LayoutParams(0,-2,1));info.addView(first);
  LinearLayout second=row();LinearLayout.LayoutParams secondPosition=new LinearLayout.LayoutParams(-1,-2);secondPosition.topMargin=dp(10);info.addView(second,secondPosition);
  LinearLayout heat=row();heat.addView(ui.icon(R.drawable.ic_demo_flame,ui.muted,15));heat.addView(ui.text(" 热量 ",12,ui.muted,false));heat.addView(text(R.id.tv_sport_cal,"0.0",13,true));heat.addView(ui.text(" 千卡",11,ui.muted,false));second.addView(heat,new LinearLayout.LayoutParams(0,-2,1));
  LinearLayout stepRow=row();stepRow.addView(ui.icon(R.drawable.ic_demo_footprints,ui.muted,15));stepRow.addView(ui.text(" 步数 ",12,ui.muted,false));steps=ui.text("—",13,ui.ink,true);stepRow.addView(steps);second.addView(stepRow,new LinearLayout.LayoutParams(0,-2,1));
  // Rules remain available in the title's native dialog without crowding the map.
  for(int id:new int[]{R.id.tv_std_distance,R.id.tv_std_speed,R.id.tv_location_accuracy}){TextView hidden=text(id,"",11,false);hidden.setVisibility(GONE);info.addView(hidden);}
  FrameLayout.LayoutParams upper=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);upper.setMargins(dp(16),dp(68),dp(16),0);addView(topCard,upper);
  bottomCard=card();LinearLayout bottom=column();bottom.setPadding(dp(14),dp(16),dp(14),dp(14));bottomCard.addView(bottom);
  LinearLayout metrics=row();String[] labels={"运动时长","跑步里程","平均配速"};String[] defaults={"00:00:00","0.00 km","00′00″ /km"};int[] ids={R.id.tv_sport_time,R.id.tv_total_distance,R.id.tv_sport_speed};
  for(int i=0;i<3;i++){
   if(i>0){View divider=new View(c);divider.setBackgroundColor(ui.outline);metrics.addView(divider,new LinearLayout.LayoutParams(dp(1),dp(40)));}
   LinearLayout cell=column();cell.setGravity(Gravity.CENTER);TextView number=text(ids[i],defaults[i],19,true);number.setSingleLine(true);if(android.os.Build.VERSION.SDK_INT>=26)number.setAutoSizeTextTypeUniformWithConfiguration(12,19,1,android.util.TypedValue.COMPLEX_UNIT_SP);cell.addView(number,new LinearLayout.LayoutParams(-2,dp(28)));TextView caption=ui.text(labels[i],11,ui.muted,false);caption.setGravity(Gravity.CENTER);cell.addView(caption,new LinearLayout.LayoutParams(-1,-2));metrics.addView(cell,new LinearLayout.LayoutParams(0,-2,1));
  }bottom.addView(metrics);
  FrameLayout controls=new FrameLayout(c);LinearLayout.LayoutParams controlsPosition=new LinearLayout.LayoutParams(-1,dp(52));controlsPosition.topMargin=dp(16);bottom.addView(controls,controlsPosition);
  FrameLayout holding=new FrameLayout(c);holding.setId(R.id.layout_status_pause);LongClickProgressView pause=new LongClickProgressView(c);pause.setId(R.id.btn_long_click_finish);holding.addView(pause,new FrameLayout.LayoutParams(-1,-1));controls.addView(holding,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout paused=row();paused.setId(R.id.layout_status_go);MaterialButton stop=material.button("停止",null);stop.setId(R.id.btn_run_finish);stop.setBackgroundTintList(ColorStateList.valueOf(0xffff3c4a));stop.setTextColor(Color.WHITE);MaterialButton resume=material.button("继续",null);resume.setId(R.id.btn_run_go);
  stop.setIconResource(R.drawable.ic_run_stop);resume.setIconResource(R.drawable.ic_demo_play);
  for(MaterialButton button:new MaterialButton[]{stop,resume}){button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);button.setIconSize(dp(18));button.setIconTint(ColorStateList.valueOf(Color.WHITE));button.setInsetTop(0);button.setInsetBottom(0);}
  paused.addView(stop,new LinearLayout.LayoutParams(0,-1,1));LinearLayout.LayoutParams continuePosition=new LinearLayout.LayoutParams(0,-1,1);continuePosition.setMarginStart(dp(8));paused.addView(resume,continuePosition);controls.addView(paused,new FrameLayout.LayoutParams(-1,-1));
  FrameLayout.LayoutParams lower=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);lower.setMargins(dp(16),0,dp(16),dp(16));addView(bottomCard,lower);
  LinearLayout countdown=column();countdown.setId(R.id.layout_countdown);countdown.setClickable(true);countdown.setGravity(Gravity.CENTER);countdown.setBackgroundColor(ui.page);TextView count=text(R.id.tv_countdown,"",48,true);count.setGravity(Gravity.CENTER);count.setIncludeFontPadding(false);countdown.addView(count,new LinearLayout.LayoutParams(-1,-1));countdown.setVisibility(GONE);addView(countdown,new FrameLayout.LayoutParams(-1,-1));
 }
 private int dp(int value){return ui.dp(value);}private LinearLayout column(){LinearLayout l=new LinearLayout(material.context());l.setOrientation(LinearLayout.VERTICAL);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(material.context());l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private TextView text(int id,String value,int size,boolean bold){TextView t=ui.text(value,size,ui.ink,bold);t.setId(id);return t;}
 private MaterialCardView card(){MaterialCardView card=new MaterialCardView(material.context());card.setRadius(dp(28));card.setCardElevation(0);card.setUseCompatPadding(false);card.setPreventCornerOverlap(false);card.setCardBackgroundColor(android.os.Build.VERSION.SDK_INT>=31?((ui.surface&0x00ffffff)|0xb3000000):ui.surface);card.setStrokeWidth(dp(1));card.setStrokeColor((ui.outline&0x00ffffff)|0x99000000);return card;}
 private ImageView glass(){ImageView image=new ImageView(getContext());image.setScaleType(ImageView.ScaleType.FIT_XY);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);image.setClickable(false);return image;}
}
