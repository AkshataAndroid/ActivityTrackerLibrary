package com.example.activitytrackerlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PushNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Intent launchIntent;

            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }

            if (extras.containsKey(Constants.DEEP_LINK_KEY)){
                launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra(Constants.DEEP_LINK_KEY)));
            } else {
                launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (launchIntent == null) {
                    return;
                }
            }

            TrackerApi.handleNotificationClicked(context,extras);

            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            launchIntent.putExtras(extras);

            //to prevent calling of pushNotificationClickedEvent(extras) in ActivityLifecycleCallback
            launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);

            context.startActivity(launchIntent);

            Logger.d("PushNotificationReceiver: handled notification: " + extras.toString());
        } catch (Throwable t) {
            Logger.v("PushNotificationReceiver: error handling notification", t);
        }
    }
}

