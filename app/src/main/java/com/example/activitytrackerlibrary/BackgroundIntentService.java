package com.example.activitytrackerlibrary;

import android.app.IntentService;
import android.content.Intent;

public class BackgroundIntentService extends IntentService {

    public final static String MAIN_ACTION = "com.app.BG_EVENT";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TrackerApi.runBackgroundIntentService(getApplicationContext());
    }
}
