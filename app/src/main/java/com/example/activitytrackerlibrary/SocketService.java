package com.example.activitytrackerlibrary;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SocketService  extends Service {

    public static Socket stream = null;
    private Boolean isConnected = false;
    Context context;
    public static boolean started = false;
    private static final SocketService instance = new SocketService();
    private Map<String, Object> customData;
    static SocService socket;
    DeviceInfo deviceInfo;
    public Long pingTimeStamp,pongTimeStamp;
    long differnceTimeStamp;
    public static String apiUrl = null;
    TrackerApi trackerApi;
    InAppFCManager inAppFCManager;
    int batteryLevel;
    int deviceStatus;
    String battryStatus;
    private  Boolean needBatteryStatus=false;
    private  Boolean needLocationDetails=false;
    public static long time;
    public static String msg;
    SocService socService;


    LocationService locationService;
    String latitude;
    EventDetails eventDetail;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // attachLifecycleListeners();
        // trackerApi = new TrackerApi(context,"");
        ActivityInstanceConfig config =  ActivityInstanceConfig.createInstance(this,"ACCOUNT_ID","ACCOUNT_TOKEN");
        trackerApi = TrackerApi.instanceWithConfig(this,config);
        locationService=new LocationService();
       // eventDetails=new EventDetails();
        needLocationDetails=true;

        this.registerReceiver(this.broadcastreceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        latitude=intent.getStringExtra("Latitude");


        if (intent != null && intent.getAction().equals("START")) {
            socket = new SocService();
            Log.i("SocService", "started SocService service");

            // String apiStatus = UserPreferences.getPreferences(getApplicationContext(), Const.KEY_API_URL);

            apiUrl = SocService.instance().api();
            // UserPreferences.setPreferences(getApplicationContext(), Const.KEY_API_URL, apiUrl);

            socketConnections();
            runHandler();


        }

        context = this;


        return START_STICKY;
    }

    private  void runHandler() {
        final Handler handler = new Handler();
        final  int delay = 10000; //milliseconds

        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            public void run() {
                //do something
                if(isConnected==true)
                {
                    //  sendToServer();
                     sendStatusCheck();
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
    }


    private void socketConnections() {
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            opts.reconnectionDelay = 10000;
            opts.reconnectionDelayMax = 10000;
            opts.reconnectionAttempts = 99999;
            // stream = IO.socket(Constants.API_URL, opts);
            stream = IO.socket(apiUrl, opts);


        } catch (Exception e) {
            e.printStackTrace();
        }

        stream.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void call(Object... args) {

                Log.d("Socket Connected", stream.toString());
                if (!isConnected) {
                    isConnected = true;
                    sendToServer();

                }
            }
        });

        stream.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = false;
            }
        });

        stream.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Socket Error", "Connectin error" + args[0]);
            }
        });

        stream.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });



        stream.on("response", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject maindata = (JSONObject) args[0];
                JsonProcessing js = new JsonProcessing();
                js.processJsonResponse(getApplicationContext(), maindata);
                pongTimeStamp=Constants.getTimeStamp();
                differnceTimeStamp=pongTimeStamp-pingTimeStamp;


                Log.d("PongTime", "Time"+differnceTimeStamp);
            }
        });



        stream.connect();
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendToServer() {

        try {
            JSONObject finalObject = new JSONObject();
            finalObject.put(Constants.EVENT_NAME, Constants.EVENT_PING_ALIVE);
            pingTimeStamp=Constants.getTimeStamp();
            Log.d("FinalOBJ", finalObject.toString()+"PING TIME "+pingTimeStamp);
            stream.emit("request", finalObject);
            sendStatusCheck();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryLevel=(int)(((float)level / (float)scale) * 100.0f);

            needBatteryStatus=true;

            if(deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING){
                battryStatus="Charging";


            }

            if(deviceStatus == BatteryManager.BATTERY_STATUS_DISCHARGING){

                battryStatus="Discharged";

            }

            if (deviceStatus == BatteryManager.BATTERY_STATUS_FULL){
                battryStatus="Full";

            }

            if(deviceStatus == BatteryManager.BATTERY_STATUS_UNKNOWN){
                battryStatus="Unknown";
            }


            if (deviceStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING){
                battryStatus="No Charging";

            }



        }
    };
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendStatusCheck()
    {
        try {
            String jsonStatus="";
            String locationStatus="";
            String eventstatus="";
            String buttoneventstatus="";


            JSONObject json = new JSONObject();
            json.put("eventName",trackerApi.ed.getName());
            JSONObject jsonData = new JSONObject();
            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date resultdate = new Date(yourmilliseconds);

            jsonData.put("SessionTime", sdf.format(resultdate));
            jsonData.put("Currunt Activity",trackerApi.getCurrentActivityName());
            long activitylastsession = trackerApi.getLastSessionLength();
            Date lastsesion = new Date(activitylastsession);
            jsonData.put("Last Session Length",sdf.format(lastsesion));
            JSONObject jsondeviceinfo=new JSONObject();
            jsondeviceinfo.put("Build version", trackerApi.deviceInfo.getBuild());
            jsondeviceinfo.put("VersionName", trackerApi.deviceInfo.getVersionName());
            jsondeviceinfo.put("Sdk Version", trackerApi.deviceInfo.getSdkVersion());
            jsondeviceinfo.put("OS Version", trackerApi.deviceInfo.getOsVersion());
            jsondeviceinfo.put("Manufracturer", trackerApi.deviceInfo.getManufacturer());
            jsondeviceinfo.put("Model Name", trackerApi.deviceInfo.getModel());
            jsondeviceinfo.put("Carrier", trackerApi.deviceInfo.getCarrier());
            jsondeviceinfo.put("Country Code", trackerApi.deviceInfo.getCountryCode());
            if(!needBatteryStatus){
                jsondeviceinfo.put("NeedBatteryStatus", "false");

            }else{
                jsondeviceinfo.put("Batterylevel", batteryLevel);
                jsondeviceinfo.put("BatteryStatus", battryStatus);
            }





            json.put("data",jsonData);
            json.put("Device Details",jsondeviceinfo);

            JSONObject locationDetails = new JSONObject();
            if(!needLocationDetails){
                jsondeviceinfo.put("NeedLocationDetails", "false");

            }else {

                locationDetails.put("Latitude", MainActivity.latitude);
                locationDetails.put("Longitude", MainActivity.longitude);
                locationDetails.put("Time", MainActivity.time);
                locationDetails.put("Speed", MainActivity.speed);
                locationDetails.put("Altitude", MainActivity.altitude);
                locationDetails.put("Accuracy", MainActivity.accuracy);
                locationDetails.put("Provider", MainActivity.provider);
                locationDetails.put("Date", MainActivity.date);
                locationDetails.put("GPS", MainActivity.gps);
                locationDetails.put("Network", MainActivity.network);
                locationDetails.put("Passive", MainActivity.passive);
            }
            JSONObject eventDetails = new JSONObject();
           // eventDetails.put("App Launched",trackerApi.ed.getName());
            eventDetails.put("Last Session Length",trackerApi.lastSessionLength+ " seconds");
            eventDetails.put("App Lifecycle",trackerApi.background);
//            eventDetails.put("Activity Destroyed time",time);

            //           eventDetails.put("App Launched",trackerApi.background);
         //   eventDetails.put("Activity Count",trackerApi.ed.getFirstTime());
           // eventDetails.put("Activity Count",trackerApi.ed.getLastTime());
            long firsttimemilliseconds = trackerApi.lastVisitTime;
            long lasttimemilliseconds = trackerApi.appLastSeen;
            SimpleDateFormat datetime = new SimpleDateFormat(" YYYY:dd:MM HH:mm:ss");
            Date lastdestroytime = new Date(firsttimemilliseconds);
           Date lastresultdatetime = new Date(lasttimemilliseconds);
            eventDetails.put("Last  Destroyed time",trackerApi.lastVisitTime);
            eventDetails.put("Current Time",datetime.format(lastresultdatetime));

//            if(MainActivity.buttonclicked){
                JSONObject buttonevent = new JSONObject();
                buttonevent.put("Button Event", trackerApi.event);
                buttoneventstatus=buttonevent.toString();
                stream.emit("Button Event",buttonevent);
//            }else{
//
//            }

            jsonStatus=json.toString();
            locationStatus=locationDetails.toString();
            eventstatus=eventDetails.toString();

            Log.d("JSON Request", jsonStatus);
            Log.d("Location Request", locationStatus);
            Log.d("Event Details", eventstatus);
            Log.d("Button Event Details", buttoneventstatus);
            stream.emit("request", json,locationDetails,eventDetails);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v("App ", trackerApi.background);
        msg=trackerApi.background;
//        time=System.currentTimeMillis();

        //Code here
        stopSelf();
    }

}
