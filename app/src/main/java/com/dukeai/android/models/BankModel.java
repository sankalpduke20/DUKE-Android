package com.dukeai.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BankModel {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("req_id")
    @Expose
    private String reqId;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("debug")
    @Expose
    private String debug;
    @SerializedName("access_code")
    @Expose
    private String accessCode;
    @SerializedName("destination")
    @Expose
    private String destination;
    @SerializedName("web_url")
    @Expose
    private String webUrl;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
