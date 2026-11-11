package com.dudu.run.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Request construction and record parsing must not run in page-transition frames. */
public final class RunnerWork {
    private RunnerWork() {}
    public static final ExecutorService IO=Executors.newSingleThreadExecutor(r->{
        Thread thread=new Thread(r,"runner-record-data");thread.setDaemon(true);return thread;
    });
}
