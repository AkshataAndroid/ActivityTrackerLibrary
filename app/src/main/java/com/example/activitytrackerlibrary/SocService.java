package com.example.activitytrackerlibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SocService implements ViewTreeObserver.OnWindowFocusChangeListener {

    Context context;
    public static Boolean serviceStatus = false;

    static final String TAG = "TrackerLib";
    public static boolean m_started = false;

    private Map<String, Object> m_customData;
    private static final SocService m_instance = new SocService();

    public static Activity m_currentActivity;

    public static String pkgName = null;

    public static SocService instance() {
        return m_instance;
    }

    static String version() {
        return "1.0.0";
    }
    //3
    private void reinitialize() {
        Log.d("TAG", "message Yo yo yo");


        // this.m_device.runRegistrationLoop();
    }
    //2
    public SocService start(Application application) {
       // this.m_customData = customData;
        if (this.m_started) {
            return this;
        }
        this.m_started = true;
        Log.i("Tracker", "Initialising Tracker " + version());
        context = application.getApplicationContext();
        Intent startServerIntent = new Intent(context, SocketService.class);
        startServerIntent.setAction("START");
        context.startService(startServerIntent);
        reinitialize();
        return this;
    }

    //1
    public SocService start(Activity currentActivity, boolean b) {
       // this.m_customData = customData;
        this.m_started = b;

        start(currentActivity.getApplication());
        setActivity(currentActivity);
        return this;
    }
    //4
    void setActivity(Activity activity) {
        if (this.m_currentActivity == activity) {
            return;
        }
        this.m_currentActivity = activity;
//        this.m_controlInjection.setActivity(activity);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    public String api() {
        if (pkgName == null) {
            pkgName = context.getPackageName();
        }
        ApplicationInfo app = null;
        try {
            app = context.getPackageManager().getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = app.metaData;

        String mData = bundle.getString("trackerService");

        return mData;
    }

    public SocService customData(Map<String, Object> customData) {

        return this;
    }

    public Map<String, Object> customData() {
        return this.m_customData;
    }

    Activity getActivity() {
        return this.m_currentActivity;
    }

    public void Expermemnt(Activity mainActivity) {
        //   Toast.makeText(mainActivity, "Hello there", Toast.LENGTH_SHORT).show();
    }

    public void stopSharing(Activity mainActivity) {

    }
}
