package com.dudu.run.simulation;

import android.content.Context;
import android.util.AttributeSet;
import com.tencent.tencentmap.mapsdk.maps.TextureMapView;

/** Applies the same startup Key to XML and programmatically created maps. */
public final class ConfigurableMapView extends TextureMapView {
    public ConfigurableMapView(Context context){super(context,MapKeySettings.options(context));}
    public ConfigurableMapView(Context context,AttributeSet attrs){super(context,attrs,MapKeySettings.options(context));}
    public ConfigurableMapView(Context context,AttributeSet attrs,int style){super(context,attrs,style,MapKeySettings.options(context));}
}
