package com.example.activitytrackerlibrary;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public abstract class AppBasenativeFragment extends AppBaseFullFragment {
    void setupInAppButton(Button inAppButton, final AppNotificationButton inAppNotificationButton, final int buttonIndex){
        if(inAppNotificationButton!=null) {
            inAppButton.setVisibility(View.VISIBLE);
            inAppButton.setTag(buttonIndex);
            inAppButton.setText(inAppNotificationButton.getText());
            inAppButton.setTextColor(Color.parseColor(inAppNotificationButton.getTextColor()));
            inAppButton.setOnClickListener(new AppNativeButtonClickListener());

            ShapeDrawable borderDrawable = null;
            ShapeDrawable shapeDrawable = null;

            if(!inAppNotificationButton.getBorderRadius().isEmpty()) {
                shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2},null,
                        new float[]{0,0,0,0,0,0,0,0}));
                shapeDrawable.getPaint().setColor(Color.parseColor(inAppNotificationButton.getBackgroundColor()));
                shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
                shapeDrawable.getPaint().setAntiAlias(true);
                borderDrawable = new ShapeDrawable(new RoundRectShape(new float[]{
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                        Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2},null,
                        new float[]{Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2,
                                Float.parseFloat(inAppNotificationButton.getBorderRadius())*(480.0f/getDPI())*2}));
            }

            if(!inAppNotificationButton.getBorderColor().isEmpty()) {
                if(borderDrawable!=null) {
                    borderDrawable.getPaint().setColor(Color.parseColor(inAppNotificationButton.getBorderColor()));
                    borderDrawable.setPadding(1,1,1,1);
                    borderDrawable.getPaint().setStyle(Paint.Style.FILL);
                }
            }

            if(shapeDrawable!=null) {
                Drawable[] drawables = new Drawable[]{
                        borderDrawable,
                        shapeDrawable
                };

                LayerDrawable layerDrawable = new LayerDrawable(drawables);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    inAppButton.setBackground(layerDrawable);
                } else {
                    inAppButton.setBackgroundDrawable(layerDrawable);
                }
            }
        }else{
            inAppButton.setVisibility(View.GONE);
        }
    }

    int getDPI(){
        WindowManager wm = (WindowManager) getActivity().getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return 0;
        }
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.densityDpi;
    }


}

