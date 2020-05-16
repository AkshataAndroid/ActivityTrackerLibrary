package com.example.activitytrackerlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import static com.example.activitytrackerlibrary.LocationService.ACTION_LOCATION_BROADCAST;
import static com.example.activitytrackerlibrary.Utils.runOnUiThread;

public class TrackerApi {
    private static HashMap<String, TrackerApi> instances;
    private static String sdkVersion;
    private static ActivityInstanceConfig defaultConfig;
    private ActivityInstanceConfig config;
    private long EXECUTOR_THREAD_ID = 0;
    private int networkRetryCount = 0;
    private int maxDelayFrequency = 1000 * 60 * 10;
    private int minDelayFrequency = 0;
    private ExecutorService es;
    private ExecutorService ns;
    public static int lastSessionTime;
    private static boolean appForeground = false;
    Context context;
    private boolean offline = false;
    private DBAdapter dbAdapter;
    private final Object notificationMapLock = new Object();
    private static int debugLevel = TrackerApi.LogLevel.INFO.intValue();
    private static SSLContext sslContext;

    private final Object optOutFlagLock = new Object();
    private boolean currentUserOptedOut = false;
    private final Object appLaunchPushedLock = new Object();
    private boolean appLaunchPushed = false;
    private Handler handlerLooper;
    private final Boolean eventLock = true;
    private static int activityCount = 0;
    private boolean isBgPing = false;
    private SyncListener syncListener = null;
    private LocalData localData;
    private Runnable pushNotificationViewedRunnable = null;
    private Runnable commsRunnable = null;
    private static final Boolean pendingValidationResultsLock = true;
    public String currentScreenName = "";
    private ArrayList<ValidationResult> pendingValidationResults = new ArrayList<>();
    private boolean firstSession = false;
    public int lastSessionLength = 0;
    private int currentSessionId = 0;
    public DeviceInfo deviceInfo;
    public  LocationService locationService;
    private InAppFCManager inAppFCManager;
    public int lastVisitTime;
    private int lastLocationPingTime = 0;


    private Validator validator;
    private String source = null, medium = null, campaign = null;
    private Runnable pendingInappRunnable = null;
    private final HashMap<String, Object> notificationIdTagMap = new HashMap<>();
    private final Object displayUnitControllerLock = new Object();
    private DisplayUnitController mDisplayUnitController;
    private WeakReference<DisplayUnitListener> displayUnitListenerWeakReference;
    private long NOTIFICATION_THREAD_ID = 0;
    private HashSet<String> inappActivityExclude = null;
    private static ArrayList<InAppNotification> pendingNotifications = new ArrayList<>();
    private static InAppNotification currentlyDisplayingInApp = null;
    private static int initialAppEnteredForegroundTime = 0;
    private boolean installReferrerDataSent = false;
    public Location locationFromUser = null;
    private boolean enableNetworkInfoReporting = false;
    public  static long appLastSeen = 0;
public static  String latitude;
    public static  String longitude;
    public  static String background;
   public  EventDetails ed;
   public JSONObject event;
   DisplayUnitController displayUnitController;


    private JSONObject wzrkParams = null;

    public static WeakReference<Activity> currentActivity;
    static boolean haveVideoPlayerSupport;

    static {
        haveVideoPlayerSupport = checkForExoPlayer();
    }

    private static boolean checkForExoPlayer() {
        boolean exoPlayerPresent = false;
        Class className = null;
        try {
            className = Class.forName("com.google.android.exoplayer2.ExoPlayerFactory");
            className = Class.forName("com.google.android.exoplayer2.source.hls.HlsMediaSource");
            className = Class.forName("com.google.android.exoplayer2.ui.PlayerView");
            Logger.d("ExoPlayer is present");
            exoPlayerPresent = true;
        } catch (Throwable t) {
            Logger.d("ExoPlayer library files are missing!!!");
            Logger.d("Please add ExoPlayer dependencies to render InApp or Inbox messages playing video. For more information checkout App documentation.");
            if (className != null)
                Logger.d("ExoPlayer classes not found " + className.getName());
            else
                Logger.d("ExoPlayer classes not found");
        }
        return exoPlayerPresent;
    }

    static void onActivityCreated(Activity activity, String trackerID) {
        // make sure we have at least the default instance created here.
        background="App Created";
        if (instances == null) {
            TrackerApi.createInstanceIfAvailable(activity.getApplicationContext(), null, trackerID);
        }

        if (instances == null) {
            Logger.v("Instances is null in onActivityCreated!");
            return;
        }

        boolean alreadyProcessedByApp = false;
        Bundle notification = null;
        Uri deepLink = null;
        String _accountId = null;

    }

    public static TrackerApi instanceWithConfig(Context context, ActivityInstanceConfig config) {
        return instanceWithConfig(context, config, null);
    }





    private synchronized void setSource(String source) {
        if (this.source == null) {
            this.source = source;
        }
    }

    // only set if not already set during the session
    private synchronized void setMedium(String medium) {
        if (this.medium == null) {
            this.medium = medium;
        }
    }

    private synchronized void setCampaign(String campaign) {
        if (this.campaign == null) {
            this.campaign = campaign;
        }
    }

    private void processDisplayUnitsResponse(JSONObject response) {
        if (response == null) {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Can't parse Display Unit Response, JSON response object is null");
            return;
        }

        if (!response.has(Constants.DISPLAY_UNIT_JSON_RESPONSE_KEY)) {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "JSON object doesn't contain the Display Units key");
            return;
        }
        try {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Processing Display Unit response");
            parseDisplayUnits(response.getJSONArray(Constants.DISPLAY_UNIT_JSON_RESPONSE_KEY));
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Failed to parse response", t);
        }
    }
    private void parseDisplayUnits(JSONArray messages) {
        if (messages == null || messages.length() == 0) {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Can't parse Display Units, jsonArray is either empty or null");
            return;
        }

        synchronized (displayUnitControllerLock) {// lock to avoid multiple instance creation for controller
            if (mDisplayUnitController == null) {
                mDisplayUnitController = new DisplayUnitController();
            }
        }
        ArrayList<DisplayUnit> displayUnits = mDisplayUnitController.updateDisplayUnits(messages);

        notifyDisplayUnitsLoaded(displayUnits);
    }
    private void notifyDisplayUnitsLoaded(final ArrayList<DisplayUnit> displayUnits) {
        if (displayUnits != null && !displayUnits.isEmpty()) {
            if (displayUnitListenerWeakReference != null && displayUnitListenerWeakReference.get() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //double check to ensure null safety
                        if (displayUnitListenerWeakReference != null && displayUnitListenerWeakReference.get() != null) {
                            displayUnitListenerWeakReference.get().onDisplayUnitsLoaded(displayUnits);
                        }
                    }
                });
            } else {
                getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "No registered listener, failed to notify");
            }
        } else {
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "No Display Units found");
        }
    }

    private void handleSendTestForDisplayUnits(Bundle extras) {
        try {
            String pushJsonPayload = extras.getString(Constants.DISPLAY_UNIT_PREVIEW_PUSH_PAYLOAD_KEY);
            Logger.v("Received Display Unit via push payload: " + pushJsonPayload);
            JSONObject r = new JSONObject();
            JSONArray displayUnits = new JSONArray();
            r.put(Constants.DISPLAY_UNIT_JSON_RESPONSE_KEY, displayUnits);
            JSONObject testPushObject = new JSONObject(pushJsonPayload);
            displayUnits.put(testPushObject);
            processDisplayUnitsResponse(r);
        } catch (Throwable t) {
            Logger.v("Failed to process Display Unit from push notification payload", t);
        }
    }
    private void processInAppResponse(final JSONObject response, final Context context) {
        try {
            getConfigLogger().verbose(getAccountId(), "InApp: Processing response");

            if (!response.has("inapp_notifs")) {
                getConfigLogger().verbose(getAccountId(), "InApp: Response JSON object doesn't contain the inapp key, bailing");
                return;
            }

            int perSession = 10;
            int perDay = 10;
            if (response.has(Constants.INAPP_MAX_PER_SESSION) && response.get(Constants.INAPP_MAX_PER_SESSION) instanceof Integer) {
                perSession = response.getInt(Constants.INAPP_MAX_PER_SESSION);
            }

            if (response.has("imp") && response.get("imp") instanceof Integer) {
                perDay = response.getInt("imp");
            }

            if(inAppFCManager != null) {
                Logger.v("Updating InAppFC Limits");
                inAppFCManager.updateLimits(context, perDay, perSession);
            }

            JSONArray inappNotifs;
            try {
                inappNotifs = response.getJSONArray(Constants.INAPP_JSON_RESPONSE_KEY);
            } catch (JSONException e) {
                getConfigLogger().debug(getAccountId(), "InApp: In-app key didn't contain a valid JSON array");
                return;
            }

            // Add all the new notifications to the queue
            SharedPreferences prefs = StorageHelper.getPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            try {
                JSONArray inappsFromPrefs = new JSONArray(getStringFromPrefs(Constants.INAPP_KEY, "[]"));

                // Now add the rest of them :)
                if (inappNotifs != null && inappNotifs.length() > 0) {
                    for (int i = 0; i < inappNotifs.length(); i++) {
                        try {
                            JSONObject inappNotif = inappNotifs.getJSONObject(i);
                            inappsFromPrefs.put(inappNotif);
                        } catch (JSONException e) {
                            Logger.v("InAppManager: Malformed inapp notification");
                        }
                    }
                }

                // Commit all the changes
                editor.putString(storageKeyWithSuffix(Constants.INAPP_KEY), inappsFromPrefs.toString());
                StorageHelper.persist(editor);
            } catch (Throwable e) {
                getConfigLogger().verbose(getAccountId(), "InApp: Failed to parse the in-app notifications properly");
                getConfigLogger().verbose(getAccountId(), "InAppManager: Reason: " + e.getMessage(), e);
            }
            // Fire the first notification, if any
//            runOnNotificationQueue(new Runnable() {
//                @Override
//                public void run() {
//                    _showNotificationIfAvailable(context);
//                }
//            });
        } catch (Throwable t) {
            Logger.v("InAppManager: Failed to parse response", t);
        }
    }

    private boolean canShowInAppOnActivity() {
        updateBlacklistedActivitySet();

        for (String blacklistedActivity : inappActivityExclude) {
            String currentActivityName = getCurrentActivityName();
           // Log.d("Akku",currentActivity);
            if (currentActivityName != null && currentActivityName.contains(blacklistedActivity)) {
                return false;
            }
        }

        return true;
    }

    public static Activity getCurrentActivity() {
        return (currentActivity == null) ? null : currentActivity.get();
    }
    public static String getCurrentActivityName() {
        Activity current = getCurrentActivity();
        return (current != null) ? current.getLocalClassName() : null;
    }
    private void updateBlacklistedActivitySet() {
        if (inappActivityExclude == null) {
            inappActivityExclude = new HashSet<>();
            try {
                String activities = ManifestInfo.getInstance(context).getExcludedActivities();
                if (activities != null) {
                    String[] split = activities.split(",");
                    for (String a : split) {
                        inappActivityExclude.add(a.trim());
                    }
                }
            } catch (Throwable t) {
                // Ignore
            }
            getConfigLogger().debug(getAccountId(), "In-app notifications will not be shown on " + Arrays.toString(inappActivityExclude.toArray()));
        }
    }
//    private void _showNotificationIfAvailable(Context context) {
//        SharedPreferences prefs = StorageHelper.getPreferences(context);
//        try {
//            if (!canShowInAppOnActivity()) {
//                Logger.v("Not showing notification on blacklisted activity");
//                return;
//            }
//
//            checkPendingNotifications(context, config);  // see if we have any pending notifications
//
//            JSONArray inapps = new JSONArray(getStringFromPrefs(Constants.INAPP_KEY, "[]"));
//            if (inapps.length() < 1) {
//                return;
//            }
//
//            JSONObject inapp = inapps.getJSONObject(0);
//            prepareNotificationForDisplay(inapp);
//
//            // JSON array doesn't have the feature to remove a single element,
//            // so we have to copy over the entire array, but the first element
//            JSONArray inappsUpdated = new JSONArray();
//            for (int i = 0; i < inapps.length(); i++) {
//                if (i == 0) continue;
//                inappsUpdated.put(inapps.get(i));
//            }
//            SharedPreferences.Editor editor = prefs.edit().putString(storageKeyWithSuffix(Constants.INAPP_KEY), inappsUpdated.toString());
//            StorageHelper.persist(editor);
//        } catch (Throwable t) {
//            // We won't get here
//            getConfigLogger().verbose(getAccountId(), "InApp: Couldn't parse JSON array string from prefs", t);
//        }
//    }
//    private static void checkPendingNotifications(final Context context, final ActivityInstanceConfig config) {
//        Logger.v(config.getAccountId(), "checking Pending Notifications");
//        if (pendingNotifications != null && !pendingNotifications.isEmpty()) {
//            try {
//                final InAppNotification notification = pendingNotifications.get(0);
//                pendingNotifications.remove(0);
//                Handler mainHandler = new Handler(context.getMainLooper());
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        showInApp(context, notification, config);
//                    }
//                });
//            } catch (Throwable t) {
//                // no-op
//            }
//        }
//    }
//    private static void showInApp(Context context, final InAppNotification inAppNotification, ActivityInstanceConfig config) {
//
//        Logger.v(config.getAccountId(), "Attempting to show next In-App");
//
//        if (!appForeground) {
//            pendingNotifications.add(inAppNotification);
//            Logger.v(config.getAccountId(), "Not in foreground, queueing this In App");
//            return;
//        }
//
//        if (currentlyDisplayingInApp != null) {
//            pendingNotifications.add(inAppNotification);
//            Logger.v(config.getAccountId(), "In App already displaying, queueing this In App");
//            return;
//        }
//
//
//        currentlyDisplayingInApp = inAppNotification;
//
//        AppBaseFragment inAppFragment = null;
//        InAppType type = inAppNotification.getInAppType();
//        switch (type) {
//            case AppTypeCoverHTML:
//            case AppTypeInterstitialHTML:
//            case AppTypeHalfInterstitialHTML:
//            case AppTypeCover:
//            case AppTypeHalfInterstitial:
//            case AppTypeInterstitial:
//            case AppTypeAlert:
//            case AppTypeInterstitialImageOnly:
//            case AppTypeHalfInterstitialImageOnly:
//            case AppTypeCoverImageOnly:
//
//                Intent intent = new Intent(context, AppNotificationActivity.class);
//                intent.putExtra("inApp", inAppNotification);
//                Bundle configBundle = new Bundle();
//                configBundle.putParcelable("config", config);
//                intent.putExtra("configBundle", configBundle);
//                try {
//                    Activity currentActivity = getCurrentActivity();
//                    if (currentActivity == null) {
//                        throw new IllegalStateException("Current activity reference not found");
//                    }
//                    config.getLogger().verbose(config.getAccountId(), "calling InAppActivity for notification: " + inAppNotification.getJsonDescription());
//                    currentActivity.startActivity(intent);
//                    Logger.d("Displaying In-App: " + inAppNotification.getJsonDescription());
//
//                } catch (Throwable t) {
//                    Logger.v("Please verify the integration of your app." +
//                            " It is not setup to support in-app notifications yet.", t);
//                }
//                break;
////            case AppTypeFooterHTML:
////                inAppFragment = new AppHtmlFooterFragment();
////                break;
////            case AppTypeHeaderHTML:
////                inAppFragment = new AppHtmlHeaderFragment();
////                break;
////            case AppTypeFooter:
////                inAppFragment = new AppNativeFooterFragment();
////                break;
////            case AppTypeHeader:
////                inAppFragment = new AppNativeHeaderFragment();
////                break;
//            default:
//                Logger.d(config.getAccountId(), "Unknown InApp Type found: " + type);
//                currentlyDisplayingInApp = null;
//                return;
//        }
//
//        if (inAppFragment != null) {
//            Logger.d("Displaying In-App: " + inAppNotification.getJsonDescription());
//            try {
//                //noinspection ConstantConditions
//                FragmentTransaction fragmentTransaction = getCurrentActivity().getFragmentManager().beginTransaction();
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("inApp", inAppNotification);
//                bundle.putParcelable("config", config);
//              //  inAppFragment.setArguments(bundle);
//                fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
//                fragmentTransaction.add(android.R.id.content, inAppFragment, inAppNotification.getType());
//                Logger.v(config.getAccountId(), "calling InAppFragment " + inAppNotification.getCampaignId());
//                fragmentTransaction.commit();
//
//            } catch (Throwable t) {
//                Logger.v(config.getAccountId(), "Fragment not able to render", t);
//            }
//        }
//    }

//    private void prepareNotificationForDisplay(final JSONObject jsonObject) {
//        getConfigLogger().debug(getAccountId(), "Preparing In-App for display: " + jsonObject.toString());
//        runOnNotificationQueue(new NotificationPrepareRunnable(this, jsonObject));
//    }
//    private final class NotificationPrepareRunnable implements Runnable {
//        private final WeakReference<TrackerApi> APIWeakReference;
//        private final JSONObject jsonObject;
//        private boolean videoSupport = haveVideoPlayerSupport;
//
//        NotificationPrepareRunnable(TrackerApi trackerApi, JSONObject jsonObject) {
//            this.APIWeakReference = new WeakReference<>(trackerApi);
//            this.jsonObject = jsonObject;
//        }
//
//        @Override
//        public void run() {
//            final InAppNotification inAppNotification = new InAppNotification().initWithJSON(jsonObject, videoSupport);
//            if (inAppNotification.getError() != null) {
//                getConfigLogger().debug(getAccountId(), "Unable to parse inapp notification " + inAppNotification.getError());
//                return;
//            }
//            inAppNotification.listener = (InAppNotification.AppNotificationListener) APIWeakReference.get();
//            inAppNotification.prepareForDisplay();
//        }
//    }

//    private void processInboxResponse(final JSONObject response) {
//        if (getConfig().isAnalyticsOnly()) {
//            getConfigLogger().verbose(getAccountId(), "Tracker instance is configured to analytics only, not processing inbox messages");
//            return;
//        }
//
//        getConfigLogger().verbose(getAccountId(), "Inbox: Processing response");
//
//        if (!response.has("inbox_notifs")) {
//            getConfigLogger().verbose(getAccountId(), "Inbox: Response JSON object doesn't contain the inbox key");
//            return;
//        }
////        try {
////            _processInboxMessages(response.getJSONArray("inbox_notifs"));
////        } catch (Throwable t) {
////            getConfigLogger().verbose(getAccountId(), "InboxResponse: Failed to parse response", t);
////        }
//    }
    private synchronized void setWzrkParams(JSONObject wzrkParams) {
        if (this.wzrkParams == null) {
            this.wzrkParams = wzrkParams;
        }
    }
    private static @Nullable
    TrackerApi createInstanceIfAvailable(Context context, String _accountId, String trackerID) {
        try {
            if (_accountId == null) {
                try {
                    return TrackerApi.getDefaultInstance(context, trackerID);
                } catch (Throwable t) {
                    Logger.v("Error creating shared Instance: ", t.getCause());
                    return null;
                }
            }
            String configJson = StorageHelper.getString(context, "instance:" + _accountId, "");
            if (!configJson.isEmpty()) {
                ActivityInstanceConfig config = ActivityInstanceConfig.createInstance(configJson);
                Logger.v("Inflated Instance Config: " + configJson);
                return config != null ? TrackerApi.instanceWithConfig(context, config, trackerID) : null;
            } else {
                try {
                    TrackerApi instance = TrackerApi.getDefaultInstance(context);
                    return (instance != null && instance.config.getAccountId().equals(_accountId)) ? instance : null;
                } catch (Throwable t) {
                    Logger.v("Error creating shared Instance: ", t.getCause());
                    return null;
                }
            }
        } catch (Throwable t) {
            return null;
        }
    }
    public void pushNotificationClickedEvent(String unitID) {
         event = new JSONObject();

        try {
            event.put("evtName", Constants.NOTIFICATION_CLICKED_EVENT_NAME);

            //wzrk fields
            if (displayUnitController != null) {
                DisplayUnit displayUnit = displayUnitController.getDisplayUnitForID(unitID);
                if (displayUnit != null) {
                    JSONObject eventExtraData = displayUnit.getWZRKFields();
                    if (eventExtraData != null) {
                        event.put("evtData", eventExtraData);
                        try {
                            setWzrkParams(eventExtraData);
                        } catch (Throwable t) {
                            // no-op
                        }
                    }
                }
            }

            queueEvent(context, event, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
            getConfigLogger().verbose(getAccountId(), Constants.FEATURE_DISPLAY_UNIT + "Failed to push Display Unit clicked event" + t);
        }
    }


    static void onActivityCreated(Activity activity) {
        onActivityCreated(activity, null);
    }
    public static void onActivityResumed(Activity activity) {
        onActivityResumed(activity, null);
    }
    public static void setAppForeground(boolean appForeground) {
        TrackerApi.appForeground = appForeground;
    }
    private static void setCurrentActivity(@Nullable Activity activity) {
        if (activity == null) {
            currentActivity = null;
            return;
        }
        if (!activity.getLocalClassName().contains("InAppNotificationActivity")) {
            currentActivity = new WeakReference<>(activity);
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static void onActivityResumed(Activity activity, String trackerID) {

        background="App Resumed";
        if (instances == null) {
            TrackerApi.createInstanceIfAvailable(activity.getApplicationContext(), null, trackerID);
        }

        TrackerApi.setAppForeground(true);

        if (instances == null) {
            Logger.v("Instances is null in onActivityResumed!");
            return;
        }

        String currentActivityName = getCurrentActivityName();
        setCurrentActivity(activity);
        if (currentActivityName == null || !currentActivityName.equals(activity.getLocalClassName())) {
            activityCount++;
        }

        if (initialAppEnteredForegroundTime <= 0) {
            initialAppEnteredForegroundTime = (int) System.currentTimeMillis() / 1000;
        }

        for (String accountId : TrackerApi.instances.keySet()) {
            TrackerApi instance = TrackerApi.instances.get(accountId);
            try {
                if (instance != null) {
                    instance.activityResumed(activity);
                }
            } catch (Throwable t) {
                Logger.v("Throwable - "+t.getLocalizedMessage());
            }
        }
    }
    public void handleInstallReferrerOnFirstInstall() {
        getConfigLogger().verbose(getAccountId(), "Starting to handle install referrer");
        try {
            final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            // Connection established.
                            ReferrerDetails response = null;
                            try {
                                response = referrerClient.getInstallReferrer();
                                String referrerUrl = response.getInstallReferrer();
                               // referrerClickTime = response.getReferrerClickTimestampSeconds();
                               // appInstallTime = response.getInstallBeginTimestampSeconds();
                                //pushInstallReferrer(referrerUrl);
                                installReferrerDataSent = true;
                                getConfigLogger().debug(getAccountId(), "Install Referrer data set");
                            } catch (RemoteException e) {
                                getConfigLogger().debug(getAccountId(),"Remote exception caused by Google Play Install Referrer library - "+e.getMessage());
                                referrerClient.endConnection();
                                installReferrerDataSent = false;
                            }
                            referrerClient.endConnection();
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            // API not available on the current Play Store app.
                            getConfigLogger().debug(getAccountId(), "Install Referrer data not set, API not supported by Play Store on device");
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            // Connection couldn't be established.
                            getConfigLogger().debug(getAccountId(), "Install Referrer data not set, connection to Play Store unavailable");
                            break;
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    if (!installReferrerDataSent) {
                        handleInstallReferrerOnFirstInstall();
                    }
                }
            });
        }catch(Throwable t){
            getConfigLogger().verbose(getAccountId(),"Google Play Install Referrer's InstallReferrerClient Class not found - " +t.getLocalizedMessage() + " \n Please add implementation \'com.android.installreferrer:installreferrer:1.0\' to your build.gradle");
        }
    }

    private void activityResumed(Activity activity) {
        getConfigLogger().verbose(getAccountId(), "App in foreground");
        checkTimeoutSession();
        if (!isAppLaunchPushed()) {
            pushAppLaunchedEvent();
            onTokenRefresh();
            postAsyncSafely("HandlingInstallReferrer", new Runnable() {
                @Override
                public void run() {
                    if (!installReferrerDataSent && isFirstSession()) {
                        handleInstallReferrerOnFirstInstall();
                    }
                }
            });
        }
        if (!inCurrentSession()) {
            pushInitialEventsAsync();
        }
       // checkExistingInAppNotifications(activity);
       // checkPendingInAppNotifications(activity);
    }
//    private void checkPendingInAppNotifications(Activity activity) {
//        final boolean canShow = canShowInAppOnActivity();
//        if (canShow) {
//            if (pendingInappRunnable != null) {
//                getConfigLogger().verbose(getAccountId(), "Found a pending inapp runnable. Scheduling it");
//                gethandlerLooper().postDelayed(pendingInappRunnable, 200);
//                pendingInappRunnable = null;
//            } else {
//                showNotificationIfAvailable(context);
//            }
//        } else {
//            Logger.d("In-app notifications will not be shown for this activity ("
//                    + (activity != null ? activity.getLocalClassName() : "") + ")");
//        }
//    }
//    private void showNotificationIfAvailable(final Context context) {
//        if (!this.config.isAnalyticsOnly()) {
//            runOnNotificationQueue(new Runnable() {
//                @Override
//                public void run() {
//                    _showNotificationIfAvailable(context);
//                }
//            });
//        }
//    }
//    private void checkExistingInAppNotifications(Activity activity) {
//        final boolean canShow = canShowInAppOnActivity();
//        if (canShow) {
//            if (currentlyDisplayingInApp != null) {
//                Fragment inAppFragment = activity.getFragmentManager().getFragment(new Bundle(), currentlyDisplayingInApp.getType());
//                if (getCurrentActivity() != null) {
//                    FragmentTransaction fragmentTransaction = getCurrentActivity().getFragmentManager().beginTransaction();
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("inApp", currentlyDisplayingInApp);
//                    bundle.putParcelable("config", config);
//                    inAppFragment.setArguments(bundle);
//                    fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
//                    fragmentTransaction.add(android.R.id.content, inAppFragment, currentlyDisplayingInApp.getType());
//                    Logger.v(config.getAccountId(), "calling InAppFragment " + currentlyDisplayingInApp.getCampaignId());
//                    fragmentTransaction.commit();
//                }
//            }
//        }
//    }
    private void checkTimeoutSession() {
//        if (appLastSeen <= 0) return;
//        long now = System.currentTimeMillis();
//        if ((now - appLastSeen) > Constants.SESSION_LENGTH_MINS * 60 * 1000) {
//            getConfigLogger().verbose(getAccountId(), "Session Timed Out");
//            destroySession();
//            setCurrentActivity(null);
//        }
    }
    private void onTokenRefresh() {
//        if (enabledPushTypes == null) {
//            enabledPushTypes = this.deviceInfo.getEnabledPushTypes();
//        }
//        if (enabledPushTypes == null) return;
//        for (PushType pushType : enabledPushTypes) {
//            if (pushType == PushType.FCM) {
//                doFCMRefresh();
//                break;
//            }
//        }
    }
    private boolean isAppLaunchReportingDisabled() {
        return this.config.isDisableAppLaunchedEvent();
    }
    private void setAppLaunchPushed(boolean pushed) {
        synchronized (appLaunchPushedLock) {
            appLaunchPushed = pushed;
        }
    }

    private void pushAppLaunchedEvent() {
        if (isAppLaunchReportingDisabled()) {
            setAppLaunchPushed(true);
            getConfigLogger().debug(getAccountId(), "App Launched Events disabled in the Android Manifest file");
            return;
        }
        if (isAppLaunchPushed()) {
            getConfigLogger().verbose(getAccountId(), "App Launched has already been triggered. Will not trigger it ");
            return;
        } else {
            getConfigLogger().verbose(getAccountId(), "Firing App Launched event");
        }
        setAppLaunchPushed(true);
        JSONObject event = new JSONObject();
        try {
            event.put("evtName", Constants.APP_LAUNCHED_EVENT);
            event.put("evtData", getAppLaunchedFields());
        } catch (Throwable t) {
            // We won't get here
        }
        queueEvent(context, event, Constants.RAISED_EVENT);
    }
    private boolean deviceIsMultiUser() {
        JSONObject cachedGUIDs = getCachedGUIDs();
        return cachedGUIDs.length() > 1;
    }


    public JSONObject getAppLaunchedFields() {
        try {
            final JSONObject evtData = new JSONObject();

            evtData.put("Build", this.deviceInfo.getBuild() + "");
            evtData.put("Version", this.deviceInfo.getVersionName());
            evtData.put("OS Version", this.deviceInfo.getOsVersion());
            evtData.put("SDK Version", this.deviceInfo.getSdkVersion());
           // evtData.put("Akshata", latitude);

            if (locationFromUser != null) {
                evtData.put("Latitude", locationFromUser.getLatitude());
                evtData.put("Longitude", locationFromUser.getLongitude());
            }

            // send up googleAdID
            if (this.deviceInfo.getGoogleAdID() != null) {
                String baseAdIDKey = "GoogleAdID";
                String adIDKey = deviceIsMultiUser() ? Constants.MULTI_USER_PREFIX + baseAdIDKey : baseAdIDKey;
                evtData.put(adIDKey, this.deviceInfo.getGoogleAdID());
                evtData.put("GoogleAdIDLimit", this.deviceInfo.isLimitAdTrackingEnabled());
            }

            try {
                // Device data
                evtData.put("Make", this.deviceInfo.getManufacturer());
                evtData.put("Model", this.deviceInfo.getModel());
                evtData.put("Carrier", this.deviceInfo.getCarrier());
                evtData.put("useIP", enableNetworkInfoReporting);
                evtData.put("OS", this.deviceInfo.getOsName());
                evtData.put("wdt", this.deviceInfo.getWidth());
                evtData.put("hgt", this.deviceInfo.getHeight());
                evtData.put("dpi", this.deviceInfo.getDPI());

                if (this.deviceInfo.getLibrary() != null) {
                    evtData.put("lib", this.deviceInfo.getLibrary());
                }

                String cc = this.deviceInfo.getCountryCode();
                if (cc != null && !cc.equals(""))
                    evtData.put("cc", cc);

                if (enableNetworkInfoReporting) {
                    final Boolean isWifi = this.deviceInfo.isWifiConnected();
                    if (isWifi != null) {
                        evtData.put("wifi", isWifi);
                    }

                    final Boolean isBluetoothEnabled = this.deviceInfo.isBluetoothEnabled();
                    if (isBluetoothEnabled != null) {
                        evtData.put("BluetoothEnabled", isBluetoothEnabled);
                    }

                    final String bluetoothVersion = this.deviceInfo.getBluetoothVersion();
                    if (bluetoothVersion != null) {
                        evtData.put("BluetoothVersion", bluetoothVersion);
                    }

                    final String radio = this.deviceInfo.getNetworkType();
                    if (radio != null) {
                        evtData.put("Radio", radio);
                    }
                }

            } catch (Throwable t) {
                // Ignore
            }

            return evtData;
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Failed to construct App Launched event", t);
            return new JSONObject();
        }
    }

    public static void onActivityPaused() {
        if (instances == null) return;

        for (String accountId : TrackerApi.instances.keySet()) {
            TrackerApi instance = TrackerApi.instances.get(accountId);
            try {
                if (instance != null) {
                    instance.activityPaused();
                }
            } catch (Throwable t) {
                // Ignore
            }
        }
    }
    public static void onActivityDestroyed() {
        if (instances == null) return;

        for (String accountId : TrackerApi.instances.keySet()) {
            TrackerApi instance = TrackerApi.instances.get(accountId);
            try {
                if (instance != null) {
                    instance.activityDestroyed();
                }
            } catch (Throwable t) {
                // Ignore
            }
        }
    }
    public void activityPaused() {
        setAppForeground(false);
        background="App in background";
        appLastSeen = System.currentTimeMillis();
        getConfigLogger().verbose(getAccountId(), "App in background");
        final int now = (int) (System.currentTimeMillis() / 1000);

        if (inCurrentSession()) {
            try {
                StorageHelper.putInt(context, storageKeyWithSuffix(Constants.LAST_SESSION_EPOCH), now);
                getConfigLogger().verbose(getAccountId(), "Updated session time: " + now);
            } catch (Throwable t) {
                getConfigLogger().verbose(getAccountId(), "Failed to update session time time: " + t.getMessage());
            }
        }
    }
    public void activityDestroyed() {
        setAppForeground(false);
        background="App is Destroyed";
        appLastSeen = System.currentTimeMillis();
        getConfigLogger().verbose(getAccountId(), "App is destroyed ");
        final int now = (int) (System.currentTimeMillis() / 1000);

        if (inCurrentSession()) {
            try {
                StorageHelper.putInt(context, storageKeyWithSuffix(Constants.LAST_SESSION_EPOCH), now);
                getConfigLogger().verbose(getAccountId(), "Updated session time: " + now);
            } catch (Throwable t) {
                getConfigLogger().verbose(getAccountId(), "Failed to update session time time: " + t.getMessage());
            }
        }
    }
    static boolean isAppForeground() {
        return appForeground;
    }

    void deviceIDCreated(String deviceId) {
        Logger.v("Initializing InAppFC after Device ID Created = "+deviceId);
        this.inAppFCManager = new InAppFCManager(context, config, deviceId);
        Logger.v("Initializing ABTesting after Device ID Created = "+deviceId);
        //initABTesting();
        getConfigLogger().verbose("Got device id from DeviceInfo, notifying user profile initialized to SyncListener");
        notifyUserProfileInitialized(deviceId);
    }

//    public static TrackerApi instanceWithConfigs(Context context, @NonNull ActivityInstanceConfig config, String trackerID) {
//        //noinspection ConstantConditions
//        if (config == null) {
//            Logger.v("AppInstanceConfig cannot be null");
//            return null;
//        }
//        if (instances == null) {
//            instances = new HashMap<>();
//        }
//
//        TrackerApi instance = instances.get(config.getAccountId());
//        if (instance == null) {
//            instance = new TrackerApi(context, config, trackerID);
//            instances.put(config.getAccountId(), instance);
//            final TrackerApi finalInstance = instance;
//            instance.postAsyncSafely("notifyProfileInitialized",new Runnable() {
//                @Override
//                public void run() {
//                    if (finalInstance.getTrackerID() != null) {
//                        finalInstance.notifyUserProfileInitialized();
//                        finalInstance.recordDeviceIDErrors();
//                    }
//                }
//            });
//        } else if (instance.isErrorDeviceId() && instance.getConfig().getEnableCustomTrackerId() && Utils.validateCTID(trackerID)) {
//           // instance.asyncProfileSwitchUser(null, null, trackerID);
//        }
//        return instance;
//    }


    private void generateEmptyMultiValueError(String key) {
        ValidationResult error = new ValidationResult();
        String msg = "Invalid multi value for key " + key + ", profile multi value operation aborted.";
        error.setErrorCode(512);
        error.setErrorDesc(msg);
        pushValidationResult(error);
        getConfigLogger().debug(getAccountId(), msg);
    }
    private void pushValidationResult(ValidationResult vr) {
        synchronized (pendingValidationResultsLock) {
            try {
                int len = pendingValidationResults.size();
                if (len > 50) {
                    ArrayList<ValidationResult> trimmed = new ArrayList<>();
                    // Trim down the list to 40, so that this loop needn't run for the next 10 events
                    // Hence, skip the first 10 elements
                    for (int i = 10; i < len; i++)
                        trimmed.add(pendingValidationResults.get(i));
                    trimmed.add(vr);
                    pendingValidationResults = trimmed;
                } else {
                    pendingValidationResults.add(vr);
                }
            } catch (Exception e) {
                // no-op
            }
        }
    }

    private void handleMultiValues(ArrayList<String> values, String key, String command) {
        if (key == null) return;

        if (values == null || values.isEmpty()) {
            generateEmptyMultiValueError(key);
            return;
        }

        ValidationResult vr;

        // validate the key
        vr = validator.cleanMultiValuePropertyKey(key);

        // Check for an error
        if (vr.getErrorCode() != 0) {
            pushValidationResult(vr);
        }

        // reset the key
        Object _key = vr.getObject();
        String cleanKey = (_key != null) ? vr.getObject().toString() : null;

        // if key is empty generate an error and return
        if (cleanKey == null || cleanKey.isEmpty()) {
            generateInvalidMultiValueKeyError(key);
            return;
        }

        key = cleanKey;

        try {
            JSONArray currentValues = constructExistingMultiValue(key, command);
            JSONArray newValues = _cleanMultiValues(values, key);
            _validateAndPushMultiValue(currentValues, newValues, values, key, command);

        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Error handling multi value operation for key " + key, t);
        }
    }
    private JSONArray _cleanMultiValues(ArrayList<String> values, String key) {

        try {
            if (values == null || key == null) return null;

            JSONArray cleanedValues = new JSONArray();
            ValidationResult vr;

            // loop through and clean the new values
            for (String value : values) {
                value = (value == null) ? "" : value;  // so we will generate a validation error later on

                // validate value
                vr = validator.cleanMultiValuePropertyValue(value);

                // Check for an error
                if (vr.getErrorCode() != 0) {
                    pushValidationResult(vr);
                }

                // reset the value
                Object _value = vr.getObject();
                value = (_value != null) ? vr.getObject().toString() : null;

                // if value is empty generate an error and return
                if (value == null || value.isEmpty()) {
                    generateEmptyMultiValueError(key);
                    // Abort
                    return null;
                }
                // add to the newValues to be merged
                cleanedValues.put(value);
            }

            return cleanedValues;

        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Error cleaning multi values for key " + key, t);
            generateEmptyMultiValueError(key);
            return null;
        }
    }

    private void generateInvalidMultiValueKeyError(String key) {
        ValidationResult error = new ValidationResult();
        error.setErrorCode(523);
        error.setErrorDesc("Invalid multi-value property key " + key);
        pushValidationResult(error);
        getConfigLogger().debug(getAccountId(), "Invalid multi-value property key " + key + " profile multi value operation aborted");
    }
    private JSONArray constructExistingMultiValue(String key, String command) {

        boolean remove = command.equals(Constants.COMMAND_REMOVE);
        boolean add = command.equals(Constants.COMMAND_ADD);

        if (!remove && !add) return new JSONArray();

        Object existing = _getProfilePropertyIgnorePersonalizationFlag(key);

        // if there is no existing value
        if (existing == null) {
            // if its a remove then return null to abort operation
            // no point in running remove against a nonexistent value
            if (remove) return null;

            // otherwise return an empty array
            return new JSONArray();
        }

        // value exists

        // the value should only ever be a JSONArray or scalar (String really)

        // if its already a JSONArray return that
        if (existing instanceof JSONArray) return (JSONArray) existing;



        JSONArray _default = (add) ? new JSONArray() : null;

        String stringified = _stringifyAndCleanScalarProfilePropValue(existing);

        return (stringified != null) ? new JSONArray().put(stringified) : _default;
    }
    private String _stringifyScalarProfilePropValue(Object value) {
        String val = null;

        try {
            val = value.toString();
        } catch (Exception e) {
            // no-op
        }

        return val;
    }
    private String _stringifyAndCleanScalarProfilePropValue(Object value) {
        String val = _stringifyScalarProfilePropValue(value);

        if (val != null) {
            ValidationResult vr = validator.cleanMultiValuePropertyValue(val);

            // Check for an error
            if (vr.getErrorCode() != 0) {
                pushValidationResult(vr);
            }

            Object _value = vr.getObject();
            val = (_value != null) ? vr.getObject().toString() : null;
        }

        return val;
    }

    private Object _getProfilePropertyIgnorePersonalizationFlag(String key) {
        return getLocalDataStore().getProfileValueForKey(key);
    }
    private void _validateAndPushMultiValue(JSONArray currentValues, JSONArray newValues, ArrayList<String> originalValues, String key, String command) {

        try {

            // if any of these are null, indicates some problem along the way so abort operation
            if (currentValues == null || newValues == null || originalValues == null || key == null || command == null)
                return;

            String mergeOperation = command.equals(Constants.COMMAND_REMOVE) ? Validator.REMOVE_VALUES_OPERATION : Validator.ADD_VALUES_OPERATION;

            // merge currentValues and newValues
            ValidationResult vr = validator.mergeMultiValuePropertyForKey(currentValues, newValues, mergeOperation, key);

            // Check for an error
            if (vr.getErrorCode() != 0) {
                pushValidationResult(vr);
            }

            // set the merged local values array
            JSONArray localValues = (JSONArray) vr.getObject();

            // update local profile
            // remove an empty array
            if (localValues == null || localValues.length() <= 0) {
                getLocalDataStore().removeProfileField(key);
            } else {
                // not empty so save to local profile
                getLocalDataStore().setProfileField(key, localValues);
            }

            // push to server
            JSONObject commandObj = new JSONObject();
            commandObj.put(command, new JSONArray(originalValues));

            JSONObject fields = new JSONObject();
            fields.put(key, commandObj);

            pushBasicProfile(fields);

            getConfigLogger().verbose(getAccountId(), "Constructed multi-value profile push: " + fields.toString());

        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Error pushing multiValue for key " + key, t);
        }
    }


    public void setMultiValuesForKey(final String key, final ArrayList<String> values) {
        postAsyncSafely("setMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                handleMultiValues(values, key, Constants.COMMAND_SET);
            }
        });
    }
    public void pushEvent(String eventName) {
        if (eventName == null || eventName.trim().equals(""))
            return;

        pushEvent(eventName, null);
    }

    private static synchronized SSLContext getSSLContext() {
        if (sslContext == null) {
            sslContext = new SSLContextBuilder().build();
        }
        return sslContext;
    }
    public static void setDebugLevel(int level) {
        debugLevel = level;
    }
    public void pushEvent(String eventName, Map<String, Object> eventActions) {

        if (eventName == null || eventName.equals(""))
            return;

        ValidationResult validationResult = validator.isRestrictedEventName(eventName);
        // Check for a restricted event name
        if (validationResult.getErrorCode() > 0) {
            pushValidationResult(validationResult);
            return;
        }

        if (eventActions == null) {
            eventActions = new HashMap<>();
        }

         event = new JSONObject();
        try {
            // Validate
            ValidationResult vr = validator.cleanEventName(eventName);

            // Check for an error
            if (vr.getErrorCode() != 0)
                event.put(Constants.ERROR_KEY, getErrorObject(vr));

            eventName = vr.getObject().toString();
            JSONObject actions = new JSONObject();
            for (String key : eventActions.keySet()) {
                Object value = eventActions.get(key);
                vr = validator.cleanObjectKey(key);
                key = vr.getObject().toString();
                // Check for an error
                if (vr.getErrorCode() != 0)
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                try {
                    vr = validator.cleanObjectValue(value, Validator.ValidationContext.Event);
                } catch (IllegalArgumentException e) {
                    // The object was neither a String, Boolean, or any number primitives
                    ValidationResult error = new ValidationResult();
                    error.setErrorCode(512);
                    final String err = "For event \"" + eventName + "\": Property value for property " + key + " wasn't a primitive (" + value + ")";
                    error.setErrorDesc(err);
                    getConfigLogger().debug(getAccountId(), err);
                    pushValidationResult(error);
                    // Skip this record
                    continue;
                }
                value = vr.getObject();
                // Check for an error
                if (vr.getErrorCode() != 0)
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                actions.put(key, value);
            }
            event.put("evtName", eventName);
            event.put("evtData", actions);
            queueEvent(context, event, Constants.RAISED_EVENT);
        } catch (Throwable t) {
            // We won't get here
        }
    }
    public void pushProfile(final Map<String, Object> profile) {
        if (profile == null || profile.isEmpty())
            return;

        postAsyncSafely("profilePush", new Runnable() {
            @Override
            public void run() {
                _push(profile);
            }
        });
    }
    public void addMultiValueForKey(String key, String value) {
        if (value == null || value.isEmpty()) {
            generateEmptyMultiValueError(key);
            return;
        }

        addMultiValuesForKey(key, new ArrayList<>(Collections.singletonList(value)));
    }
    public void removeMultiValuesForKey(final String key, final ArrayList<String> values) {
        postAsyncSafely("removeMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                handleMultiValues(values, key, Constants.COMMAND_REMOVE);
            }
        });
    }
    private void _push(Map<String, Object> profile) {
        if (profile == null || profile.isEmpty())
            return;

        try {
            ValidationResult vr;
            JSONObject customProfile = new JSONObject();
            JSONObject fieldsToUpdateLocally = new JSONObject();
            for (String key : profile.keySet()) {
                Object value = profile.get(key);

                vr = validator.cleanObjectKey(key);
                key = vr.getObject().toString();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    pushValidationResult(vr);
                }

                if (key.isEmpty()) {
                    ValidationResult keyError = new ValidationResult();
                    keyError.setErrorCode(512);
                    final String keyErr = "Profile push key is empty";
                    keyError.setErrorDesc(keyErr);
                    pushValidationResult(keyError);
                    getConfigLogger().debug(getAccountId(), keyErr);
                    // Skip this property
                    continue;
                }

                try {
                    vr = validator.cleanObjectValue(value, Validator.ValidationContext.Profile);
                } catch (Throwable e) {
                    // The object was neither a String, Boolean, or any number primitives
                    ValidationResult error = new ValidationResult();
                    error.setErrorCode(512);
                    final String err = "Object value wasn't a primitive (" + value + ") for profile field " + key;
                    error.setErrorDesc(err);
                    pushValidationResult(error);
                    getConfigLogger().debug(getAccountId(), err);
                    // Skip this property
                    continue;
                }
                value = vr.getObject();
                // Check for an error
                if (vr.getErrorCode() != 0) {
                    pushValidationResult(vr);
                }

                // test Phone:  if no device country code, test if phone starts with +, log but always send
                if (key.equalsIgnoreCase("Phone")) {
                    try {
                        value = value.toString();
                        String countryCode = this.deviceInfo.getCountryCode();
                        if (countryCode == null || countryCode.isEmpty()) {
                            String _value = (String) value;
                            if (!_value.startsWith("+")) {
                                ValidationResult error = new ValidationResult();
                                error.setErrorCode(512);
                                final String err = "Device country code not available and profile phone: " + value + " does not appear to start with country code";
                                error.setErrorDesc(err);
                                pushValidationResult(error);
                                getConfigLogger().debug(getAccountId(), err);
                            }
                        }
                        getConfigLogger().verbose(getAccountId(), "Profile phone is: " + value + " device country code is: " + ((countryCode != null) ? countryCode : "null"));
                    } catch (Exception e) {
                        pushValidationResult(new ValidationResult(512, "Invalid phone number"));
                        getConfigLogger().debug(getAccountId(), "Invalid phone number: " + e.getLocalizedMessage());
                        continue;
                    }
                }

                // add to the local profile update object
                fieldsToUpdateLocally.put(key, value);
                customProfile.put(key, value);
            }

            getConfigLogger().verbose(getAccountId(), "Constructed custom profile: " + customProfile.toString());

            // update local profile values
            if (fieldsToUpdateLocally.length() > 0) {
                getLocalDataStore().setProfileFields(fieldsToUpdateLocally);
            }

            pushBasicProfile(customProfile);

        } catch (Throwable t) {
            // Will not happen
            getConfigLogger().verbose(getAccountId(), "Failed to push profile", t);
        }
    }

    public void removeValueForKey(final String key) {
        postAsyncSafely("removeValueForKey", new Runnable() {
            @Override
            public void run() {
                _removeValueForKey(key);
            }
        });
    }
    private void _removeValueForKey(String key) {
        try {
            key = (key == null) ? "" : key; // so we will generate a validation error later on

            // validate the key
            ValidationResult vr;

            vr = validator.cleanObjectKey(key);
            key = vr.getObject().toString();

            if (key.isEmpty()) {
                ValidationResult error = new ValidationResult();
                error.setErrorCode(512);
                error.setErrorDesc("Key is empty, profile removeValueForKey aborted.");
                pushValidationResult(error);
                getConfigLogger().debug(getAccountId(), "Key is empty, profile removeValueForKey aborted");
                // Abort
                return;
            }
            // Check for an error
            if (vr.getErrorCode() != 0) {
                pushValidationResult(vr);
            }

            // remove from the local profile
            getLocalDataStore().removeProfileField(key);

            // send the delete command
            JSONObject command = new JSONObject().put(Constants.COMMAND_DELETE, true);
            JSONObject update = new JSONObject().put(key, command);
            pushBasicProfile(update);

            getConfigLogger().verbose(getAccountId(), "removing value for key " + key + " from user profile");

        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Failed to remove profile value for key " + key, t);
        }
    }

    public void addMultiValuesForKey(final String key, final ArrayList<String> values) {
        postAsyncSafely("addMultiValuesForKey", new Runnable() {
            @Override
            public void run() {
                final String command = (getLocalDataStore().getProfileValueForKey(key) != null) ? Constants.COMMAND_ADD : Constants.COMMAND_SET;
                handleMultiValues(values, key, command);
            }
        });
    }
    public void removeMultiValueForKey(String key, String value) {
        if (value == null || value.isEmpty()) {
            generateEmptyMultiValueError(key);
            return;
        }

        removeMultiValuesForKey(key, new ArrayList<>(Collections.singletonList(value)));
    }
    TrackerApi(Context context,String trackerID){
        this.context = context;

    }
    TrackerApi(final Context context, final ActivityInstanceConfig config, String trackerID) {
        this.config = new ActivityInstanceConfig(config);
        this.context = context;
        this.handlerLooper = new Handler(Looper.getMainLooper());
        this.es = Executors.newFixedThreadPool(1);
        this.ns = Executors.newFixedThreadPool(1);
        this.localData = new LocalData(context, config);
        this.deviceInfo = new DeviceInfo(context, config, trackerID);
      // this.locationService=new LocationService(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        latitude = intent.getStringExtra(LocationService.EXTRA_LATITUDE);
                        longitude = intent.getStringExtra(LocationService.EXTRA_LONGITUDE);

                        //  contentValues.put(SocketService.LNG, longitude);
                        if (latitude != null && longitude != null) {

//                            Intent i = new Intent(ACTION_LOCATION_BROADCAST);
//                            intent.putExtra("EXTRA_LATITUDE", latitude);
//                            intent.putExtra("EXTRA_LONGITUDE", longitude);
//                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                            // serviceIntent.putExtra("Longitude", longitude);
                           // locationText.setText(getString(R.string.msg_location_service_started) + "\n Latitude : " + latitude + "\n Longitude: " + longitude);
                        }

                    }
                }, new IntentFilter(ACTION_LOCATION_BROADCAST)
        );


        if(this.deviceInfo.getDeviceID() != null){
            Logger.v("Initializing InAppFC with device Id = "+ this.deviceInfo.getDeviceID());
            this.inAppFCManager = new InAppFCManager(context,config,this.deviceInfo.getDeviceID());
        }

        this.validator = new Validator();

        postAsyncSafely("TrackerApi#initializeDeviceInfo", new Runnable() {
            @Override
            public void run() {

                if (config.isDefaultInstance()) {
                    manifestAsyncValidation();
                }
            }
        });

        int now = (int) System.currentTimeMillis() / 1000;
        if (now - initialAppEnteredForegroundTime > 5) {
            this.config.setCreatedPostAppLaunch();
        }

        setLastVisitTime();

        // Default (flag is set in the config init) or first non-default instance gets the ABTestController
//        if (!config.isDefaultInstance()) {
//            if (instances == null || instances.size() <= 0) {
//                config.setEnableABTesting(true);
//            }
//        }
       // initABTesting();

//        postAsyncSafely("setStatesAsync", new Runnable() {
//            @Override
//            public void run() {
//                setDeviceNetworkInfoReportingFromStorage();
//                setCurrentUserOptOutStateFromStorage();
//            }
//        });

        postAsyncSafely("saveConfigtoSharedPrefs", new Runnable() {
            @Override
            public void run() {
                String configJson = config.toJSONString();
                if (configJson == null) {
                    Logger.v("Unable to save config to SharedPrefs, config Json is null");
                    return;
                }
                StorageHelper.putString(context, storageKeyWithSuffix("instance"), configJson);
            }
        });

//        if (this.config.isBackgroundSync() && !this.config.isAnalyticsOnly()) {
//            postAsyncSafely("createOrResetJobScheduler", new Runnable() {
//                @Override
//                public void run() {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        createOrResetJobScheduler(context);
//                    } else {
//                        createAlarmScheduler(context);
//                    }
//                }
//            });
//        }
        Logger.i("Tracker initialized with accountId: " + config.getAccountId() + " accountToken: " + config.getAccountToken() + " accountRegion: " + config.getAccountRegion());
    }
    static void runJobWork(Context context, JobParameters parameters) {
        if (instances == null) {
            TrackerApi instance = TrackerApi.getDefaultInstance(context);
            if (instance != null) {
                if (instance.getConfig().isBackgroundSync()) {
                    instance.runInstanceJobWork(context, parameters);
                } else {
                    Logger.d("Instance doesn't allow Background sync, not running the Job");
                }
            }
            return;
        }
        for (String accountId : TrackerApi.instances.keySet()) {
            TrackerApi instance = TrackerApi.instances.get(accountId);
            if (instance != null && instance.getConfig().isAnalyticsOnly()) {
                Logger.d(accountId, "Instance is Analytics Only not running the Job");
                continue;
            }
            if (!(instance != null && instance.getConfig().isBackgroundSync())) {
                Logger.d(accountId, "Instance doesn't allow Background sync, not running the Job");
                continue;
            }
            instance.runInstanceJobWork(context, parameters);
        }
    }

    private void manifestAsyncValidation() {
        postAsyncSafely("Manifest Validation", new Runnable() {
            @Override
            public void run() {
                ManifestValidator.validate(context, deviceInfo);
            }
        });
    }
    static void handleNotificationClicked(Context context, Bundle notification) {
        if (notification == null) return;

        String _accountId = null;
        try {
            _accountId = notification.getString(Constants.WZRK_ACCT_ID_KEY);
        } catch (Throwable t) {
            // no-op
        }

//        if (instances == null) {
//            TrackerApi instance = createInstanceIfAvailable(context, _accountId);
//            if (instance != null) {
//                instance.pushNotificationClickedEvent(notification);
//            }
//            return;
//        }

//        for (String accountId : instances.keySet()) {
//            TrackerApi instance = TrackerApi.instances.get(accountId);
//            boolean shouldProcess = false;
//            if (instance != null) {
//                shouldProcess = (_accountId == null && instance.config.isDefaultInstance()) || instance.getAccountId().equals(_accountId);
//            }
//            if (shouldProcess) {
//                instance.pushNotificationClickedEvent(notification);
//                break;
//            }
//        }
    }
    private static TrackerApi createInstanceIfAvailable(Context context, String _accountId) {
        return createInstanceIfAvailable(context, _accountId, null);
    }
    private SharedPreferences getPreferences() {
        try {
            return (context == null) ? null : StorageHelper.getPreferences(context);
        } catch (Throwable t) {
            return null;
        }
    }

    static void runBackgroundIntentService(Context context) {
        if (instances == null) {
            TrackerApi instance = TrackerApi.getDefaultInstance(context);
            if (instance != null) {
                if (instance.getConfig().isBackgroundSync()) {
                    instance.runInstanceJobWork(context, null);
                } else {
                    Logger.d("Instance doesn't allow Background sync, not running the Job");
                }
            }
            return;
        }
        for (String accountId : TrackerApi.instances.keySet()) {
            TrackerApi instance = TrackerApi.instances.get(accountId);
            if (instance == null) continue;
            if (instance.getConfig().isAnalyticsOnly()) {
                Logger.d(accountId, "Instance is Analytics Only not processing device token");
                continue;
            }
            if (!instance.getConfig().isBackgroundSync()) {
                Logger.d(accountId, "Instance doesn't allow Background sync, not running the Job");
                continue;
            }
            instance.runInstanceJobWork(context, null);
        }
    }
    private ActivityInstanceConfig getConfig() {
        return config;
    }
    private int getPingFrequency(Context context) {
        return StorageHelper.getInt(context, Constants.PING_FREQUENCY, Constants.PING_FREQUENCY_VALUE); //intentional global key because only one Job is running
    }
    private void setLastVisitTime() {
         ed = getLocalDataStore().getEventDetail(Constants.APP_LAUNCHED_EVENT);
        if (ed == null) {
            lastVisitTime = -1;
        } else {
            lastVisitTime = ed.getLastTime();
        }
    }
    private void runInstanceJobWork(final Context context, final JobParameters parameters) {
        postAsyncSafely("runningJobService", new Runnable() {
            @Override
            public void run() {
                if (getCachedFCMToken() == null) {
                    Logger.v(getAccountId(), "Token is not present, not running the Job");
                    return;
                }

                Calendar now = Calendar.getInstance();

                int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
                int minute = now.get(Calendar.MINUTE);

                Date currentTime = parseTimeToDate(hour + ":" + minute);
                Date startTime = parseTimeToDate(Constants.DND_START);
                Date endTime = parseTimeToDate(Constants.DND_STOP);

                if (isTimeBetweenDNDTime(startTime, endTime, currentTime)) {
                    Logger.v(getAccountId(), "Job Service won't run in default DND hours");
                    return;
                }

               // long lastTS = loadDBAdapter(context).getLastUninstallTimestamp();

//                if (lastTS == 0 || lastTS > System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
//                    try {
//                        JSONObject eventObject = new JSONObject();
//                        eventObject.put("bk", 1);
//                        queueEvent(context, eventObject, Constants.PING_EVENT);
//
//                        if (parameters == null) {
//                            int pingFrequency = getPingFrequency(context);
//                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//                            Intent cancelIntent = new Intent(ActivityBackgroundIntentService.MAIN_ACTION);
//                            cancelIntent.setPackage(context.getPackageName());
//                            PendingIntent alarmPendingIntent = PendingIntent.getService(context, getAccountId().hashCode(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                            if (alarmManager != null) {
//                                alarmManager.cancel(alarmPendingIntent);
//                            }
//                            Intent alarmIntent = new Intent(ActivityBackgroundIntentService.MAIN_ACTION);
//                            alarmIntent.setPackage(context.getPackageName());
//                            PendingIntent alarmServicePendingIntent = PendingIntent.getService(context, getAccountId().hashCode(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                            if (alarmManager != null) {
//                                if (pingFrequency != -1) {
//                                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (pingFrequency * Constants.ONE_MIN_IN_MILLIS), Constants.ONE_MIN_IN_MILLIS * pingFrequency, alarmServicePendingIntent);
//                                }
//                            }
//                        }
//                    } catch (JSONException e) {
//                        Logger.v("Unable to raise background Ping event");
//                    }
//
//                }
            }
        });
    }
    private void queueEvent(final Context context, final JSONObject event, final int eventType) {
        postAsyncSafely("queueEvent", new Runnable() {
            @Override
            public void run() {
                if (isCurrentUserOptedOut()) {
                    String eventString = event == null ? "null" : event.toString();
                    getConfigLogger().debug(getAccountId(), "Current user is opted out dropping event: " + eventString);
                    return;
                }
                if (shouldDeferProcessingEvent(event, eventType)) {
                    getConfigLogger().debug(getAccountId(), "App Launched not yet processed, re-queuing event " + event + "after 2s");
                    gethandlerLooper().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            postAsyncSafely("queueEventWithDelay", new Runnable() {
                                @Override
                                public void run() {
                                    lazyCreateSession(context);
                                    addToQueue(context, event, eventType);
                                }
                            });
                        }
                    }, 2000);
                } else {
                    lazyCreateSession(context);
                    addToQueue(context, event, eventType);
                }
            }
        });
    }
    private void addToQueue(final Context context, final JSONObject event, final int eventType) {
        if (isMuted()) {
            return;
        }
        if (eventType == Constants.NV_EVENT) {
            getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto separate queue");
            processPushNotificationViewedEvent(context, event);
        } else {
            processEvent(context, event, eventType);
        }
    }
//    private void queuePushNotificationViewedEventToDB(final Context context, final JSONObject event) {
//        queueEventInternal(context, event, DBAdapter.Table.PUSH_NOTIFICATION_VIEWED);
//    }
    private void queueEventInternal(final Context context, final JSONObject event, DBAdapter.Table table) {
        synchronized (eventLock) {
            DBAdapter adapter = loadDBAdapter(context);
            int returnCode = adapter.storeObject(event, table);

            if (returnCode > 0) {
                getConfigLogger().debug(getAccountId(), "Queued event: " + event.toString());
                getConfigLogger().verbose(getAccountId(), "Queued event to DB table " + table + ": " + event.toString());
            }
        }
    }
    private void processPushNotificationViewedEvent(final Context context, final JSONObject event) {
        synchronized (eventLock) {
            try {
                int session = getCurrentSession();
                event.put("s", session);
                event.put("type", "event");
                event.put("ep", System.currentTimeMillis() / 1000);
                // Report any pending validation error
                ValidationResult vr = popValidationResult();
                if (vr != null) {
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                }
                getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto DB");
                //queuePushNotificationViewedEventToDB(context, event);
                getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto queue flush");
                schedulePushNotificationViewedQueueFlush(context);
            } catch (Throwable t) {
                getConfigLogger().verbose(getAccountId(), "Failed to queue notification viewed event: " + event.toString(), t);
            }
        }
    }
    private enum EventGroup {

        REGULAR(""),
        PUSH_NOTIFICATION_VIEWED("-spiky");

        private final String httpResource;

        EventGroup(String httpResource) {
            this.httpResource = httpResource;
        }
    }
    private void schedulePushNotificationViewedQueueFlush(final Context context) {
        if (pushNotificationViewedRunnable == null)
            pushNotificationViewedRunnable = new Runnable() {
                @Override
                public void run() {
                    getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto queue flush async");
                    flushQueueAsync(context, EventGroup.PUSH_NOTIFICATION_VIEWED);
                }
            };
        gethandlerLooper().removeCallbacks(pushNotificationViewedRunnable);
        gethandlerLooper().post(pushNotificationViewedRunnable);
    }
    private void flushQueueAsync(final Context context, final EventGroup eventGroup) {
        postAsyncSafely("CommsManager#flushQueueAsync", new Runnable() {
            @Override
            public void run() {
                if (eventGroup == EventGroup.PUSH_NOTIFICATION_VIEWED) {
                    getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto queue flush sync");
                } else {
                    getConfigLogger().verbose(getAccountId(), "Pushing event onto queue flush sync");
                }
                flushQueueSync(context, eventGroup);
            }
        });
    }
    private boolean isNetworkOnline(Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                // lets be optimistic, if we are truly offline we handle the exception
                return true;
            }
            @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Throwable ignore) {
            // lets be optimistic, if we are truly offline we handle the exception
            return true;
        }
    }
    private boolean isOffline() {
        return offline;
    }


    private void flushQueueSync(final Context context, final EventGroup eventGroup) {
        if (!isNetworkOnline(context)) {
            getConfigLogger().verbose(getAccountId(), "Network connectivity unavailable. Will retry later");
            return;
        }

        if (isOffline()) {
            getConfigLogger().debug(getAccountId(), "App Instance has been set to offline, won't send events queue");
            return;
        }

//        if (needsHandshakeForDomain(eventGroup)) {
//            mResponseFailureCount = 0;
//            setDomain(context, null);
//            performHandshakeForDomain(context, eventGroup, new Runnable() {
//                @Override
//                public void run() {
//                    flushDBQueue(context, eventGroup);
//                }
//            });
//        } else {
//            getConfigLogger().verbose(getAccountId(), "Pushing Notification Viewed event onto queue DB flush");
//            flushDBQueue(context, eventGroup);
//        }
    }


    public void processEvent(final Context context, final JSONObject event, final int eventType) {
        synchronized (eventLock) {
            try {
                activityCount = activityCount == 0 ? 1 : activityCount;
                String type;
                if (eventType == Constants.PAGE_EVENT) {
                    type = "page";
                } else if (eventType == Constants.PING_EVENT) {
                    type = "ping";
                    attachMeta(event, context);
                    if (event.has("bk")) {
                        isBgPing = true;
                        event.remove("bk");
                    }
                } else if (eventType == Constants.PROFILE_EVENT) {
                    type = "profile";
                } else if (eventType == Constants.DATA_EVENT) {
                    type = "data";
                } else {
                    type = "event";
                }

                // Complete the received event with the other params

                String currentActivityName = getScreenName();
                if (currentActivityName != null) {
                    event.put("Akku", currentActivityName);
                }

                int session = getCurrentSession();
                event.put("session", session);
                event.put("activitycount", activityCount);
                event.put("type", type);
                event.put("ep", System.currentTimeMillis() / 1000);
                event.put("firstSession", isFirstSession());
                event.put("lastSession", getLastSessionLength());
                event.put("lastSession", getLastSessionLength());
                attachPackageNameIfRequired(context, event);

                // Report any pending validation error
                ValidationResult vr = popValidationResult();
                if (vr != null) {
                    event.put(Constants.ERROR_KEY, getErrorObject(vr));
                }
                getLocalDataStore().setDataSyncFlag(event);
                queueEventToDB(context, event, eventType);
                updateLocalStore(context, event, eventType);
                scheduleQueueFlush(context);

            } catch (Throwable e) {
                getConfigLogger().verbose(getAccountId(), "Failed to queue event: " + event.toString(), e);
            }
        }
    }
    public SyncListener getSyncListener() {
        return syncListener;
    }
    public LocalData getLocalDataStore() {
        return localData;
    }
    private void queueEventToDB(final Context context, final JSONObject event, final int type) {
        DBAdapter.Table table = (type == Constants.PROFILE_EVENT) ? DBAdapter.Table.PROFILE_EVENTS : DBAdapter.Table.EVENTS;
        queueEventInternal(context, event, table);
    }
    private void updateLocalStore(final Context context, final JSONObject event, final int type) {
        if (type == Constants.RAISED_EVENT) {
            getLocalDataStore().persistEvent(context, event, type);
        }
    }
    private void scheduleQueueFlush(final Context context) {
        if (commsRunnable == null)
            commsRunnable = new Runnable() {
                @Override
                public void run() {
                    flushQueueAsync(context, EventGroup.REGULAR);
                    flushQueueAsync(context, EventGroup.PUSH_NOTIFICATION_VIEWED);
                }
            };
        // Cancel any outstanding send runnables, and issue a new delayed one
        gethandlerLooper().removeCallbacks(commsRunnable);
        gethandlerLooper().postDelayed(commsRunnable, getDelayFrequency());

        getConfigLogger().verbose(getAccountId(), "Scheduling delayed queue flush on main event loop");
    }
    private int getDelayFrequency() {
        getConfigLogger().debug(getAccountId(), "Network retry #" + networkRetryCount);

        //Retry with delay as 1s for first 10 retries
        if (networkRetryCount < 10) {
            getConfigLogger().debug(getAccountId(), "Failure count is " + networkRetryCount + ". Setting delay frequency to 1s");
            minDelayFrequency = Constants.PUSH_DELAY_MS; //reset minimum delay to 1s
            return minDelayFrequency;
        }

        if (config.getAccountRegion() == null) {
            //Retry with delay as 1s if region is null in case of eu1
            getConfigLogger().debug(getAccountId(), "Setting delay frequency to 1s");
            return Constants.PUSH_DELAY_MS;
        } else {
            //Retry with delay as minimum delay frequency and add random number of seconds to scatter traffic
            Random randomGen = new Random();
            int randomDelay = (randomGen.nextInt(10) + 1) * 1000;
            minDelayFrequency += randomDelay;
            if (minDelayFrequency < maxDelayFrequency) {
                getConfigLogger().debug(getAccountId(), "Setting delay frequency to " + minDelayFrequency);
                return minDelayFrequency;
            } else {
                minDelayFrequency = Constants.PUSH_DELAY_MS;
            }
            getConfigLogger().debug(getAccountId(), "Setting delay frequency to " + minDelayFrequency);
            return minDelayFrequency;
        }
    }

    private JSONObject getErrorObject(ValidationResult vr) {
        JSONObject error = new JSONObject();
        try {
            error.put("c", vr.getErrorCode());
            error.put("d", vr.getErrorDesc());
        } catch (JSONException e) {
            // Won't reach here
        }
        return error;
    }
    private ValidationResult popValidationResult() {
        // really a shift
        ValidationResult vr = null;

        synchronized (pendingValidationResultsLock) {
            try {
                if (!pendingValidationResults.isEmpty()) {
                    vr = pendingValidationResults.remove(0);
                }
            } catch (Exception e) {
                // no-op
            }
        }
        return vr;
    }
    private boolean isFirstSession() {
        return firstSession;
    }
    public int getLastSessionLength() {
        return lastSessionLength;
    }
    private void attachPackageNameIfRequired(final Context context, final JSONObject event) {
        try {
            final String type = event.getString("type");
            // Send it only for app launched events
            if ("event".equals(type) && Constants.APP_LAUNCHED_EVENT.equals(event.getString("evtName"))) {
                event.put("pai", context.getPackageName());
            }
        } catch (Throwable t) {
            // Ignore
        }
    }
    public String getScreenName() {
        return currentScreenName.equals("") ? null : currentScreenName;
    }
    private int getCurrentSession() {
        return currentSessionId;
    }
    private void attachMeta(final JSONObject o, final Context context) {
        // Memory consumption
        try {
            o.put("mc", Utils.getMemoryConsumption());
        } catch (Throwable t) {
            // Ignore
        }

        // Attach the network type
        try {
            o.put("nt", Utils.getCurrentNetworkType(context));
        } catch (Throwable t) {
            // Ignore
        }
    }

    private boolean isMuted() {
        final int now = (int) (System.currentTimeMillis() / 1000);
        final int muteTS = getIntFromPrefs(Constants.KEY_MUTED, 0);

        return now - muteTS < 24 * 60 * 60;
    }
    private int getIntFromPrefs(String rawKey, int defaultValue) {
        if (this.config.isDefaultInstance()) {
            int dummy = -1000;
            int _new = StorageHelper.getInt(this.context, storageKeyWithSuffix(rawKey), dummy);
            return _new != dummy ? _new : StorageHelper.getInt(this.context, rawKey, defaultValue);
        } else {
            return StorageHelper.getInt(this.context, storageKeyWithSuffix(rawKey), defaultValue);
        }
    }
    private boolean inCurrentSession() {
        return currentSessionId > 0;
    }
    public String getTrackerID() {
        return this.deviceInfo.getDeviceID();
    }
    private boolean isErrorDeviceId() {
        return this.deviceInfo.isErrorDeviceId();
    }
    private JSONObject getCachedGUIDs() {
        JSONObject cache = null;
        String json = getStringFromPrefs(Constants.CACHED_GUIDS_KEY, null);
        if (json != null) {
            try {
                cache = new JSONObject(json);
            } catch (Throwable t) {
                // no-op
                getConfigLogger().verbose(getAccountId(), "Error reading guid cache: " + t.toString());
            }
        }

        return (cache != null) ? cache : new JSONObject();
    }
    private void setCachedGUIDs(JSONObject cachedGUIDs) {
        if (cachedGUIDs == null) return;
        try {
            StorageHelper.putString(context, storageKeyWithSuffix(Constants.CACHED_GUIDS_KEY), cachedGUIDs.toString());
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Error persisting guid cache: " + t.toString());
        }
    }
    private void cacheGUIDForIdentifier(String guid, String key, String identifier) {
        if (isErrorDeviceId() || guid == null || key == null || identifier == null) {
            return;
        }

        String cacheKey = key + "_" + identifier;
        JSONObject cache = getCachedGUIDs();
        try {
            cache.put(cacheKey, guid);
            setCachedGUIDs(cache);
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Error caching guid: " + t.toString());
        }
    }
    private void pushBasicProfile(JSONObject baseProfile) {
        try {
            String guid = getTrackerID();

            JSONObject profileEvent = new JSONObject();

            if (baseProfile != null && baseProfile.length() > 0) {
                Iterator i = baseProfile.keys();
                while (i.hasNext()) {
                    String next = i.next().toString();

                    // need to handle command-based JSONObject props here now
                    Object value = null;
                    try {
                        value = baseProfile.getJSONObject(next);
                    } catch (Throwable t) {
                        try {
                            value = baseProfile.get(next);
                        } catch (JSONException e) {
                            //no-op
                        }
                    }

                    if (value != null) {
                        profileEvent.put(next, value);

                        // cache the valid identifier: guid pairs
                        if (Constants.PROFILE_IDENTIFIER_KEYS.contains(next)) {
                            try {
                                cacheGUIDForIdentifier(guid, next, value.toString());
                            } catch (Throwable t) {
                                // no-op
                            }
                        }
                    }
                }
            }

            try {
                String carrier = this.deviceInfo.getCarrier();
                if (carrier != null && !carrier.equals("")) {
                    profileEvent.put("Carrier", carrier);
                }

                String cc = this.deviceInfo.getCountryCode();
                if (cc != null && !cc.equals("")) {
                    profileEvent.put("country", cc);
                }

                profileEvent.put("timeZone", TimeZone.getDefault().getID());

              event = new JSONObject();
                event.put("profile", profileEvent);
                queueEvent(context, event, Constants.PROFILE_EVENT);
            } catch (JSONException e) {
                getConfigLogger().verbose(getAccountId(), "FATAL: Creating basic profile update event failed!");
            }
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Basic profile sync", t);
        }
    }

    private void pushInitialEventsAsync() {
        postAsyncSafely("TrackerApi#pushInitialEventsAsync", new Runnable() {
            @Override
            public void run() {
                try {
                    getConfigLogger().verbose(getAccountId(), "Queuing daily events");
                    pushBasicProfile(null);
                } catch (Throwable t) {
                    getConfigLogger().verbose(getAccountId(), "Daily profile sync failed", t);
                }
            }
        });
    }
    private void createSession(final Context context) {
        currentSessionId = (int) (System.currentTimeMillis() / 1000);

        getConfigLogger().verbose(getAccountId(), "Session created with ID: " + currentSessionId);

        SharedPreferences prefs = StorageHelper.getPreferences(context);

        final int lastSessionID = getIntFromPrefs(Constants.SESSION_ID_LAST, 0);
        lastSessionTime = getIntFromPrefs(Constants.LAST_SESSION_EPOCH, 0);
        if (lastSessionTime > 0) {
            lastSessionLength = lastSessionTime - lastSessionID;
        }

        getConfigLogger().verbose(getAccountId(), "Last session length: " + lastSessionLength + " seconds");

        if (lastSessionID == 0) {
            firstSession = true;
        }

        final SharedPreferences.Editor editor = prefs.edit().putInt(storageKeyWithSuffix(Constants.SESSION_ID_LAST), currentSessionId);
        StorageHelper.persist(editor);
    }

    private void lazyCreateSession(Context context) {
        if (!inCurrentSession()) {
            createSession(context);
            pushInitialEventsAsync();
        }
    }
    private Handler gethandlerLooper() {
        return handlerLooper;
    }
    private boolean shouldDeferProcessingEvent(JSONObject event, int eventType) {
        //noinspection SimplifiableIfStatement
        if (getConfig().isCreatedPostAppLaunch()) {
            return false;
        }
        if (event.has("eventName")) {
            try {
                if (Arrays.asList(Constants.SYSTEM_EVENTS).contains(event.getString("eventtName"))) {
                    return false;
                }
            } catch (JSONException e) {
                //no-op
            }
        }
        return (eventType == Constants.RAISED_EVENT && !isAppLaunchPushed());
    }
    private boolean isAppLaunchPushed() {
        synchronized (appLaunchPushedLock) {
            return appLaunchPushed;
        }
    }

    private boolean isCurrentUserOptedOut() {
        synchronized (optOutFlagLock) {
            return currentUserOptedOut;
        }
    }
    private Date parseTimeToDate(String time) {

        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);
        try {
            return inputParser.parse(time);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }
    private boolean isTimeBetweenDNDTime(Date startTime, Date stopTime, Date currentTime) {
        //Start Time
        Calendar startTimeCalendar = Calendar.getInstance();
        startTimeCalendar.setTime(startTime);
        //Current Time
        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.setTime(currentTime);
        //Stop Time
        Calendar stopTimeCalendar = Calendar.getInstance();
        stopTimeCalendar.setTime(stopTime);

        if (stopTime.compareTo(startTime) < 0) {
            if (currentTimeCalendar.compareTo(stopTimeCalendar) < 0) {
                currentTimeCalendar.add(Calendar.DATE, 1);
            }
            stopTimeCalendar.add(Calendar.DATE, 1);
        }
        return currentTimeCalendar.compareTo(startTimeCalendar) >= 0 && currentTimeCalendar.compareTo(stopTimeCalendar) < 0;
    }
    private DBAdapter loadDBAdapter(Context context) {
        if (dbAdapter == null) {
            dbAdapter = new DBAdapter(context, this.config);
            dbAdapter.cleanupStaleEvents(DBAdapter.Table.EVENTS);
            dbAdapter.cleanupStaleEvents(DBAdapter.Table.PROFILE_EVENTS);
//            dbAdapter.cleanupStaleEvents(DBAdapter.Table.PUSH_NOTIFICATION_VIEWED);
//            dbAdapter.cleanUpPushNotifications();
        }
        return dbAdapter;
    }
    private String getCachedFCMToken() {
        SharedPreferences prefs = getPreferences();
        return (prefs == null) ? null : getStringFromPrefs(Constants.FCM_PROPERTY_REG_ID, null);
    }
    private String getStringFromPrefs(String rawKey, String defaultValue) {
        if (this.config.isDefaultInstance()) {
            String _new = StorageHelper.getString(this.context, storageKeyWithSuffix(rawKey), defaultValue);
            //noinspection ConstantConditions
            return _new != null ? _new : StorageHelper.getString(this.context, rawKey, defaultValue);
        } else {
            return StorageHelper.getString(this.context, storageKeyWithSuffix(rawKey), defaultValue);
        }
    }
    private String storageKeyWithSuffix(String key) {
        return key + ":" + getConfig().getAccountId();
    }

    private void postAsyncSafely(final String name, final Runnable runnable) {
        try {
            final boolean executeSync = Thread.currentThread().getId() == EXECUTOR_THREAD_ID;

            if (executeSync) {
                runnable.run();
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        EXECUTOR_THREAD_ID = Thread.currentThread().getId();
                        try {
                            runnable.run();
                        } catch (Throwable t) {
                            getConfigLogger().verbose(getAccountId(), "Executor service: Failed to complete the scheduled task", t);
                        }
                    }
                });
            }
        } catch (Throwable t) {
            getConfigLogger().verbose(getAccountId(), "Failed to submit task to the executor service", t);
        }
    }
    private Logger getConfigLogger() {
        return getConfig().getLogger();
    }
    private String getAccountId() {
        return config.getAccountId();
    }
    private void notifyUserProfileInitialized() {
        notifyUserProfileInitialized(this.deviceInfo.getDeviceID());
    }
    private void notifyUserProfileInitialized(String deviceID) {
        deviceID = (deviceID != null) ? deviceID : getTrackerID();

        if (deviceID == null) return;

        final SyncListener sl;
        try {
            sl = getSyncListener();
            if (sl != null) {
                sl.profileDidInitialize(deviceID);
            }
        } catch (Throwable t) {
            // Ignore
        }
    }

    public static TrackerApi instanceWithConfig(Context context, @NonNull ActivityInstanceConfig config, String trackerID) {
        //noinspection ConstantConditions
        if (config == null) {
            Logger.v("AppInstanceConfig cannot be null");
            return null;
        }
        if (instances == null) {
            instances = new HashMap<>();
        }

        TrackerApi instance = instances.get(config.getAccountId());
        if (instance == null) {
            instance = new TrackerApi(context, config, trackerID);
            instances.put(config.getAccountId(), instance);
            final TrackerApi finalInstance = instance;
            instance.postAsyncSafely("notifyProfileInitialized",new Runnable() {
                @Override
                public void run() {
                    if (finalInstance.getTrackerID() != null) {
                        finalInstance.notifyUserProfileInitialized();
                        finalInstance.recordDeviceIDErrors();
                    }
                }
            });
        } else if (instance.isErrorDeviceId() && instance.getConfig().getEnableCustomTrackerId() && Utils.validateCTID(trackerID)) {
           // instance.asyncProfileSwitchUser(null, null, trackerID);
        }
        return instance;
    }

    private void recordDeviceIDErrors() {
        for (ValidationResult validationResult : this.deviceInfo.getValidationResults()) {
            pushValidationResult(validationResult);
        }
    }
//    public static void getDefaultInstance(Context context) {
//
//        return getDefaultInstance(context, null);
//    }
    public static @Nullable TrackerApi getDefaultInstance(Context context) {
        return getDefaultInstance(context, null);
    }
    public void enableDeviceNetworkInfoReporting(boolean value) {
        enableNetworkInfoReporting = value;
        StorageHelper.putBoolean(context, storageKeyWithSuffix(Constants.NETWORK_INFO), enableNetworkInfoReporting);
        getConfigLogger().verbose(getAccountId(), "Device Network Information reporting set to " + enableNetworkInfoReporting);
    }
    public static TrackerApi getDefaultInstance(Context context, String trackerID) {
        // For Google Play Store/Android Studio tracking
        sdkVersion = BuildConfig.APPLICATION_ID;

        if (defaultConfig != null) {
            return instanceWithConfig(context, defaultConfig, trackerID);
        } else {
            defaultConfig = getDefaultConfig(context);
            if (defaultConfig != null) {
                return instanceWithConfig(context, defaultConfig, trackerID);
            }
        }
        return null;
    }

    private static ActivityInstanceConfig getDefaultConfig(Context context) {
        ManifestInfo manifest = ManifestInfo.getInstance(context);
        String accountId = manifest.getAccountId();
        String accountToken = manifest.getAcountToken();
        String accountRegion = manifest.getAccountRegion();
        if (accountId == null || accountToken == null) {
            Logger.i("Account ID or Account token is missing from AndroidManifest.xml, unable to create default instance");
            return null;
        }
        if (accountRegion == null) {
            Logger.i("Account Region not specified in the AndroidManifest - using default region");
        }

        return ActivityInstanceConfig.createDefaultInstance(context, accountId, accountToken, accountRegion);

    }


    public enum LogLevel {
        OFF(-1),
        INFO(0),
        DEBUG(2);

        private final int value;

        LogLevel(final int newValue) {
            value = newValue;
        }

        public int intValue() {
            return value;
        }
    }
    public static int getDebugLevel() {
        return debugLevel;
    }
}
