package com.dukeai.android.apiUtils;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ServiceManager extends ContextWrapper {
    Context mContext;

    public ServiceManager(Context base) {
        super(base);
        mContext = base;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
