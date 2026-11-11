package com.dudu.run.simulation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.*;

/** Installed before all other business API interceptors; no request proceeds in test mode. */
public final class OfflineApiGuard implements x {
    public static final AtomicInteger blocked=new AtomicInteger();
    @Override public d0 a(x.a chain) throws IOException{
        if(OfflineTestSession.active()){
            blocked.incrementAndGet();
            throw new IOException("离线测试模式：业务接口请求已在本机拦截");
        }
        return chain.a(chain.S());
    }
}
