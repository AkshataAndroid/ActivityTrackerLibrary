package com.example.activitytrackerlibrary;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public  class AppBaseFullFragment extends AppBaseFragment {

    @Override
    void cleanup() {/* no-op */}

    @Override
    void generateListener() {
//        if (parent instanceof AppNotificationActivity) {
//            setListener((AppBaseFragment.InAppListener) parent);
//        }
    }

//    boolean isTablet(){
//        WindowManager wm = (WindowManager) getActivity().getBaseContext().getSystemService(Context.WINDOW_SERVICE);
//        if (wm == null) {
//            Logger.v("Screen size is null ");
//            return false;
//        }
//        DisplayMetrics dm = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(dm);
//        float yInches= dm.heightPixels/dm.ydpi;
//        float xInches= dm.widthPixels/dm.xdpi;
//        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
//        if (diagonalInches>=6.5){
//            Logger.v("Screen size is : "+diagonalInches);
//            return true;
//        }else{
//            Logger.v("Screen size is : "+diagonalInches);
//            return false;
//        }
//    }
}

