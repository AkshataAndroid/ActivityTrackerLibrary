package com.example.activitytrackerlibrary;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DisplayUnit implements Parcelable {

    /**
     * Display unit identifier
     */
    private String unitID;

    /**
     * Display Type Could be (banner,carousel,custom key value etc.)
     */
    private DisplayUnitType type;

    /**
     * Background Color
     */
    private String bgColor;


    /**
     * List of Display Content Items
     */
    private ArrayList<DisplayUnitContent> contents;

    /**
     * Custom Key Value Pairs
     */
    private HashMap<String, String> customExtras;

    private JSONObject jsonObject;

    private String error;

    //constructors
    private DisplayUnit(JSONObject jsonObject, String unitID, DisplayUnitType type,
                                 String bgColor, ArrayList<DisplayUnitContent> contentArray,
                                 JSONObject kvObject, String error) {
        this.jsonObject = jsonObject;
        this.unitID = unitID;
        this.type = type;
        this.bgColor = bgColor;
        this.contents = contentArray;
        this.customExtras = getKeyValues(kvObject);
        this.error = error;
    }


    @NonNull
    public static DisplayUnit toDisplayUnit(JSONObject jsonObject) {
        //logic to convert json obj to item
        try {
            String unitID = jsonObject.has(Constants.NOTIFICATION_ID_TAG) ? jsonObject.getString(Constants.NOTIFICATION_ID_TAG) : Constants.TEST_IDENTIFIER;
            DisplayUnitType displayUnitType = jsonObject.has(Constants.KEY_TYPE) ? DisplayUnitType.type(jsonObject.getString(Constants.KEY_TYPE)) : null;

            String bgColor = jsonObject.has(Constants.KEY_BG) ? jsonObject.getString(Constants.KEY_BG) : "";

            JSONArray contentArray = jsonObject.has(Constants.KEY_CONTENT) ? jsonObject.getJSONArray(Constants.KEY_CONTENT) : null;
            ArrayList<DisplayUnitContent> contentArrayList = new ArrayList<>();
            if (contentArray != null) {
                for (int i = 0; i < contentArray.length(); i++) {
                   DisplayUnitContent displayUnitContent = DisplayUnitContent.toContent(contentArray.getJSONObject(i));
                    if (TextUtils.isEmpty(displayUnitContent.getError())) {
                        contentArrayList.add(displayUnitContent);
                    }
                }
            }
            JSONObject customKV = null;
            //custom KV can be added to Display unit of any types, no need to add type check here
            if (jsonObject.has(Constants.KEY_CUSTOM_KV)) {
                customKV = jsonObject.getJSONObject(Constants.KEY_CUSTOM_KV);
            }
            return new DisplayUnit(jsonObject, unitID, displayUnitType, bgColor, contentArrayList, customKV, null);
        } catch (Exception e) {
            Logger.d(Constants.FEATURE_DISPLAY_UNIT, "Unable to init DisplayUnit with JSON - " + e.getLocalizedMessage());
            return new DisplayUnit(null, "", null, null, null, null, "Error Creating Display Unit from JSON : " + e.getLocalizedMessage());
        }
    }

    /**
     * Getter for the unitId of the Display Unit
     *
     * @return String
     */
    public String getUnitID() {
        return unitID;
    }

    public String getError() {
        return error;
    }

    /**
     * Getter for the Key Value pairs of the Display Unit
     *
     * @return HashMap<String, String>
     */
    @SuppressWarnings("unused")
    public HashMap<String, String> getCustomExtras() {
        return customExtras;
    }


    public JSONObject getJsonObject() {
        return jsonObject;
    }

    /**
     * Getter for the hex-value background color of the Display Unit e.g. #000000
     *
     * @return String
     */
    @SuppressWarnings("unused")
    public String getBgColor() {
        return bgColor;
    }

    /**
     * Getter for the DisplayUnitType of the Display Unit, Refer{@link DisplayUnitType}
     *
     * @return CTDisplayUnitType
     */
    @SuppressWarnings("unused")
    public DisplayUnitType getType() {
        return type;
    }


    @SuppressWarnings("unused")
    public ArrayList<DisplayUnitContent> getContents() {
        return contents;
    }

    /**
     * Getter for the WiZRK fields obj to be passed in the data for recording event.
     *
     * @return JSONObject
     */
    public JSONObject getWZRKFields() {
        try {
            if (jsonObject != null) {
                Iterator<String> iterator = jsonObject.keys();
                JSONObject object = new JSONObject();
                while (iterator.hasNext()) {
                    String keyName = iterator.next();
                    if (keyName.startsWith(Constants.WZRK_PREFIX)) {
                        object.put(keyName, jsonObject.get(keyName));
                    }
                }
                return object;
            }
        } catch (Exception e) {
            //no op
            Logger.d(Constants.FEATURE_DISPLAY_UNIT, "Error in getting WiZRK fields " + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * populates the custom key values pairs from json
     *
     * @param kvObj- Custom Key Values
     */
    private HashMap<String, String> getKeyValues(JSONObject kvObj) {
        try {
            if (kvObj != null) {
                Iterator<String> keys = kvObj.keys();
                if (keys != null) {
                    String key, value;
                    HashMap<String, String> hashMap = null;
                    while (keys.hasNext()) {
                        key = keys.next();
                        value = kvObj.getString(key);
                        if (!TextUtils.isEmpty(key)) {
                            if (hashMap == null)
                                hashMap = new HashMap<>();
                            hashMap.put(key, value);
                        }
                    }
                    return hashMap;
                }
            }
        } catch (Exception e) {
            //no op
            Logger.d(Constants.FEATURE_DISPLAY_UNIT, "Error in getting Key Value Pairs " + e.getLocalizedMessage());
        }
        return null;
    }

    public static final Creator<DisplayUnit> CREATOR = new Creator<DisplayUnit>() {
        @Override
        public DisplayUnit createFromParcel(Parcel in) {
            return new DisplayUnit(in);
        }

        @Override
        public DisplayUnit[] newArray(int size) {
            return new DisplayUnit[size];
        }
    };

    @SuppressWarnings("unchecked")
    private DisplayUnit(Parcel in) {
        try {
            this.unitID = in.readString();
            this.type = (DisplayUnitType) in.readValue(DisplayUnitType.class.getClassLoader());
            this.bgColor = in.readString();

            if (in.readByte() == 0x01) {
                contents = new ArrayList<>();
                in.readList(contents, DisplayUnitContent.class.getClassLoader());
            } else {
                contents = null;
            }

            this.customExtras = in.readHashMap(null);
            this.jsonObject = in.readByte() == 0x00 ? null : new JSONObject(in.readString());
            this.error = in.readString();
        } catch (Exception e) {
            error = "Error Creating Display Unit from parcel : " + e.getLocalizedMessage();
            Logger.d(Constants.FEATURE_DISPLAY_UNIT, error);
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(unitID);
        parcel.writeValue(type);
        parcel.writeString(bgColor);

        if (contents == null) {
            parcel.writeByte((byte) (0x00));
        } else {
            parcel.writeByte((byte) (0x01));
            parcel.writeList(contents);
        }

        parcel.writeMap(customExtras);
        if (jsonObject == null) {
            parcel.writeByte((byte) (0x00));
        } else {
            parcel.writeByte((byte) (0x01));
            parcel.writeString(jsonObject.toString());
        }
        parcel.writeString(error);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            stringBuilder.append(" Unit id- ").append(unitID);
            stringBuilder.append(", Type- ").append((type != null ? type.toString() : null));
            stringBuilder.append(", bgColor- ").append(bgColor);
            if (contents != null && !contents.isEmpty()) {
                for (int i = 0; i < contents.size(); i++) {
                    DisplayUnitContent item = contents.get(i);
                    if (item != null) {
                        stringBuilder.append(", Content Item:").append(i).append(" ").append(item.toString());
                        stringBuilder.append("\n");
                    }
                }
            }
            if (customExtras != null) {
                stringBuilder.append(", Custom KV:").append(customExtras);
            }
            stringBuilder.append(", JSON -").append(jsonObject);
            stringBuilder.append(", Error-").append(error);
            stringBuilder.append(" ]");
            return stringBuilder.toString();
        } catch (Exception e) {
            Logger.d(Constants.FEATURE_DISPLAY_UNIT, "Exception in toString:" + e);
        }
        return super.toString();
    }

}
