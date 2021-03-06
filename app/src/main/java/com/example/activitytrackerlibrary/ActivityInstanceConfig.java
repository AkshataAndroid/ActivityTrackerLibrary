package com.example.activitytrackerlibrary;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class ActivityInstanceConfig implements Parcelable {

    private String accountId;
    private String accountToken;
    private String accountRegion;
    private boolean analyticsOnly;
    private boolean isDefaultInstance;
   // private boolean useGoogleAdId;
    private boolean disableAppLaunchedEvent;
    private boolean personalization;
    private int debugLevel;
    private Logger logger;
    private boolean createdPostAppLaunch;
    private boolean sslPinning;
    private boolean backgroundSync;
    private boolean enableCustomAppId;
    private String fcmSenderId;
    private boolean enableUIEditor;
    private String packageName;
    private boolean beta;

    private ActivityInstanceConfig(Context context, String accountId, String accountToken, String accountRegion, boolean isDefault) {
        this.accountId = accountId;
        this.accountToken = accountToken;
        this.accountRegion = accountRegion;
        this.isDefaultInstance = isDefault;
        this.analyticsOnly = false;
        this.personalization = true;
        this.debugLevel = TrackerApi.LogLevel.INFO.intValue();
        this.logger = new Logger(this.debugLevel);
        this.createdPostAppLaunch = false;

        ManifestInfo manifest = ManifestInfo.getInstance(context);
       // this.useGoogleAdId = manifest.useGoogleAdId();
        this.disableAppLaunchedEvent = manifest.isAppLaunchedDisabled();
        this.sslPinning = manifest.isSSLPinningEnabled();
        this.backgroundSync = manifest.isBackgroundSync();
        this.fcmSenderId = manifest.getFCMSenderId();
        this.packageName = manifest.getPackageName();
        this.enableCustomAppId = manifest.useCustomId();
        this.beta = manifest.enableBeta();
    }

    ActivityInstanceConfig(ActivityInstanceConfig config) {
        this.accountId = config.accountId;
        this.accountToken = config.accountToken;
        this.accountRegion = config.accountRegion;
        this.isDefaultInstance = config.isDefaultInstance;
        this.analyticsOnly = config.analyticsOnly;
        this.personalization = config.personalization;
        this.debugLevel = config.debugLevel;
        this.logger = config.logger;
      //  this.useGoogleAdId = config.useGoogleAdId;
        this.disableAppLaunchedEvent = config.disableAppLaunchedEvent;
        this.createdPostAppLaunch = config.createdPostAppLaunch;
        this.sslPinning = config.sslPinning;
        this.backgroundSync = config.backgroundSync;
        this.enableCustomAppId = config.enableCustomAppId;
        this.fcmSenderId = config.fcmSenderId;
        this.packageName = config.packageName;
        this.beta = config.beta;
    }

    private ActivityInstanceConfig(String jsonString) throws Throwable {
        try{
            JSONObject configJsonObject = new JSONObject(jsonString);
            if(configJsonObject.has(Constants.KEY_ACCOUNT_ID))
                this.accountId = configJsonObject.getString(Constants.KEY_ACCOUNT_ID);
            if(configJsonObject.has(Constants.KEY_ACCOUNT_TOKEN))
                this.accountToken = configJsonObject.getString(Constants.KEY_ACCOUNT_TOKEN);
            if(configJsonObject.has(Constants.KEY_ACCOUNT_REGION))
                this.accountRegion = configJsonObject.getString(Constants.KEY_ACCOUNT_REGION);
            if(configJsonObject.has(Constants.KEY_ANALYTICS_ONLY))
                this.analyticsOnly = configJsonObject.getBoolean(Constants.KEY_ANALYTICS_ONLY);
            if(configJsonObject.has(Constants.KEY_DEFAULT_INSTANCE))
                this.isDefaultInstance = configJsonObject.getBoolean(Constants.KEY_DEFAULT_INSTANCE);
//            if(configJsonObject.has(Constants.KEY_USE_GOOGLE_AD_ID))
//                this.useGoogleAdId = configJsonObject.getBoolean(Constants.KEY_USE_GOOGLE_AD_ID);
            if(configJsonObject.has(Constants.KEY_DISABLE_APP_LAUNCHED))
                this.disableAppLaunchedEvent = configJsonObject.getBoolean(Constants.KEY_DISABLE_APP_LAUNCHED);
            if(configJsonObject.has(Constants.KEY_PERSONALIZATION))
                this.personalization = configJsonObject.getBoolean(Constants.KEY_PERSONALIZATION);
            if(configJsonObject.has(Constants.KEY_DEBUG_LEVEL)) {
                this.debugLevel = configJsonObject.getInt(Constants.KEY_DEBUG_LEVEL);
            }
            this.logger = new Logger(this.debugLevel);

            if(configJsonObject.has(Constants.KEY_ENABLE_UIEDITOR))
                this.enableUIEditor = configJsonObject.getBoolean(Constants.KEY_ENABLE_UIEDITOR);
            if(configJsonObject.has(Constants.KEY_PACKAGE_NAME))
                this.packageName = configJsonObject.getString(Constants.KEY_PACKAGE_NAME);
            if(configJsonObject.has(Constants.KEY_CREATED_POST_APP_LAUNCH))
                this.createdPostAppLaunch = configJsonObject.getBoolean(Constants.KEY_CREATED_POST_APP_LAUNCH);
            if(configJsonObject.has(Constants.KEY_SSL_PINNING))
                this.sslPinning = configJsonObject.getBoolean(Constants.KEY_SSL_PINNING);
            if(configJsonObject.has(Constants.KEY_BACKGROUND_SYNC))
                this.backgroundSync = configJsonObject.getBoolean(Constants.KEY_BACKGROUND_SYNC);
            if(configJsonObject.has(Constants.KEY_ENABLE_CUSTOM_CT_ID))
                this.enableCustomAppId = configJsonObject.getBoolean(Constants.KEY_ENABLE_CUSTOM_CT_ID);
            if(configJsonObject.has(Constants.KEY_FCM_SENDER_ID))
                this.fcmSenderId = configJsonObject.getString(Constants.KEY_FCM_SENDER_ID);
            if(configJsonObject.has(Constants.KEY_BETA)) {
                this.beta = configJsonObject.getBoolean(Constants.KEY_BETA);
            }
        } catch (Throwable t){
            Logger.v("Error constructing AppInstanceConfig from JSON: " + jsonString +": ", t.getCause());
            throw(t);
        }
    }

    private ActivityInstanceConfig(Parcel in){
        accountId = in.readString();
        accountToken = in.readString();
        accountRegion = in.readString();
        analyticsOnly = in.readByte() != 0x00;
        isDefaultInstance = in.readByte() != 0x00;
        //useGoogleAdId = in.readByte() != 0x00;
        disableAppLaunchedEvent = in.readByte() != 0x00;
        personalization = in.readByte() != 0x00;
        debugLevel = in.readInt();
        createdPostAppLaunch = in.readByte() != 0x00;
        sslPinning = in.readByte() != 0x00;
        backgroundSync = in.readByte() != 0x00;
        enableCustomAppId = in.readByte() != 0x00;
        fcmSenderId = in.readString();
        enableUIEditor = in.readByte() != 0x00;
        packageName = in.readString();
        logger = new Logger(debugLevel);
        beta = in.readByte() != 0x00;
    }

    public static ActivityInstanceConfig createInstance(Context context, @NonNull  String accountId, @NonNull  String accountToken) {
        //noinspection ConstantConditions
        if (accountId == null || accountToken == null) {
            Logger.i("Tracker accountId and accountToken cannot be null");
            return null;
        }
        return new ActivityInstanceConfig(context, accountId, accountToken, null,false);
    }

    @SuppressWarnings({"unused"})
    public static ActivityInstanceConfig createInstance(Context context, @NonNull String accountId, @NonNull String accountToken,String accountRegion) {
        //noinspection ConstantConditions
        if (accountId == null || accountToken == null) {
            Logger.i("App accountId and accountToken cannot be null");
            return null;
        }
        return new ActivityInstanceConfig(context, accountId, accountToken, accountRegion,false);
    }

    // for internal use only!
    @SuppressWarnings({"unused", "WeakerAccess"})
    protected static ActivityInstanceConfig createInstance(@NonNull String jsonString){
        try {
            return new ActivityInstanceConfig(jsonString);
        } catch (Throwable t) {
            return null;
        }
    }

    // convenience to construct the internal only default config
    @SuppressWarnings({"unused", "WeakerAccess"})
    protected static ActivityInstanceConfig createDefaultInstance(Context context, @NonNull String accountId, @NonNull String accountToken, String accountRegion) {
        return new ActivityInstanceConfig(context, accountId, accountToken, accountRegion, true);
    }

    public String getAccountId() {
        return accountId;
    }

    @SuppressWarnings({"unused"})
    public String getAccountToken() {
        return accountToken;
    }

    @SuppressWarnings({"unused"})
    public String getAccountRegion() {
        return accountRegion;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public boolean isAnalyticsOnly() {
        return analyticsOnly;
    }

    @SuppressWarnings({"unused"})
    public void setAnalyticsOnly(boolean analyticsOnly) {
        this.analyticsOnly = analyticsOnly;
    }

    boolean isDefaultInstance() {
        return isDefaultInstance;
    }

    boolean isCreatedPostAppLaunch() {
        return createdPostAppLaunch;
    }

    void setCreatedPostAppLaunch() {
        this.createdPostAppLaunch = true;
    }

//    boolean isUseGoogleAdId() {
//        return useGoogleAdId;
//    }

    @SuppressWarnings({"unused"})
//    public void useGoogleAdId(boolean value){
//        this.useGoogleAdId = value;
//    }

    boolean isDisableAppLaunchedEvent() {
        return disableAppLaunchedEvent;
    }

    @SuppressWarnings({"unused"})
    public void setDisableAppLaunchedEvent(boolean disableAppLaunchedEvent) {
        this.disableAppLaunchedEvent = disableAppLaunchedEvent;
    }

    boolean isSslPinningEnabled() {
        return sslPinning;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void enablePersonalization(boolean enablePersonalization) {
        this.personalization = enablePersonalization;
    }

    boolean isPersonalizationEnabled() {
        return personalization;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getDebugLevel() {
        return debugLevel;
    }

    @SuppressWarnings({"unused"})
    public void setDebugLevel(TrackerApi.LogLevel debugLevel) {
        this.debugLevel = debugLevel.intValue();
    }

    @SuppressWarnings({"unused"})
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = new Logger(this.debugLevel);
        }
        return logger;
    }

    boolean isBackgroundSync() {
        return backgroundSync;
    }

    @SuppressWarnings({"unused"})
    public void setBackgroundSync(boolean backgroundSync) {
        this.backgroundSync = backgroundSync;
    }


    @SuppressWarnings({"unused", "WeakerAccess"})
    public String getFcmSenderId() {
        return fcmSenderId;
    }

    boolean getEnableCustomTrackerId() {
        return enableCustomAppId;
    }

    @SuppressWarnings({"unused"})
    public void setEnableCustomAppId(boolean enableCustomAppId) {
        this.enableCustomAppId = enableCustomAppId;
    }

    @SuppressWarnings({"unused"})
    public boolean isUIEditorEnabled() {
        return enableUIEditor;
    }

    @SuppressWarnings({"unused"})
    void setEnableUIEditor(boolean enableUIEditor) {
        this.enableUIEditor = enableUIEditor;
    }



    public String getPackageName() {
        return packageName;
    }

    public boolean isBeta() {
        return beta;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ActivityInstanceConfig> CREATOR = new Parcelable.Creator<ActivityInstanceConfig>() {
        @Override
        public ActivityInstanceConfig createFromParcel(Parcel in) {
            return new ActivityInstanceConfig(in);
        }

        @Override
        public ActivityInstanceConfig[] newArray(int size) {
            return new ActivityInstanceConfig[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountId);
        dest.writeString(accountToken);
        dest.writeString(accountRegion);
        dest.writeByte((byte) (analyticsOnly ? 0x01 : 0x00));
        dest.writeByte((byte) (isDefaultInstance ? 0x01 : 0x00));
     //   dest.writeByte((byte) (useGoogleAdId ? 0x01 : 0x00));
        dest.writeByte((byte) (disableAppLaunchedEvent ? 0x01 : 0x00));
        dest.writeByte((byte) (personalization ? 0x01 : 0x00));
        dest.writeInt(debugLevel);
        dest.writeByte((byte) (createdPostAppLaunch ? 0x01 : 0x00));
        dest.writeByte((byte) (sslPinning ? 0x01 : 0x00));
        dest.writeByte((byte) (backgroundSync ? 0x01 : 0x00));
        dest.writeByte((byte) (enableCustomAppId ? 0x01 : 0x00));
        dest.writeString(fcmSenderId);

        dest.writeByte((byte) (enableUIEditor ? 0x01 : 0x00));
        dest.writeString(packageName);
        dest.writeByte((byte) (beta ? 0x01 : 0x00));
    }

    String toJSONString() {
        JSONObject configJsonObject = new JSONObject();
        try{
            configJsonObject.put(Constants.KEY_ACCOUNT_ID, getAccountId());
            configJsonObject.put(Constants.KEY_ACCOUNT_TOKEN, getAccountToken());
            configJsonObject.put(Constants.KEY_ACCOUNT_REGION, getAccountRegion());
            configJsonObject.put(Constants.KEY_FCM_SENDER_ID, getFcmSenderId());
            configJsonObject.put(Constants.KEY_ANALYTICS_ONLY, isAnalyticsOnly());
            configJsonObject.put(Constants.KEY_DEFAULT_INSTANCE, isDefaultInstance());
          //  configJsonObject.put(Constants.KEY_USE_GOOGLE_AD_ID, isUseGoogleAdId());
            configJsonObject.put(Constants.KEY_DISABLE_APP_LAUNCHED, isDisableAppLaunchedEvent());
            configJsonObject.put(Constants.KEY_PERSONALIZATION, isPersonalizationEnabled());
            configJsonObject.put(Constants.KEY_DEBUG_LEVEL, getDebugLevel());
            configJsonObject.put(Constants.KEY_CREATED_POST_APP_LAUNCH, isCreatedPostAppLaunch());
            configJsonObject.put(Constants.KEY_SSL_PINNING, isSslPinningEnabled());
            configJsonObject.put(Constants.KEY_BACKGROUND_SYNC, isBackgroundSync());
            configJsonObject.put(Constants.KEY_ENABLE_CUSTOM_CT_ID, getEnableCustomTrackerId());
            configJsonObject.put(Constants.KEY_PACKAGE_NAME, getPackageName());
            configJsonObject.put(Constants.KEY_BETA, isBeta());
            configJsonObject.put(Constants.KEY_ENABLE_UIEDITOR,isUIEditorEnabled());

            return configJsonObject.toString();
        }catch (Throwable e){
            Logger.v("Unable to convert config to JSON : ",e.getCause());
            return null;
        }
    }
}

