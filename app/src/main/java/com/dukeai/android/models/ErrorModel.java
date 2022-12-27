package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class ErrorModel {

    @SerializedName("Code")
    String code;

    @SerializedName("Message")
    String message;


    public String getErrorMessage() {
        return message;
    }

    public void setErrorMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
