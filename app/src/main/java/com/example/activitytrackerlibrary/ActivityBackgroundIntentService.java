package com.example.activitytrackerlibrary;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class ActivityBackgroundIntentService extends IntentService {

    public final static String MAIN_ACTION = "com.activitytracker.BG_EVENT";

    public ActivityBackgroundIntentService() {
        super("ActivityBackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        TrackerApi.runBackgroundIntentService(getApplicationContext());

    }
}
