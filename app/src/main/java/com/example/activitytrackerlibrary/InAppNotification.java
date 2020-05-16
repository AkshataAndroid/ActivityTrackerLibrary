package com.example.activitytrackerlibrary;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.LruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

class InAppNotification implements Parcelable {

    private String id;
    private String campaignId;
    private String type;
    private InAppType inAppType;

    private String html;
    private boolean excludeFromCaps;
    private boolean showClose;
    private boolean darkenScreen;
    private int maxPerSession;
    private int totalLifetimeCount;
    private int totalDailyCount;
    private char position;
    private int height;
    private int heightPercentage;
    private int width;
    private int widthPercentage;

    private String title;
    private String titleColor;
    private String message;
    private String messageColor;
    private String backgroundColor;

    private boolean hideCloseButton;

    private ArrayList<AppNotificationButton> buttons = new ArrayList<>();
    private ArrayList<AppNotificationMedia> mediaList = new ArrayList<>();
    private int buttonCount;

    private JSONObject jsonDescription;
    private String error;
    private JSONObject customExtras;
    private JSONObject actionExtras;
    AppNotificationListener listener;

    private boolean isTablet;
    private boolean videoSupported;
    private String customInAppUrl;
    private boolean jsEnabled;
    private boolean isPortrait;
    private boolean isLandscape;
    private String landscapeImageUrl;
    private String _landscapeImageCacheKey;

    InAppNotification(){}

    interface AppNotificationListener{
        void notificationReady(InAppNotification inAppNotification);
    }

    InAppNotification initWithJSON(JSONObject jsonObject, boolean videoSupported){
        this.videoSupported = videoSupported;
        this.jsonDescription = jsonObject;
        try {

            this.type = jsonObject.has(Constants.KEY_TYPE) ? jsonObject.getString(Constants.KEY_TYPE) : null;

            if(this.type ==null || this.type.equals(Constants.KEY_CUSTOM_HTML)){
                legacyConfigureWithJson(jsonObject);
            }else{
                configureWithJson(jsonObject);
            }

        } catch (JSONException e) {
            this.error = "Invalid JSON : "+e.getLocalizedMessage();
        }
        return this;

    }

    private void legacyConfigureWithJson(JSONObject jsonObject){
        Bundle b = getBundleFromJsonObject(jsonObject);
        if(!validateNotifBundle(b)){
            this.error = "Invalid JSON";
            return;
        }
        try {
            this.id = jsonObject.has(Constants.INAPP_ID_IN_PAYLOAD) ? jsonObject.getString(Constants.INAPP_ID_IN_PAYLOAD) : "";
            this.campaignId = jsonObject.has(Constants.NOTIFICATION_ID_TAG) ? jsonObject.getString(Constants.NOTIFICATION_ID_TAG) : "";
            this.excludeFromCaps = jsonObject.has(Constants.KEY_EFC) && jsonObject.getInt(Constants.KEY_EFC) == 1;
            this.totalLifetimeCount = jsonObject.has(Constants.KEY_TLC) ? jsonObject.getInt(Constants.KEY_TLC) : -1;
            this.totalDailyCount = jsonObject.has(Constants.KEY_TDC) ? jsonObject.getInt(Constants.KEY_TDC) : -1;
            this.jsEnabled = jsonObject.has(Constants.INAPP_JS_ENABLED) && jsonObject.getBoolean(Constants.INAPP_JS_ENABLED);

            JSONObject data = jsonObject.has(Constants.INAPP_DATA_TAG) ? jsonObject.getJSONObject(Constants.INAPP_DATA_TAG) : null;
            if (data != null) {
                this.html = data.getString(Constants.INAPP_HTML_TAG);

                this.customInAppUrl = data.has(Constants.KEY_URL) ? data.getString(Constants.KEY_URL) : "";

                this.customExtras = data.has(Constants.KEY_KV) ? data.getJSONObject(Constants.KEY_KV) : null;
                if (this.customExtras == null)
                    this.customExtras = new JSONObject();

                JSONObject displayParams = jsonObject.getJSONObject(Constants.INAPP_WINDOW);
                if (displayParams != null) {
                    this.darkenScreen = displayParams.getBoolean(Constants.INAPP_NOTIF_DARKEN_SCREEN);
                    this.showClose = displayParams.getBoolean(Constants.INAPP_NOTIF_SHOW_CLOSE);
                    this.position = displayParams.getString(Constants.INAPP_POSITION).charAt(0);
                    this.width = displayParams.has(Constants.INAPP_X_DP) ? displayParams.getInt(Constants.INAPP_X_DP) : 0;
                    this.widthPercentage = displayParams.has(Constants.INAPP_X_PERCENT) ? displayParams.getInt(Constants.INAPP_X_PERCENT) : 0;
                    this.height = displayParams.has(Constants.INAPP_Y_DP) ? displayParams.getInt(Constants.INAPP_Y_DP) : 0;
                    this.heightPercentage = displayParams.has(Constants.INAPP_Y_PERCENT) ? displayParams.getInt(Constants.INAPP_Y_PERCENT) : 0;
                    this.maxPerSession = displayParams.has(Constants.INAPP_MAX_DISPLAY_COUNT) ? displayParams.getInt(Constants.INAPP_MAX_DISPLAY_COUNT) : -1;
                }

                if (this.html != null) {
                    if (this.position == 't' && this.widthPercentage == 100 && this.heightPercentage <= 30)
                        this.inAppType = InAppType.AppTypeHeaderHTML;
                    else if (this.position == 'b' && this.widthPercentage == 100 && this.heightPercentage <= 30)
                        this.inAppType = InAppType.AppTypeFooterHTML;
                    else if (this.position == 'c' && this.widthPercentage == 90 && this.heightPercentage == 85)
                        this.inAppType = InAppType.AppTypeInterstitialHTML;
                    else if (this.position == 'c' && this.widthPercentage == 100 && this.heightPercentage == 100)
                        this.inAppType = InAppType.AppTypeCoverHTML;
                    else if (this.position == 'c' && this.widthPercentage == 90 && this.heightPercentage == 50)
                        this.inAppType = InAppType.AppTypeHalfInterstitialHTML;
                }
            }
        } catch (JSONException e){
            this.error = "Invalid JSON";
        }
    }

    private void configureWithJson(JSONObject jsonObject){
        try {
            this.id = jsonObject.has(Constants.INAPP_ID_IN_PAYLOAD) ? jsonObject.getString(Constants.INAPP_ID_IN_PAYLOAD) : "";
            this.campaignId = jsonObject.has(Constants.NOTIFICATION_ID_TAG) ? jsonObject.getString(Constants.NOTIFICATION_ID_TAG) : "";
            this.type = jsonObject.getString(Constants.KEY_TYPE);
            this.excludeFromCaps = jsonObject.has(Constants.KEY_EFC) && jsonObject.getInt(Constants.KEY_EFC) == 1;
            this.totalLifetimeCount = jsonObject.has(Constants.KEY_TLC) ? jsonObject.getInt(Constants.KEY_TLC) : -1;
            this.totalDailyCount = jsonObject.has(Constants.KEY_TDC) ? jsonObject.getInt(Constants.KEY_TDC) : -1;
            this.inAppType = InAppType.fromString(this.type);
            this.isTablet = jsonObject.has(Constants.KEY_IS_TABLET) && jsonObject.getBoolean(Constants.KEY_IS_TABLET);
            this.backgroundColor = jsonObject.has(Constants.KEY_BG) ? jsonObject.getString(Constants.KEY_BG) : Constants.WHITE;
            this.isPortrait = !jsonObject.has(Constants.KEY_PORTRAIT) || jsonObject.getBoolean(Constants.KEY_PORTRAIT);
            this.isLandscape = jsonObject.has(Constants.KEY_LANDSCAPE) && jsonObject.getBoolean(Constants.KEY_LANDSCAPE);
            JSONObject titleObject = jsonObject.has(Constants.KEY_TITLE) ? jsonObject.getJSONObject(Constants.KEY_TITLE) : null;
            if(titleObject != null) {
                this.title = titleObject.has(Constants.KEY_TEXT) ? titleObject.getString(Constants.KEY_TEXT) : "";
                this.titleColor = titleObject.has(Constants.KEY_COLOR) ? titleObject.getString(Constants.KEY_COLOR) : Constants.BLACK;
            }
            JSONObject msgObject = jsonObject.has(Constants.KEY_MESSAGE) ? jsonObject.getJSONObject(Constants.KEY_MESSAGE) : null;
            if(msgObject != null) {
                this.message = msgObject.has(Constants.KEY_TEXT) ? msgObject.getString(Constants.KEY_TEXT) : "";
                this.messageColor = msgObject.has(Constants.KEY_COLOR) ? msgObject.getString(Constants.KEY_COLOR) : Constants.BLACK;
            }
            this.hideCloseButton = jsonObject.has(Constants.KEY_HIDE_CLOSE) && jsonObject.getBoolean(Constants.KEY_HIDE_CLOSE);
            JSONObject media = jsonObject.has(Constants.KEY_MEDIA) ? jsonObject.getJSONObject(Constants.KEY_MEDIA) : null;
            if(media!=null){
                AppNotificationMedia portraitMedia = new AppNotificationMedia().initWithJSON(media, Configuration.ORIENTATION_PORTRAIT);
                if(portraitMedia!=null) {
                    mediaList.add(portraitMedia);
                }
            }

            JSONObject media_landscape = jsonObject.has(Constants.KEY_MEDIA_LANDSCAPE) ? jsonObject.getJSONObject(Constants.KEY_MEDIA_LANDSCAPE) : null;
            if(media_landscape!=null){
                AppNotificationMedia landscapeMedia = new AppNotificationMedia().initWithJSON(media_landscape,Configuration.ORIENTATION_LANDSCAPE);
                if(landscapeMedia!=null) {
                    mediaList.add(landscapeMedia);
                }
            }
            JSONArray buttonArray = jsonObject.has(Constants.KEY_BUTTONS) ? jsonObject.getJSONArray(Constants.KEY_BUTTONS) : null;
            if(buttonArray  != null) {
                for (int i = 0; i < buttonArray.length(); i++) {
                    AppNotificationButton inAppNotificationButton = new AppNotificationButton().initWithJSON(buttonArray.getJSONObject(i));
                    if (inAppNotificationButton != null && inAppNotificationButton.getError() == null) {
                        this.buttons.add(inAppNotificationButton);
                        this.buttonCount++;
                    }
                }
            }
            switch(this.inAppType){
                case AppTypeFooter:
                case AppTypeHeader:
                    for(AppNotificationMedia inAppMedia : this.mediaList){
                        if(inAppMedia.isGIF() || inAppMedia.isAudio() || inAppMedia.isVideo()){
                            inAppMedia.setMediaUrl(null);
                            Logger.d("Unable to download to media. Wrong media type for template");
                        }
                    }
                    break;
                case AppTypeCover:
                case AppTypeHalfInterstitial:
                    for(AppNotificationMedia inAppMedia : this.mediaList){
                        if(inAppMedia.isGIF() || inAppMedia.isAudio() || inAppMedia.isVideo()){
                            inAppMedia.setMediaUrl(null);
                            Logger.d("Unable to download to media. Wrong media type for template");
                        }
                    }
                    break;
                case AppTypeCoverImageOnly:
                case AppTypeHalfInterstitialImageOnly:
                case AppTypeInterstitialImageOnly:
                    if(!this.mediaList.isEmpty()) {
                        for (AppNotificationMedia inAppMedia : this.mediaList) {
                            if (inAppMedia.isGIF() || inAppMedia.isAudio() || inAppMedia.isVideo() || !inAppMedia.isImage()) {
                                this.error = "Wrong media type for template";
                            }
                        }
                    }else{
                        this.error = "No media type for template";
                    }
                    break;
            }
        }catch (JSONException e){
            this.error = "Invalid JSON"+e.getLocalizedMessage();
        }
    }

    void prepareForDisplay() {

        for(AppNotificationMedia media : this.mediaList){
            if(media.isGIF()){
                GifCache.init();
                if (this.getGifByteArray(media) != null) {
                    listener.notificationReady(this);
                    return;
                }

                if(media.getMediaUrl() != null) {
                    Logger.v("InAppNotification: downloading GIF :" + media.getMediaUrl());
                    byte[] gifByteArray = Utils.getByteArrayFromImageURL(media.getMediaUrl());
                    if (gifByteArray != null) {
                        Logger.v("GIF Downloaded from url: " + media.getMediaUrl());
                        if (!GifCache.addByteArray(media.getCacheKey(), gifByteArray)) {
                            this.error = "Error processing GIF";
                        }
                    }
                }
            }
            else if(media.isImage()){
                ImageCache.init();
                if (this.getImage(media) != null) {
                    listener.notificationReady(this);
                    return;
                }

                if(media.getMediaUrl() !=null) {
                    Logger.v("InAppNotification: downloading Image :" + media.getMediaUrl());
                    Bitmap imageBitmap = Utils.getBitmapFromURL(media.getMediaUrl());
                    if (imageBitmap != null) {
                        Logger.v("Image Downloaded from url: " + media.getMediaUrl());
                        if (!ImageCache.addBitmap(media.getCacheKey(), imageBitmap)) {
                            this.error = "Error processing image";
                        }
                    }
                }
            }else if(media.isVideo() || media.isAudio()){
                if (!this.videoSupported) {
                    this.error = "InApp Video/Audio is not supported";
                }
            }
        }
        listener.notificationReady(this);
    }

    private boolean validateNotifBundle(Bundle notif) {
        try {
            final Bundle w = notif.getBundle(Constants.INAPP_WINDOW);
            final Bundle d = notif.getBundle("d");
            if (w == null || d == null) return false;

            // Check that either xdp or xp is set
            if (!isKeyValid(w, Constants.INAPP_X_DP, Integer.class))
                if (!isKeyValid(w, Constants.INAPP_X_PERCENT, Integer.class))
                    return false;

            // Check that either ydp or yp is set
            if (!isKeyValid(w, Constants.INAPP_Y_DP, Integer.class))
                if (!isKeyValid(w, Constants.INAPP_Y_PERCENT, Integer.class))
                    return false;

            // Check that dk is set
            if (!(isKeyValid(w, Constants.INAPP_NOTIF_DARKEN_SCREEN, Boolean.class)))
                return false;

            // Check that sc is set
            if (!(isKeyValid(w, Constants.INAPP_NOTIF_SHOW_CLOSE, Boolean.class)))
                return false;

            // Check that html is set
            if (!(isKeyValid(d, Constants.INAPP_HTML_TAG, String.class)))
                return false;

            // Check that pos contains the right value
            if ((isKeyValid(w, Constants.INAPP_POSITION, String.class))) {
                //noinspection ConstantConditions
                char pos = w.getString(Constants.INAPP_POSITION).charAt(0);
                switch (pos) {
                    case Constants.INAPP_POSITION_TOP:
                        break;
                    case Constants.INAPP_POSITION_RIGHT:
                        break;
                    case Constants.INAPP_POSITION_BOTTOM:
                        break;
                    case Constants.INAPP_POSITION_LEFT:
                        break;
                    case Constants.INAPP_POSITION_CENTER:
                        break;
                    default:
                        return false;
                }
            } else
                return false;

            return true;
        } catch (Throwable t) {
            Logger.v("Failed to parse in-app notification!", t);
            return false;
        }
    }

    private boolean isKeyValid(Bundle b, String key, Class<?> type) {
        //noinspection ConstantConditions
        return b.containsKey(key) && b.get(key).getClass().equals(type);
    }

    private static Bundle getBundleFromJsonObject(JSONObject notif) {
        Bundle b = new Bundle();
        Iterator iterator = notif.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            try {
                Object value = notif.get(key);
                if (value instanceof String)
                    b.putString(key, (String) value);
                else if (value instanceof Character)
                    b.putChar(key, (Character) value);
                else if (value instanceof Integer)
                    b.putInt(key, (Integer) value);
                else if (value instanceof Float)
                    b.putFloat(key, (Float) value);
                else if (value instanceof Double)
                    b.putDouble(key, (Double) value);
                else if (value instanceof Long)
                    b.putLong(key, (Long) value);
                else if (value instanceof Boolean)
                    b.putBoolean(key, (Boolean) value);
                else if (value instanceof JSONObject)
                    b.putBundle(key, getBundleFromJsonObject((JSONObject) value));
            } catch (JSONException e) {
                Logger.v("Key had unknown object. Discarding");
            }
        }
        return b;
    }

    String getId() {
        return id;
    }

    String getCampaignId() {
        return campaignId;
    }

    @SuppressWarnings({"WeakerAccess"})
    public InAppType getInAppType() {
        return inAppType;
    }

    String getHtml() {
        return html;
    }

    boolean isExcludeFromCaps() {
        return excludeFromCaps;
    }

    boolean isShowClose() {
        return showClose;
    }

    boolean isDarkenScreen() {
        return darkenScreen;
    }

    int getMaxPerSession() {
        return maxPerSession;
    }

    int getTotalLifetimeCount() {
        return totalLifetimeCount;
    }

    int getTotalDailyCount() {
        return totalDailyCount;
    }

    char getPosition() {
        return position;
    }

    int getHeight() {
        return height;
    }

    int getHeightPercentage() {
        return heightPercentage;
    }

    int getWidth() {
        return width;
    }

    int getWidthPercentage() {
        return widthPercentage;
    }

    JSONObject getJsonDescription() {
        return jsonDescription;
    }

    String getError() {
        return error;
    }

    JSONObject getCustomExtras() {
        return customExtras;
    }

    @SuppressWarnings({"unused"})
    public JSONObject getActionExtras() {
        return actionExtras;
    }

    String getType() {
        return type;
    }

    String getTitle() {
        return title;
    }

    String getTitleColor() {
        return titleColor;
    }

    String getMessage() {
        return message;
    }

    String getMessageColor() {
        return messageColor;
    }

    String getBackgroundColor() {
        return backgroundColor;
    }

    boolean isTablet() {
        return isTablet;
    }

    ArrayList<AppNotificationButton> getButtons() {
        return buttons;
    }

    ArrayList<AppNotificationMedia> getMediaList() {
        return mediaList;
    }

    String getCustomInAppUrl() {
        return customInAppUrl;
    }

    boolean isJsEnabled() {
        return jsEnabled;
    }

    Bitmap getImage(AppNotificationMedia inAppMedia) {
        return ImageCache.getBitmap(inAppMedia.getCacheKey());
    }

    AppNotificationMedia getInAppMediaForOrientation(int orientation){
        AppNotificationMedia returningMedia = null;
        for(AppNotificationMedia inAppNotificationMedia : this.mediaList){
            if(orientation == inAppNotificationMedia.getOrientation()){
                returningMedia = inAppNotificationMedia;
                break;
            }
        }
        return returningMedia;
    }

    byte[] getGifByteArray(AppNotificationMedia inAppMedia){
        return GifCache.getByteArray(inAppMedia.getCacheKey());
    }

    void didDismiss() {
        removeImageOrGif();
    }

    private void removeImageOrGif(){
        for (AppNotificationMedia inAppMedia : this.mediaList){
            if (inAppMedia.getMediaUrl() != null){
                if (!inAppMedia.getContentType().equals("image/gif")) {
                    ImageCache.removeBitmap(inAppMedia.getCacheKey(), false);
                    Logger.v("Deleted image - "+inAppMedia.getCacheKey());
                } else {
                    GifCache.removeByteArray(inAppMedia.getCacheKey());
                    Logger.v("Deleted GIF - "+inAppMedia.getCacheKey());
                }
            }
        }
    }

    boolean isPortrait() {
        return isPortrait;
    }

    boolean isLandscape() {
        return isLandscape;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isHideCloseButton() {
        return hideCloseButton;
    }

    int getButtonCount() {
        return buttonCount;
    }


    private InAppNotification(Parcel in) {
        try {
            id = in.readString();
            campaignId = in.readString();
            inAppType = (InAppType) in.readValue(InAppType.class.getClassLoader());
            html = in.readString();
            excludeFromCaps = in.readByte() != 0x00;
            showClose = in.readByte() != 0x00;
            darkenScreen = in.readByte() != 0x00;
            maxPerSession = in.readInt();
            totalLifetimeCount = in.readInt();
            totalDailyCount = in.readInt();
            position = (char) in.readValue(char.class.getClassLoader());
            height = in.readInt();
            heightPercentage = in.readInt();
            width = in.readInt();
            widthPercentage = in.readInt();
            jsonDescription = in.readByte() == 0x00 ? null : new JSONObject(in.readString());
            error = in.readString();
            customExtras = in.readByte() == 0x00 ? null : new JSONObject(in.readString());
            actionExtras = in.readByte() == 0x00 ? null : new JSONObject(in.readString());
            type = in.readString();
            title = in.readString();
            titleColor = in.readString();
            backgroundColor = in.readString();
            message = in.readString();
            messageColor = in.readString();
            try {
                buttons = in.createTypedArrayList(AppNotificationButton.CREATOR);
            } catch (Throwable t) {
                // no-op
            }
            try {
                mediaList = in.createTypedArrayList(AppNotificationMedia.CREATOR);
            } catch (Throwable t) {
                // no-op
            }
            hideCloseButton = in.readByte() != 0x00;
            buttonCount = in.readInt();
            isTablet = in.readByte() != 0x00;
            customInAppUrl = in.readString();
            jsEnabled = in.readByte() != 0x00;
            isPortrait = in.readByte() != 0x00;
            isLandscape = in.readByte() != 0x00;
            landscapeImageUrl = in.readString();
            _landscapeImageCacheKey = in.readString();

        }catch (JSONException e){
            // no-op
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(campaignId);
        dest.writeValue(inAppType);
        dest.writeString(html);
        dest.writeByte((byte) (excludeFromCaps ? 0x01 : 0x00));
        dest.writeByte((byte) (showClose ? 0x01 : 0x00));
        dest.writeByte((byte) (darkenScreen ? 0x01 : 0x00));
        dest.writeInt(maxPerSession);
        dest.writeInt(totalLifetimeCount);
        dest.writeInt(totalDailyCount);
        dest.writeValue(position);
        dest.writeInt(height);
        dest.writeInt(heightPercentage);
        dest.writeInt(width);
        dest.writeInt(widthPercentage);
        if (jsonDescription == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(jsonDescription.toString());
        }
        dest.writeString(error);
        if (customExtras == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(customExtras.toString());
        }
        if (actionExtras == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(actionExtras.toString());
        }
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(titleColor);
        dest.writeString(backgroundColor);
        dest.writeString(message);
        dest.writeString(messageColor);
        dest.writeTypedList(buttons);
        dest.writeTypedList(mediaList);
        dest.writeByte((byte) (hideCloseButton ? 0x01 : 0x00));
        dest.writeInt(buttonCount);
        dest.writeByte((byte) (isTablet ? 0x01 : 0x00));
        dest.writeString(customInAppUrl);
        dest.writeByte((byte) (jsEnabled ? 0x01 : 0x00));
        dest.writeByte((byte) (isPortrait ? 0x01 : 0x00));
        dest.writeByte((byte) (isLandscape ? 0x01 : 0x00));
        dest.writeString(landscapeImageUrl);
        dest.writeString(_landscapeImageCacheKey);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<InAppNotification> CREATOR = new Parcelable.Creator<InAppNotification>() {
        @Override
        public InAppNotification createFromParcel(Parcel in) {
            return new InAppNotification(in);
        }

        @Override
        public InAppNotification[] newArray(int size) {
            return new InAppNotification[size];
        }
    };

    // intended to only hold an gif byte array reference for the life of the parent InAppNotification, in order to facilitate parceling
    private static class GifCache {
        private static final int MIN_CACHE_SIZE = 1024 * 5; // 5mb minimum (in KB)
        private final static int maxMemory = (int) (Runtime.getRuntime().maxMemory())/1024;
        private final static int cacheSize = Math.max((maxMemory / 32), MIN_CACHE_SIZE);

        private static LruCache<String, byte[]> mMemoryCache;

        static void init(){
            synchronized (GifCache.class) {
                if(mMemoryCache == null) {
                    Logger.v("InAppNotification.GifCache: init with max device memory: " + maxMemory + "KB and allocated cache size: " + cacheSize + "KB");
                    try {
                        mMemoryCache = new LruCache<String, byte[]>(cacheSize) {
                            @Override
                            protected int sizeOf(String key, byte[] byteArray) {
                                // The cache size will be measured in kilobytes rather than
                                // number of items.
                                int size = getByteArraySizeInKB(byteArray);
                                Logger.v( "InAppNotification.GifCache: have gif of size: "+size + "KB for key: " + key);
                                return size;
                            }
                        };
                    } catch (Throwable t) {
                        Logger.v( "InAppNotification.GifCache: unable to initialize cache: ", t.getCause());
                    }
                }
            }
        }

        private static int getByteArraySizeInKB(byte[] byteArray) {
            return byteArray.length / 1024;
        }

        private static int getAvailableMemory() {
            synchronized (GifCache.class) {
                return mMemoryCache == null ? 0 : cacheSize - mMemoryCache.size();
            }
        }

        private static boolean isEmpty() {
            synchronized (GifCache.class) {
                return mMemoryCache.size() <= 0;
            }
        }

        private static void cleanup() {
            synchronized (GifCache.class) {
                if (isEmpty()) {
                    Logger.v( "InAppNotification.GifCache: cache is empty, removing it");
                    mMemoryCache = null;
                }
            }
        }

        static boolean addByteArray(String key, byte[] byteArray) {

            if(mMemoryCache == null) return false;

            if (getByteArray(key) == null) {
                synchronized (GifCache.class) {
                    int arraySize = getByteArraySizeInKB(byteArray);
                    int available = getAvailableMemory();
                    Logger.v( "InAppNotification.GifCache: gif size: "+ arraySize +"KB. Available mem: "+available+"KB.");
                    if (arraySize > getAvailableMemory()) {
                        Logger.v( "InAppNotification.GifCache: insufficient memory to add gif: " + key);
                        return false;
                    }
                    mMemoryCache.put(key, byteArray);
                    Logger.v( "InAppNotification.GifCache: added gif for key: " + key);
                }
            }
            return true;
        }

        static byte[] getByteArray(String key) {
            synchronized (GifCache.class) {
                return mMemoryCache == null ? null : mMemoryCache.get(key);
            }
        }

        static void removeByteArray(String key) {
            synchronized (GifCache.class) {
                if (mMemoryCache == null) return;
                mMemoryCache.remove(key);
                Logger.v( "InAppNotification.GifCache: removed gif for key: " + key);
                cleanup();
            }
        }
    }
}

