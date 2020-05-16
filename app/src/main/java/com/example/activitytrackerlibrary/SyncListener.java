package com.example.activitytrackerlibrary;

import org.json.JSONObject;

public interface SyncListener {
    void profileDataUpdated(JSONObject updates);
    void profileDidInitialize(String TrackerID);
}
