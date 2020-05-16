package com.example.activitytrackerlibrary;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();
    }
}
