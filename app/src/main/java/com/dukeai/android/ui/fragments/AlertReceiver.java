package com.dukeai.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dukeai.android.Duke;

import static com.dukeai.android.Duke.isAutoIFTAAutoRestartInProgress;

public class AlertReceiver extends BroadcastReceiver {

    private static String TAG = "Schedule task triggered";

    @Override
    public void onReceive(Context context, Intent intent) {
        HomeFragment homeFragment = new HomeFragment();
        Log.d(TAG, "IFTA tracking stopped");
        isAutoIFTAAutoRestartInProgress = true;
        homeFragment.stopTracking();
        Log.d(TAG, "IFTA tracking started");
//        Duke.tripId = "";
//        Duke.tripSessionState = "";
//        homeFragment.requestLocationUpdates();
    }
}
