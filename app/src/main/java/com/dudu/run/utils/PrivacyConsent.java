package com.dudu.run.utils;

import android.content.Context;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer;

/** Initializes location and analytics SDKs after the user's explicit choice. */
public final class PrivacyConsent {
    private static boolean initialized;

    private PrivacyConsent() {}

    public static boolean isAgreed(Context context) {
        return context.getSharedPreferences("privacy_consent", Context.MODE_PRIVATE)
                .getBoolean("sdk_consent_v1", false);
    }

    public static void restoreIfAgreed(Context context) {
        if (isAgreed(context)) apply(context);
    }

    public static void agree(Context context) {
        context.getSharedPreferences("privacy_consent", Context.MODE_PRIVATE).edit()
                .putBoolean("sdk_consent_v1", true).apply();
        apply(context);
    }

    public static synchronized void apply(Context context) {
        if (initialized || !isAgreed(context)) return;
        TencentMapInitializer.setAgreePrivacy(true);
        TencentLocationManager.setUserAgreePrivacy(true);
        initialized = true;
    }
}
