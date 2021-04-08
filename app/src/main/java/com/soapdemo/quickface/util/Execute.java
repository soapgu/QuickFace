package com.soapdemo.quickface.util;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Execute {
    private static volatile Execute instance;
    private final Handler mainThreadHandler;
    private ExecutorService executor;

    private Execute()
    {
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        /*
        this.executor = new ThreadPoolExecutor(1,1,1, TimeUnit.SECONDS , new LinkedBlockingQueue<>(),
                                                r -> {
                                                    Thread t = new Thread(r);
                                                    t.setName("activity-sub-thread-" + t.getId());
                                                    return t;
                                                } );
         */
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static Execute getInstance()
    {
        if( instance == null ) {
            synchronized ( Execute.class ) {
                if( instance == null ){
                    instance = new Execute();
                }
            }
        }
        return instance;
    }

    /**
     * Post Delegate to UI Thread
     * @param r 相关代理
     */
    public void BeginOnUIThread(Runnable r)
    {
        this.mainThreadHandler.post(r);
    }

    /**
     * Excute on sub thread
     * @param r 相关代理
     */
    public void BeginOnSubThread( Runnable r ){
        this.executor.execute( r );
    }

}
