/**
 * Author: Shiboborta Das
 * Divami Design Labs Pvt. Ltd.
 */

package com.dukeai.android;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import androidx.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.dukeai.android.apiUtils.ServiceManager;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.TripIdModel;
import com.dukeai.android.ui.activities.SplashActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.IFTAViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class LocationService extends Service {

    public static final String TAG = LocationService.class.getSimpleName();

    public static boolean isForegroundStarted = false;

    public static final String ACTION_BROADCAST = "com.truckertaxtool.android.broadcast";

    public static final String EXTRA_LOCATION = "com.truckertaxtool.android.location";

//    public static final float SMALLEST_DISPLACEMENT_IN_METERS = 1609.34f;

    public static final String LOCATION_DATA = "com.truckertaxtool.android.locationData";
    /**
     * The name of the channel for notifications.
     */
    public static final String CHANNEL_ID = "LOCATION_SERVICE_CHANNEL_01";
    private static final int INTERNET_OFF_NOTIFICATION_ID = 20;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;
    /**
     * Stores the last updated location Address and Time
     */
    public static String lastUpdatedAddress = "";
    public static String lastUpdatedTime = "";
    Context mcontext;
    NotificationManager mNotificationManager;
    /**
     * To bind the service with Activity
     */
    IBinder mBinder = new LocationBinder();
    /**
     * IFTAViewModel for making API calls
     */
    IFTAViewModel iftaViewModel;
    /**
     * To Check for Internet Connectivity
     */
    ServiceManager serviceManager;
    /**
     * Location Buffer: to keep locations in buffer in case updates to server fails
     */
    JsonArray locationsArray;
    Date date;
    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private boolean isFirstTimeLocationUpdate = true;
    private boolean isFinishingLocationUpdate = false;
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;
    /**
     * To Request Locations and for configuring updates
     */
    private LocationRequest mLocationRequest;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;
    /**
     * To Handle Location Updates
     */
    private Handler mServiceHandler;
    /**
     * The current location.
     */
    private Location mLocation;
    /**
     * Receiver of GPS state change.
     */
    private BroadcastReceiver gpsReceiver;
    private int GPS_NOTIFICATION_ID = 17;

    public static String lastLocationReceived = null;
    SharedPreferences sharedPreferences;

    public LocationService() {
        //Empty Constructor
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize variables

//        startForeground(NOTIFICATION_ID, getNotification());

        sdf.setTimeZone(TimeZone.getDefault());
        locationsArray = new JsonArray();
        createNotificationChannel();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createLocationRequest();
        iftaViewModel = new IFTAViewModel();
        serviceManager = new ServiceManager(Duke.getInstance());
        initializeAndRegisterGPSStateReceiver();
    }

    private void initializeAndRegisterGPSStateReceiver() {
        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mcontext = context;
                if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    //Stuff on GPS status change
                    Log.d(TAG, "onReceive: ");
                    if (Utilities.serviceIsRunningInForeground(getBaseContext(), LocationService.class)) {
                        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            //GPS is turned OFF
                            try {
                                //Stop location updates
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //Change the existing Notification
                            mNotificationManager.notify(NOTIFICATION_ID, getNotification(getString(R.string.trip_updates),
                                    getString(R.string.location_updates_stopped),
                                    new Intent(getApplicationContext(), SplashActivity.class)));

                            //Show Notification for GPS Settings
                            mNotificationManager.notify(GPS_NOTIFICATION_ID,
                                    getNotification(getString(R.string.location_services_disabled),
                                            getString(R.string.duke_needs_access_to_your_location),
                                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
                        } else {
                            //GPS is turned ON
                            mNotificationManager.cancel(GPS_NOTIFICATION_ID);
                            try {
                                //start location updates
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                        mLocationCallback, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (DashboardActivity) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onBind()");
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        isForegroundStarted = false;
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tells the system to not try to recreate the service after it has been killed.
//        if(!isForegroundStarted) {
//        }
        startForeground(NOTIFICATION_ID, getNotification());
//        stopForeground(true);
//        stopSelf();
//        mChangingConfiguration = false;
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        this.unregisterReceiver(gpsReceiver);
        super.onDestroy();
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Duke.isLocationBeingFetched = true;
        if (!Duke.tripId.equals("")) {
            Log.i(TAG, "Requesting location updates");
            Log.i(TAG, "________________NEW TRIP______________________");
            mLocation = null;
            isFirstTimeLocationUpdate = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mcontext.startForegroundService(new Intent(getApplicationContext(), LocationService.class));
            } else {
                startService(new Intent(getApplicationContext(), LocationService.class));
            }

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, null);
                Utilities.setRequestingLocationUpdates(getApplicationContext(), true);
            } catch (Exception e) {
                Log.e(TAG, "Lost location permission. Could not request updates. " + e);
                Utilities.setRequestingLocationUpdates(getApplicationContext(), false);
            }
        } else {
            iftaViewModel.getTripId().observeForever(new Observer<TripIdModel>() {
                @Override
                public void onChanged(@Nullable TripIdModel tripIdModel) {
                    try {
                        if (tripIdModel != null && tripIdModel.getErrorMessage() == null) {
                            Log.d(TAG, "onChanged: " + tripIdModel.getTripId());
                            Duke.tripId = tripIdModel.getTripId();
                            //Start Location Updates
                            requestLocationUpdates();
                        } else if (tripIdModel != null && tripIdModel.getErrorMessage() != null) {
                            Log.d(TAG, "onChanged: " + tripIdModel.getErrorMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return;
        }
    }

    public void requestLocationUpdatesWithoutTrip() {
        mLocation = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mcontext.startForegroundService(new Intent(getApplicationContext(), LocationService.class));
        } else {
            startService(new Intent(getApplicationContext(), LocationService.class));
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);
            Utilities.setRequestingLocationUpdates(getApplicationContext(), true);
        } catch (Exception e) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + e);
            Utilities.setRequestingLocationUpdates(getApplicationContext(), false);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates(Context context) {
        Duke.isLocationBeingFetched = false;
        Log.i(TAG, "Removing location updates");
        CustomProgressLoader progressLoader = null;
        if(context != null) {
            progressLoader  = new CustomProgressLoader(context);
        }
        if (context == null) {
            try {
                if (Utilities.serviceIsRunningInForeground(this, LocationService.class)) {
                    try {
                        Log.d(TAG, "inside remove location");
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        stopForeground(true);
                        stopSelf();
                        lastUpdatedAddress = "";
                        mNotificationManager.cancel(GPS_NOTIFICATION_ID);
                        mNotificationManager.cancel(INTERNET_OFF_NOTIFICATION_ID);
                        isFinishingLocationUpdate = true;
                        if (locationsArray.size() == 0) {
                            if (mLocation != null) {
                                addItemToLocationsArray(mLocation);
                                isFinishingLocationUpdate = true;
                                updateLocationsToServer(AppConstants.Ifta.LOCATION_UPDATE_STATUS_FINISHED);
                            } else {
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        addItemToLocationsArray(location);
                                        isFinishingLocationUpdate = true;
                                        updateLocationsToServer(AppConstants.Ifta.LOCATION_UPDATE_STATUS_FINISHED);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lost location permission. Could not remove updates. " + e);
                    }
                }
                Utilities.setRequestingLocationUpdates(this, false);
            } catch (Exception e) {
                Log.e(TAG, "removeLocationUpdates: ", e.fillInStackTrace());
            }
        } else {
            progressLoader.showDialog();
            try {
                if (Utilities.serviceIsRunningInForeground(this, LocationService.class)) {
                    try {
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        stopForeground(true);
                        stopSelf();
                        lastUpdatedAddress = "";
                        mNotificationManager.cancel(GPS_NOTIFICATION_ID);
                        mNotificationManager.cancel(INTERNET_OFF_NOTIFICATION_ID);
                        isFinishingLocationUpdate = true;
                        if (locationsArray.size() == 0) {
                            if (mLocation != null) {
                                addItemToLocationsArray(mLocation);
                                isFinishingLocationUpdate = true;
                                updateLocationsToServer(AppConstants.Ifta.LOCATION_UPDATE_STATUS_FINISHED);
                            } else {
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        addItemToLocationsArray(location);
                                        isFinishingLocationUpdate = true;
                                        updateLocationsToServer(AppConstants.Ifta.LOCATION_UPDATE_STATUS_FINISHED);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lost location permission. Could not remove updates. " + e);
                    }
                }
                Utilities.setRequestingLocationUpdates(this, false);
                progressLoader.hideDialog();
            } catch (Exception e) {
                Log.e(TAG, "removeLocationUpdates: ", e.fillInStackTrace());
                progressLoader.hideDialog();
            }
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);
    }

    /**
     * Create
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    AppConstants.Ifta.TRIP_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Updates server and local variables on new location update.
     */
    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location.getLatitude() + "    " + location.getLongitude());
        mLocation = location;
        //Broadcast about the new location
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(LOCATION_DATA, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        //Update notification content if running as foreground service.
        if (Utilities.serviceIsRunningInForeground( this, LocationService.class) & serviceManager.isNetworkAvailable()) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }

        try {
            addItemToLocationsArray(location);
            updateLocationsToServer(AppConstants.Ifta.LOCATION_UPDATE_STATUS_INCOMPLETE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemToLocationsArray(Location location) {
        if (location != null) {
            String address = Utilities.getLocalAddress(mcontext, location);
            String[] splitted = address.split(",");
            String stateName = null;
            if(splitted.length>1) {
                address = address.split(", ")[1];
                LocationService.lastUpdatedAddress = address;
                lastLocationReceived = address;
            }

            Log.i("adres", address);
            Map<String, String> states = new HashMap<String, String>();
            states.put("Alabama", "AL");
            states.put("Alaska", "AK");
            states.put("Alberta", "AB");
            states.put("American Samoa", "AS");
            states.put("Arizona", "AZ");
            states.put("Arkansas", "AR");
            states.put("Armed Forces (AE)", "AE");
            states.put("Armed Forces Americas", "AA");
            states.put("Armed Forces Pacific", "AP");
            states.put("British Columbia", "BC");
            states.put("California", "CA");
            states.put("Colorado", "CO");
            states.put("Connecticut", "CT");
            states.put("Delaware", "DE");
            states.put("District Of Columbia", "DC");
            states.put("Florida", "FL");
            states.put("Georgia", "GA");
            states.put("Guam", "GU");
            states.put("Hawaii", "HI");
            states.put("Idaho", "ID");
            states.put("Illinois", "IL");
            states.put("Indiana", "IN");
            states.put("Iowa", "IA");
            states.put("Kansas", "KS");
            states.put("Kentucky", "KY");
            states.put("Louisiana", "LA");
            states.put("Maine", "ME");
            states.put("Manitoba", "MB");
            states.put("Maryland", "MD");
            states.put("Massachusetts", "MA");
            states.put("Michigan", "MI");
            states.put("Minnesota", "MN");
            states.put("Mississippi", "MS");
            states.put("Missouri", "MO");
            states.put("Montana", "MT");
            states.put("Nebraska", "NE");
            states.put("Nevada", "NV");
            states.put("New Brunswick", "NB");
            states.put("New Hampshire", "NH");
            states.put("New Jersey", "NJ");
            states.put("New Mexico", "NM");
            states.put("New York", "NY");
            states.put("Newfoundland", "NF");
            states.put("North Carolina", "NC");
            states.put("North Dakota", "ND");
            states.put("Northwest Territories", "NT");
            states.put("Nova Scotia", "NS");
            states.put("Nunavut", "NU");
            states.put("Ohio", "OH");
            states.put("Oklahoma", "OK");
            states.put("Ontario", "ON");
            states.put("Oregon", "OR");
            states.put("Pennsylvania", "PA");
            states.put("Prince Edward Island", "PE");
            states.put("Puerto Rico", "PR");
            states.put("Quebec", "QC");
            states.put("Rhode Island", "RI");
            states.put("Saskatchewan", "SK");
            states.put("South Carolina", "SC");
            states.put("South Dakota", "SD");
            states.put("Tennessee", "TN");
            states.put("Texas", "TX");
            states.put("Utah", "UT");
            states.put("Vermont", "VT");
            states.put("Virgin Islands", "VI");
            states.put("Virginia", "VA");
            states.put("Washington", "WA");
            states.put("West Virginia", "WV");
            states.put("Wisconsin", "WI");
            states.put("Wyoming", "WY");
            states.put("Yukon Territory", "YT");
            Log.i("address", address);
            JsonObject item = new JsonObject();

            if(Duke.stateName.length()>0) {
                stateName = Duke.stateName;
                Duke.stateName = "";
            } else {
                stateName = "";
            }

            String stateCode = address.length() > 0 ? address : "";
            date = new java.util.Date(location.getTime());
            item.addProperty("timestamp", sdf.format(date));
            item.addProperty("state", stateName);
            item.addProperty("longitude", location.getLongitude());
            item.addProperty("latitude", location.getLatitude());
            locationsArray.add(item);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        CharSequence message;
        if (mLocation == null) {
            message = this.getString(R.string.getting_location_updates);
        } else {
            if (Utilities.getLocalAddress(getApplicationContext(), mLocation).equals("")) {
                message = getString(R.string.something_went_wrong);
            } else {
                lastUpdatedAddress = Utilities.getLocalAddress(getApplicationContext(), mLocation);
                message = this.getString(R.string.latest_update) + lastUpdatedAddress;
            }
        }
        CharSequence title = this.getString(R.string.trip_updates);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(message)
                .setContentTitle(title)
                .setOngoing(true)
                .setContentIntent(activityPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setWhen(System.currentTimeMillis());

        return builder.build();
    }

    /**
     * Calls IFTA API for location updates
     */
    private void updateLocationsToServer(final String type) {
        if (serviceManager.isNetworkAvailable()) {
            Log.d("updating status1", String.valueOf(isFirstTimeLocationUpdate));
            Log.d("updating status2", String.valueOf(isFinishingLocationUpdate));
            if ((locationsArray.size() > 0) || isFirstTimeLocationUpdate || isFinishingLocationUpdate) {
                if (isFirstTimeLocationUpdate) {
                    isFirstTimeLocationUpdate = !isFirstTimeLocationUpdate;
                } else if (isFinishingLocationUpdate) {
                    isFinishingLocationUpdate = !isFinishingLocationUpdate;
                }

//                String tripId = Duke.tripId;
//                String tripSessionState = Duke.tripSessionState;

//                if(type.equals(AppConstants.Ifta.LOCATION_UPDATE_STATUS_FINISHED)) {
//                    Duke.tripId = "";
//                    Duke.tripSessionState = "";
//                }

                iftaViewModel.sendTripData(locationsArray, Duke.tripId, type)
                        .observeForever(new Observer<ErrorModel>() {

                            @Override
                            public void onChanged(@Nullable ErrorModel errorModel) {
                                try {
                                    Log.d(TAG, "onChanged: " + errorModel.getErrorMessage());
                                    if ((errorModel.getErrorMessage() != null) && (errorModel.getErrorCode() != null) && errorModel.getErrorCode().equals(AppConstants.Ifta.API_STATUS_SUCCESS)) {
                                        locationsArray = null;
                                        locationsArray = new JsonArray();
                                        if(Duke.isAutoIFTAAutoRestartInProgress) {
                                            Duke.isAutoIFTAAutoRestartInProgress = false;
                                            Duke.tripId = "";
                                            Duke.tripSessionState = "";
                                            requestLocationUpdates();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        });
            }
            mNotificationManager.cancel(INTERNET_OFF_NOTIFICATION_ID);

        } else {
            mNotificationManager.notify(INTERNET_OFF_NOTIFICATION_ID, getNotification(this.getString(R.string.connectivity_issue),
                    this.getString(R.string.unable_to_connect_to_internet),
                    new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)));
            //Put data into Local DB
        }
    }

    /**
     * Get a customized notification based on use case
     */
    public Notification getNotification(String title, String message, Intent actionIntent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10,
                actionIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(message)
                .setContentTitle(title)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setOnlyAlertOnce(true)
                .setWhen(System.currentTimeMillis());
        return builder.build();
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocationBinder extends Binder {
        public LocationService getService(Context context) {
            mcontext = context;
            return LocationService.this;
        }
    }

    private void sendTripStatusToServer(String type) {
        iftaViewModel.sendTripData(locationsArray, Duke.tripId, type)
                .observeForever(new Observer<ErrorModel>() {

                    @Override
                    public void onChanged(@Nullable ErrorModel errorModel) {
                        try {
                            Log.d(TAG, "onChanged: " + errorModel.getErrorMessage());
                            if ((errorModel.getErrorMessage() != null) && (errorModel.getErrorCode() != null) && errorModel.getErrorCode().equals(AppConstants.Ifta.API_STATUS_SUCCESS)) {
                                locationsArray = null;
                                locationsArray = new JsonArray();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
    }
}