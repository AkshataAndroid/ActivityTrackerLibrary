package com.example.activitytrackerlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.Date;
import java.sql.Time;
import java.util.Timer;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
private static final String TAG = LocationService.class.getSimpleName();
static GoogleApiClient mLocationClient;
static LocationRequest mLocationRequest = new LocationRequest();
public static boolean isGPSEnabled = false;// flag for GPS status
        boolean isNetworkEnabled = false;// flag for network status
        boolean isPassiveEnable = false;// flag for GPS status
private static final int TWO_MINUTES = 1000 * 60 * 2;
public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationBroadcast";
public static final String EXTRA_LATITUDE = "extra_latitude";
public static final String EXTRA_LONGITUDE = "extra_longitude";
public static final String TIME = "yyyy-MM-dd hh:mm:ss";
public static final String DATE = "DDD/MM/MMMMM";
public static final String ACCURACY = "extra_accuracy";
public static final String SPEED = "extra_speed";
public static final String ALTITUDE = "extra_altitude";
public static final String PROVIDER = "extra_provider";
public static final String GPSPROVIDER = "gps_provider";
public static final String NETWORKPROVIDER = "network_provider";
public static final String PASSIVEPROVIDER = "passive_provider";
public static Location location;
        Context mContext;
        // flag for GPS status
        boolean canGetLocation = false;
        LocationListener  locationListener;
private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

// The minimum time between updates in milliseconds
private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
public static LocationManager locationManager;
        PendingIntent pendingIntent;
        LocationListener listener;
public static  double latitude;
public static  double longitude;
//    public static  double lat=location.getLatitude(); // latitude
//    public static   double longi=location.getLongitude(); // longitude
        String provider;
        Timer timer;
private static final LocationService mLocation = new LocationService();
        AlarmManager alarmManager;
public static LocationService instance() {
        return mLocation;
        }
public void Expermemnt(Activity mainActivity) {
        //   Toast.makeText(mainActivity, "Hello there", Toast.LENGTH_SHORT).show();
        }

@Override
public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager
        .isProviderEnabled(LocationManager.GPS_PROVIDER);

        Log.v("isGPSEnabled", "=" + isGPSEnabled);

        // getting network status
        isNetworkEnabled = locationManager
        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //getting passive status
        isPassiveEnable=locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        if(isGPSEnabled){
       // Toast.makeText(this, "gps is enabled", Toast.LENGTH_SHORT).show();

        }else if(isNetworkEnabled){
       // Toast.makeText(this, "network is enabled", Toast.LENGTH_SHORT).show();

        }else{
      //  Toast.makeText(this, "passive is enabled", Toast.LENGTH_SHORT).show();
        }



        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
//        timer = new Timer();
//        timer.schedule(new TimerTask(){
//            @Override
//            public void run(){
//                onLocationChanged(location);
//            }
//        },0,seconds);

        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes
        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;




        }
@Nullable
@Override
public IBinder onBind(Intent intent) {
        return null;
        }



/*
 * LOCATION CALLBACKS
 */



@Override
public void onConnected(Bundle dataBundle) {

//        Intent dailyQuotes = new Intent(mContext, LocationService.class);
//        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pi = PendingIntent.getService(mContext, 0, dailyQuotes, 0);
//        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, seconds, seconds, pi);

        // long remainder = timeInterval - 60 * 60;
//            int secs = remainder;


        mLocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(1000)
        .setFastestInterval(500);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if ( !(isGPSEnabled) && !(isNetworkEnabled )) {
        // no network provider is enabled
       // Toast.makeText(this, "no network provider is enabled " + location, Toast.LENGTH_SHORT).show();
        } else {
        this.canGetLocation = true;
        if (isNetworkEnabled) {
        location = null;
        locationManager.requestLocationUpdates(
        LocationManager.NETWORK_PROVIDER,
        1000,
        0, (android.location.LocationListener) locationListener);
        Log.d("Network", "Network");
        if (locationManager != null) {
        location = locationManager
        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        }
        }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
        location=null;
        if (location == null) {
        locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        1000,
        0, (android.location.LocationListener) locationListener);
        Log.d("GPS Enabled", "GPS Enabled");
        if (locationManager != null) {
        location = locationManager
        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        }
        }
        }
        }
        }
       // Toast.makeText(this, "user permissions have blocked the app from getting location updates", Toast.LENGTH_SHORT).show();
        return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);



        }

//    @SuppressLint("MissingPermission")
//    private void getLastKnownLocation() {
//        //Log.d(TAG, "getLastKnownLocation()");
//        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        if (location != null) {
//            Log.i(TAG, "LasKnown location. " +
//                    "Long: " + location.getLongitude() +
//                    " | Lat: " + location.getLatitude());
//
//            startLocationUpdates();
//
//        } else {
//            Log.w(TAG, "No location retrieved yet");
//            startLocationUpdates();
//
//            //here we can show Alert to start location
//        }
//    }
/*
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
@Override
public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient,this);
        }

//to get the location change
@SuppressLint("MissingPermission")
@Override
public void onLocationChanged(Location location) {
      //  Toast.makeText(this, "testing message, at location " + location, Toast.LENGTH_SHORT).show();

        if (location != null) {
        Bundle extras = location.getExtras();

//            try {
//                LocationManager locationManager = (LocationManager)  mContext.getSystemService(Context.LOCATION_SERVICE);
//
//                // getting GPS status
//                isGPSEnabled = locationManager
//                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//                Log.v("isGPSEnabled", "=" + isGPSEnabled);
//
//                // getting network status
//                isNetworkEnabled = locationManager
//                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//                Log.v("isNetworkEnabled", "=" + isNetworkEnabled);
//
//                if ( !(isGPSEnabled) && !(isNetworkEnabled )) {
//                    // no network provider is enabled
//                    Toast.makeText(this, "no network provider is enabled " + location, Toast.LENGTH_SHORT).show();
//                } else {
//                    this.canGetLocation = true;
//                    if (isNetworkEnabled) {
//                        location = null;
//                        locationManager.requestLocationUpdates(
//                                LocationManager.NETWORK_PROVIDER,
//                                MIN_TIME_BW_UPDATES,
//                                MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) locationListener);
//                        Log.d("Network", "Network");
//                        if (locationManager != null) {
//                            location = locationManager
//                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                            if (location != null) {
//                                latitude = location.getLatitude();
//                                longitude = location.getLongitude();
//                            }
//                        }
//                    }
//                    // if GPS Enabled get lat/long using GPS Services
//                    if (isGPSEnabled) {
//                        location=null;
//                        if (location == null) {
//                            locationManager.requestLocationUpdates(
//                                    LocationManager.GPS_PROVIDER,
//                                    MIN_TIME_BW_UPDATES,
//                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) locationListener);
//                            Log.d("GPS Enabled", "GPS Enabled");
//                            if (locationManager != null) {
//                                location = locationManager
//                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                if (location != null) {
//                                    latitude = location.getLatitude();
//                                    longitude = location.getLongitude();
//                                }
//                            }
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()),String.valueOf(new Date(location.getTime())),String.valueOf(new Time(location.getTime())),String.valueOf(location.getSpeed()),String.valueOf(location.getAccuracy()),String.valueOf(location.getAltitude()),String.valueOf(location.getProvider()),String.valueOf(isNetworkEnabled),String.valueOf(isGPSEnabled),String.valueOf(isPassiveEnable));

        }

        }

private void sendMessageToUI(String lat, String lng, String time,String date, String speed,String accuracy,String altitude,String provider,String network,String gps,String passive) {
        //  Log.d(TAG, "Sending info...");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        intent.putExtra(TIME,time);
        intent.putExtra(DATE,date);
        intent.putExtra(SPEED,speed);
        intent.putExtra(ACCURACY,accuracy);
        intent.putExtra(ALTITUDE,altitude);
        intent.putExtra(PROVIDER,provider);
        intent.putExtra(GPSPROVIDER,gps);
        intent.putExtra(NETWORKPROVIDER,network);
        intent.putExtra(PASSIVEPROVIDER,passive);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

@Override
public void onConnectionFailed(ConnectionResult connectionResult) {
        //  Log.d(TAG, "Failed to connect to Google API");

        // alertbox("GPS STATUS", "Your GPS is: OFF");
        // Toast.makeText(context, "Please turn on the GPS to get current location.", Toast.LENGTH_SHORT).show();




        }

@Override
public void onDestroy() {

        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient,this);
        super.onDestroy();
        }
        }