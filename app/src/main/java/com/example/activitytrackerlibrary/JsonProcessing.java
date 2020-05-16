package com.example.activitytrackerlibrary;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonProcessing {
    private Context context;


    public void processJsonResponse(Context context, JSONObject maindata) {
        this.context = context;
        try {

            String jsonStr = maindata.toString();
            JSONObject json = new JSONObject(jsonStr);
            String eventName=json.getString("eventName");
            JSONArray pointsArray = maindata.getJSONArray("data");

            Log.d("eventName", eventName);

            // Log.d("jsonresp arr", pointsArray.toString());

        } catch (JSONException e) {
            Log.e("jsonresp JSONException", e.getMessage().toString());
        }
    }

}
