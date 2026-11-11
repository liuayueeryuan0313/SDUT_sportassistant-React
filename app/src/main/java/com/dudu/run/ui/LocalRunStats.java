package com.dudu.run.ui;

import android.content.Context;
import android.content.SharedPreferences;

/** Private, per-account totals. Re-saving a run replaces its contribution. */
public final class LocalRunStats {
    private LocalRunStats() {}
    private static SharedPreferences store(Context context,String owner) {
        // An account's statistics never become another account's totals after logout.
        String key=android.util.Base64.encodeToString(owner.getBytes(java.nio.charset.StandardCharsets.UTF_8),android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);
        return context.getSharedPreferences("local_run_stats_"+key,0);
    }
    public static final class Totals {
        public final long steps,updatedAt;
        public final double calories;
        public final boolean hasSteps,hasRuns;
        private Totals(SharedPreferences prefs) {
            steps=prefs.getLong("steps",0);calories=prefs.getLong("milli_calories",0)/1000d;
            updatedAt=prefs.getLong("updated_at",0);hasSteps=prefs.getLong("step_runs",0)>0;hasRuns=prefs.getLong("runs",0)>0;
        }
    }
    public static Totals read(Context context) {return new Totals(store(context,String.valueOf(com.dudu.run.b.c().n())));}
    public static synchronized void record(Context context,String owner,long id,long steps,boolean known,float meters) {
        if(id<=0||Float.isNaN(meters)||Float.isInfinite(meters)||meters<0)return;
        SharedPreferences prefs=store(context,owner);String prefix="run_"+id;
        boolean exists=prefs.contains(prefix+"_cal");long oldSteps=prefs.getLong(prefix+"_steps",-1);
        long newSteps=known?Math.max(0,steps):-1;
        // Same estimate used by the app's individual record screen (60 kg).
        long calories=Math.round(60d*meters*1.036d);
        prefs.edit().putLong("steps",Math.max(0,prefs.getLong("steps",0)-Math.max(0,oldSteps)+Math.max(0,newSteps)))
                .putLong("milli_calories",Math.max(0,prefs.getLong("milli_calories",0)-prefs.getLong(prefix+"_cal",0)+calories))
                .putLong("step_runs",prefs.getLong("step_runs",0)-(exists&&oldSteps>=0?1:0)+(known?1:0))
                .putLong("runs",prefs.getLong("runs",0)+(exists?0:1))
                .putLong(prefix+"_steps",newSteps).putLong(prefix+"_cal",calories)
                .putLong("updated_at",System.currentTimeMillis()).commit();
    }
}
