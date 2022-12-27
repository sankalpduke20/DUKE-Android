package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class CognitoUserDataModel {
    @SerializedName("email")
    String email;
    @SerializedName("name")
    String name;
    @SerializedName("sub")
    String sub;
    @SerializedName("phone_number_verified")
    String phone_number_verified;
    @SerializedName("phone_number")
    String phone_number;
    @SerializedName("email_verified")
    String email_verified;
}
