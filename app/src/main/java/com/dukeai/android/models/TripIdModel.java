package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class TripIdModel extends ErrorModel {

    @SerializedName("tripId")
    String tripId;

    @SerializedName("session_status")
    String sessionStatus;

    public TripIdModel(String msg) {
        setErrorMessage(msg);
    }

    public TripIdModel() {
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
