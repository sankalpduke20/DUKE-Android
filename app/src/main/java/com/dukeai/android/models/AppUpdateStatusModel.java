package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class AppUpdateStatusModel {

    @SerializedName("update_type")
    String updateType;
    @SerializedName("latest_version")
    String latestVersion;
    @SerializedName("latest_build")
    String latestBuild;
    @SerializedName("app_url")
    String appURL;
    @SerializedName("message")
    String message;

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestBuild() {
        return latestBuild;
    }

    public void setLatestBuild(String latestBuild) {
        this.latestBuild = latestBuild;
    }

    public String getAppURL() {
        return appURL;
    }

    public void setAppURL(String appURL) {
        this.appURL = appURL;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
