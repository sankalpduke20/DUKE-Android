package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReportSpinnerDataModel implements Serializable {

    @SerializedName("spinnerText")
    String spinnerText;

    public ReportSpinnerDataModel(String spinnerText) {
        setSpinnerText(spinnerText);
    }

    public String getSpinnerText() {
        return spinnerText;
    }

    public void setSpinnerText(String spinnerText) {
        this.spinnerText = spinnerText;
    }
}
