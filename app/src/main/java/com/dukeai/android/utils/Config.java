package com.dukeai.android.utils;

public class Config {
    public static final String SMS_ORIGIN = "AD-SMSCou";
    public static final String OTP_DELIMITER = ":";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "news";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
}
