package com.example.activitytrackerlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

public final class ActivityLifecycleCallback {
    static boolean registered = false;



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static synchronized void register(android.app.Application application, final String trackerID) {
        if (application == null) {
            Logger.i("Application instance is null/system API is too old");
            return;
        }

        if (registered) {
            Logger.v("Lifecycle callbacks have already been registered");
            return;
        }

        registered = true;
        application.registerActivityLifecycleCallbacks(
                new android.app.Application.ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        if(trackerID != null) {
                            TrackerApi.onActivityCreated(activity,trackerID);
                        }else{
                            TrackerApi.onActivityCreated(activity);
                        }
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {

                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        if(trackerID != null) {
                            TrackerApi.onActivityResumed(activity,trackerID);
                        }else{
                            TrackerApi.onActivityResumed(activity);
                        }
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        TrackerApi.onActivityPaused();
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {

                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                       TrackerApi.onActivityDestroyed();

                    }
                }

        );
        Logger.i("Activity Lifecycle Callback successfully registered");
    }

    /**
     * Enables lifecycle callbacks for Android devices
     * @param application App's Application object
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static synchronized void register(android.app.Application application) {
        register(application,null);
    }
}
