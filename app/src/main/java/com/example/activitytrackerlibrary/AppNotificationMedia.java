package com.example.activitytrackerlibrary;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class AppNotificationMedia  implements Parcelable {

    private String mediaUrl;
    private String contentType;
    private String cacheKey;
    int orientation;

    AppNotificationMedia(){}

    AppNotificationMedia initWithJSON(JSONObject mediaObject, int orientation){
        this.orientation = orientation;
        try {
            this.contentType = mediaObject.has(Constants.KEY_CONTENT_TYPE) ? mediaObject.getString(Constants.KEY_CONTENT_TYPE) : "";
            String mediaUrl = mediaObject.has(Constants.KEY_URL) ? mediaObject.getString(Constants.KEY_URL) : "";
            if (!mediaUrl.isEmpty()) {
                if (this.contentType.startsWith("image")) {
                    this.mediaUrl = mediaUrl;
                    if (mediaObject.has("key")) {
                        this.cacheKey = UUID.randomUUID().toString() + mediaObject.getString("key");
                    } else {
                        this.cacheKey = UUID.randomUUID().toString();
                    }
                } else {
                    this.mediaUrl = mediaUrl;
                }
            }
        }catch (JSONException e){
            Logger.v("Error parsing Media JSONObject - "+e.getLocalizedMessage());
        }
        if(contentType.isEmpty()){
            return null;
        }else {
            return this;
        }
    }

    String getMediaUrl() {
        return mediaUrl;
    }

    String getContentType() {
        return contentType;
    }

    String getCacheKey() {
        return cacheKey;
    }

    @SuppressWarnings("SameParameterValue")
    void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getOrientation() {
        return orientation;
    }

    boolean isImage() {
        String contentType = this.getContentType();
        return contentType != null && this.mediaUrl != null && contentType.startsWith("image") && !contentType.equals("image/gif");
    }

    boolean isGIF () {
        String contentType = this.getContentType();
        return contentType != null && this.mediaUrl != null && contentType.equals("image/gif");
    }

    boolean isVideo () {
        String contentType = this.getContentType();
        return contentType != null && this.mediaUrl != null && contentType.startsWith("video");
    }

    boolean isAudio () {
        String contentType = this.getContentType();
        return contentType != null && this.mediaUrl != null && contentType.startsWith("audio");
    }

    private AppNotificationMedia(Parcel in) {
        mediaUrl = in.readString();
        contentType = in.readString();
        cacheKey = in.readString();
        orientation = in.readInt();
    }

    public static final Creator<AppNotificationMedia> CREATOR = new Creator<AppNotificationMedia>() {
        @Override
        public AppNotificationMedia createFromParcel(Parcel in) {
            return new AppNotificationMedia(in);
        }

        @Override
        public AppNotificationMedia[] newArray(int size) {
            return new AppNotificationMedia[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaUrl);
        dest.writeString(contentType);
        dest.writeString(cacheKey);
        dest.writeInt(orientation);
    }
}

