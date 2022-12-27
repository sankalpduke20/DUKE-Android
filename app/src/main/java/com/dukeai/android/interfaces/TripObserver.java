package com.dukeai.android.interfaces;

import com.dukeai.android.models.TripIdModel;

public interface TripObserver {

    void onChanged(String status, TripIdModel tripIdModel);
}
