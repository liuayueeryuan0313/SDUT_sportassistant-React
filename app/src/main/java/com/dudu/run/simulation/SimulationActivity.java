package com.dudu.run.simulation;

import android.app.*;
import com.dudu.run.ui.NativeMaterial;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.dudu.run.R;
import com.dudu.run.bean.RandomPoint;
import com.dudu.run.ui.RunnerUi;
import com.dudu.run.utils.PrivacyConsent;
import com.tencent.tencentmap.mapsdk.maps.*;
import com.tencent.tencentmap.mapsdk.maps.model.*;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/** Map-first local route editor with tap waypoints and projected freehand strokes. */
public final class SimulationActivity extends Activity implements SimulationController.Listener {
    private static final int IMPORT_GPX=820,BROWSE=0,PICK=1,DRAW=2;
    private static final String PRESET_EDITOR = "preset_editor";
    private static final String PRESET_DRAFT = "preset_draft";
    private boolean presetEditor;
    private SimulationController controller;
    private SimulationController.RouteEdit routeEdit;
    private boolean routeApplied;
    private TextureMapView mapView;
    private TencentMap map;
    private Marker moving;
    private boolean mapTouchHadMultiplePointers;
    private MapExplorer explorer;
    private LinearLayout bottomCard;
    private FrameLayout root;
    private DrawingLayer drawing;
    private TextView hint,routeSummary,state,pauseButton,startButton,stopButton,parameterSummary,checkpointSummary;
    private MaterialButton undoButton,moreButton;
    RunnerUi ui;
    NativeMaterial material;
    private com.google.android.material.button.MaterialButtonToggleGroup modeGroup;
    private boolean selectingMode;
    private boolean routeUnchanged,roadSnapped,roadSnapBusy;
    private boolean roadSnapEnabled;
    private final RoadSnapper roadSnapper=new RoadSnapper();
    private final ExecutorService roadIo=Executors.newSingleThreadExecutor();
    private Future<?> roadTask;
    private int roadGeneration;
    private final Runnable pendingRoadSnap=this::requestRoadSnap;
    private final android.os.Handler checkpointHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Map<RandomPoint,CheckpointPin> checkpointPins=new LinkedHashMap<>();
    private boolean checkpointsVisible;
    private final Runnable checkpointRefresh=new Runnable(){
        @Override public void run(){
            if(!checkpointsVisible||isFinishing()||isDestroyed())return;
            bindStartedRun();updateCheckpoints();checkpointHandler.postDelayed(this,1000);
        }
    };
    private static final class CheckpointPin{
        final Marker marker;final int number;boolean passed;
        CheckpointPin(Marker marker,int number,boolean passed){this.marker=marker;this.number=number;this.passed=passed;}
    }
    private final TextView[] modeButtons=new TextView[3];
    private final ArrayList<SimulationEngine.Point> points=new ArrayList<>();
    private final ArrayDeque<List<SimulationEngine.Point>> history=new ArrayDeque<>();
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private double speed=10,variation=.8,cadence=164,cadenceVariation=6,altitude=30,altitudeVariation=1.5;
    private double routeOffsetMeters=SimulationEngine.Config.DEFAULT_ROUTE_OFFSET_METERS;
    private LatLng deviceAnchor;
    private SimulationEngine.Point routeAnchor;
    private boolean fixed=false,gpxGps=true,follow=false;
    private int mode=BROWSE;
    private int dp(float n) {return SimulationUi.dp(this,n);}

    public static void openPresetEditor(Activity host) {
        host.startActivity(new Intent(host, SimulationActivity.class).putExtra(PRESET_EDITOR, true));
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        ui=new RunnerUi(this); material=new NativeMaterial(this);
        presetEditor = getIntent().getBooleanExtra(PRESET_EDITOR, false);
        if(!PrivacyConsent.isAgreed(this)){finish();return;}
        ui.applySystemBars(this);
        if (!presetEditor) controller=SimulationController.get(this);
        if(!presetEditor&&com.dudu.run.c.n().q()!=0){
            try{routeEdit=controller.beginRouteEdit();}catch(Exception error){Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();}
        }
        roadSnapEnabled=!presetEditor&&getSharedPreferences("map_editor_roads",0).getBoolean("enabled",false);
        if (!presetEditor) readDraft();
        else if (saved != null) points.addAll(RouteIO.parse(saved.getString(PRESET_DRAFT, ""), altitude));
        root=new FrameLayout(this);root.setBackgroundColor(ui.page);setContentView(root);
        try {
            mapView=new ConfigurableMapView(this);map=mapView.getMap();
            map.setMapType(ui.dark ? TencentMap.MAP_TYPE_DARK : TencentMap.MAP_TYPE_NORMAL);
            mapView.setId(R.id.simulation_map);
            FrameLayout mapSurface=new FrameLayout(this){
                @Override public boolean dispatchTouchEvent(MotionEvent event){
                    if(event.getActionMasked()==MotionEvent.ACTION_DOWN)mapTouchHadMultiplePointers=false;
                    if(event.getPointerCount()>1)mapTouchHadMultiplePointers=true;
                    return super.dispatchTouchEvent(event);
                }
            };
            mapSurface.addView(mapView,new FrameLayout.LayoutParams(-1,-1));root.addView(mapSurface,new FrameLayout.LayoutParams(-1,-1));
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.setOnMapClickListener(point->{if(mode==PICK&&!mapTouchHadMultiplePointers)addPoint(point);});
        } catch(Throwable error) {
            map=null;
            String message = presetEditor ? "地图暂不可用，请返回后重试" : "地图暂不可用\n仍可使用坐标输入和 GPX 导入";
            TextView fallback=SimulationUi.text(this,message,16,SimulationUi.MUTED,false);
            fallback.setGravity(Gravity.CENTER);root.addView(fallback,new FrameLayout.LayoutParams(-1,-1));
        }
        drawing=new DrawingLayer();drawing.setId(R.id.simulation_drawing);
        root.addView(drawing,new FrameLayout.LayoutParams(-1,-1));drawing.setVisibility(View.GONE);
        explorer=new MapExplorer(this,map,new MapExplorer.Target(){
            @Override public void beforeMove(){
                follow=false;
                if (presetEditor) drawing.cancelStroke();
                else setMode(BROWSE);
            }
            @Override public boolean append(LatLng point){if(!addPoint(point))return false;setMode(PICK);return true;}
            @Override public void located(LatLng point){deviceAnchor=point;refreshHint();}
        });
        createHeader();createBottomCard();attachLocationButton();
        setMode(presetEditor && map != null ? DRAW : BROWSE);
        drawRoute(presetEditor && !points.isEmpty());
        onSimulationChanged();
        root.post(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (routeEdit != null) focusRun();
            else if (!presetEditor || (map != null && points.isEmpty()
                    && checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED)) explorer.locate();
        });
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (presetEditor) out.putString(PRESET_DRAFT, RouteIO.format(points));
    }
    private void createHeader() {
        int glass=ui.surface;
        LinearLayout row=SimulationUi.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        MaterialButton back=material.iconButton(R.drawable.ic_demo_arrow_left,"返回",this::finish);
        back.setBackgroundTintList(android.content.res.ColorStateList.valueOf(glass));
        back.setElevation(0);
        row.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));

        com.google.android.material.card.MaterialCardView titleCapsule=headerCapsule(glass);
        com.google.android.material.textview.MaterialTextView title=new com.google.android.material.textview.MaterialTextView(material.context());
        title.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        title.setText(presetEditor?"手绘预设路线":"路线工作台");title.setTextColor(ui.ink);
        title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);title.setPadding(dp(12),0,dp(12),0);
        title.setMaxWidth(Math.max(dp(64),getResources().getDisplayMetrics().widthPixels-dp(232)));
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->title.setMaxWidth(Math.max(dp(64),r-l-dp(232))));
        titleCapsule.addView(title,new FrameLayout.LayoutParams(-2,-1));
        // The title is a label; consume touches so they cannot draw on the map.
        titleCapsule.setOnTouchListener((v,event)->true);
        LinearLayout.LayoutParams titlePosition=new LinearLayout.LayoutParams(-2,dp(48));
        titlePosition.setMarginStart(dp(8));titlePosition.setMarginEnd(dp(8));row.addView(titleCapsule,titlePosition);

        com.google.android.material.card.MaterialCardView toolsCapsule=headerCapsule(glass);
        LinearLayout actions=SimulationUi.row(this);
        MaterialButton clear=headerAction(R.drawable.ic_simulation_delete,"清空路线",this::confirmClear);
        clear.setId(R.id.simulation_clear);
        clear.setIconTint(android.content.res.ColorStateList.valueOf(SimulationUi.errorColor(this)));
        actions.addView(clear,new LinearLayout.LayoutParams(dp(48),-1));
        undoButton=headerAction(R.drawable.ic_simulation_undo,"撤销上一步",this::undo);
        undoButton.setId(R.id.simulation_undo);actions.addView(undoButton,new LinearLayout.LayoutParams(dp(48),-1));
        moreButton=headerAction(R.drawable.ic_editor_more_vert,"更多",this::moreTools);
        MaterialButton more=moreButton;
        more.setId(R.id.simulation_more);actions.addView(more,new LinearLayout.LayoutParams(dp(48),-1));
        toolsCapsule.addView(actions,new FrameLayout.LayoutParams(-1,-1));
        row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
        row.addView(toolsCapsule,new LinearLayout.LayoutParams(dp(144),dp(48)));
        place(row,-1,48,Gravity.TOP,12,8,12,0);
    }
    private com.google.android.material.card.MaterialCardView headerCapsule(int color){
        com.google.android.material.card.MaterialCardView card=new com.google.android.material.card.MaterialCardView(material.context());
        card.setRadius(dp(24));card.setCardElevation(0);card.setCardBackgroundColor(color);
        card.setStrokeColor(ui.outline);card.setStrokeWidth(dp(1));card.setPreventCornerOverlap(false);
        card.setUseCompatPadding(false);card.setClipToOutline(true);return card;
    }
    private MaterialButton headerAction(int icon,String label,Runnable action){
        MaterialButton button=material.iconButton(icon,label,action);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        return button;
    }
    private void attachLocationButton(){
        View button=explorer.locationButton();
        place(button,52,52,Gravity.BOTTOM|Gravity.RIGHT,0,0,16,280);
        Runnable layout=()->{
            if(isDestroyed())return;
            FrameLayout.LayoutParams position=(FrameLayout.LayoutParams)button.getLayoutParams();
            int margin=bottomCard.getHeight()+dp(24);
            if(position.bottomMargin!=margin){position.bottomMargin=margin;button.setLayoutParams(position);}
        };
        bottomCard.addOnLayoutChangeListener((v,left,top,right,bottom,oldLeft,oldTop,oldRight,oldBottom)->layout.run());
        bottomCard.post(layout);
    }
    private void moreTools() {
        androidx.appcompat.widget.PopupMenu popup=new androidx.appcompat.widget.PopupMenu(material.context(),moreButton,Gravity.END);
        if(presetEditor){
            popup.getMenu().add(0,1,0,"全览路线");popup.getMenu().add(0,2,1,"地图 Key 设置");
            popup.setOnMenuItemClickListener(item->{if(item.getItemId()==1)drawRoute(true);else MapKeySettings.show(this);return true;});
        }else{
            String[] labels={"运动参数","坐标输入","导入 GPX","道路吸附","椭圆模板","全览路线","全览打卡点","地图 Key 设置","保存当前路径到预设路线","选择预设路线"};
            for(int i=0;i<labels.length;i++){
                android.view.MenuItem item=popup.getMenu().add(0,1000+i,i,labels[i]);
                if(i==3)item.setCheckable(true).setChecked(roadSnapEnabled);
            }
            popup.setOnMenuItemClickListener(item->{
                switch(item.getItemId()-1000){
                    case 0:parameters();break;
                    case 1:coordinatesDialog();break;
                    case 2:importGpx();break;
                    case 3:
                        roadSnapEnabled=!item.isChecked();item.setChecked(roadSnapEnabled);
                        getSharedPreferences("map_editor_roads",0).edit().putBoolean("enabled",roadSnapEnabled).apply();
                        cancelRoadSnap();if(roadSnapEnabled&&!roadSnapped&&points.size()>1)scheduleRoadSnap();break;
                    case 4:oval();break;
                    case 5:follow=false;drawRoute(true);break;
                    case 6:showCheckpoints();break;
                    case 7:MapKeySettings.show(this);break;
                    case 8:savePreset();break;
                    case 9:choosePreset();break;
                }return true;
            });
        }popup.show();
    }
    public void choosePreset(){
        org.json.JSONArray saved=PresetRoutes.read(this);
        if(saved.length()==0){new NativeMaterial(this).dialog().setTitle("预设路线").setMessage("还没有保存的路线").setPositiveButton("知道了",null).show();return;}
        String[] names=new String[saved.length()];for(int i=0;i<names.length;i++)names[i]=saved.optJSONObject(i)==null?"无效路线":saved.optJSONObject(i).optString("name","未命名路线");
        new NativeMaterial(this).dialog().setTitle("选择预设路线").setItems(names,(dialog,index)->{
            try{
                if(!anchorReady())return;
                List<SimulationEngine.Point> selected=RouteIO.parse(saved.getJSONObject(index).optString("coordinates"),altitude);
                if(selected.size()<2)throw new IllegalArgumentException("这条预设路线没有足够的点");
                if(routeEdit!=null&&selected.size()>=5000&&SimulationEngine.distance(routeEdit.anchor,selected.get(0))>.2)throw new IllegalArgumentException("路线点太多，无法连接本次起点");
                remember();points.clear();points.addAll(selected);fixed=false;resetRouteAnchor();anchorRoute();routeChanged(true);
            }catch(Exception error){error(error);}
        }).setNegativeButton("取消",null).show();
    }
    private void confirmClear(){
        if(points.isEmpty()){Toast.makeText(this,"当前没有路线",Toast.LENGTH_SHORT).show();return;}
        new NativeMaterial(this).dialog().setTitle("清空当前路线？").setMessage("清空后仍可使用顶部的撤销恢复。")
            .setNegativeButton("取消",null).setPositiveButton("清空",(d,w)->clearRoute()).show();
    }
    private void createBottomCard() {
        if (presetEditor) {
            createPresetCard();
            return;
        }
        LinearLayout card=SimulationUi.column(this);bottomCard=card;card.setPadding(dp(14),dp(12),dp(14),dp(12));
        addRouteSearch(card);
        LinearLayout heading=SimulationUi.row(this);
        routeSummary=SimulationUi.text(this,"尚未设置路线",14,SimulationUi.NAVY,true);
        routeSummary.setSingleLine(false);routeSummary.setEllipsize(null);
        routeSummary.setId(R.id.simulation_route_summary);heading.addView(routeSummary,new LinearLayout.LayoutParams(0,-2,1));
        com.google.android.material.chip.Chip statusChip=new com.google.android.material.chip.Chip(material.context());
        statusChip.setText("待开始");statusChip.setTextSize(10);statusChip.setTextColor(ui.accentText);
        statusChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(ui.tone));
        statusChip.setCheckable(false);statusChip.setClickable(false);statusChip.setEnsureMinTouchTargetSize(false);statusChip.setChipMinHeight(dp(24));
        statusChip.setMaxWidth(dp(120));statusChip.setEllipsize(android.text.TextUtils.TruncateAt.END);state=statusChip;
        MaterialButton presets=material.tonalButton("预设路线",this::choosePreset);
        presets.setTextSize(12);presets.setSingleLine(true);presets.setEllipsize(null);
        presets.setMinWidth(dp(88));presets.setMinimumWidth(dp(88));
        presets.setMinHeight(dp(48));presets.setMinimumHeight(dp(48));
        presets.setInsetTop(0);presets.setInsetBottom(0);presets.setPadding(dp(12),0,dp(12),0);
        LinearLayout.LayoutParams presetPosition=new LinearLayout.LayoutParams(-2,-2);
        presetPosition.setMarginStart(dp(10));heading.addView(presets,presetPosition);
        card.addView(heading);SimulationUi.gap(card,9);
        card.addView(modePicker());SimulationUi.gap(card,5);
        hint=SimulationUi.text(this,"",11,SimulationUi.MUTED,false);hint.setMaxLines(2);hint.setPadding(dp(2),dp(2),dp(2),dp(2));
        checkpointSummary=SimulationUi.text(this,"",11,SimulationUi.TEAL,true);
        checkpointSummary.setPadding(dp(2),dp(3),dp(2),dp(3));checkpointSummary.setVisibility(View.GONE);
        checkpointSummary.setOnClickListener(v->showCheckpoints());card.addView(checkpointSummary);
        parameterSummary=SimulationUi.text(this,"",12,SimulationUi.TEAL,false);parameterSummary.setId(R.id.simulation_settings);parameterSummary.setPadding(dp(2),dp(6),dp(2),dp(6));
        parameterSummary.setOnClickListener(v->parameters());
        LinearLayout buttons=SimulationUi.row(this);
        startButton=SimulationUi.button(this,"先开始跑步",SimulationUi.NAVY,Color.WHITE,()->startSimulation());
        startButton.setId(R.id.simulation_start);buttons.addView(startButton,new LinearLayout.LayoutParams(-1,dp(44)));
        pauseButton=SimulationUi.button(this,"暂停",SimulationUi.NAVY,Color.WHITE,()->{if(controller.running())controller.pause();else controller.resume();});
        stopButton=SimulationUi.button(this,"停止",0xffFFF0F1,SimulationUi.RED,()->controller.stop());
        LinearLayout.LayoutParams stopLayout=new LinearLayout.LayoutParams(dp(70),dp(44));stopLayout.leftMargin=dp(6);
        card.addView(buttons);showPanel(card);
    }

    private LinearLayout modePicker() {
        modeGroup = material.segmentedButtons();
        String[] names = {"浏览", "选点", "手绘"};
        int[] ids = {R.id.simulation_mode_browse, R.id.simulation_mode_pick, R.id.simulation_mode_draw};
        for(int index=0;index<names.length;index++) {
            MaterialButton button=material.segment(names[index]);button.setId(ids[index]);button.setTextSize(13);
            button.setMinWidth(0);button.setMinimumWidth(0);button.setPadding(dp(6),0,dp(6),0);
            modeButtons[index]=button;modeGroup.addView(button,new LinearLayout.LayoutParams(0,dp(44),1));
        }
        modeGroup.addOnButtonCheckedListener((group,id,checked)->{
            if(selectingMode || !checked)return;
            for(int index=0;index<ids.length;index++)if(ids[index]==id){setMode(index);break;}
        });
        return modeGroup;
    }

    private void selectModeButton() {
        if(modeGroup==null)return;
        selectingMode=true;modeGroup.check(modeButtons[mode].getId());selectingMode=false;
    }

    private void showPanel(LinearLayout body) {
        com.google.android.material.card.MaterialCardView panel=new com.google.android.material.card.MaterialCardView(material.context());
        panel.setCardBackgroundColor(ui.surface);panel.setStrokeWidth(dp(1));panel.setStrokeColor(ui.outline);
        panel.setRadius(dp(24));panel.setCardElevation(dp(5));panel.setPreventCornerOverlap(false);panel.setUseCompatPadding(false);
        // Children keep their normal input handling; unused panel space owns the
        // remaining gesture instead of falling through to the drawing/map layers.
        panel.setOnTouchListener((view,event)->true);
        panel.addView(body,new android.widget.FrameLayout.LayoutParams(-1,-2));
        place(panel,-1,-2,Gravity.BOTTOM,12,0,12,12);
    }

    private void createPresetCard() {
        RunnerUi ui = new RunnerUi(this);
        LinearLayout card = ui.column();
        bottomCard = card;
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        addRouteSearch(card);
        routeSummary = ui.text("", 14, ui.ink, true);
        card.addView(routeSummary);
        ui.gap(card, 8);
        card.addView(modePicker());
        hint = ui.text("", 12, ui.muted, false);
        ui.gap(card, 4);
        card.addView(new NativeMaterial(this).button("保存到预设路线", this::savePreset));
        showPanel(card);
    }

    private void addRouteSearch(LinearLayout card) {
        card.addView(explorer.bar(), new LinearLayout.LayoutParams(-1, dp(48)));
        SimulationUi.gap(card, 6);
    }

    private void savePreset() {
        if (fixed || points.size() < 2) {
            Toast.makeText(this, "请先画出一段路线", Toast.LENGTH_SHORT).show();
            return;
        }
        PresetRoutes.nameAndSave(this, points, () -> {
            Toast.makeText(this, "已保存到预设路线库", Toast.LENGTH_SHORT).show();
            if (presetEditor) finish();
        });
    }
    private void place(View view,int width,int height,int gravity,int left,int top,int right,int bottom) {
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(width<0?width:dp(width),height<0?height:dp(height),gravity);
        p.setMargins(dp(left),dp(top),dp(right),dp(bottom));root.addView(view,p);
    }
    private void setMode(int value) {
        if(value!=BROWSE&&!anchorReady()){selectModeButton();return;}
        if(drawing!=null)drawing.cancelStroke();
        explorer.cancelLocation();
        mode=value;follow=false;
        selectModeButton();
        drawing.setVisibility(mode==DRAW?View.VISIBLE:View.GONE);
        if(map!=null)map.getUiSettings().setAllGesturesEnabled(true);
        if(mode==DRAW)fixed=false;
        refreshHint();
        persistDraft();refreshSummary();
    }
    private void bindStartedRun(){
        if(routeEdit!=null||com.dudu.run.c.n().q()==0||!controller.enabled())return;
        try{
            cancelRoadSnap();persistDraft();routeEdit=controller.beginRouteEdit();points.clear();history.clear();
            readDraft();setMode(BROWSE);drawRoute(false);onSimulationChanged();focusRun();
        }catch(Exception error){state.setText("请重新打开工作台");}
    }
    private void focusRun(){
        if(map==null||routeEdit==null)return;
        if(!currentCheckpoints().isEmpty())showCheckpoints();
        else map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(routeEdit.anchor.latitude,routeEdit.anchor.longitude),17));
    }
    private void refreshHint(){
        if(hint==null)return;
        if (presetEditor) {
            hint.setText(mode == DRAW ? "单指手绘，双指缩放"
                    : mode == PICK ? "点按地图添加路线点" : "移动或缩放地图，选择起点区域");
            return;
        }
        if(routeEdit!=null){
            hint.setText(mode==DRAW?"计时已暂停 · 单指画线，双指缩放；完成后自动继续":
                mode==PICK?"计时已暂停 · 单指选点，双指缩放；保留当前剩余路线":
                "计时与位置已暂停；可修改现有路线和参数后继续");return;
        }
        hint.setText(deviceAnchor==null?"定位成功后可先开始跑步，再手动画线":
            "先按原流程开始跑步，获取打卡点后在此画线");
    }
    private boolean anchorReady(){
        if (presetEditor) return map != null;
        if(routeEdit!=null){
            if(controller.isCurrentEdit(routeEdit))return true;
            Toast.makeText(this,"本次跑步已变化，请重新打开工作台",Toast.LENGTH_LONG).show();return false;
        }
        if(deviceAnchor!=null)return true;
        Toast.makeText(this,"请先定位当前位置，再设置路线起点",Toast.LENGTH_LONG).show();
        if(explorer!=null)explorer.locate();return false;
    }
    private int anchorSlots(){
        if (presetEditor) return 0;
        SimulationEngine.Point start=routeAnchor!=null?routeAnchor:new SimulationEngine.Point(deviceAnchor.latitude,deviceAnchor.longitude,altitude);
        return points.isEmpty()||SimulationEngine.distance(start,points.get(0))>.2?1:0;
    }
    private void resetRouteAnchor(){routeAnchor=routeEdit==null?null:routeEdit.anchor;}
    private void anchorRoute(){
        if (presetEditor) return;
        if(routeAnchor==null)routeAnchor=new SimulationEngine.Point(deviceAnchor.latitude,deviceAnchor.longitude,altitude);
        if(points.isEmpty())points.add(routeAnchor);
        else if(SimulationEngine.distance(routeAnchor,points.get(0))<=.2)points.set(0,routeAnchor);
        else {
            if(points.size()>=5000)throw new IllegalArgumentException("添加定位起点后超过 5000 个点，请减少路线点");
            points.add(0,routeAnchor);
        }
    }
    private void remember() {
        history.push(new ArrayList<>(points));while(history.size()>20)history.removeLast();
    }
    private boolean addPoint(LatLng ll) {
        try {
            SimulationEngine.Point point=new SimulationEngine.Point(ll.latitude,ll.longitude,altitude);
            if(!anchorReady())return false;
            fixed=false;
            int extra=anchorSlots()+1;
            if(points.size()+extra>5000)throw new IllegalArgumentException("最多 5000 个路线点（含定位起点）");
            
            remember();anchorRoute();
            if(points.isEmpty()||SimulationEngine.distance(points.get(points.size()-1),point)>.02)points.add(point);
            routeChanged(false);return true;
        } catch(Exception error){error(error);return false;}
    }
    private void undo() {
        if(history.isEmpty()){
            if(points.size()>1){points.remove(points.size()-1);routeChanged(false);return;}
            Toast.makeText(this,"没有可撤销的操作",Toast.LENGTH_SHORT).show();return;
        }
        points.clear();points.addAll(history.pop());routeChanged(false);
    }
    private void clearRoute() {if(points.isEmpty())return;remember();points.clear();resetRouteAnchor();routeChanged(false);}
    private void routeChanged(boolean fit) {
        cancelRoadSnap();roadSnapped=false;routeUnchanged=false;persistDraft();drawRoute(fit);refreshSummary();scheduleRoadSnap();
    }
    private void cancelRoadSnap(){
        roadGeneration++;checkpointHandler.removeCallbacks(pendingRoadSnap);
        if(roadTask!=null){roadTask.cancel(true);roadTask=null;}roadSnapper.cancel();roadSnapBusy=false;
    }
    private void scheduleRoadSnap(){
        checkpointHandler.removeCallbacks(pendingRoadSnap);
        if(roadSnapEnabled&&!presetEditor&&!fixed&&points.size()>1)checkpointHandler.postDelayed(pendingRoadSnap,1000);
    }
    private void requestRoadSnap(){
        if(!roadSnapEnabled||presetEditor||fixed||points.size()<2||isFinishing()||isDestroyed())return;
        cancelRoadSnap();final int generation=roadGeneration;
        final ArrayList<SimulationEngine.Point> original=new ArrayList<>(points);
        final SimulationController.RouteEdit edit=routeEdit;roadSnapBusy=true;
        roadTask=roadIo.submit(()->{
            try{
                List<SimulationEngine.Point> result=roadSnapper.snap(getApplicationContext(),original);
                if(result.size()>5000)throw new IllegalArgumentException("道路点超过 5000 个，已保留原线");
                runOnUiThread(()->{
                    if(generation!=roadGeneration||isFinishing()||isDestroyed())return;
                    roadTask=null;roadSnapBusy=false;
                    if(routeEdit!=edit||(edit!=null&&!controller.isCurrentEdit(edit)))return;
                    remember();points.clear();points.addAll(result);roadSnapped=true;
                    persistDraft();drawRoute(false);refreshSummary();
                });
            }catch(Exception error){runOnUiThread(()->{
                if(generation!=roadGeneration||isFinishing()||isDestroyed())return;
                roadTask=null;roadSnapBusy=false;
                Toast.makeText(this,error.getMessage()==null?"道路吸附失败，已保留原线":error.getMessage(),Toast.LENGTH_LONG).show();
            });}
        });
    }
    private double length() {
        if(fixed || points.size()<2)return 0;
        double value=0;for(int i=1;i<points.size();i++)value+=SimulationEngine.distance(points.get(i-1),points.get(i));
        return value;
    }
    private void refreshDistance(double meters){
        if(routeSummary==null)return;
        if (presetEditor) {
            routeSummary.setText(String.format(Locale.CHINA, "路线 %.3f km", meters / 1000));
            return;
        }
        double recorded=routeEdit==null?0:com.dudu.run.c.n().r();
        String text=String.format(Locale.CHINA,"路线 %.3f km\n本次预计 %.3f km",meters/1000,(recorded+meters)/1000);
        if(!OfflineTestSession.active()){
            double minimum=com.dudu.run.b.c().g(),maximum=com.dudu.run.b.c().e();
            if(minimum>0)text+=String.format(Locale.CHINA,"\n目标 %.2f～%.2f km · %s",minimum/1000,maximum/1000,
                recorded+meters<minimum?"还差 "+String.format(Locale.CHINA,"%.3f km",(minimum-recorded-meters)/1000):
                maximum>0&&recorded+meters>maximum?"超过目标上限":"预计距离达标");
        }
        routeSummary.setText(text);
    }
    private void refreshSummary() {
        if(routeSummary==null)return;
        refreshDistance(length());
        if(undoButton!=null){boolean available=!history.isEmpty()||points.size()>1;undoButton.setEnabled(available);undoButton.setIconTint(android.content.res.ColorStateList.valueOf(available?ui.accentText:ui.muted));}
    }
    private SimulationEngine.Config config() {
        return new SimulationEngine.Config(points,speed,variation,cadence,cadenceVariation,altitudeVariation,routeOffsetMeters,false,fixed);
    }
    private void startSimulation() {
        try {
            if(routeEdit!=null){
                if(!anchorReady())return;
                if(points.isEmpty())throw new IllegalArgumentException("请先选点或手绘路线");
                if(fixed)throw new IllegalArgumentException("请关闭定点模式后画线");
                if(roadSnapEnabled&&!roadSnapped&&!routeUnchanged){
                    if(roadSnapBusy)throw new IllegalArgumentException("道路吸附中，请稍后再完成");
                    checkpointHandler.removeCallbacks(pendingRoadSnap);requestRoadSnap();
                    throw new IllegalArgumentException("正在吸附道路，完成后再点继续；也可在更多菜单关闭道路吸附");
                }
                remember();anchorRoute();persistDraft();controller.applyToRun(routeEdit,config());
                routeApplied=true;finish();return;
            }
            if(com.dudu.run.c.n().q()!=0)throw new IllegalStateException("请重新打开工作台以连接本次跑步");
            if(OfflineTestSession.active()){
                if(!anchorReady())return;
                OfflineTestSession.startWaitingRun(this,new SimulationEngine.Point(deviceAnchor.latitude,deviceAnchor.longitude,altitude));
            }else{
                persistDraft();Toast.makeText(this,"请按原流程开始跑步，获取打卡点后会打开画线工作台",Toast.LENGTH_LONG).show();finish();
            }
        }catch(Exception e){error(e);}
    }
    private void drawRoute(boolean fit) {
        if(map==null){refreshSummary();return;}
        try {
            map.clear();moving=null;checkpointPins.clear();if(explorer!=null)explorer.mapCleared();
            if(points.isEmpty()){
                if(routeEdit!=null)map.addMarker(new MarkerOptions().position(new LatLng(routeEdit.anchor.latitude,routeEdit.anchor.longitude))
                    .title("本次跑步起点（位置保持）").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                if(explorer!=null)explorer.redrawMarkers();updateCheckpoints();return;
            }
            LatLngBounds.Builder bounds=new LatLngBounds.Builder();
            PolylineOptions path=new PolylineOptions().color(ui.accent).width(dp(4));
            int count=fixed?1:points.size();
            for(int i=0;i<count;i++){
                SimulationEngine.Point p=points.get(i);LatLng ll=new LatLng(p.latitude,p.longitude);path.add(ll);bounds.include(ll);
            }
            if(count>1)map.addPolyline(path);
            SimulationEngine.Point first=points.get(0);
            map.addMarker(new MarkerOptions().position(new LatLng(first.latitude,first.longitude)).title(fixed?"定点":"起点")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if(count>1){
                SimulationEngine.Point last=points.get(count-1);
                map.addMarker(new MarkerOptions().position(new LatLng(last.latitude,last.longitude)).title("末点")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            }
            if(fit)mapView.post(()->{
                if(map==null||isDestroyed()||(!presetEditor&&mode==DRAW))return;
                if(count==1)map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(first.latitude,first.longitude),17));
                else map.moveCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds.build(),dp(30),dp(30),dp(130),bottomCard==null?dp(230):bottomCard.getHeight()+dp(24)));
            });
            updateMarker();if(explorer!=null)explorer.redrawMarkers();updateCheckpoints();
        }catch(Exception e){error(e);}
    }
    /** Reads current run checkpoints without taking over the recorder listener or modifying pass flags. */
    private List<RandomPoint> currentCheckpoints(){
        ArrayList<RandomPoint> result=new ArrayList<>();
        if(presetEditor||com.dudu.run.c.n().q()==0)return result;
        for(RandomPoint point:new ArrayList<>(com.dudu.run.c.n().o())){
            if(point==null)continue;
            double lat=point.a(),lon=point.b();
            if(Double.isNaN(lat)||Double.isNaN(lon)||Double.isInfinite(lat)||Double.isInfinite(lon)||Math.abs(lat)>85||Math.abs(lon)>180)continue;
            result.add(point);
        }
        return result;
    }
    private BitmapDescriptor checkpointIcon(int number,boolean passed){
        int size=dp(34);Bitmap bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG);
        brush.setColor(Color.WHITE);canvas.drawCircle(size/2f,size/2f,dp(16),brush);
        brush.setColor(passed?SimulationUi.TEAL:0xffD78312);canvas.drawCircle(size/2f,size/2f,dp(14),brush);
        brush.setColor(Color.WHITE);brush.setTypeface(Typeface.DEFAULT_BOLD);brush.setTextAlign(Paint.Align.CENTER);
        brush.setTextSize(dp(number>=100?10:13));Paint.FontMetrics metrics=brush.getFontMetrics();
        canvas.drawText(String.valueOf(number),size/2f,size/2f-(metrics.ascent+metrics.descent)/2,brush);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void updateCheckpoints(){
        if(map==null||checkpointSummary==null)return;
        try{
            List<RandomPoint> current=currentCheckpoints();Set<RandomPoint> present=new HashSet<>(current);
            Iterator<Map.Entry<RandomPoint,CheckpointPin>> old=checkpointPins.entrySet().iterator();
            while(old.hasNext()){
                Map.Entry<RandomPoint,CheckpointPin> item=old.next();
                if(!present.contains(item.getKey())){item.getValue().marker.remove();old.remove();}
            }
            int passed=0,covered=0;
            for(int i=0;i<current.size();i++){
                RandomPoint point=current.get(i);int number=i+1;boolean done=point.c();if(done)passed++;
                if(done||CheckpointGeometry.covered(new SimulationEngine.Point(point.a(),point.b(),0),points))covered++;
                CheckpointPin pin=checkpointPins.get(point);
                if(pin!=null&&pin.number!=number){pin.marker.remove();checkpointPins.remove(point);pin=null;}
                String label="打卡点 "+number+" · "+(done?"已通过":"待通过");
                if(pin==null){
                    map.addCircle(new CircleOptions().center(new LatLng(point.a(),point.b())).radius(CheckpointGeometry.RADIUS_METERS)
                        .fillColor(0x1500A594).strokeColor(0x7000A594).strokeWidth(dp(1)));
                    Marker marker=map.addMarker(new MarkerOptions().position(new LatLng(point.a(),point.b())).anchor(.5f,.5f)
                        .title(label).snippet("本次跑步打卡点；编号仅为显示顺序")
                        .icon(checkpointIcon(number,done)));
                    checkpointPins.put(point,new CheckpointPin(marker,number,done));
                }else if(pin.passed!=done){
                    pin.marker.setIcon(checkpointIcon(number,done));pin.marker.setTitle(label);pin.passed=done;
                }
            }
            checkpointSummary.setVisibility(current.isEmpty()?View.GONE:View.VISIBLE);
            checkpointSummary.setText("已过 "+passed+"/"+current.size()+" · 路线覆盖 "+covered+"/"+current.size());
        }catch(Exception ignored){if(checkpointSummary!=null){checkpointSummary.setVisibility(View.VISIBLE);checkpointSummary.setText("打卡点暂不可用，返回跑步页查看");}}
    }
    private void showCheckpoints(){
        if(map==null)return;
        List<RandomPoint> current=currentCheckpoints();
        if(current.isEmpty()){Toast.makeText(this,"本次跑步尚无可显示的打卡点",Toast.LENGTH_LONG).show();return;}
        setMode(BROWSE);updateCheckpoints();
        LatLngBounds.Builder bounds=new LatLngBounds.Builder();
        for(RandomPoint point:current)bounds.include(new LatLng(point.a(),point.b()));
        boolean separateAnchor=routeEdit!=null&&SimulationEngine.distance(routeEdit.anchor,new SimulationEngine.Point(current.get(0).a(),current.get(0).b(),0))>.2;
        if(routeEdit!=null)bounds.include(new LatLng(routeEdit.anchor.latitude,routeEdit.anchor.longitude));
        if(current.size()==1&&!separateAnchor)map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.get(0).a(),current.get(0).b()),17));
        else map.moveCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds.build(),dp(32),dp(32),dp(130),bottomCard.getHeight()+dp(24)));
    }
    private void updateMarker(){
        if(presetEditor||routeEdit!=null)return;
        if(map==null||!controller.enabled()||controller.sample()==null)return;
        SimulationEngine.Sample s=controller.sample();LatLng ll=new LatLng(s.point.latitude,s.point.longitude);
        if(moving==null)moving=map.addMarker(new MarkerOptions().position(ll).title("模拟位置")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        else moving.setPosition(ll);
        if(follow)map.moveCamera(CameraUpdateFactory.newLatLng(ll));
    }
    @Override public void onSimulationChanged(){
        if(state==null)return;SimulationEngine.Sample sample=controller.sample();boolean enabled=controller.enabled();
        state.setText(enabled?controller.state():"待开始");
        boolean editing=routeEdit!=null;
        startButton.setText(editing?"完成并继续":OfflineTestSession.active()?"先开始离线跑步":"返回开始跑步");
        startButton.setVisibility(View.VISIBLE);startButton.setEnabled(!editing||controller.isCurrentEdit(routeEdit));
        startButton.setAlpha(startButton.isEnabled()?1:.45f);
        pauseButton.setVisibility(enabled&&!editing?View.VISIBLE:View.GONE);stopButton.setVisibility(enabled&&!editing?View.VISIBLE:View.GONE);
        pauseButton.setText(controller.running()?"暂停":"继续");
        double liveSpeed=sample!=null&&enabled?(controller.running()?sample.speedKmh:0):speed;
        double liveCadence=sample!=null&&enabled?(controller.running()?sample.cadence:0):cadence;
        double liveAltitude=sample!=null&&enabled?sample.point.altitude:altitude;
        parameterSummary.setText(String.format(Locale.CHINA,"%.1f km/h  ·  %.0f 步/分  ·  %.0f m   参数 ›",liveSpeed,liveCadence,liveAltitude));
        updateMarker();
    }
    private void parameters(){
        LinearLayout page=SimulationUi.column(this);page.setPadding(dp(18),dp(18),dp(18),dp(18));
        page.addView(SimulationUi.text(this,"运动参数",20,SimulationUi.NAVY,true));
        EditText[] fields=new EditText[7];double[] values={speed,variation,cadence,cadenceVariation,altitude,altitudeVariation,routeOffsetMeters};
        LinearLayout basics=SimulationUi.row(this);
        parameterField(basics,fields,0,"速度 · km/h",values[0]);parameterField(basics,fields,2,"步频 · 步/分",values[2]);page.addView(basics);
        LinearLayout heightRow=SimulationUi.row(this);parameterField(heightRow,fields,4,"默认海拔 · m",values[4]);
        parameterField(heightRow,fields,6,"路线偏移 ±m",values[6]);page.addView(heightRow);
        page.addView(SimulationUi.text(this,"默认偏移 1.2 米，0 关闭，最大 5 米；平滑变化，起终点归零。",11,SimulationUi.MUTED,false));
        LinearLayout advanced=SimulationUi.column(this);advanced.setVisibility(View.GONE);
        TextView toggle=SimulationUi.button(this,"高级设置  ▾",SimulationUi.PALE,SimulationUi.MUTED,()->{});
        toggle.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);page.addView(toggle,new LinearLayout.LayoutParams(-1,dp(42)));
        toggle.setOnClickListener(v->{boolean open=advanced.getVisibility()!=View.VISIBLE;advanced.setVisibility(open?View.VISIBLE:View.GONE);toggle.setText(open?"收起高级设置  ▴":"高级设置  ▾");});
        LinearLayout variations=SimulationUi.row(this);
        parameterField(variations,fields,1,"速度波动 ±",values[1]);parameterField(variations,fields,3,"步频波动 ±",values[3]);parameterField(variations,fields,5,"高度起伏 ±m",values[5]);advanced.addView(variations);
        CheckBox fixedBox=check(advanced,"定点模式（不沿路线运动）",fixed);
        if(routeEdit!=null){fixedBox.setChecked(false);fixedBox.setEnabled(false);}
        CheckBox gpsBox=check(advanced,"GPX 坐标为 WGS-84，导入时转换",gpxGps);page.addView(advanced);
        SimulationUi.gap(page,9);page.addView(SimulationUi.text(this,"点击应用路线后生效；步频仅本机模拟。默认海拔为示例值，请按所在地调整。",11,SimulationUi.MUTED,false));
        SimulationUi.gap(page,12);Dialog dialog=sheet(page);
        page.addView(SimulationUi.button(this,"保存参数",SimulationUi.NAVY,Color.WHITE,()->{
            try{
                double a=number(fields[0]),b=number(fields[1]),c=number(fields[2]),d=number(fields[3]),e=number(fields[4]),f=number(fields[5]),g=number(fields[6]);
                new SimulationEngine.Config(Collections.singletonList(new SimulationEngine.Point(0,0,e)),a,b,c,d,f,g,false,true);
                speed=a;variation=b;cadence=c;cadenceVariation=d;altitude=e;altitudeVariation=f;routeOffsetMeters=g;
                fixed=fixedBox.isChecked();gpxGps=gpsBox.isChecked();
                persistDraft();drawRoute(false);refreshSummary();onSimulationChanged();dialog.dismiss();
            }catch(Exception error){error(error);}
        }),new LinearLayout.LayoutParams(-1,dp(44)));dialog.show();sizeSheet(dialog);
    }
    private void parameterField(LinearLayout row,EditText[] fields,int index,String label,double value){
        LinearLayout cell=SimulationUi.column(this);cell.setPadding(dp(4),dp(10),dp(4),0);
        EditText field=material.field(cell,label,InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED,false);
        field.setText(String.format(Locale.US,"%.1f",value));field.setTextSize(17);fields[index]=field;SimulationUi.weighted(row,cell,84);
    }

    private CheckBox check(LinearLayout page,String text,boolean checked){
        CheckBox box=new com.google.android.material.checkbox.MaterialCheckBox(material.context());box.setText(text);box.setTextSize(12);box.setTextColor(ui.ink);box.setChecked(checked);page.addView(box);return box;
    }
    private Dialog sheet(LinearLayout page){
        Dialog dialog=new NativeMaterial(this).sheet();
        ScrollView scroll=new CappedScrollView();scroll.addView(page);
        dialog.setContentView(scroll);dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setGravity(Gravity.BOTTOM);dialog.getWindow().setDimAmount(.25f);return dialog;
    }
    private void sizeSheet(Dialog dialog){
        SimulationUi.styleSheet(this,dialog);
        Window w=dialog.getWindow();
        ((BottomSheetDialog)dialog).getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    private final class CappedScrollView extends ScrollView{
        CappedScrollView(){super(SimulationActivity.this);}
        @Override protected void onMeasure(int width,int height){
            int cap=Math.max(dp(160),getResources().getDisplayMetrics().heightPixels-dp(80));
            if(MeasureSpec.getMode(height)!=MeasureSpec.UNSPECIFIED)cap=Math.min(cap,MeasureSpec.getSize(height));
            super.onMeasure(width,MeasureSpec.makeMeasureSpec(cap,MeasureSpec.AT_MOST));
        }
    }
    private void coordinatesDialog(){
        LinearLayout page=SimulationUi.column(this);page.setPadding(dp(20),dp(20),dp(20),dp(20));
        page.addView(SimulationUi.text(this,"坐标与路线",21,SimulationUi.NAVY,true));
        page.addView(SimulationUi.text(this,"每行：纬度,经度,高度（高度可省略）\n坐标使用腾讯 GCJ-02。",12,SimulationUi.MUTED,false));
        EditText text=material.field(page,"路线坐标",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE,true);
        text.setGravity(Gravity.TOP);text.setMinLines(8);text.setTextSize(13);text.setText(RouteIO.format(points));
        Dialog dialog=sheet(page);
        page.addView(SimulationUi.button(this,"应用到地图",SimulationUi.NAVY,Color.WHITE,()->{
            try{List<SimulationEngine.Point> parsed=RouteIO.parse(text.getText().toString(),altitude);remember();points.clear();points.addAll(parsed);resetRouteAnchor();routeChanged(true);dialog.dismiss();}
            catch(Exception error){error(error);}
        }),new LinearLayout.LayoutParams(-1,dp(48)));dialog.show();sizeSheet(dialog);
    }
    private void oval(){
        if(points.isEmpty()){error(new IllegalArgumentException("先点击地图选一个中心点，或用“坐标”输入中心点"));return;}
        try{
            SimulationEngine.Point center=points.get(0);ArrayList<SimulationEngine.Point> ring=new ArrayList<>();
            for(int i=0;i<=48;i++){double a=2*Math.PI*i/48;ring.add(new SimulationEngine.Point(center.latitude+70*Math.cos(a)/111195,
                    center.longitude+55*Math.sin(a)/(111195*Math.cos(Math.toRadians(center.latitude))),center.altitude));}
            remember();points.clear();points.addAll(ring);fixed=false;resetRouteAnchor();routeChanged(true);
        }catch(Exception e){error(e);}
    }
    private void importGpx(){startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),IMPORT_GPX);}
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=IMPORT_GPX||result!=RESULT_OK||data==null||data.getData()==null)return;
        android.net.Uri uri=data.getData();boolean convert=gpxGps;double height=altitude;
        Toast.makeText(this,"正在导入 GPX",Toast.LENGTH_SHORT).show();
        io.execute(()->{
            try(InputStream input=getContentResolver().openInputStream(uri)){
                if(input==null)throw new IllegalArgumentException("无法读取 GPX");
                List<SimulationEngine.Point> imported=RouteIO.readGpx(this,input,convert,height);
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){remember();points.clear();points.addAll(imported);resetRouteAnchor();routeChanged(true);}});
            }catch(Exception e){runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())error(e);});}
        });
    }
    private void persistDraft(){
        if (presetEditor || controller == null) return;
        SimulationEngine.Config parameters=new SimulationEngine.Config(Collections.singletonList(new SimulationEngine.Point(0,0,altitude)),
            speed,variation,cadence,cadenceVariation,altitudeVariation,routeOffsetMeters,false,true);
        controller.save(parameters);
        SimulationPreferences.store(this).edit().putFloat("altitude",(float)altitude).putBoolean("fixed",fixed).putBoolean("gpxGps",gpxGps).apply();
        if(routeEdit!=null&&controller.isCurrentEdit(routeEdit))RunRouteDraft.save(routeEdit,points,roadSnapped,routeUnchanged);
    }
    private void useParameters(SimulationEngine.Config c){
        if(c==null)return;speed=c.speedKmh;variation=c.speedVariationKmh;cadence=c.cadence;cadenceVariation=c.cadenceVariation;
        altitude=c.points.get(0).altitude;altitudeVariation=c.altitudeVariation;routeOffsetMeters=c.routeOffsetMeters;
    }
    private void readDraft(){
        useParameters(controller.load());
        android.content.SharedPreferences settings=SimulationPreferences.store(this);
        fixed=routeEdit==null&&settings.getBoolean("fixed",false);gpxGps=settings.getBoolean("gpxGps",true);
        
        if(routeEdit!=null){
            routeAnchor=routeEdit.anchor;deviceAnchor=new LatLng(routeEdit.anchor.latitude,routeEdit.anchor.longitude);
            if(RunRouteDraft.matches(routeEdit)){
                points.addAll(RunRouteDraft.load(routeEdit));roadSnapped=RunRouteDraft.snapped(routeEdit);routeUnchanged=RunRouteDraft.accepted(routeEdit);
            }else if(routeEdit.remaining.size()>1){points.addAll(routeEdit.remaining);routeUnchanged=true;}
        }
    }
    private double number(EditText text){return Double.parseDouble(text.getText().toString().trim());}
    private void error(Throwable e){new NativeMaterial(this).dialog().setTitle("请检查设置").setMessage(e.getMessage()==null?"操作未完成":e.getMessage()).setPositiveButton("知道了",null).show();}
    @Override protected void onResume(){super.onResume();
        if (controller != null) {
            bindStartedRun();controller.addListener(this);onSimulationChanged();
            checkpointsVisible=true;checkpointHandler.removeCallbacks(checkpointRefresh);checkpointHandler.post(checkpointRefresh);
        }
        if(mapView!=null)mapView.onResume();
        if(roadSnapEnabled&&!roadSnapped&&!routeUnchanged)scheduleRoadSnap();
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if(explorer!=null&&event.getActionMasked()==MotionEvent.ACTION_DOWN)explorer.dismissSearchOutside(event.getRawX(),event.getRawY());
        return super.dispatchTouchEvent(event);
    }
    @Override protected void onPause(){cancelRoadSnap();if(explorer!=null)explorer.hideSearch();checkpointsVisible=false;checkpointHandler.removeCallbacks(checkpointRefresh);if(drawing!=null)drawing.cancelStroke();persistDraft();if(controller!=null)controller.removeListener(this);if(mapView!=null)mapView.onPause();super.onPause();}
    @Override protected void onStart(){super.onStart();if(mapView!=null)mapView.onStart();}
    @Override protected void onStop(){if(explorer!=null)explorer.cancelLocation();if(mapView!=null)mapView.onStop();super.onStop();}
    @Override protected void onRestart(){super.onRestart();if(mapView!=null)mapView.onRestart();}
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);if(explorer!=null)explorer.permissionResult(code);}
    @Override protected void onDestroy(){cancelRoadSnap();roadIo.shutdownNow();if(isFinishing()&&!routeApplied&&routeEdit!=null)controller.cancelRouteEdit(routeEdit);checkpointsVisible=false;checkpointHandler.removeCallbacksAndMessages(null);if(explorer!=null)explorer.close();io.shutdownNow();if(mapView!=null)mapView.onDestroy();super.onDestroy();}
    private final class DrawingLayer extends View{
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path=new Path();
        private final ArrayList<SimulationEngine.Point> stroke=new ArrayList<>();
        private float lastX,lastY;
        private double strokeBaseMeters,strokeMeters;
        private SimulationEngine.Point strokeOrigin;
        private boolean active;
        private int pointer=-1;
        private final RouteGestureState gestures=new RouteGestureState();
        private boolean mapGesture;
        private long mapDownTime;
        private MotionEvent lastMapEvent;
        DrawingLayer(){super(SimulationActivity.this);paint.setColor(ui.accent);paint.setStrokeWidth(dp(4));paint.setStyle(Paint.Style.STROKE);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.ROUND);}
        private void clearStroke(){active=false;pointer=-1;stroke.clear();path.reset();invalidate();refreshSummary();getParent().requestDisallowInterceptTouchEvent(false);}
        void cancelStroke(){endMapGesture();gestures.reset();clearStroke();}
        private void forwardToMap(MotionEvent event){
            int count=event.getPointerCount();MotionEvent.PointerProperties[] properties=new MotionEvent.PointerProperties[count];
            MotionEvent.PointerCoords[] coordinates=new MotionEvent.PointerCoords[count];
            for(int i=0;i<count;i++){properties[i]=new MotionEvent.PointerProperties();coordinates[i]=new MotionEvent.PointerCoords();
                event.getPointerProperties(i,properties[i]);event.getPointerCoords(i,coordinates[i]);}
            MotionEvent translated=MotionEvent.obtain(mapDownTime,event.getEventTime(),event.getAction(),count,properties,coordinates,
                event.getMetaState(),event.getButtonState(),event.getXPrecision(),event.getYPrecision(),event.getDeviceId(),event.getEdgeFlags(),event.getSource(),event.getFlags());
            int[] layer=new int[2],surface=new int[2];getLocationOnScreen(layer);mapView.getLocationOnScreen(surface);
            translated.offsetLocation(layer[0]-surface[0],layer[1]-surface[1]);
            if(lastMapEvent!=null)lastMapEvent.recycle();lastMapEvent=MotionEvent.obtain(translated);
            try{mapView.dispatchTouchEvent(translated);}finally{translated.recycle();}
        }
        private void beginMapGesture(MotionEvent event){
            clearStroke();mapGesture=true;mapDownTime=event.getEventTime();
            getParent().requestDisallowInterceptTouchEvent(true);
            MotionEvent.PointerProperties property=new MotionEvent.PointerProperties();event.getPointerProperties(0,property);
            MotionEvent.PointerCoords coordinate=new MotionEvent.PointerCoords();event.getPointerCoords(0,coordinate);
            MotionEvent down=MotionEvent.obtain(mapDownTime,mapDownTime,MotionEvent.ACTION_DOWN,1,
                new MotionEvent.PointerProperties[]{property},new MotionEvent.PointerCoords[]{coordinate},event.getMetaState(),event.getButtonState(),
                event.getXPrecision(),event.getYPrecision(),event.getDeviceId(),event.getEdgeFlags(),event.getSource(),event.getFlags());
            try{forwardToMap(down);}finally{down.recycle();}
            MotionEvent second=MotionEvent.obtain(event);
            second.setAction(MotionEvent.ACTION_POINTER_DOWN|((event.getPointerCount()-1)<<MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            try{forwardToMap(second);}finally{second.recycle();}
        }
        private void endMapGesture(){
            if(lastMapEvent!=null){
                if(mapGesture){MotionEvent cancel=MotionEvent.obtain(lastMapEvent);cancel.setAction(MotionEvent.ACTION_CANCEL);
                    try{mapView.dispatchTouchEvent(cancel);}finally{cancel.recycle();}}
                lastMapEvent.recycle();lastMapEvent=null;
            }
            mapGesture=false;
        }
        private void sample(float x,float y,boolean force){
            if(map==null||(!force&&Math.hypot(x-lastX,y-lastY)<dp(1)))return;
            lastX=x;lastY=y;
            int[] layer=new int[2],surface=new int[2];getLocationOnScreen(layer);mapView.getLocationOnScreen(surface);
            LatLng ll=map.getProjection().fromScreenLocation(new android.graphics.Point(Math.round(x+layer[0]-surface[0]),Math.round(y+layer[1]-surface[1])));
            if(ll==null)return;
            SimulationEngine.Point p=new SimulationEngine.Point(ll.latitude,ll.longitude,altitude);
            double gap=stroke.isEmpty()?Double.MAX_VALUE:SimulationEngine.distance(stroke.get(stroke.size()-1),p);
            if(force&&!stroke.isEmpty()&&gap<.0001){stroke.set(stroke.size()-1,p);return;}
            if(stroke.isEmpty()||force||gap>=.25){
                int reserve=anchorSlots();
                if(stroke.size()+points.size()+reserve>=5000)throw new IllegalArgumentException("已达到 5000 个路线点；本笔未保存，请分段或缩短路线");
                SimulationEngine.Point before=stroke.isEmpty()?strokeOrigin:stroke.get(stroke.size()-1);
                if(before!=null)strokeMeters+=SimulationEngine.distance(before,p);
                stroke.add(p);refreshDistance(strokeBaseMeters+strokeMeters);
            }
        }
        private void history(MotionEvent event,int index){
            for(int i=0;i<event.getHistorySize();i++){
                float x=event.getHistoricalX(index,i),y=event.getHistoricalY(index,i);path.lineTo(x,y);sample(x,y,false);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(mode!=DRAW||map==null)return false;
            try{
                RouteGestureState.Action action=gestures.accept(event.getActionMasked(),event.getPointerCount());
                switch(action){
                    case BEGIN_STROKE:
                        endMapGesture();clearStroke();if(!anchorReady())return true;
                        
                        explorer.cancelLocation();active=true;pointer=event.getPointerId(0);
                        getParent().requestDisallowInterceptTouchEvent(true);
                        SimulationEngine.Point first = points.isEmpty() ? null : points.get(points.size() - 1);
                        if (first == null && !presetEditor) first = routeAnchor != null ? routeAnchor
                                : new SimulationEngine.Point(deviceAnchor.latitude, deviceAnchor.longitude, altitude);
                        strokeOrigin=first;strokeBaseMeters=length();strokeMeters=0;
                        if (first == null) path.moveTo(event.getX(), event.getY());
                        else {
                            android.graphics.Point screen=map.getProjection().toScreenLocation(new LatLng(first.latitude,first.longitude));
                            int[] layer=new int[2],surface=new int[2];getLocationOnScreen(layer);mapView.getLocationOnScreen(surface);
                            path.moveTo(screen.x+surface[0]-layer[0],screen.y+surface[1]-layer[1]);path.lineTo(event.getX(),event.getY());
                        }
                        sample(event.getX(),event.getY(),true);invalidate();return true;
                    case BEGIN_ZOOM:beginMapGesture(event);return true;
                    case MOVE_ZOOM:if(mapGesture)forwardToMap(event);return true;
                    case END_ZOOM:
                        if(mapGesture)forwardToMap(event);endMapGesture();clearStroke();return true;
                    case MOVE_STROKE:
                    case COMMIT_STROKE:
                        if(!active)return true;
                        int index=event.findPointerIndex(pointer);if(index<0){cancelStroke();return true;}
                        history(event,index);float x=event.getX(index),y=event.getY(index);path.lineTo(x,y);
                        boolean done=action==RouteGestureState.Action.COMMIT_STROKE;sample(x,y,done);
                        if(done){
                            if(!stroke.isEmpty()){
                                
                                remember();fixed=false;anchorRoute();points.addAll(stroke);routeChanged(false);
                            }
                            cancelStroke();performClick();
                        }else invalidate();return true;
                    case CANCEL:cancelStroke();return true;
                    case IGNORE:return true;
                }
            }catch(Exception e){cancelStroke();error(e);}
            return true;
        }
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.drawPath(path,paint);}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
