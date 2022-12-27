package com.dukeai.android;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.dukeai.android.ui.activities.BaseActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.Config;
import com.dukeai.android.utils.NotificationUtils;
import com.dukeai.android.utils.Utilities;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onNewToken(String token) {
//        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra(AppConstants.UserPreferencesConstants.TOKEN, token);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.e("dataChat", remoteMessage.getData().toString());
            try {
                Map<String, String> params = remoteMessage.getData();
                JSONObject object = new JSONObject(params);
                handleDataMessage(object);
                Log.e("JSON_OBJECT", object.toString());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message, String title) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

            Intent resultIntent = new Intent(getApplicationContext(), BaseActivity.class);
            resultIntent.putExtra("message", message);
            showNotificationMessage(getApplicationContext(), title, message, Utilities.getReportCurrentDate(), resultIntent);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(pushNotification);


        } else {
            // If the app is in background, firebase itself handles the notification
            Log.e(TAG, "handleNotification: " + "background");
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            String title = "", msg = "", imageUrl = "", timestamp = "", notificationType = "";
            boolean isBackground;
            JSONObject payload;

            //Not using the following fields
            if (json.has("title")) {
                title = json.getString("title");
            }
            if (json.has("body")) {
                msg = json.getString("body");
            }
            if (json.has("is_background")) {
                isBackground = json.getBoolean("is_background");
            }
            if (json.has("icon")) {
                imageUrl = json.getString("icon");
            }
            if (json.has("timestamp")) {
                timestamp = "2018-03-29";
            }
            if (json.has("payload")) {
                payload = json.getJSONObject("payload");
            }

            //Actually using only following fields
            if (json.has("msg")) {
                msg = json.getString("msg");
            }
            if (json.has("notificationType")) {
                notificationType = json.getString("notificationType");
            }

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                // Two times broadcast is sent, first because of Notification & second because of Data message, so commenting the below lines
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("dataMessage", notificationType + "###" + msg);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), BaseActivity.class);
                resultIntent.putExtra("message", msg);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, msg, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, msg, timestamp, resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
