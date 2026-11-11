package com.dudu.run.simulation;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.text.InputType;
import android.widget.*;
import com.dudu.run.ui.NativeMaterial;
import com.dudu.run.ui.RunnerUi;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;
import java.util.Locale;

/** Keys are saved locally; one immutable snapshot is used throughout each process. */
public final class MapKeySettings {
    private static String sdkKey,serviceKey;
    private MapKeySettings(){}
    private static android.content.SharedPreferences store(Context c){return c.getSharedPreferences("map_keys_v1",0);}
    private static String bundled(Context c){
        try{return c.getPackageManager().getApplicationInfo(c.getPackageName(),PackageManager.GET_META_DATA).metaData.getString("TencentMapSDK","");}
        catch(Exception e){return "";}
    }
    public static synchronized void initialize(Context c){
        if(sdkKey!=null)return;
        String override=store(c).getString("sdk","");sdkKey=override.isEmpty()?bundled(c):override;
        String web=store(c).getString("service","");serviceKey=web.isEmpty()?sdkKey:web;
        if(!sdkKey.isEmpty())TencentLocationManagerOptions.setKey(sdkKey);
    }
    static String serviceKey(Context c){initialize(c);return serviceKey;}
    static TencentMapOptions options(Context c){initialize(c);return new TencentMapOptions().setMapKey(sdkKey);}
    private static String normalize(String key){return key.trim().toUpperCase(Locale.US);}
    private static boolean valid(String key){return key.isEmpty()||key.matches("[A-Z0-9]{5}(?:-[A-Z0-9]{5}){5}");}
    public static void show(Activity activity){
        RunnerUi ui = new RunnerUi(activity);
        NativeMaterial material = new NativeMaterial(activity);
        LinearLayout page = material.dialogBody();
        int keyInput = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        EditText sdk = material.field(page, "地图 / 定位 Key", keyInput, false);
        sdk.setText(store(activity).getString("sdk", ""));
        ui.gap(page, 12);
        EditText web = material.field(page, "搜索 / 道路吸附 Key（可选）", keyInput, false);
        web.setText(store(activity).getString("service", ""));
        ui.gap(page, 12);
        page.addView(ui.text("地图 Key 留空使用内置值，搜索 Key 留空则与地图共用。"
                + "\n保存后下次完整启动生效。地图 Key 需绑定包名和签名，搜索 Key 需开通 WebService API。", 12, ui.muted, false));
        ScrollView scroll=new ScrollView(activity);scroll.addView(page);
        com.google.android.material.button.MaterialButton save = material.primaryAction(page, "保存");
        AlertDialog dialog=material.dialog().setTitle("地图 Key 设置").setView(scroll)
            .setNegativeButton("取消",null).setNeutralButton("恢复内置",null).create();
        dialog.setOnShowListener(d->{
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{sdk.setText("");web.setText("");});
            save.setOnClickListener(v->{
                String map=normalize(sdk.getText().toString()),service=normalize(web.getText().toString());
                if(!valid(map)){sdk.setError("请输入完整的腾讯地图 Key");return;}
                if(!valid(service)){web.setError("请输入完整的腾讯地图 Key");return;}
                if(!store(activity).edit().putString("sdk",map).putString("service",service).commit()){
                    Toast.makeText(activity,"保存失败，请重试",Toast.LENGTH_LONG).show();return;
                }
                dialog.dismiss();Toast.makeText(activity,"已保存，下次完整启动应用后生效",Toast.LENGTH_LONG).show();
            });
        });dialog.show();
    }
}
