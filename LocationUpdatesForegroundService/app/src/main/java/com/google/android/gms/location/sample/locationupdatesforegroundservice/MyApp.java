package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.app.Application;

public class MyApp extends Application {
    private static MyApp sInstance;

    public static MyApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
