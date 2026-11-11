package com.dudu.run.simulation;

import android.Manifest;
import android.app.*;
import com.dudu.run.ui.NativeMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.dudu.run.R;
import com.tencent.map.geolocation.*;
import com.tencent.tencentmap.mapsdk.maps.*;
import com.tencent.tencentmap.mapsdk.maps.model.*;
import java.util.*;
import java.util.concurrent.*;

/** Editor-only camera/location/search controls. Does not feed the sport recorder. */
final class MapExplorer {
    interface Target{void beforeMove();boolean append(LatLng point);void located(LatLng point);}
    private static final int LOCATION_PERMISSION=831;
    private final Activity activity;private final TencentMap map;private final Target target;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final com.google.android.material.floatingactionbutton.FloatingActionButton locateButton;
    private final com.dudu.run.ui.RunnerUi palette;
    private final NativeMaterial material;
    private TencentLocationManager locationManager;private TencentLocationListener locationListener;
    private LatLng devicePosition;private String locatedCity="";private PlaceSearch.Place selected;
    private Marker deviceMarker,selectedMarker;private Runnable locateTimeout;
    private com.google.android.material.textfield.TextInputLayout searchField;
    private TextInputEditText searchInput;
    private androidx.appcompat.widget.ListPopupWindow suggestions;
    private SuggestionAdapter suggestionAdapter;
    private ViewTreeObserver.OnGlobalLayoutListener searchLayoutListener;
    private int popupWidth,popupHeight,popupOffset;
    private Runnable pendingSearch;private Future<?> searchTask;private int searchGeneration;private boolean closed;
    private int dp(float value){return SimulationUi.dp(activity,value);}
    MapExplorer(Activity activity,TencentMap map,Target target){
        this.activity=activity;this.map=map;this.target=target;
        palette=new com.dudu.run.ui.RunnerUi(activity);material=new NativeMaterial(activity);
        locateButton=new com.google.android.material.floatingactionbutton.FloatingActionButton(material.context());
        locateButton.setImageResource(R.drawable.ic_simulation_my_location);locateButton.setCustomSize(dp(52));
        locateButton.setShapeAppearanceModel(com.google.android.material.shape.ShapeAppearanceModel.builder().setAllCornerSizes(new com.google.android.material.shape.RelativeCornerSize(.5f)).build());
        locateButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(palette.tone));
        locateButton.setSupportImageTintList(android.content.res.ColorStateList.valueOf(palette.accentText));
        locateButton.setUseCompatPadding(false);locateButton.setEnsureMinTouchTargetSize(true);locateButton.setCompatElevation(dp(6));
        locateButton.setContentDescription("定位当前位置");locateButton.setOnClickListener(v->locate());locateButton.setId(R.id.simulation_locate_current);
    }
    View bar(){
        if(searchField!=null)return searchField;
        searchField=new com.google.android.material.textfield.TextInputLayout(material.context());
        searchField.setId(R.id.simulation_place_search);searchField.setHintEnabled(false);
        searchField.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        searchField.setBoxBackgroundColor(palette.tone);searchField.setBoxStrokeColor(palette.accent);
        searchField.setBoxCornerRadii(dp(16),dp(16),dp(16),dp(16));searchField.setElevation(0);
        searchField.setStartIconDrawable(androidx.appcompat.R.drawable.abc_ic_search_api_material);
        searchField.setStartIconTintList(android.content.res.ColorStateList.valueOf(palette.muted));
        searchField.setStartIconContentDescription("搜索地点");
        searchField.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_CLEAR_TEXT);
        searchField.setEndIconTintList(android.content.res.ColorStateList.valueOf(palette.muted));
        searchInput=new TextInputEditText(material.editContext());searchInput.setId(R.id.simulation_search_query);
        searchInput.setSingleLine(true);searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setHint("搜索地点、地址");searchInput.setContentDescription("搜索地点、地址");
        searchInput.setTextColor(palette.ink);searchInput.setHintTextColor(palette.muted);searchInput.setTextSize(16);
        searchInput.setBackground(null);searchInput.setMinimumHeight(0);searchInput.setMinHeight(0);
        searchInput.setPadding(dp(16),0,dp(16),0);searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        searchField.addView(searchInput,new LinearLayout.LayoutParams(-1,dp(48)));
        searchField.setFocusableInTouchMode(true);searchField.requestFocus();
        suggestionAdapter=new SuggestionAdapter();
        suggestions=new androidx.appcompat.widget.ListPopupWindow(material.context());
        suggestions.setAnchorView(searchField);suggestions.setAdapter(suggestionAdapter);
        suggestions.setModal(false);suggestions.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        suggestions.setOverlapAnchor(false);suggestions.setVerticalOffset(dp(4));
        suggestions.setDropDownGravity(Gravity.START);
        com.google.android.material.shape.MaterialShapeDrawable background=new com.google.android.material.shape.MaterialShapeDrawable(
                com.google.android.material.shape.ShapeAppearanceModel.builder().setAllCornerSizes(dp(16)).build());
        background.setFillColor(android.content.res.ColorStateList.valueOf(palette.surface));
        background.setStroke(dp(1),palette.outline);suggestions.setBackgroundDrawable(background);
        suggestions.setOnDismissListener(this::cancelSearch);
        searchLayoutListener=()->{if(suggestions.isShowing())showSuggestions();};
        searchField.getViewTreeObserver().addOnGlobalLayoutListener(searchLayoutListener);
        searchField.setStartIconOnClickListener(v->{
            searchInput.requestFocus();
            InputMethodManager keyboard=(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(keyboard!=null)keyboard.showSoftInput(searchInput,InputMethodManager.SHOW_IMPLICIT);
            if(searchInput.length()>0)executeSearch();
        });
        searchInput.setOnFocusChangeListener((v,focused)->{if(focused)queueSearch();else{cancelSearch();suggestions.dismiss();}});
        searchInput.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_SEARCH){executeSearch();return true;}return false;});
        searchInput.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){queueSearch();}
            public void afterTextChanged(Editable value){}
        });
        return searchField;
    }
    View locationButton(){return locateButton;}
    private void locationBusy(boolean busy){
        locateButton.setEnabled(!busy);locateButton.setAlpha(busy?.55f:1);
        locateButton.setContentDescription(busy?"正在定位当前位置":"定位当前位置");
    }
    private boolean alive(){return !closed&&!activity.isFinishing()&&!activity.isDestroyed();}
    private void toast(String text){if(alive())Toast.makeText(activity,text,Toast.LENGTH_LONG).show();}
    private boolean hasPermission(){return Build.VERSION.SDK_INT<23||activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
    void permissionResult(int requestCode){if(requestCode==LOCATION_PERMISSION){if(hasPermission())locate();else toast("需要位置权限才能确定路线起点；请授权后点击当前位置");}}
    void locate(){
        if(locationListener!=null)return;
        if(!hasPermission()){
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION);return;
        }
        try{
            LocationManager system=(LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
            if(system!=null&&!system.isProviderEnabled(LocationManager.GPS_PROVIDER)&&!system.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                new NativeMaterial(activity).dialog().setTitle("开启设备定位").setMessage("请开启手机的位置服务，再点击当前位置。")
                    .setNegativeButton("取消",null).setPositiveButton("打开设置",(d,w)->activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();return;
            }
            locationBusy(true);
            locationManager=TencentLocationManager.getInstance(activity.getApplicationContext());
            locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
            locationListener=new TencentLocationListener(){
                @Override public void onLocationChanged(TencentLocation location,int error,String reason){
                    if(locationListener!=this||!alive())return;
                    if(error!=0||location==null){cancelLocation();toast("当前位置定位失败（"+error+"），请检查定位权限、网络和设备定位开关");return;}
                    double lat=location.getLatitude(),lon=location.getLongitude();
                    if(Double.isNaN(lat)||Double.isNaN(lon)||Double.isInfinite(lat)||Double.isInfinite(lon)||Math.abs(lat)>85||Math.abs(lon)>180){cancelLocation();toast("未获得有效位置，请重试");return;}
                    float accuracy=location.getAccuracy();
                    if(Float.isNaN(accuracy)||Float.isInfinite(accuracy)||accuracy<=0||accuracy>50){
                        cancelLocation();toast("定位误差超过 50 米或精度未知，请开启精确定位并在开阔处重试；未更新路线起点");return;
                    }
                    devicePosition=new LatLng(lat,lon);locatedCity=location.getCity()==null?"":location.getCity();
                    cancelLocation();target.beforeMove();target.located(devicePosition);redrawMarkers();
                    if(map!=null)map.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition,17));
                    if(deviceMarker!=null)deviceMarker.showInfoWindow();
                    toast(String.format(Locale.CHINA,"已定位当前位置 · 精度约 %.0f 米",accuracy));
                }
                @Override public void onStatusUpdate(String name,int status,String detail){}
            };
            locateTimeout=()->{if(locationListener!=null){cancelLocation();toast("定位超时，可在室外或联网后重试");}};
            handler.postDelayed(locateTimeout,20000);
            int code=locationManager.requestSingleFreshLocation(TencentLocationRequest.create()
                    .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA).setAllowCache(false).setAllowGPS(true),locationListener,Looper.getMainLooper());
            if(code!=0){cancelLocation();toast("无法发起定位（"+code+"），请检查设备定位权限");}
        }catch(Exception error){cancelLocation();toast("无法定位当前位置，请检查权限与系统设置");}
    }
    void cancelLocation(){
        if(locateTimeout!=null)handler.removeCallbacks(locateTimeout);locateTimeout=null;
        TencentLocationListener pending=locationListener;locationListener=null;
        if(locationManager!=null&&pending!=null)try{locationManager.removeUpdates(pending);}catch(Exception ignored){}
        locationBusy(false);
    }
    void mapCleared(){deviceMarker=null;selectedMarker=null;}
    void redrawMarkers(){
        if(map==null||!alive())return;
        if(devicePosition!=null){
            if(deviceMarker==null)deviceMarker=map.addMarker(new MarkerOptions().position(devicePosition).title("当前位置（设备定位）")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            else deviceMarker.setPosition(devicePosition);
        }
        if(selected!=null&&selectedMarker==null)selectedMarker=map.addMarker(new MarkerOptions().position(selected.point).title(selected.title).snippet(selected.address)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
    }
    private void choose(PlaceSearch.Place place,boolean append){
        target.beforeMove();if(selectedMarker!=null){selectedMarker.remove();selectedMarker=null;}
        if(append&&!target.append(place.point))return;
        selected=append?null:place;redrawMarkers();
        if(map!=null){map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.point,17));if(selectedMarker!=null)selectedMarker.showInfoWindow();}
        hideSearch();
        toast(append?"已添加为路线点，可继续选点或画线":"已定位到搜索地点，可在地图上继续选点或画线");
    }
    private LatLng cameraCenter(){try{return map==null||map.getCameraPosition()==null?null:map.getCameraPosition().target;}catch(Exception ignored){return null;}}
    private void cancelSearch(){
        searchGeneration++;
        if(pendingSearch!=null)handler.removeCallbacks(pendingSearch);
        pendingSearch=null;
        if(searchTask!=null)searchTask.cancel(true);
        searchTask=null;
    }
    private void queueSearch(){
        cancelSearch();
        if(searchInput==null||!searchInput.hasFocus()||searchInput.getText().toString().trim().isEmpty()){
            if(suggestions!=null)suggestions.dismiss();return;
        }
        suggestionAdapter.update(Collections.emptyList(),"搜索中…");showSuggestions();
        pendingSearch=()->{pendingSearch=null;executeSearch();};
        handler.postDelayed(pendingSearch,600);
    }
    private void executeSearch(){
        cancelSearch();
        if(!alive()||searchInput==null||!searchInput.hasFocus())return;
        String keyword=searchInput.getText().toString().trim();
        if(keyword.isEmpty()){suggestions.dismiss();return;}
        final int generation=searchGeneration;
        String region=activity.getSharedPreferences("map_editor_search",0).getString("city",locatedCity);
        LatLng center=cameraCenter();
        suggestionAdapter.update(Collections.emptyList(),"搜索中…");showSuggestions();
        searchTask=io.submit(()->{
            try{
                if(Thread.currentThread().isInterrupted())return;
                List<PlaceSearch.Place> found=PlaceSearch.find(activity.getApplicationContext(),keyword,region,center);
                handler.post(()->{
                    if(!alive()||generation!=searchGeneration||!searchInput.hasFocus())return;
                    suggestionAdapter.update(found,found.isEmpty()?"未找到地点，可补充城市名称或换个关键词":"");
                    showSuggestions();
                });
            }catch(Exception error){handler.post(()->{
                if(!alive()||generation!=searchGeneration||!searchInput.hasFocus())return;
                String message=error.getMessage();
                if(error instanceof java.net.SocketTimeoutException)message="搜索超时，请检查网络后重试";
                if(message==null||message.trim().isEmpty())message="搜索失败，请检查网络或地图 Key";
                suggestionAdapter.update(Collections.emptyList(),message);showSuggestions();
            });}
        });
    }
    private void showSuggestions(){
        if(!alive()||searchField==null||!searchField.isAttachedToWindow()||!searchInput.hasFocus())return;
        if(searchField.getWidth()==0){searchField.post(this::showSuggestions);return;}
        android.graphics.Rect visible=new android.graphics.Rect();searchField.getWindowVisibleDisplayFrame(visible);
        int[] position=new int[2];searchField.getLocationOnScreen(position);
        int above=position[1]-visible.top-dp(8);
        int below=visible.bottom-position[1]-searchField.getHeight()-dp(8);
        // Keep the route controls visible: expand above the bottom search field.
        boolean openAbove=above>=dp(48);
        int available=openAbove?above:below;
        if(available<dp(48)){suggestions.dismiss();return;}
        int wanted=suggestionAdapter.places.isEmpty()?dp(100):dp(132)*suggestionAdapter.places.size();
        int height=Math.min(Math.min(wanted,dp(320)),available),width=searchField.getWidth();
        int offset=openAbove?-searchField.getHeight()-height-dp(4):dp(4);
        if(!suggestions.isShowing()||popupWidth!=width||popupHeight!=height||popupOffset!=offset){
            popupWidth=width;popupHeight=height;popupOffset=offset;
            suggestions.setWidth(width);suggestions.setHeight(height);suggestions.setVerticalOffset(offset);
            suggestions.show();
        }
        if(suggestions.getListView()!=null)suggestions.getListView().setId(R.id.simulation_search_results);
    }
    void hideSearch(){
        cancelSearch();
        if(suggestions!=null)suggestions.dismiss();
        if(searchInput!=null){
            searchInput.clearFocus();
            InputMethodManager keyboard=(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(keyboard!=null)keyboard.hideSoftInputFromWindow(searchInput.getWindowToken(),0);
        }
    }
    void dismissSearchOutside(float x,float y){
        if(searchField==null||searchInput==null||!searchInput.hasFocus())return;
        android.graphics.Rect bounds=new android.graphics.Rect();searchField.getGlobalVisibleRect(bounds);
        if(!bounds.contains((int)x,(int)y))hideSearch();
    }
    private final class SuggestionAdapter extends BaseAdapter {
        private final ArrayList<PlaceSearch.Place> places=new ArrayList<>();
        private String message="";
        void update(List<PlaceSearch.Place> found,String status){places.clear();places.addAll(found);message=status;notifyDataSetChanged();}
        @Override public int getCount(){return places.isEmpty()?1:places.size();}
        @Override public Object getItem(int position){return places.isEmpty()?message:places.get(position);}
        @Override public long getItemId(int position){return position;}
        @Override public boolean areAllItemsEnabled(){return false;}
        @Override public boolean isEnabled(int position){return !places.isEmpty();}
        @Override public View getView(int position,View recycled,ViewGroup parent){
            if(places.isEmpty()){
                TextView status=SimulationUi.text(activity,message,13,palette.muted,false);
                status.setPadding(dp(16),dp(16),dp(16),dp(16));return status;
            }
            PlaceSearch.Place place=places.get(position);
            LinearLayout row=SimulationUi.column(activity);row.setPadding(dp(12),dp(10),dp(12),dp(10));
            TextView title=SimulationUi.text(activity,place.title,15,palette.ink,true);title.setMaxLines(2);row.addView(title);
            TextView address=SimulationUi.text(activity,place.address,12,palette.muted,false);address.setMaxLines(2);row.addView(address);
            SimulationUi.gap(row,6);LinearLayout actions=SimulationUi.row(activity);
            SimulationUi.weighted(actions,material.button("定位到这里",()->choose(place,false)),44);
            SimulationUi.weighted(actions,material.button("添加路线点",()->choose(place,true)),44);
            row.addView(actions);row.setOnClickListener(v->choose(place,false));return row;
        }
    }
    void close(){
        closed=true;cancelLocation();hideSearch();
        if(searchField!=null&&searchLayoutListener!=null&&searchField.getViewTreeObserver().isAlive())
            searchField.getViewTreeObserver().removeOnGlobalLayoutListener(searchLayoutListener);
        searchLayoutListener=null;io.shutdownNow();handler.removeCallbacksAndMessages(null);
    }
}
