package com.dudu.run;

import android.app.Application;
import com.dudu.run.utils.Accompaniment;
import com.dudu.run.utils.PrivacyConsent;
public class MyApp extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        com.dudu.run.simulation.MapKeySettings.initialize(this);
        com.dudu.run.simulation.SimulationPreferences.initialize(this);
        com.dudu.run.utils.StartupDisclaimer.install(this);
        com.dudu.run.simulation.OfflineTestSession.init(this);
        c.n().x(this);
        com.dudu.run.simulation.SimulationController.get(this);
        registerActivityLifecycleCallbacks(new com.dudu.run.simulation.SimulationOverlay());
        registerActivityLifecycleCallbacks(new com.dudu.run.ui.NativeInsets());
        b.c().p(this);
        Accompaniment.a().b(this);
        PrivacyConsent.restoreIfAgreed(this);
    }
}
