package com.dudu.run.simulation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;

/** Samples only the map texture; native blur never includes toolbar text/icons. */
final class MapHeaderGlass {
    private final ImageView background;
    private final ViewGroup mapView;
    private final TencentMap map;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG);
    private final float radius;
    private boolean active,closed,scheduled;
    private long lastCapture;
    private int retries;
    private Bitmap capture,strip;
    private final Runnable captureFrame=this::captureFrame;
    private final TencentMap.OnMapLoadedCallback loaded=this::requestRefresh;
    private final View.OnLayoutChangeListener layout=this::layoutChanged;
    private void layoutChanged(View v,int l,int t,int r,int b,int ol,int ot,int or,int ob){
        if(Build.VERSION.SDK_INT>=31)Api31.apply(background,radius);
        requestRefresh();
    }

    MapHeaderGlass(Activity host,ViewGroup mapView,TencentMap map){
        this.mapView=mapView;this.map=map;
        radius=host.getResources().getDisplayMetrics().density*8;
        background=new ImageView(host);background.setScaleType(ImageView.ScaleType.FIT_XY);
        background.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        background.setClickable(false);background.setVisibility(View.INVISIBLE);
        background.addOnLayoutChangeListener(layout);mapView.addOnLayoutChangeListener(layout);
        map.addOnMapLoadedCallback(loaded);
        map.setOnCameraChangeListener(new TencentMap.OnCameraChangeListener(){
            @Override public void onCameraChange(CameraPosition position){requestRefresh();}
            @Override public void onCameraChangeFinished(CameraPosition position){requestRefresh();}
        });
    }
    View view(){return background;}
    void resume(){active=true;retries=0;requestRefresh();}
    void pause(){active=false;handler.removeCallbacks(captureFrame);scheduled=false;}
    void requestRefresh(){
        if(closed||!active||Build.VERSION.SDK_INT<31||scheduled)return;
        scheduled=true;
        handler.postDelayed(captureFrame,Math.max(0,200-(SystemClock.uptimeMillis()-lastCapture)));
    }
    private static TextureView texture(View view){
        if(view instanceof TextureView)return (TextureView)view;
        if(view instanceof ViewGroup)for(int n=0;n<((ViewGroup)view).getChildCount();n++){
            TextureView found=texture(((ViewGroup)view).getChildAt(n));if(found!=null)return found;
        }
        return null;
    }
    private void captureFrame(){
        scheduled=false;
        if(!active||closed||background.getWidth()==0||background.getHeight()==0)return;
        lastCapture=SystemClock.uptimeMillis();
        TextureView texture=texture(mapView);
        if(texture==null||!texture.isAvailable()||texture.getWidth()==0||texture.getHeight()==0){
            if(retries++<8)requestRefresh();return;
        }
        try{
            // Reuse a small map buffer and an even smaller toolbar strip.
            int width=Math.min(256,texture.getWidth());
            float scale=width/(float)texture.getWidth();
            int height=Math.max(1,Math.round(texture.getHeight()*scale));
            if(capture==null||capture.getWidth()!=width||capture.getHeight()!=height)
                capture=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
            if(texture.getBitmap(capture)==null)return;
            int outWidth=Math.max(1,Math.round(background.getWidth()*scale));
            int outHeight=Math.max(1,Math.round(background.getHeight()*scale));
            if(strip==null||strip.getWidth()!=outWidth||strip.getHeight()!=outHeight)
                strip=Bitmap.createBitmap(outWidth,outHeight,Bitmap.Config.ARGB_8888);
            int[] mapPosition=new int[2],glassPosition=new int[2];
            texture.getLocationOnScreen(mapPosition);background.getLocationOnScreen(glassPosition);
            strip.eraseColor(Color.TRANSPARENT);
            new Canvas(strip).drawBitmap(capture,(mapPosition[0]-glassPosition[0])*scale,
                    (mapPosition[1]-glassPosition[1])*scale,paint);
            background.setImageBitmap(strip);background.setVisibility(View.VISIBLE);retries=0;
        }catch(RuntimeException unavailable){
            // A detached/recreated texture leaves the translucent capsules usable.
            background.setVisibility(View.INVISIBLE);
        }
    }
    void close(){
        closed=true;pause();
        background.removeOnLayoutChangeListener(layout);mapView.removeOnLayoutChangeListener(layout);
        map.removeOnMapLoadedCallback(loaded);map.setOnCameraChangeListener(null);
        background.setImageDrawable(null);capture=null;strip=null;
    }
    @android.annotation.TargetApi(31)
    private static final class Api31 {
        static void apply(ImageView view,float radius){
            if(view.getHeight()==0)return;
            android.graphics.RenderEffect blur=android.graphics.RenderEffect.createBlurEffect(
                    radius,radius,android.graphics.Shader.TileMode.CLAMP);
            android.graphics.Shader fade=new android.graphics.LinearGradient(0,0,0,view.getHeight(),
                    new int[]{Color.WHITE,Color.WHITE,Color.TRANSPARENT},new float[]{0,.65f,1},
                    android.graphics.Shader.TileMode.CLAMP);
            view.setRenderEffect(android.graphics.RenderEffect.createBlendModeEffect(blur,
                    android.graphics.RenderEffect.createShaderEffect(fade),android.graphics.BlendMode.DST_IN));
        }
    }
}
