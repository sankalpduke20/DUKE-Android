package com.dukeai.android.ui.fragments;

import static com.dukeai.android.Duke.accessToken;
import static com.dukeai.android.Duke.api_key;
import static com.dukeai.android.Duke.client_id;
import static com.dukeai.android.Duke.cust_id;
import static com.dukeai.android.Duke.idToken;
import static com.dukeai.android.Duke.isRecentCleared;
import static com.dukeai.android.Duke.refreshToken;
import static com.facebook.FacebookSdk.getApplicationContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken;
import com.dukeai.android.BuildConfig;
import com.dukeai.android.Duke;
import com.dukeai.android.LocationService;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.apiUtils.ServiceManager;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.MemberStatusUpdateObserver;
import com.dukeai.android.interfaces.OnResultListener;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.TripObserver;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.TripIdModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.activities.PaymentActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.UploadStatusDialog;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.IFTAViewModel;
import com.dukeai.android.views.CustomHeader;
import com.dukeai.manageloads.model.ConfigModel;
import com.dukeai.manageloads.ui.activities.DashboardActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;
import com.revenuecat.purchases.PurchaserInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.ReceivePurchaserInfoListener;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

// Firebase: Setup
// Firebase: Setup


public class HomeFragment extends Fragment implements HeaderActions, PopupActions, ActivityCompat.OnRequestPermissionsResultCallback {
    public final static String LOCATION_SETTINGS_KEY = "LOCATION_SETTINGS_KEY";
    public final static String IS_DOCUMENT_UPLOAD_SUCCESSFUL = "isUploadSuccessful";
    public final static String IS_DOC_SCAN_SUCCESSFUL = "isScanSuccessful";
    public final static String IS_DOCUMENT_INVALID = "INVALID_DOCUMENT";
    public static final int REQUEST_CODE_GPS_ENABLE = 99;
    public static final int REQUEST_GPS_FOR_UPLOAD_DOC = 11;
    private static final int NO_INTERNET_DIALOG_ID = 32;
    private static final int INCOMPLETE_TRIP_DIALOG_ID = 35;
    private static final int PLAY_SERVICES_DIALOG_ID = 37;
    private static final int ENABLE_GPS_DIALOG_ID = 98;
    private static final int LOCATION_PERMISSION_NEEDED_DIALOG_ID = 70;
    private static final int LOCATION_SETTINGS_INADEQUATE_DIALOG_ID = 33;
    private static final int REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34;
    // A reference to the service used to get location updates.
    public static LocationService mService = null;
    public static boolean isIFTATrackingOn = false;
    private static boolean isBackgroundLocationPermissionRequested = false;
    public OnResultListener resultListener;
    String TAG = HomeFragment.class.getSimpleName();
    UploadDocumentInterface uploadDocumentInterface;
    View homeView;
    @BindView(R.id.custom_header)
    CustomHeader customHeader;
    @BindView(R.id.rejected_count)
    TextView rejectedCount;
    @BindView(R.id.rejected_count_wrapper)
    RelativeLayout rejectedCountWrapper;
    //    @BindView(R.id.share)
//    FloatingActionButton shareButton;
    @BindView(R.id.buy)
    FloatingActionButton buyButton;
    //    @BindView(R.id.utility_support)
//    FloatingActionButton utilitySupportButton;
    @BindView(R.id.util_wrapper)
    LinearLayout utilWrapperLinearLatout;
    @BindView(R.id.location_switch_button)
    SwitchCompat locationSwitch;
    @BindView(R.id.ifta_title)
    TextView iftaTitleText;
    @BindView(R.id.ifta_description)
    TextView iftaDescriptionText;
    @BindView(R.id.location_switch_helper_text)
    TextView locationSwitchbottomText;
    @BindView(R.id.parent_layout)
    FrameLayout parentLayout;
    //    @BindView(R.id.view_id)
//    @Nullable
//    TextView viewId;
//    @BindView(R.id.view_document_id)
//    @Nullable
//    TextView viewDocument;
    @BindView(R.id.view_report_id)
    TextView reportView;
    //    @BindView(R.id.upload_id) @Nullable
//    TextView uploadId;
//    @BindView(R.id.upload_document_id) @Nullable
//    TextView uploadDocumentId;
    @BindView(R.id.upload_count)
    RelativeLayout uploadCountWrapper;
    @BindView(R.id.upload_count_text)
    TextView uploadCountValue;
    CognitoUser user;
    FileStatusViewModel fileStatusViewModel;
    CustomProgressLoader customProgressLoader;
    int rejectedDocumentsModelsCount;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int screenWidth, screenHeight;
    UserConfig userConfig = UserConfig.getInstance();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean trackingStatus;
    SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    IFTAViewModel iftaViewModel;
    UploadStatusDialog uploadStatusDialog;
    //Stores the latest locations
    String address = "";
    //To check for Active internet
    ServiceManager serviceManager;
    Boolean isUploadSuccessful = null;
    Boolean isScanSuccessful = null;
    private String addressReceived = null;
    private Boolean firstTime = null;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    private String message;
    private OnFragmentInteractionListener mListener;
    // The BroadcastReceiver used to listen to broadcasts from the LocationService.
    private LocationUpdatesReceiver myReceiver;
    // Tracks the bound state of the service.
    private boolean mBound = false;
    // Monitors the state of the connection to the service.
    @Nullable
    @BindView(R.id.home_txt)
    TextView utilHelp;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService(getContext());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }



    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public static void requestLocationUpdates() {
        if (HomeFragment.mService != null) {
            /**Start Location Service**/
            HomeFragment.mService.requestLocationUpdates();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        Duke.selectRecipientDataForGlobalList.clear();
        Duke.selectRecipientDataCustomList.clear();


        if (Duke.isFirebaseSetUp) {
            // Firebase: Send user login information
            UserConfig userConfig = UserConfig.getInstance();
            UserDataModel userDataModel;
            userDataModel = userConfig.getUserDataModel();
            mFirebaseAnalytics.setUserProperty("UserEmail", userDataModel.getUserEmail());
            Bundle params = new Bundle();
            params.putString("UserEmail", userDataModel.getUserEmail());
            mFirebaseAnalytics.logEvent("UserLogin", params);
            Duke.isFirebaseSetUp = false;
        }

        myReceiver = new LocationUpdatesReceiver();
        sharedPreferences = getContext().getSharedPreferences(AppConstants.Ifta.LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        registerLocationPreferenceChangeListener();
        serviceManager = new ServiceManager(Duke.getInstance());

        String name = (Duke.userName != null ? Duke.userName : "");
        if (AppConstants.currentTheme.equals("duke") && userConfig != null) {
            if (userConfig.getUserDataModel() != null) {
                if (userConfig.getUserDataModel().getUserEmail() != null) {
                    message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download) + userConfig.getUserDataModel().getUserEmail() + this.getString(R.string.when_you_register_in_order_to) + AppConstants.iosLink + this.getString(R.string.for_ios_or) + AppConstants.androidLink + this.getString(R.string.for_android);
                }
            }
        } else {
            message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download_ttt) + "INFO@TAXATIONSOLUTIONS.NET" + this.getString(R.string.when_you_register_in_order_to_ttt) + "bit.ly/TruckerTTiOS" + this.getString(R.string.for_ios_or) + "http://bit.ly/TruckerTTAndroid" + this.getString(R.string.for_android);
        }

        autoIFTAService();

    }

    private void autoIFTAService() {
        //Code for Auto IFTA start/stop using AlarmManager
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);

        if (isIFTATrackingOn) {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getContext(), AlertReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, 0);
            if (c.before(Calendar.getInstance())) {
                c.add(Calendar.DATE, 1);
            }
            Log.d("autoIFTAservice: ", "running!");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        } else {
            Log.d("autoIFTAservice: ", "not running!");
        }
    }

    private void checkLastTripStatus(final TripObserver tripObserver) {
        iftaViewModel.getLastTripStatus().observe(this, new Observer<TripIdModel>() {
            @Override
            public void onChanged(@Nullable TripIdModel tripIdModel) {
                try {
                    String msg = "";
                    if (tripIdModel != null && tripIdModel.getErrorMessage() == null) {
                        /**Received Last Trip Id and Status, If trip session_status is "continue" then it is an active trip **/
                        msg = "SUCCESS";
                    } else {
                        //Error
                        msg = "ERROR";
                    }
                    tripObserver.onChanged(msg, tripIdModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    /**Error occurred**/
                    tripObserver.onChanged("ERROR", null);
                }
            }
        });
    }

    private void registerLocationPreferenceChangeListener() {
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "onSharedPreferenceChanged: " + sharedPreferences.getBoolean(AppConstants.Ifta.LOCATION_PREFERENCE_NAME, false));
                if (sharedPreferences.getBoolean(LOCATION_SETTINGS_KEY, false)) {

                    turnTrackingButtonStatusON();
                } else {

                    turnTrackingButtonStatusOFF();
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void turnTrackingButtonStatusOFF() {
        locationSwitchbottomText.setText("START");
        iftaDescriptionText.setText("On Duty/Off Duty");
        iftaTitleText.setText("TRACK MY MILES");
        locationSwitch.setChecked(false);
        Log.d(TAG, "TRACKING IS OFF");
//        }
    }

    private void turnTrackingButtonStatusON() {
//        if(addressReceived!=null) {
//            iftaDescriptionText.setText(addressReceived.length() > 0 ? getContext().getString(R.string.last_updated_location_is) + addressReceived : "Getting latest location updates...");
//            locationSwitchbottomText.setText("STOP");
//            iftaTitleText.setText("TRACKING MILES");
//            locationSwitch.setChecked(true);
//            Log.d(TAG, "TRACKING IS ON");
//        } else {
//            locationSwitchbottomText.setText("STOP");
//            iftaDescriptionText.setText(LocationService.lastUpdatedAddress.equals("") ? "Getting latest location updates..." : getContext().getString(R.string.last_updated_location_is) + LocationService.lastUpdatedAddress);
//            iftaTitleText.setText("TRACKING MILES");
//            locationSwitch.setChecked(true);
//            Log.d(TAG, "TRACKING IS ON");
//        }

        locationSwitchbottomText.setText("STOP");
//        if(getContext() != null) {
//            iftaDescriptionText.setText(LocationService.lastUpdatedAddress.equals("") ? "Getting latest location updates..." : getContext().getString(R.string.last_updated_location_is) + LocationService.lastUpdatedAddress);
//        } else {
//            iftaDescriptionText.setText("Getting latest location updates...");
//        }
        iftaDescriptionText.setText(LocationService.lastUpdatedAddress.equals("") ? "Getting latest location updates..." : getContext().getString(R.string.last_updated_location_is) + LocationService.lastUpdatedAddress);
        iftaTitleText.setText("TRACKING MILES");
        locationSwitch.setChecked(true);
        Log.d(TAG, "TRACKING IS ON");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int this_layout;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
//        Utilities.resetLoadFlags();
        Duke.isLoadDocument = false;
        Duke.selectedRecipients.clear();
        Duke.selectedLoadUUID = "";
        Duke.selectedLoadSHA1 = "";
        Duke.isDocumentAddingToLoad = false;
        Duke.isDocumentBeingScanned = false;

        /**Show Document Upload status after returning from UploadPreviewFragment/UploadSelectedFilesFragment**/
        if (getArguments() != null && getArguments().containsKey(IS_DOCUMENT_UPLOAD_SUCCESSFUL)) {
            isUploadSuccessful = getArguments().getBoolean(IS_DOCUMENT_UPLOAD_SUCCESSFUL);
            setArguments(null);
        } else if (getArguments() != null && getArguments().containsKey(IS_DOC_SCAN_SUCCESSFUL)) {
            isScanSuccessful = getArguments().getBoolean(IS_DOC_SCAN_SUCCESSFUL);
            setArguments(null);
        } else if (getArguments() != null && getArguments().getBoolean(IS_DOCUMENT_INVALID)) {
            confirmationComponent = null;
            confirmationComponent = new ConfirmationComponent(getActivity(), getString(R.string.document_error), getString(R.string.invalid_document), true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                @Override
                public void onPopupActions(String id, int dialogId) {
                    confirmationComponent.dismiss();
                }
            }, 1);
            setArguments(null);
        } else {
            isUploadSuccessful = null;
        }

        if (isUploadSuccessful != null) {
            /**Show SUCCESS toast if true, else open upload documents menu**/
            if (isUploadSuccessful) {

                Context context = getContext();

                inflater = getLayoutInflater();

                View toastRoot = inflater.inflate(R.layout.custom_toast, null);
                TextView tv = toastRoot.findViewById(R.id.textView1);


                Toast toast = new Toast(context);

                toast.setView(toastRoot);
                toast.show();
                toast.setDuration(Toast.LENGTH_LONG);

            } else {
                /**Open Document Upload Menu**/
                openUploadDocumentOptionsMenu();
            }
            /**Make it null*/
            isUploadSuccessful = null;
        }

        if (isScanSuccessful != null) {
            /**Show SUCCESS toast if true, else open upload documents menu**/
            if (isScanSuccessful) {

                Context context = getContext();

                inflater = getLayoutInflater();

                View toastRoot = inflater.inflate(R.layout.custom_scan_toast, null);
                TextView tv = toastRoot.findViewById(R.id.textView1);


                Toast toast = new Toast(context);

                toast.setView(toastRoot);
                toast.show();
                toast.setDuration(Toast.LENGTH_LONG);

            } else {
                /**Open Document Upload Menu**/
                openUploadDocumentOptionsMenu();
            }
            /**Make it null*/
            isScanSuccessful = null;
        }

        /**Changing the UI as per the resolution(Width) of Mobile Screen**/
        if (height <= 1920) {
            this_layout = R.layout.fragment_home;
        } else {
            this_layout = R.layout.fragment_home_sh1921dp;
        }
        /****/

        try {
            homeView = inflater.inflate(this_layout, container, false);
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }
//        homeView = inflater.inflate(this_layout, container, false);
        ButterKnife.bind(this, homeView);
        popupActions = this;
        setCurrentTheme(homeView);

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isTurnedOn) {

                if (compoundButton.isChecked() && compoundButton.isPressed()) {
                    //Start Tracking
                    showStartTrackingAlert();
//                    if(Build.VERSION.SDK_INT>=30) {
//                        showIFTATrackingNotAvailableAlert();
//                    } else {
//                        showStartTrackingAlert();
//                    }
                } else {
                    if (compoundButton.isPressed() && sharedPreferences.getBoolean(LOCATION_SETTINGS_KEY, false)) {
                        //Stop Tracking
                        showStopTrackingAlert();
                    }
                }

            }
        });


        return homeView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateDocumentsData();
    }


    private void showIFTATrackingNotAvailableAlert() {
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.temporarily_disabled), getString(R.string.temporarily_disabled_desc), false, getString(R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
                locationSwitch.setChecked(false);
            }
        }, 1);
    }

    private void showStartTrackingAlert() {
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                getResources().getString(R.string.start_trip),
                getResources().getString(R.string.do_you_want_to_start_tracking),
                false,
                "Yes",
                "Cancel",
                popupActions,
                AppConstants.Ifta.START_TRACKING_TRIP_DIALOG_ID,
                getResources().getString(R.string.location_required_desc)
        );
    }

    private void showStopTrackingAlert() {
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                getResources().getString(R.string.stop_trip),
                getResources().getString(R.string.do_you_want_to_stop_tracking),
                false,
                "Yes",
                "Cancel",
                popupActions,
                AppConstants.Ifta.STOP_TRIP_TRACKING_DIALOG_ID
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        getContext().bindService(new Intent(getContext(), LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInitials();
    }

    private void setInitials() {
        setToolbarColor();
        getWindowWidthAndHeight();
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        iftaViewModel = ViewModelProviders.of(this).get(IFTAViewModel.class);

        setCustomHeader();
        openAppWithHomeOrCamera();
    }

    private void setUploadCountIndicator(FileStatusModel fileStatusModel) {
        double fileCount = ((fileStatusModel.getUploadLimit() - fileStatusModel.getUploadCount()) / fileStatusModel.getUploadLimit()) * 100;
        if (fileStatusModel.getUploadLimit() > 0 || fileStatusModel.getUploadLimit() >= fileStatusModel.getUploadCount()) {
            uploadCountWrapper.setVisibility(View.VISIBLE);
            uploadCountValue.setText(((int) fileStatusModel.getUploadCount()) + "/" + ((int) fileStatusModel.getUploadLimit()));
        }
        if (fileCount > 70) {
            uploadCountWrapper.setBackgroundResource(R.drawable.upload_count_safe);
        } else if (fileCount > 40 && fileCount < 70) {
            uploadCountWrapper.setBackgroundResource(R.drawable.upload_count_moderate);
        } else {
            uploadCountWrapper.setBackgroundResource(R.drawable.upload_count_warning);
        }

        try {
            if (fileStatusModel.getMemberStatus().toLowerCase().equals("none") || fileStatusModel.getMemberStatus().toLowerCase().equals("free_pod")) {
                utilWrapperLinearLatout.setVisibility(View.VISIBLE);
            }
        }catch (NullPointerException ex){
            System.out.println("HomeFragment Getting Exception =====> "+ex);
        }

    }

    private void setBuyButton(FileStatusModel fileStatusModel) {
//        if (fileStatusModel != null) {
        try {
            if (fileStatusModel.getMemberStatus().equals("NONE") || fileStatusModel.getMemberStatus().equals("FREE") || fileStatusModel.getMemberStatus().equals("FREE_POD")) {
                buyButton.setVisibility(View.VISIBLE);
            } else {
                buyButton.setVisibility(View.GONE);
            }
        }catch (Exception ex){
            System.out.println("Home Fragment Getting Exception =====>"+ex);
        }
//        }
    }

    private void setupLocationTrackingStatus() {

        trackingStatus = sharedPreferences.getBoolean(LOCATION_SETTINGS_KEY, false);

//        if(trackingStatus == true) {
//            checkLastTripStatus(new TripObserver() {
//                @Override
//                public void onChanged(String status, TripIdModel tripIdModel) {
//                    if (status.equals("SUCCESS")) {
//                        if (tripIdModel.getSessionStatus().equals("continue") || tripIdModel.getSessionStatus().equals("new")) {
//                            Duke.tripSessionState = tripIdModel.getSessionStatus();
//                            Duke.tripId = tripIdModel.getTripId();
//                            turnTrackingButtonStatusON();
//                            notifyIncompleteTrip();
//                        }
//                    } else {
//                        Log.d(TAG, "checkLastTripStatus: ERROR");
//                    }
//                }
//            });
//        } else {
//            checkLastTripStatus(new TripObserver() {
//                @Override
//                public void onChanged(String status, TripIdModel tripIdModel) {
//                    if (status.equals("SUCCESS")) {
//                        if (tripIdModel.getSessionStatus().equals("continue")) {
//                            Duke.tripSessionState = tripIdModel.getSessionStatus();
//                            Duke.tripId = tripIdModel.getTripId();
//                            turnTrackingButtonStatusON();
//                            notifyIncompleteTrip();
//                        }
//                    } else {
//                        Log.d(TAG, "checkLastTripStatus: ERROR");
//                    }
//                }
//            });
//        }

        if (Utilities.serviceIsRunningInForeground(getContext(), LocationService.class)) {
            editor.putBoolean(LOCATION_SETTINGS_KEY, true);
            editor.apply();
            String loc = LocationService.lastUpdatedAddress;
            turnTrackingButtonStatusON();
            if (!hasLocationPermissionsGranted()) {
                // Tracking Status is True - Permission not Granted -> getPermissions
                requestLocationAccessPermissions();
            }
            if (isRecentCleared) {
                checkLastTripStatus(new TripObserver() {
                    @Override
                    public void onChanged(String status, TripIdModel tripIdModel) {
                        if (status.equals("SUCCESS")) {
                            if (tripIdModel.getSessionStatus().equals("continue")) {
                                Duke.tripSessionState = tripIdModel.getSessionStatus();
                                Duke.tripId = tripIdModel.getTripId();
                                turnTrackingButtonStatusON();
                                notifyIncompleteTrip();
                            }
                        } else {
                            Log.d(TAG, "checkLastTripStatus: ERROR");
                        }
                    }
                });
                isRecentCleared = false;
            }
//                Duke.isInBackground = false;
        } else {
            checkLastTripStatus(new TripObserver() {
                @Override
                public void onChanged(String status, TripIdModel tripIdModel) {
                    if (status.equals("SUCCESS")) {
                        if (tripIdModel.getSessionStatus().equals("continue") || tripIdModel.getSessionStatus().equals("new")) {
                            Duke.tripSessionState = tripIdModel.getSessionStatus();
                            Duke.tripId = tripIdModel.getTripId();
                            turnTrackingButtonStatusON();
                            notifyIncompleteTrip();
                        }
                    } else {
                        Log.d(TAG, "checkLastTripStatus: ERROR");
                    }
                }
            });
        }

    }

    private void openAppWithHomeOrCamera() {
        if (Duke.isOpenWithHome != null && Duke.isOpenWithHome.equals(AppConstants.UserPreferencesConstants.OPEN_WITH_CAMERA)) {
            if (uploadDocumentInterface != null) {
                Utilities.resetFileData();
                uploadDocumentInterface.uploadDocumentListener(true);
            }
        }
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorTransparent));
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    private void getProfileImage() {
        String fileName = Utilities.getProfileImageName();
        fileStatusViewModel.downloadFile(fileName, screenWidth, screenHeight, true).observe(this, new Observer<DownloadImageModel>() {
            @Override
            public void onChanged(@Nullable DownloadImageModel downloadImageModel) {
                if (downloadImageModel != null && downloadImageModel.getBitmap() != null) {

                    Duke.ProfileImage = downloadImageModel.getBitmap();
                    setCustomHeaderImage();
                }
            }
        });
    }

    private void setCustomHeaderImage() {
        customHeader.setHeaderImage(Duke.ProfileImage);
    }

    private void setCustomHeader() {
        customHeader.setHeaderActions(this);
        customHeader.setHeaderTitle(getString(R.string.home));
        customHeader.setToolbarTitle("");
        customHeader.showHideBackButton(View.GONE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.d("PM", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
        Log.d("PM", "onDetach");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
        Log.d("PM", "onDestroy");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
        Log.d("PM", "onDestroyView");

    }

    @OnClick(R.id.home_txt)
    void onHelpClick(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://duke.ai/automated-bookkeeping-and-tax-filing/#BCO"));
        startActivity(browserIntent);
    }

    @OnClick(R.id.upload_document)
    void onClickUploadButton() {
        // Firebase: Send click upload button event
        try {
            if ((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD")) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() < 1.0) {
                loadPaywall();
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Duke.isLocationPermissionProvided = true;
                    uploadDocumentHandler();
                } else {
                    requestLocationAccessForCapturingCurrentLocation();
                }
            }
        }catch (Exception ex){
            System.out.println("Home Fragment Getting Exception =====>"+ex);
        }
    }

    @OnClick(R.id.scan_document)
    void onScanDocumentClicked() {
        try {
            if ((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD")) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() < 1.0) {
                loadPaywall();
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Duke.isLocationPermissionProvided = true;
                    Duke.isDocumentBeingScanned = true;
                    uploadDocumentHandler();
                } else {
                    Duke.isDocumentBeingScanned = true;
                    requestLocationAccessForCapturingCurrentLocation();
                }
            }
        }catch (Exception ex){
            System.out.println("Home Fragment Getting Exception =====>"+ex);
        }
    }

    @OnClick(R.id.loads)
    void onClickLoadsButton() {
        /*Bundle params = new Bundle();
        params.putString("Page", "Home");
        Bundle args = new Bundle();
        mFirebaseAnalytics.logEvent("LoadsButtonClicked", params);
        NavigationFlowManager.openFragments(new LoadsFragment(), args, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.LOADS);
*/
        CognitoAccessToken accessToken1 = AppHelper.getCurrSession().getAccessToken();
        CognitoIdToken idToken1 = AppHelper.getCurrSession().getIdToken();
        CognitoRefreshToken refreshToken1 = AppHelper.getCurrSession().getRefreshToken();


        ConfigModel.client_id = "9ZykPApk15UaiU5Jd8jXcD3UKB1D3LJf67py6Qigcmix";
        ConfigModel.api_key = "69WRsRBUGKQWzJ21BffEsjWSfcDHkDyxegAJoArrzHgn";
        ConfigModel.cust_id = AppHelper.getCurrUser();
        ConfigModel.idToken = idToken1.getJWTToken();
        ConfigModel.accessToken = accessToken1.getJWTToken();
        ConfigModel.refreshToken = refreshToken1.getToken();
        Intent in = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(in);


    }

    private void uploadDocumentHandler() {
        if (!isGPSEnabled()) {
            Duke.isLocationPermissionProvided = false;
        }
        Bundle params = new Bundle();
        params.putString("Page", "Home");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        openUploadDocumentOptionsMenu();
    }

    private void openSettings() {
        isBackgroundLocationPermissionRequested = true;
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//        alertDialog.setTitle("Permission Needed");
//        alertDialog.setMessage("Duke.ai needs background location permission to track IFTA miles precisely. Please select 'Allow all the time' in the location access settings to grant required permission");
//        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//        });
//        alertDialog.show();
    }

//    @OnClick(R.id.utility_support)
//    void onClickSupportButton() {
//        Bundle params = new Bundle();
//        params.putString("button", "Support");
//        mFirebaseAnalytics.logEvent("SupportButton", params);
//        String supportURL = "https://duke.ai/home-page/resources/";
//        if (!AppConstants.currentTheme.equals("duke")) {
//            supportURL = "http://www.truckertaxtools.com/support";
//        }
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(supportURL));
//        startActivity(browserIntent);
//    }

    private void openUploadDocumentOptionsMenu() {
        if (uploadDocumentInterface != null) {
            Utilities.resetFileData();
            uploadDocumentInterface.uploadDocumentListener(false);
        }
    }

    void startLocationTrackerService() {

        /**Check if Network available or not**/
        if (serviceManager.isNetworkAvailable()) {

            /**Network Available, GooglePlayServices are Available**/
            if (Utilities.isPlayServicesAvailable(getContext())) {

                /**GooglePlayServices Available, Check for Location Access permissions*/
                if (hasLocationPermissionsGranted()) {

                    /**Location Permissions are Granted, Check for GPS Status**/
                    if (isGPSEnabled()) {
                        /**GPS is Enabled, check if any Active Trip Exists**/
                        checkTripStatusAndProceed();
                    } else {
                        /**GPS is not Enabled, Start Activity for result-GPS**/
                        RequestGPSStateEnable();
                    }

                } else {

                    /**Location Permissions are NOT Granted, Request Permissions**/
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        dismissExistingComponentIfAny();
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
//                        confirmationComponent = new ConfirmationComponent(
//                                getContext(),
//                                "Permissions Needed",
//                                "Access to Location is needed, in order to track your IFTA miles.",
//                                false,
//                                "Grant Access",
//                                "Cancel",
//                                popupActions,
//                                LOCATION_PERMISSION_NEEDED_DIALOG_ID);
                        confirmationComponent = new ConfirmationComponent(
                                getContext(),
                                "Permissions Needed",
                                getResources().getString(R.string.note_for_bg_location),
                                false,
                                "Grant Access",
                                "Cancel",
                                popupActions,
                                LOCATION_PERMISSION_NEEDED_DIALOG_ID);
                    } else {
                        // No explanation needed, we can request the permission.
                        dismissExistingComponentIfAny();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            confirmationComponent = new ConfirmationComponent(
                                    getContext(),
                                    "Permissions Needed",
                                    getResources().getString(R.string.note_for_bg_location),
                                    false,
                                    "Grant Access",
                                    "Cancel",
                                    popupActions,
                                    LOCATION_PERMISSION_NEEDED_DIALOG_ID);
                        } else {
                            requestLocationAccessPermissions();
                        }
                    }
                }

            } else {
                /**GooglePlayServices Unavailable, Turn switch off & Show Alert**/
                turnTrackingButtonStatusOFF();
                confirmationComponent = new ConfirmationComponent(
                        getContext(),
                        getResources().getString(R.string.google_play_services_not_available),
                        getResources().getString(R.string.please_update_play_services),
                        false,
                        "Update",
                        "Cancel",
                        popupActions,
                        PLAY_SERVICES_DIALOG_ID);
            }
        } else {
            /**Network Unavailable, Turn switch off & Show Alert**/
            turnTrackingButtonStatusOFF();
            confirmationComponent = new ConfirmationComponent(
                    getContext(),
                    getContext().getString(R.string.no_internet_connectivity),
                    getContext().getString(R.string.no_internet),
                    true,
                    "Open Settings",
                    "Cancel",
                    popupActions,
                    NO_INTERNET_DIALOG_ID);
        }
    }

    private void checkTripStatusAndProceed() {
        if (Duke.tripSessionState.equals("continue")) {
            /**It is incomplete trip, Start Location service**/
            requestLocationUpdates();
        } else {
            customProgressLoader.showDialog();
            /**GPS is ON, Check for last trip status and proceed**/
            checkLastTripStatus(new TripObserver() {

                @Override
                public void onChanged(String status, TripIdModel tripIdModel) {
                    if (status.equals("SUCCESS")) {
                        /**Received Response from the Server**/
                        if (tripIdModel.getSessionStatus().equals("continue")) {
                            Duke.tripId = tripIdModel.getTripId();
                            Duke.tripSessionState = tripIdModel.getSessionStatus();
                            customProgressLoader.hideDialog();
                            /**There exists an active trip, show Alert**/
                            notifyIncompleteTrip();
                        } else {
                            /**No active trips, Proceed with obtaining new Token**/
                            getNewtripId(new TripObserver() {
                                @Override
                                public void onChanged(String status, TripIdModel tripIdModel) {
                                    customProgressLoader.hideDialog();
                                    if (status.equals("SUCCESS")) {
                                        /**Received Response from the Server**/
                                        if (tripIdModel.getSessionStatus().equals("new")) {
                                            Duke.tripId = tripIdModel.getTripId();
                                            Duke.tripSessionState = tripIdModel.getSessionStatus();
                                            /**This is a new trip**/
                                            requestLocationUpdates();
                                        } else {
                                            /**There exists an active trip, show Alert**/
                                            notifyIncompleteTrip();
                                        }
                                    } else {
                                        /**Something went wrong, Show user alert dialog**/
                                        turnTrackingButtonStatusOFF();
                                        showSomethingWentWrongDialog();
                                    }
                                }
                            });
                        }
                    } else {
                        /**Something went wrong, Show user alert dialog**/
                        turnTrackingButtonStatusOFF();
                        customProgressLoader.hideDialog();
                        showSomethingWentWrongDialog();
                    }
                }
            });
        }

    }

    private void showSomethingWentWrongDialog() {
        dismissExistingComponentIfAny();
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                getString(R.string.error),
                getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later),
                false,
                getString(R.string.ok),
                popupActions,
                LOCATION_SETTINGS_INADEQUATE_DIALOG_ID);
    }

    private void getNewtripId(final TripObserver tripObserver) {
        iftaViewModel.getTripId().observe(getActivity(), new Observer<TripIdModel>() {
            @Override
            public void onChanged(@Nullable TripIdModel tripIdModel) {
                try {
                    String msg = "";
                    if (tripIdModel != null && tripIdModel.getErrorMessage() == null) {
                        /**Received Trip Id and Status, If trip session_status is "continue" then it is an active trip **/
                        msg = "SUCCESS";
                    } else {
                        //Error
                        msg = "ERROR";
                    }
                    tripObserver.onChanged(msg, tripIdModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    tripObserver.onChanged("ERROR", null);
                }
            }
        });
    }

    private void RequestGPSStateEnable() {
        //Start Activity for result <Location Update>
        Log.d(TAG, "Open Location setting for result: ");
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(getActivity());
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();
        // **************************
        builder.setAlwaysShow(true); // this is the Important
        // **************************

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        /**GPS is already enabled**/
                        checkTripStatusAndProceed();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    /**GPS not Enabled, Request!!**/
                                    rae.startResolutionForResult(getActivity(), AppConstants.Ifta.GPS_REQUEST);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                    showLocationSettingsTobeFixedInSettingsAlert(false);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be "
                                        + "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                showLocationSettingsTobeFixedInSettingsAlert(false);
                        }
                    }
                });
    }

    private void RequestGPSStateEnableForUploadDocument() {
        //Start Activity for result <Location Update>
        Log.d(TAG, "Open Location setting for result: ");
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(getActivity());
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();
        // **************************
        builder.setAlwaysShow(true); // this is the Important
        // **************************

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        /**GPS is already enabled**/
                        Duke.isLocationPermissionProvided = true;
                        uploadDocumentHandler();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    /**GPS not Enabled, Request!!**/
                                    rae.startResolutionForResult(getActivity(), REQUEST_GPS_FOR_UPLOAD_DOC);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                    showLocationSettingsTobeFixedInSettingsAlert(true);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be "
                                        + "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                showLocationSettingsTobeFixedInSettingsAlert(true);
                        }
                    }
                });
    }

    private void showLocationSettingsTobeFixedInSettingsAlert(boolean forUploadDoc) {
        if (!forUploadDoc) {
            turnTrackingButtonStatusOFF();
        }
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                "Error!",
                "Location settings are inadequate, and cannot be fixed here. Please fix in Settings.",
                false,
                "OK",
                popupActions,
                LOCATION_SETTINGS_INADEQUATE_DIALOG_ID);
    }

    private boolean isGPSEnabled() {
        return ((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestLocationAccessPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_CODE_GPS_ENABLE);
        }
    }

    private void requestLocationAccessForCapturingCurrentLocation() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE);
    }

    private void dismissExistingComponentIfAny() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    void clearTripData() {
        Duke.tripId = "";
        Duke.tripSessionState = "";
    }

//    @OnClick(R.id.share)
//    void onClickShare() {
//        Bundle params = new Bundle();
//        params.putString("button", "Share");
//        mFirebaseAnalytics.logEvent("SupportButton", params);
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, so Request for Permission
//            requestPermissions(
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    Utilities.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//            // app-defined int constant. The callback method gets the
//            // result of the request.
//        } else {
//            Utilities.openContactsPickerAndShare(getActivity(), getContext(), message);
//        }
//    }

    @OnClick(R.id.buy)
    void onClickBuy() {
//        Uri uriUrl;
//        if (!AppConstants.currentTheme.equals("duke")) {
//            uriUrl = Uri.parse("https://duke.ai/truckertaxtools/");
//        } else {
//            uriUrl = Uri.parse("https://duke.ai/signin/");
//        }
//        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
//        startActivity(launchBrowser);
        loadPaywall();
    }

    @OnClick(R.id.view_document)
    void onClickViewDocument() {
        // Firebase: Send click view document button event
        Bundle params = new Bundle();
        params.putString("Page", "Home");
        mFirebaseAnalytics.logEvent("ViewDocButtonClick", params);
        Bundle args = new Bundle();
        if (!Duke.isFromSignUp) {
            if (Duke.fileStatusModel != null && Duke.fileStatusModel.getRejectedDocumentsModels() != null && Duke.fileStatusModel.getRejectedDocumentsModels().size() > 0) {
                args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.REJECT);
            } else {
                args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.IN_PROCESS);
            }
        } else {
            args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.PROCESS);
        }
        NavigationFlowManager.openFragments(Documents.newInstance(), args, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.DOCUMENTS);
    }

    @OnClick(R.id.view_report)
    void onClickViewReport() {
        // Firebase: Send click view report button event
        Bundle params = new Bundle();
        params.putString("Page", "Home");
        mFirebaseAnalytics.logEvent("ViewReportButtonClick", params);
        NavigationFlowManager.openFragments(ReportsFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.REPORTS);
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        dismissExistingComponentIfAny();
        if (dialogId == NO_INTERNET_DIALOG_ID) {
            /**Switch Already Turned OFF, Open network settings**/
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
        } else if (dialogId == PLAY_SERVICES_DIALOG_ID) {
            /**Switch Already Turned OFF, Open Play Store to Update GooglePlayServices**/
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                }
            }
        } else if (dialogId == LOCATION_PERMISSION_NEEDED_DIALOG_ID) {
            /**Turn Switch OFF if Negative and Request permissions on Positive**/
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
//                requestLocationAccessPermissions();
                openSettings();
            } else if (id.equals(AppConstants.PopupConstants.NEGATIVE)) {
                turnTrackingButtonStatusOFF();
            }
        } else if (dialogId == INCOMPLETE_TRIP_DIALOG_ID) {
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                /**This trip is incomplete, Continue trip**/
                startLocationTrackerService();
                Bundle params = new Bundle();
                params.putString("Continue", "Yes");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
            } else {
                //Terminate trip
                /**Terminate trip and turn Switch Off**/
                turnTrackingButtonStatusOFF();
                iftaViewModel.sendTripData(null, Duke.tripId, AppConstants.Ifta.LOCATION_UPDATE_STATUS_TERMINATE)
                        .observeForever(new Observer<ErrorModel>() {

                            @Override
                            public void onChanged(@Nullable ErrorModel errorModel) {
                                try {
                                    Log.d(TAG, "onChanged: " + errorModel.getErrorMessage());
                                    if ((errorModel.getErrorMessage() != null) && (errorModel.getErrorCode() != null) && errorModel.getErrorCode().equals(AppConstants.Ifta.API_STATUS_SUCCESS)) {
//                                        editor.putBoolean(LOCATION_SETTINGS_KEY, false);
//                                        editor.apply();
                                        Log.d(TAG, "onChanged: Trip -> Terminated");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                /**Clear last Trip details**/
                clearTripData();
                Bundle params = new Bundle();
                params.putString("Continue", "No");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
            }
        } else if (dialogId == AppConstants.Ifta.STOP_TRIP_TRACKING_DIALOG_ID) {
            /**User switched to Stop location Updates**/
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                /**User chose to stop tracking the trip**/
                Bundle params = new Bundle();
                params.putString("Stop", "Yes");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
                stopTracking();
            } else {
                /**Turn Trip tracking button ON**/
                turnTrackingButtonStatusON();
                Bundle params = new Bundle();
                params.putString("Stop", "No");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
            }
        } else if (dialogId == ENABLE_GPS_DIALOG_ID) {
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                RequestGPSStateEnable();
            } else {
                turnTrackingButtonStatusOFF();
            }
        } else if (dialogId == AppConstants.Ifta.START_TRACKING_TRIP_DIALOG_ID) {
            if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
                Bundle params = new Bundle();
                params.putString("Start", "Yes");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
                startLocationTrackerService();
            } else {
                Bundle params = new Bundle();
                params.putString("Start", "No");
                mFirebaseAnalytics.logEvent("Tracking_Miles", params);
                turnTrackingButtonStatusOFF();
            }
        }
    }

    public void stopTracking() {
        mService.removeLocationUpdates(getActivity());
        isIFTATrackingOn = false;
    }

    @Override
    public void onClickProfile() {
        // Firebase: Send click profile button event
        Bundle params = new Bundle();
        params.putString("Page", "Home");
        mFirebaseAnalytics.logEvent("ProfileButtonClick", params);
        Utilities.navigateToProfilePage(getActivity());
    }

    @Override
    public void onBackClicked() {

    }

    @Override
    public void onClickToolbarTitle() {
    }

    @Override
    public void onClickHeaderTitle() {
        // Firebase: Send click Home button event
        Bundle params = new Bundle();
        params.putString("button", "Home");
        mFirebaseAnalytics.logEvent("SupportButton", params);

    }

    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = getActivity().getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                fileStatusViewModel.getFileStatusModelLiveData("").observe(this, new Observer<FileStatusModel>() {
                    @Override
                    public void onChanged(FileStatusModel fileStatusModel) {
                        customProgressLoader.hideDialog();
                        if (fileStatusModel != null && fileStatusModel.getMessage() == null) {
                            Duke.fileStatusModel = fileStatusModel;
                            if (fileStatusModel.getMemberStatus().equals("NONE") || fileStatusModel.getMemberStatus().equals("FREE")) {
                                loadPaywall();
                            }
                        } else {

                        }
                    }
                });
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }

    @Override
    public void onResume() {
        super.onResume();

//        updateDocumentsData();
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
//        isFirstTime();
//        checkForCancelledSubscription();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
        if (!isBackgroundLocationPermissionRequested) {
            setupLocationTrackingStatus();
        } else {
            isBackgroundLocationPermissionRequested = false;
            checkTripStatusAndProceed();
        }
    }

    private void checkForCancelledSubscription() {
        Purchases.getSharedInstance().getPurchaserInfo(new ReceivePurchaserInfoListener() {
            @Override
            public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
                // access latest purchaserInfo
                if (!purchaserInfo.getEntitlements().getActive().isEmpty() || purchaserInfo.getEntitlements().get("DocumentUpload") != null) {
                    //user has access to some entitlement
                    if (!purchaserInfo.getEntitlements().get("DocumentUpload").getPeriodType().equals("TRIAL") &&
                            purchaserInfo.getEntitlements().get("DocumentUpload").getUnsubscribeDetectedAt() != null) {
//                        if(fileStatusModel != null && !fileStatusModel.getRequest().getCustomerId().toLowerCase().equals("sankalp@divami.com")) {
//                            updatePayment("cancel");
//                        }
                        updatePayment("cancel");
                    }
                }
                Log.d("check for canclled subs", purchaserInfo.toString());
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                Log.d("error", error.toString());
            }
        });
    }

    private void updatePayment(String subscriptionType) {
        JsonObject plan = new JsonObject();
        plan.addProperty("plan", subscriptionType);

        updatePaymentStatus(new MemberStatusUpdateObserver() {
            @Override
            public void onChanged(String status, UpdatePaymentModel updatePaymentModel) {
                if (updatePaymentModel != null) {
                    if (status.toLowerCase().equals("success") && updatePaymentModel.getMsg() != null) {
                        Log.d("Status updated in BE", updatePaymentModel.getMsg());
                    } else {
                        Log.d("Status not update in BE", updatePaymentModel.getMsg());
                    }
                }
            }
        }, plan);
    }

    private void updatePaymentStatus(MemberStatusUpdateObserver memberStatusUpdateObserver, JsonObject plan) {

        fileStatusViewModel.memberStatusUpdate(plan).observe(this, new Observer<UpdatePaymentModel>() {
            @Override
            public void onChanged(UpdatePaymentModel updatePaymentModel) {
                try {
                    String msg = "";
                    if (updatePaymentModel != null && updatePaymentModel.getMsg() == null) {
                        /**Received Last Trip Id and Status, If trip session_status is "continue" then it is an active trip **/
                        if (updatePaymentModel.getMsg().contentEquals("Subscription updated")) {
                            msg = "SUCCESS";
                        }
                    } else {
                        //Error
                        msg = "ERROR";
                    }
                    memberStatusUpdateObserver.onChanged(msg, updatePaymentModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    /**Error occurred**/
                    memberStatusUpdateObserver.onChanged("ERROR", updatePaymentModel);
                }
            }
        });
    }

    private void loadPaywall() {
//        Purchases.getSharedInstance().getPurchaserInfo(new ReceivePurchaserInfoListener() {
//            @Override
//            public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
//                // access latest purchaserInfo
//                if (purchaserInfo.getEntitlements().getActive().isEmpty()) {
//                    //user has access to some entitlement
//                    Intent intent = new Intent(requireActivity(), PaymentActivity.class);
//                    requireActivity().startActivity(intent);
//                    requireActivity().finish();
//                }
//                Log.d("check for canclled subs", purchaserInfo.toString());
//            }
//
//            @Override
//            public void onError(@NonNull PurchasesError error) {
//                Log.d("error", error.toString());
//            }
//        });
        Intent intent = new Intent(requireActivity(), PaymentActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            getContext().unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
    }

    private void updateDocumentsData() {
        if (customProgressLoader == null || fileStatusViewModel == null) {
            return;
        }
        String numberOfDocs = "";
        customProgressLoader.showDialog();
        fileStatusViewModel.getFileStatusModelLiveData(numberOfDocs).observe(getViewLifecycleOwner(), new Observer<FileStatusModel>() {
            @Override
            public void onChanged(FileStatusModel fileStatusModel) {
                customProgressLoader.hideDialog();
                if (fileStatusModel != null && fileStatusModel.getMessage() == null) {
                    Duke.fileStatusModel = fileStatusModel;
                } else {
                    String message = "";
                    if (fileStatusModel != null) {
                        // Firebase: Send Error information
                        UserDataModel userDataModel;
                        userDataModel = userConfig.getUserDataModel();
                        Bundle params = new Bundle();
                        params.putString("Page", "Home");
                        params.putString("Description", "Not a member");
                        params.putString("UserEmail", userDataModel.getUserEmail());
                        mFirebaseAnalytics.logEvent("Error", params);
                        if (fileStatusModel.getMessage().contains(AppConstants.HomePageConstants.NO_GROUPS)) {
                            message = getString(R.string.not_part_of_any_group);
                        } else {
                            message = fileStatusModel.getMessage();
                        }
                        confirmationComponent = new ConfirmationComponent(getContext(), getResources().getString(R.string.error), message, false, getResources().getString(R.string.ok), popupActions, 1);
                    }
                }
                getProfileImage();
                setUploadCountIndicator(Duke.fileStatusModel);
                setBuyButton(Duke.fileStatusModel);
                setRejectedCountWrapper();
                checkForCancelledSubscription();
            }
        });
    }

    private void setRejectedCountWrapper() {
        if (Duke.fileStatusModel != null && Duke.fileStatusModel.getRejectedDocumentsModels() != null && Duke.fileStatusModel.getRejectedDocumentsModels().size() > 0) {
            rejectedDocumentsModelsCount = Duke.fileStatusModel.getRejectedDocumentsModels().size();
        } else {
            rejectedDocumentsModelsCount = 0;
        }
        if (rejectedDocumentsModelsCount > 0) {
            rejectedCountWrapper.setVisibility(View.VISIBLE);
            rejectedCount.setText(String.valueOf(Duke.fileStatusModel.getRejectedDocumentsModels().size()));
        } else {
            rejectedCountWrapper.setVisibility(View.GONE);
        }
    }

    public boolean hasLocationPermissionsGranted() {
//        if(Build.VERSION.SDK_INT>=30) {
//            if (checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) return
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("Allow Background Location")
//                            .setMessage("Duke.ai needs to access when the app is in background to track IFTA miles precisely!")
//                            .setPositiveButton("YES").
//                    requestPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
//            }

//        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setTitle("Background Location Required")
//                .setMessage("Duke.ai needs to access when the app in background to track IFTA miles precisely!")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //set what would happen when positive button is clicked
//                        String[] permissionsNeeded = {Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
//                        requestPermissions(permissionsNeeded, 12);
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(getApplicationContext(),"This app needs background access to properly track IFTA miles",Toast.LENGTH_LONG).show();
//                    }
//                })
//                .show();
//        } else {
//            return ContextCompat.checkSelfPermission(getContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            return ContextCompat.checkSelfPermission(getContext(),
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
//        } else {
//            return ContextCompat.checkSelfPermission(getContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//        }

        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private Boolean checkSinglePermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void notifyIncompleteTrip() {
        dismissExistingComponentIfAny();
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                getResources().getString(R.string.trip_updates),
                getContext().getString(R.string.your_last_trip_is_active),
                false,
                getContext().getString(R.string.continue_trip),
                getContext().getString(R.string.end_trip),
                popupActions,
                INCOMPLETE_TRIP_DIALOG_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode" + requestCode);
        switch (requestCode) {
            case Utilities.MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Utilities.openContactsPickerAndShare(getActivity(), getContext(), message);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case REQUEST_CODE_GPS_ENABLE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates Here:
                        if (isGPSEnabled()) {
                            checkTripStatusAndProceed();
                        } else {
                            RequestGPSStateEnable();
                        }
                    }

                } else {
                    /**Location Permission denied, Disable the Switch which is ON**/
                    turnTrackingButtonStatusOFF();
                }
                return;
            }
            case REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates Here:
                        if (isGPSEnabled()) {
                            Duke.isLocationPermissionProvided = true;
                            uploadDocumentHandler();
                        } else {
                            RequestGPSStateEnableForUploadDocument();
                        }
                    }

                } else {
                    /**Location Permission denied, Disable the Switch which is ON**/
                    Duke.isLocationPermissionProvided = false;
                    uploadDocumentHandler();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == Utilities.CONTACT_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
//                Log.d("MyTag", results.get(0).getDisplayName());
                ListIterator<ContactResult> iterator = results.listIterator();
                String allNumbers = "";
                while (iterator.hasNext()) {
                    allNumbers += iterator.next().getPhoneNumbers().get(0).getNumber() + ";";
                }
                String name = (Duke.userName != null ? Duke.userName : "");
                if (AppConstants.currentTheme.equals("duke")) {
                    message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download) + userConfig.getUserDataModel().getUserEmail() + this.getString(R.string.when_you_register_in_order_to) + AppConstants.iosLink + this.getString(R.string.for_ios_or) + AppConstants.androidLink + this.getString(R.string.for_android);
                } else {
                    message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download_ttt) + "INFO@TAXATIONSOLUTIONS.NET" + this.getString(R.string.when_you_register_in_order_to_ttt) + "bit.ly/TruckerTTiOS" + this.getString(R.string.for_ios_or) + "http://bit.ly/TruckerTTAndroid" + this.getString(R.string.for_android);
                }
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("smsto:" + allNumbers)); // This ensures only SMS apps respond
                intent.putExtra("sms_body", message);
                startActivity(intent);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        } else if (requestCode == AppConstants.Ifta.GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                /**GPS turned ON, proceed with Check Active trip status**/
                checkTripStatusAndProceed();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Request failed Show popup
                /**GPS is OFF, Show user the Alert**/
                showEnableLocationServicesDialog();
            }
        } else if (requestCode == REQUEST_GPS_FOR_UPLOAD_DOC) {
            if (resultCode == Activity.RESULT_OK) {
                /**GPS turned ON, proceed with Check Active trip status**/
                Duke.isLocationPermissionProvided = true;
                uploadDocumentHandler();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Request failed Show popup
                /**GPS is OFF, Show user the Alert**/
                Duke.isLocationPermissionProvided = false;
                uploadDocumentHandler();
            }
        } else if (requestCode == 12) {

        }
    }

    private void showEnableLocationServicesDialog() {
        confirmationComponent = new ConfirmationComponent(
                getContext(),
                getResources().getString(R.string.error),
                getString(R.string.please_enable_location_services),
                false,
                getString(R.string.open_strings),
                getString(R.string.cancel),
                popupActions,
                ENABLE_GPS_DIALOG_ID);
    }

    private void setCurrentTheme(View view) {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
//        view1.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        uploadId.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        uploadDocumentId.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        viewDocument.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        viewId.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
        reportView.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
        iftaTitleText.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        shareButton.setColorFilter(Color.parseColor((changeThemeModel.getFloatingButtonColor())));
//        utilitySupportButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        utilWrapperLinearLatout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class LocationUpdatesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
//                Toast.makeText(context, "Received Location Updates: ", Toast.LENGTH_SHORT).show();
                if (intent.hasExtra(LocationService.LOCATION_DATA)) {
                    Location location = intent.getExtras().getParcelable(LocationService.LOCATION_DATA);
                    if (location != null) {
                        address = Utilities.getLocalAddress(getContext(), location);

                        iftaDescriptionText.setText(getContext().getString(R.string.last_updated_location_is) + address);

                        LocationService.lastUpdatedAddress = address;
                        Log.d("LOCATION UPDATES: ", address);
                        isIFTATrackingOn = true;
                        autoIFTAService();
                    }
                }
            } catch (Exception e) {
                //No need to do anything
                Log.d("Location receive error", e.getLocalizedMessage());
            }
        }
    }
}
