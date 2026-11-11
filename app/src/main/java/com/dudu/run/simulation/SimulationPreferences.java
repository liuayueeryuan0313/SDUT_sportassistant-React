package com.dudu.run.simulation;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Collections;
import org.json.*;

/** Persistent scalar settings only. Legacy route coordinates are discarded during migration. */
public final class SimulationPreferences {
    private SimulationPreferences(){}
    private static final String[] NUMBERS={"speed","variation","cadence","cadenceVariation","altitude","altitudeVariation","routeOffsetMeters"};
    private static SharedPreferences store(Context c,boolean offline){return c.getSharedPreferences((offline?"offline_":"")+"simulation_parameters_v2",0);}
    static SharedPreferences store(Context c){return store(c,OfflineTestSession.active());}
    public static void initialize(Context c){migrate(c,false);migrate(c,true);upgradeSpeed(c,false);upgradeSpeed(c,true);}
    private static void upgradeSpeed(Context c,boolean offline){
        SharedPreferences p=store(c,offline);
        if(!p.getBoolean("speed_default_10",false)){
            SharedPreferences.Editor e=p.edit();
            if(p.contains("speed")&&Math.abs(p.getFloat("speed",10)-9)<.001)e.putFloat("speed",10);
            e.putBoolean("speed_default_10",true).apply();
        }
    }
    private static void migrate(Context c,boolean offline){
        SharedPreferences target=store(c,offline),legacy=c.getSharedPreferences(offline?"offline_simulation_settings":"simulation_settings",0);
        String[] drafts={offline?"offline_simulation_draft_v1":"simulation_draft_v203",(offline?"offline_":"")+"simulation_run_draft_v1"};
        if(!target.getBoolean("migrated",false)){
            SharedPreferences.Editor next=target.edit();
            try{
                String saved=legacy.getString("config",null);
                if(saved!=null){
                    JSONObject j=new JSONObject(saved);for(String key:NUMBERS)if(j.has(key))next.putFloat(key,(float)j.getDouble(key));
                    JSONArray rows=j.optJSONArray("points");
                    if(rows!=null&&rows.length()>0)next.putFloat("altitude",(float)rows.getJSONArray(0).getDouble(2));
                    next.putBoolean("fixed",j.optBoolean("fixed",false));
                }
            }catch(Exception ignored){}
            for(String name:drafts){
                SharedPreferences old=c.getSharedPreferences(name,0);
                for(String key:NUMBERS)if(old.contains(key))try{next.putFloat(key,old.getFloat(key,0));}catch(ClassCastException ignored){}
                if(old.contains("fixed"))next.putBoolean("fixed",old.getBoolean("fixed",false));
                if(old.contains("gpxGps"))next.putBoolean("gpxGps",old.getBoolean("gpxGps",true));
            }
            if(!next.putBoolean("migrated",true).commit())return;
        }
        legacy.edit().remove("config").apply();
        for(String name:drafts)c.getSharedPreferences(name,0).edit().remove("points").remove("anchor").remove("runId").remove("roadSnapped").apply();
    }
    static void save(Context c,SimulationEngine.Config config){
        if(!store(c).contains("altitude"))store(c).edit().putFloat("altitude",(float)config.points.get(0).altitude).apply();
        store(c).edit().putFloat("speed",(float)config.speedKmh).putFloat("variation",(float)config.speedVariationKmh)
            .putFloat("cadence",(float)config.cadence).putFloat("cadenceVariation",(float)config.cadenceVariation)
            .putFloat("altitudeVariation",(float)config.altitudeVariation)
            .putFloat("routeOffsetMeters",(float)config.routeOffsetMeters).putBoolean("fixed",config.fixed).apply();
    }
    static SimulationEngine.Config load(Context c){
        SharedPreferences p=store(c);
        try{return new SimulationEngine.Config(Collections.singletonList(new SimulationEngine.Point(0,0,p.getFloat("altitude",30))),
            p.getFloat("speed",10),p.getFloat("variation",.8f),p.getFloat("cadence",164),p.getFloat("cadenceVariation",6),
            p.getFloat("altitudeVariation",1.5f),p.getFloat("routeOffsetMeters",(float)SimulationEngine.Config.DEFAULT_ROUTE_OFFSET_METERS),false,true);
        }catch(Exception badSavedSettings){
            return new SimulationEngine.Config(Collections.singletonList(new SimulationEngine.Point(0,0,30)),10,.8,164,6,1.5,
                SimulationEngine.Config.DEFAULT_ROUTE_OFFSET_METERS,false,true);
        }
    }
}
