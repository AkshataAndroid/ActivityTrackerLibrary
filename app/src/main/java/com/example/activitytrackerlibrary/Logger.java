package com.example.activitytrackerlibrary;

import android.util.Log;

public final class Logger {

    private int debugLevel;

    Logger(int level){
        this.debugLevel = level;
    }

    private int getDebugLevel() {
        return debugLevel;
    }

    private static int getStaticDebugLevel(){
        return TrackerApi.getDebugLevel();
    }

    /**
     * Logs to Debug if the debug level is greater than 1.
     */
    public static void d(String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG,message);
        }
    }

    public static void d(String suffix, String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG+":"+suffix, message);
        }
    }
    public static void d(String suffix, String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    public static void d(String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG,message,t);
        }
    }
    public void debug(String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG, message);
        }
    }

    public void debug(String suffix, String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            if(message.length() > 4000){
                Log.d(Constants.TRACKER_LOG_TAG + ":" + suffix, message.substring(0,4000));
                debug(suffix,message.substring(4000));
            }else {
                Log.d(Constants.TRACKER_LOG_TAG + ":" + suffix, message);
            }
        }
    }
    public void debug(String suffix, String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    @SuppressWarnings("unused")
    public void debug(String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.INFO.intValue()){
            Log.d(Constants.TRACKER_LOG_TAG,message,t);
        }
    }
    /**
     * Logs to Verbose if the debug level is greater than 2.
     */
    public static void v(String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG,message);
        }
    }
    public static void v(String suffix, String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG+":"+suffix, message);
        }
    }
    public  static void v(String suffix, String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    public static void v(String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG,message,t);
        }
    }
    public void verbose(String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG,message);
        }
    }
    public  void verbose(String suffix, String message){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            if(message.length() > 4000){
                Log.v(Constants.TRACKER_LOG_TAG + ":" + suffix, message.substring(0,4000));
                verbose(suffix,message.substring(4000));
            }else {
                Log.v(Constants.TRACKER_LOG_TAG + ":" + suffix, message);
            }
        }
    }
    public void verbose(String suffix, String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    public void verbose(String message, Throwable t){
        if(getStaticDebugLevel() > TrackerApi.LogLevel.DEBUG.intValue()){
            Log.v(Constants.TRACKER_LOG_TAG,message,t);
        }
    }

    /**
     * Logs to Info if the debug level is greater than or equal to 1.
     */
    public static void i(String message){
        if (getStaticDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG,message);
        }
    }
    @SuppressWarnings("unused")
    public static void i(String suffix, String message){
        if(getStaticDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG+":"+suffix, message);
        }
    }
    @SuppressWarnings("unused")
    public static void i(String suffix, String message, Throwable t){
        if(getStaticDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    @SuppressWarnings("SameParameterValue")
    public static void i(String message, Throwable t){
        if(getStaticDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG,message,t);
        }
    }

    @SuppressWarnings("unused")
    public void info(String message){
        if (getDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG,message);
        }
    }

    public  void info(String suffix, String message){
        if(getDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG+":"+suffix, message);
        }
    }
    @SuppressWarnings("unused")
    public void info(String suffix, String message, Throwable t){
        if(getDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG+":"+suffix, message,t);
        }
    }
    @SuppressWarnings("unused")
    public void info(String message, Throwable t){
        if(getDebugLevel() >= TrackerApi.LogLevel.INFO.intValue()){
            Log.i(Constants.TRACKER_LOG_TAG,message,t);
        }
    }


}
