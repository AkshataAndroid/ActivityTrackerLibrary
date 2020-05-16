package com.example.activitytrackerlibrary;

import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

class WebInterface {
    private WeakReference<TrackerApi> weakReference;

    public WebInterface(TrackerApi instance){
        this.weakReference = new WeakReference<>(instance);
    }


    /**
     * Method to be called from WebView Javascript to raise event in App
     * @param eventName String value of event name
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void pushEvent(String eventName){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d(" Instance is null.");
        } else {
            trackerApi.pushEvent(eventName);
        }
    }

    /**
     * Method to be called from WebView Javascript to raise event with event properties in App
     * @param eventName String value of event name
     * @param eventActions Stringified JSON Object of event properties
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void pushEvent(String eventName, String eventActions){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d(" Instance is null.");
        } else {
            if(eventActions!=null){
                try {
                    JSONObject eventActionsObject = new JSONObject(eventActions);
                    trackerApi.pushEvent(eventName,Utils.convertJSONObjectToHashMap(eventActionsObject));
                } catch (JSONException e) {
                    Logger.v("Unable to parse eventActions from WebView "+e.getLocalizedMessage());
                }
            }else{
                Logger.v("eventActions passed to CTWebInterface is null");
            }
        }


    }

    /**
     * Method to be called from WebView Javascript to push profile properties in AppTap
     * @param profile Stringified JSON Object of profile properties
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void pushProfile(String profile){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if (profile != null) {
                try {
                    JSONObject profileObject = new JSONObject(profile);
                    trackerApi.pushProfile(Utils.convertJSONObjectToHashMap(profileObject));
                } catch (JSONException e) {
                    Logger.v("Unable to parse profile from WebView " + e.getLocalizedMessage());
                }
            } else {
                Logger.v("profile passed to CTWebInterface is null");
            }
        }
    }

    /**
     * Method to be called from WebView Javascript to add profile properties in AppTap
     * @param key String value of profile property key
     * @param value String value of profile property value
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void addMultiValueForKey(String key, String value){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            trackerApi.addMultiValueForKey(key,value);
        }
    }

    /**
     * Method to be called from WebView Javascript to add profile properties in App
     * @param key String value of profile property key
     * @param values Stringified JSON Array of profile property values
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void addMultiValuesForKey(String key, String values){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if(key == null) {
                Logger.v("Key passed to CTWebInterface is null");
                return;
            }
            if(values != null) {
                try{
                    JSONArray valuesArray = new JSONArray(values);
                    trackerApi.addMultiValuesForKey(key, Utils.convertJSONArrayToArrayList(valuesArray));
                }catch (JSONException e){
                    Logger.v("Unable to parse values from WebView "+e.getLocalizedMessage());
                }
            }else{
                Logger.v("values passed to CTWebInterface is null");
            }
        }

    }

    /**
     * Method to be called from WebView Javascript to remove profile properties in App
     * @param key String value of profile property key
     * @param value String value of profile property value
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void removeMultiValueForKey(String key, String value){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if(key == null) {
                Logger.v("Key passed to CTWebInterface is null");
                return;
            }
            if(value == null) {
                Logger.v("Value passed to CTWebInterface is null");
                return;
            }
            trackerApi.removeMultiValueForKey(key,value);
        }
    }

    /**
     * Method to be called from WebView Javascript to remove profile properties in App
     * @param key String value of profile property key
     * @param values Stringified JSON Array of profile property values
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void removeMultiValuesForKey(String key, String values){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if(key == null) {
                Logger.v("Key passed to CTWebInterface is null");
                return;
            }
            if(values != null) {
                try{
                    JSONArray valuesArray = new JSONArray(values);
                    trackerApi.removeMultiValuesForKey(key, Utils.convertJSONArrayToArrayList(valuesArray));
                }catch (JSONException e){
                    Logger.v("Unable to parse values from WebView "+e.getLocalizedMessage());
                }
            }else{
                Logger.v("values passed to CTWebInterface is null");
            }
        }
    }

    /**
     * Method to be called from WebView Javascript to remove profile properties for given key in App
     * @param key String value of profile property key
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void removeValueForKey(String key){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if(key == null) {
                Logger.v("Key passed to CTWebInterface is null");
                return;
            }
            trackerApi.removeValueForKey(key);
        }
    }

    /**
     * Method to be called from WebView Javascript to set profile properties in App
     * @param key String value of profile property key
     * @param values Stringified JSON Array of profile property values
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void setMultiValueForKey(String key, String values){
        TrackerApi trackerApi = weakReference.get();
        if (trackerApi == null) {
            Logger.d("App Instance is null.");
        } else {
            if(key == null) {
                Logger.v("Key passed to CTWebInterface is null");
                return;
            }
            if(values != null) {
                try{
                    JSONArray valuesArray = new JSONArray(values);
                    trackerApi.setMultiValuesForKey(key, Utils.convertJSONArrayToArrayList(valuesArray));
                }catch (JSONException e){
                    Logger.v("Unable to parse values from WebView "+e.getLocalizedMessage());
                }
            }else{
                Logger.v("values passed to CTWebInterface is null");
            }
        }
    }
}
