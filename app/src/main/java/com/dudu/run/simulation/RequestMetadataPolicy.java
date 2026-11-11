package com.dudu.run.simulation;

import java.util.Locale;
import okhttp3.b0;

/** Business requests have no version headers or version-bearing default User-Agent. */
public final class RequestMetadataPolicy {
    private RequestMetadataPolicy(){}
    public static b0 sanitize(b0 request){
        b0.a builder=request.h();
        for(int i=0;i<request.e().size();i++){
            String name=request.e().b(i);
            if(name.toLowerCase(Locale.US).contains("version"))builder.g(name);
        }
        builder.c("User-Agent","Android");return builder.b();
    }
}
