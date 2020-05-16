package com.example.activitytrackerlibrary;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import java.util.ArrayList;

final class ManifestValidator {

    private final static String ourApplicationClassName = "com.example.activitytrackerlibrary";

    static void validate(final Context context, DeviceInfo deviceInfo) {
        if (!deviceInfo.testPermission(context, "android.permission.INTERNET")) {
            Logger.d("Missing Permission: android.permission.INTERNET");
        }
        checkSDKVersion(deviceInfo);
        validationApplicationLifecyleCallback(context);
        checkReceiversServices(context, deviceInfo);
    }

    private static void checkSDKVersion(DeviceInfo deviceInfo) {
        Logger.i("SDK Version Code is " + deviceInfo.getSdkVersion());
    }

    private static void checkReceiversServices(final Context context, DeviceInfo deviceInfo) {
        try {
            validateReceiverInManifest((Application) context.getApplicationContext(), PushNotificationReceiver.class.getName());
            validateServiceInManifest((Application) context.getApplicationContext(), NotificationIntentService.class.getName());
            validateServiceInManifest((Application) context.getApplicationContext(), BackgroundJobService.class.getName());
            validateServiceInManifest((Application) context.getApplicationContext(), ActivityBackgroundIntentService.class.getName());
            validateActivityInManifest((Application) context.getApplicationContext(), AppNotificationActivity.class);
        } catch (Exception e) {
            Logger.v("Receiver/Service issue : " + e.toString());

        }
        ArrayList<PushType> enabledPushTypes = deviceInfo.getEnabledPushTypes();
        if (enabledPushTypes == null) return;
        for (PushType pushType : enabledPushTypes) {
            //no-op
            if (pushType == PushType.FCM) {
                try {
                    // use class name string directly here to avoid class not found issues on class import, because we only use FCM
                    validateServiceInManifest((Application) context.getApplicationContext(), "com.example.activitytrackerlibrary.FcmMessageListenerService");
                    validateServiceInManifest((Application) context.getApplicationContext(), "com.example.activitytrackerlibrary.FcmTokenListenerService");
                } catch (Exception e) {
                    Logger.v("Receiver/Service issue : " + e.toString());

                } catch (Error error) {
                    Logger.v("FATAL : " + error.getMessage());

                }
            }
        }

    }

    private static void validateReceiverInManifest(Application application, String receiverClassName) throws PackageManager.NameNotFoundException {
        PackageManager pm = application.getPackageManager();
        String packageName = application.getPackageName();

        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_RECEIVERS);
        ActivityInfo[] receivers = packageInfo.receivers;

        for (ActivityInfo activityInfo : receivers) {
            if (activityInfo.name.equals(receiverClassName)) {
                Logger.i(receiverClassName.replaceFirst("com.example.activitytrackerlibrary", "") + " is present");
                return;
            }
        }
        Logger.i(receiverClassName.replaceFirst("com.example.activitytrackerlibrary", "") + " not present");
    }


    private static void validateServiceInManifest(Application application, String serviceClassName) throws PackageManager.NameNotFoundException {
        PackageManager pm = application.getPackageManager();
        String packageName = application.getPackageName();

        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES);
        ServiceInfo[] services = packageInfo.services;
        for (ServiceInfo serviceInfo : services) {
            if (serviceInfo.name.equals(serviceClassName)) {
                Logger.i(serviceClassName.replaceFirst("com.example.activitytrackerlibrary.", "") + " is present");
                return;
            }
        }
        Logger.i(serviceClassName.replaceFirst("com.example.activitytrackerlibrary.", "") + " not present");
    }

    @SuppressWarnings("SameParameterValue")
    private static void validateActivityInManifest(Application application, Class activityClass) throws PackageManager.NameNotFoundException {
        PackageManager pm = application.getPackageManager();
        String packageName = application.getPackageName();

        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        ActivityInfo[] activities = packageInfo.activities;
        String activityClassName = activityClass.getName();
        for (ActivityInfo activityInfo : activities) {
            if (activityInfo.name.equals(activityClassName)) {
                Logger.i(activityClassName.replaceFirst("com.example.activitytrackerlibrary.", "") + " is present");
                return;
            }
        }
        Logger.i(activityClassName.replaceFirst("com.example.activitytrackerlibrary.", "") + " not present");
    }

    private static void validationApplicationLifecyleCallback(final Context context) {
        if (!ActivityLifecycleCallback.registered && !TrackerApi.isAppForeground()) {
            Logger.i("Activity Lifecycle Callback not registered. Either set the android:name in your AndroidManifest.xml application tag to com.example.activitytrackerlibrary.Application, \n or, " +
                    "if you have a custom Application class, call ActivityLifecycleCallback.register(this); before super.onCreate() in your class");
            //Check for Application class only if the application lifecycle seems to be a problem
            checkApplicationClass(context);
        }
    }

    private static void checkApplicationClass(final Context context) {
        String appName = context.getApplicationInfo().className;
        if (appName == null || appName.isEmpty()) {
            Logger.i("Unable to determine Application Class");
        } else if (appName.equals(ourApplicationClassName)) {
            Logger.i("AndroidManifest.xml uses the  Application class, " +
                    "be sure you have properly added the  Account ID and Token to your AndroidManifest.xml, \n" +
                    "or set them programmatically in the onCreate method of your custom application class prior to calling super.onCreate()");
        } else {
            Logger.i("Application Class is " + appName);
        }
    }
}

