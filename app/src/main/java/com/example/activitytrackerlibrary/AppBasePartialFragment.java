package com.example.activitytrackerlibrary;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class AppBasePartialFragment extends AppBaseFragment {

    @Override
    public void onStart() {
        super.onStart();
        if (isCleanedUp.get()) {
            cleanup();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    void generateListener() {
        if (config != null) {
            setListener((InAppListener) TrackerApi.instanceWithConfig(getActivity().getBaseContext(),config));
        }
    }

    @Override
    void cleanup() {
        if (!isCleanedUp.get()) {
            final FragmentManager fragmentManager = parent.getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            try {
                transaction.remove(this).commit();
            } catch (IllegalStateException e) {
                fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }
        isCleanedUp.set(true);
    }
}

