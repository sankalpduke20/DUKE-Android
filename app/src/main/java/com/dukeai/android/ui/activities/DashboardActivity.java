package com.dukeai.android.ui.activities;

import static com.dukeai.android.Duke.accessToken;
import static com.dukeai.android.Duke.api_key;
import static com.dukeai.android.Duke.client_id;
import static com.dukeai.android.Duke.cust_id;
import static com.dukeai.android.Duke.idToken;
import static com.dukeai.android.Duke.refreshToken;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken;
import com.android.billingclient.api.SkuDetails;
import com.dukeai.android.Duke;
import com.dukeai.android.LocationService;
import com.dukeai.android.R;
import com.dukeai.android.RealPathUtil;
import com.dukeai.android.UriUtils;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.interfaces.UploadStatusClickActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.fragments.HomeFragment;
import com.dukeai.android.ui.fragments.UploadPreviewFragment;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.CameraUtils;
import com.dukeai.android.utils.Config;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.utils.UploadDocument;
import com.dukeai.android.utils.UploadStatusDialog;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.DeviceTokenViewModel;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.UploadFileViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.sun.mail.iap.ByteArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.gauriinfotech.commons.Commons;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DashboardActivity extends BaseActivity implements UploadDocumentInterface {

    String TAG = DashboardActivity.class.getSimpleName();

    UploadDocument uploadDocument;
    Context context;
    Bundle savedInstance;
    GetDetailsHandler detailsHandler;
    UserConfig userConfig = UserConfig.getInstance();
    CustomProgressLoader customProgressLoader;
    UserDataModel userDataModel;
    DeviceTokenViewModel deviceTokenViewModel;
    int screenWidth, screenHeight;
    @BindView(R.id.parent_layout)
    RelativeLayout parentLayout;
    //Firebse broadcast receiver
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    String upload = null;
    UploadFileViewModel uploadFileViewModel;
    UploadStatusDialog uploadStatusDialog;
    ArrayList<String> pdfURIs = new ArrayList<>();
    ArrayList<Uri> uriList = new ArrayList<>();
    ArrayList<Uri> imageUriList = new ArrayList<>();
    ArrayList<String> imageURIs = new ArrayList<>();
    ArrayList<Integer> bitmapsCount = new ArrayList<>();
    FileStatusViewModel fileStatusViewModel;
    Uri uri2;
    Bitmap imageBitmap;
    Boolean firstTime = null;
    //Stores the latest locations
    String address = "";
    String latitude = "";
    String longitude = "";
    FusedLocationProviderClient fusedLocationProviderClient;
    // A reference to the service used to get location updates.
    ConfirmationComponent confirmationComponent;
    String currentLocation = "none";

    public DashboardActivity() {
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        context = this;
        initFirebase();
        savedInstance = savedInstanceState;
        ButterKnife.bind(this);
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        setInitials();
        setCurrentTheme();
        //Register Receiver
        IntentFilter intentFilter = new IntentFilter("com.truckertaxtool.android.RESTART_TRACKING");
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    }

    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                fileStatusViewModel.getFileStatusModelLiveData("").observe(this, new Observer<FileStatusModel>() {
                    @Override
                    public void onChanged(FileStatusModel fileStatusModel) {
                        customProgressLoader.hideDialog();
                        if (fileStatusModel != null && fileStatusModel.getMessage() == null) {
                            Duke.fileStatusModel = fileStatusModel;
                            if (fileStatusModel.getMemberStatus().equals("NONE") || fileStatusModel.getMemberStatus().equals("FREE")) {
                                Intent intent = new Intent(DashboardActivity.this, PaymentActivity.class);
                                startActivity(intent);
                                finish();
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
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            Duke.isInBackground = false;
            Log.d("SD", "1");
        }
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
    }

    private void setInitials() {
        Duke.isWithOutToken = false;
        customProgressLoader = new CustomProgressLoader(this);
        deviceTokenViewModel = ViewModelProviders.of(this).get(DeviceTokenViewModel.class);
        Utilities.resetGlobalData();
        isFirstTime();
        loadHomeFragments();
        setDetailsHandler();
        getUserDetails();
        mRegisterFirebaseBroadcastReceiver();
        if (Duke.deviceToken == null) {
            getDeviceToken();
        }
        if (Duke.referralId == null) {
            Duke.referralId = "none";
            PreferenceManager.saveString(this, AppConstants.UserPreferencesConstants.REFERRAL_ID, Duke.referralId);
        }
    }

    private void mRegisterFirebaseBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    Duke.deviceToken = intent.getStringExtra(AppConstants.UserPreferencesConstants.TOKEN);
                    PreferenceManager.saveString(getApplicationContext(), AppConstants.UserPreferencesConstants.DEVICE_TOKEN, Duke.deviceToken);
                    updateUserDeviceToken(Duke.deviceToken, Duke.referralId);
                }
            }
        };
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
    }

    private void setDetailsHandler() {
        detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                String username = cognitoUserDetails.getAttributes().getAttributes().get("name");
                Duke.userName = username;

                if (username == null) {
                    Duke.userName = cognitoUserDetails.getAttributes().getAttributes().get("email").split("@")[0];
                }
                AppHelper.setUserDetails(cognitoUserDetails);
            }

            @Override
            public void onFailure(Exception exception) {
            }
        };
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackLength = fm.getBackStackEntryCount();

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.dashboard_wrapper);
        if (currentFragment instanceof HomeFragment) {
            closeApp();
        } else if (backStackLength > 1) {
            NavigationFlowManager.openFragments(new HomeFragment(), null, this, R.id.dashboard_wrapper);
        }
    }

    private void closeApp() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void getDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        Duke.deviceToken = task.getResult().getToken();
                        PreferenceManager.saveString(getApplicationContext(), AppConstants.UserPreferencesConstants.DEVICE_TOKEN, Duke.deviceToken);
                        updateUserDeviceToken(Duke.deviceToken, Duke.referralId);
                    }
                });
    }

    private void updateUserDeviceToken(String deviceTk, String referralId) {
        JsonObject jsonObject = InputParams.updateDeviceToken(deviceTk, referralId);
        deviceTokenViewModel.updateDeviceToken(jsonObject).observe(this, new Observer<DeviceTokenModel>() {
            @Override
            public void onChanged(@Nullable DeviceTokenModel deviceTokenModel) {
                if (deviceTokenModel != null && deviceTokenModel.getCode() != null && deviceTokenModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {

                }
            }
        });
    }

    private void getUserDetails() {
        if (!Duke.isFromLogin) {
            userDataModel = userConfig.getUserDataModel();
            if (userDataModel != null && userDataModel.getCognitoUserSession() != null) {

                AppHelper.setCurrSession(userDataModel.getCognitoUserSession());
                AppHelper.setUser(userDataModel.getUserEmail());
                Duke.isFirebaseSetUp = true;
            }
        }

        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    private void loadHomeFragments() {
        NavigationFlowManager.openFragments(HomeFragment.newInstance(), null, this, R.id.dashboard_wrapper, AppConstants.Pages.HOME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                CameraUtils.refreshGallery(context.getApplicationContext(), Duke.imageStoragePath);

                /**Fix for Image Rotation Issue**/
                rotateImageIfNecessary(Duke.imageStoragePath);
                if (Duke.isLocationPermissionProvided) {
                    previewCapturedImage();
                } else {
//                    openPreviewImage(Duke.imageStoragePath, "none", "", "");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Duke.isLocationPermissionProvided = true;
                        getCurrentLocation(data);
                    } else {
                        openPreviewImage(Duke.imageStoragePath, "none", "", "");
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this.getApplicationContext(), getString(R.string.cancelled_image_capture), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getApplicationContext(), getString(R.string.failed_to_capture_image), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {

            if (resultCode == RESULT_CANCELED) {
                return;
            }

            //Fetch Location - if permission is available
            if (Duke.isLocationPermissionProvided) {
                getCurrentLocation(data);
            } else {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Duke.isLocationPermissionProvided = true;
                    getCurrentLocation(data);
                } else {
                    performUploadTasks(data, "none", latitude, longitude);
                }
            }

        } else {
            //Call Fragment's onActivityResult
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.dashboard_wrapper);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void performUploadTasks(Intent data, String locationToBE, String latitude, String longitude) {
        if (Duke.isNewLoadBeingCreated || Duke.isDocumentAddingToLoad || Duke.isDocumentBeingScanned) {
            loadDocumentUpload(data, locationToBE, latitude, longitude);
        } else {
            normalDocUpload(data, locationToBE, latitude, longitude);
        }

    }

    private void loadDocumentUpload(Intent data, String locationToBE, String latitude, String longitude) {
        Uri uri = null;
        int pdfCount = 0;
        int imageCount = 0;
        pdfURIs.clear();
        imageURIs.clear();
        imageUriList.clear();
        uriList.clear();
//        Duke.PDFDocURIs.clear();

        if (data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    ContentResolver cR = context.getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getExtensionFromMimeType(cR.getType(data.getClipData().getItemAt(i).getUri()));
                    if(type==null){
                        type = cR.getType(data.getClipData().getItemAt(i).getUri());
                    }
                   try{
                       if (type.equalsIgnoreCase("image/pdf")) {
                           pdfCount++;
                           uriList.add(data.getClipData().getItemAt(i).getUri());
                           pdfURIs.add(data.getClipData().getItemAt(i).getUri().getPath());
                           uri2 = data.getClipData().getItemAt(i).getUri();
                           Duke.PDFDocURIs.add(data.getClipData().getItemAt(i).getUri());
                       }else if (type.equalsIgnoreCase("pdf")) {
                           pdfCount++;
                           uriList.add(data.getClipData().getItemAt(i).getUri());
                           pdfURIs.add(data.getClipData().getItemAt(i).getUri().getPath());
                           uri2 = data.getClipData().getItemAt(i).getUri();
                           Duke.PDFDocURIs.add(data.getClipData().getItemAt(i).getUri());
                       } else {
                           imageCount++;
                           imageUriList.add(data.getClipData().getItemAt(i).getUri());
                           imageURIs.add(data.getClipData().getItemAt(i).getUri().getPath());
                       }
                   }catch (Exception ex){
                       Log.e("Exception: DashboardActivity-437 ",ex.toString());
                   }
                }
            } else {
                uri2 = data.getData();
                ContentResolver cR = context.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(uri2));
                if (type.equalsIgnoreCase("pdf")) {
//                    uri = data.getData();
                    if (data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = data.getClipData().getItemAt(0).getUri();
                    }
                    uriList.add(uri);
                    Duke.PDFDocURIs.add(uri);
                    pdfURIs.add(uri.getPath());

                    /** Code needed when adding feature to detect corrupt PDF **/
//                    if(inavlidPDFDocumentCheck()) {
//                        uploadPDFDoc(pdfURIs, locationToBE);
//                    } else {
//                        invalidDocumentDetected();
//                        return;
//                    }
//                    uploadPDFDoc(pdfURIs, locationToBE);
                    String path = uri.getPath();
//                    makeFileCopyInCacheDir(uri);
//                    String fullPath = Commons.getPath(uri, context);
//                    openPreviewImage(fullPath, locationToBE);
//                    if ( UriUtils.getPathFromUri(this,uri) != null && !UriUtils.getPathFromUri(this,uri).isEmpty()) {
//                        openPreviewImage(UriUtils.getPathFromUri(this,uri), locationToBE);
//                    }
                    String realPath = UriUtils.getMediaFilePathForN(uri, this);
                    if (realPath != null) {
                        openPreviewImage(realPath, locationToBE, latitude, longitude);
                    }
//                    if (Utilities.getPath(context, uri) != null && !Utilities.getPath(context, uri).isEmpty()) {
//                        openPreviewImage(Utilities.getPath(context, uri), locationToBE);
//                    }
//                    openPreviewImage(uri.getPath(), locationToBE);
                } else {
                    pdfURIs.clear();
//                    uri = data.getData();
                    if (data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = data.getClipData().getItemAt(0).getUri();
                    }
                    imageUriList.add(uri);
                    imageURIs.add(uri.getPath());
                }
            }
        }

        if ((pdfURIs.size() + imageURIs.size()) > 1) {
            ArrayList<String> pathList = new ArrayList<>();

//            openPreviewDocuments();
        }

        try {
            if (data.getData() != null) {
                uri = data.getData();
            } else {
                uri = data.getClipData().getItemAt(0).getUri();
            }
        }catch (Exception ex){
            Log.e("Exception-DASHBOARD 502 ",ex+"");
        }


        /*try{

            uri = data.getData();
        }catch (Exception ex){
          try{
              uri = data.getClipData().getItemAt(0).getUri();
          }catch (Exception e){
              Log.e("Exception-DASHBOARD 507 ",ex+"");
          }
            Log.e("Exception-DASHBOARD 509 ",ex+"");
        }*/


        if (pdfURIs.size() == 0 && imageURIs.size() > 0) {
            Duke.imageStoragePath = Utilities.getPath(context, uri);
//            Duke.imageStoragePath = Utilities.getPath(context, data.getClipData().getItemAt(0).getUri());
            if (Duke.imageStoragePath != null && !Duke.imageStoragePath.isEmpty() && (Duke.imageStoragePath.toLowerCase().endsWith(".jpg") || Duke.imageStoragePath.toLowerCase().endsWith(".jpeg")
                    || Duke.imageStoragePath.toLowerCase().endsWith(".png"))) {
                openPreviewImage(Duke.imageStoragePath, locationToBE, latitude, longitude);
            } else {
                Duke.imageStoragePath = "";
            }
        }
    }

    private void normalDocUpload(Intent data, String locationToBE, String latitude, String longitude) {
        Uri uri = null;
        int pdfCount = 0;
        int imageCount = 0;
        pdfURIs.clear();
        imageURIs.clear();
        imageUriList.clear();
        uriList.clear();

        if (data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    ContentResolver cR = context.getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String type = mime.getExtensionFromMimeType(cR.getType(data.getClipData().getItemAt(i).getUri()));
                   try{
                       if (type.equalsIgnoreCase("pdf")) {
                           pdfCount++;
                           uriList.add(data.getClipData().getItemAt(i).getUri());
                           pdfURIs.add(data.getClipData().getItemAt(i).getUri().getPath());
                           uri2 = data.getClipData().getItemAt(i).getUri();
                       } else {
                           imageCount++;
                           imageUriList.add(data.getClipData().getItemAt(i).getUri());
                           imageURIs.add(data.getClipData().getItemAt(i).getUri().getPath());
                       }
                   }catch (Exception ex){

                   }
                }
            } else {
                uri2 = data.getData();
                ContentResolver cR = context.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(uri2));
                if (type.equalsIgnoreCase("pdf")) {
//                    uri = data.getData();
                    if (data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = data.getClipData().getItemAt(0).getUri();
                    }
                    uriList.add(uri);
                    pdfURIs.add(uri.getPath());
                    /** Code needed when adding feature to detect corrupt PDF **/
//                    if(inavlidPDFDocumentCheck()) {
//                        uploadPDFDoc(pdfURIs, locationToBE);
//                    } else {
//                        invalidDocumentDetected();
//                        return;
//                    }
//                    uploadPDFDoc(pdfURIs, locationToBE);
                    String path = uri.getPath();
                    if (Utilities.getPath(context, uri) != null && !Utilities.getPath(context, uri).isEmpty()) {
                        openPreviewImage(Utilities.getPath(context, uri), locationToBE, latitude, longitude);
                    }
                } else {
                    pdfURIs.clear();
//                    uri = data.getData();
                    if (data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = data.getClipData().getItemAt(0).getUri();
                    }
                    imageUriList.add(uri);
                    imageURIs.add(uri.getPath());
                }
            }
        }

        //Check For corrupted Docs
        for (int i = 0; i < imageUriList.size(); i++) {
            String path = Utilities.getPath(context, imageUriList.get(i));
            Bitmap isBitmapValid = Utilities.getPortraitResizedBitmap(BitmapFactory.decodeFile(path), 2160, 1440);
            if (isBitmapValid == null) {
                invalidDocumentDetected();
                return;
            }
        }

        /** Code for corrupt PDF detection **/
//        if(!inavlidPDFDocumentCheck()){
//            invalidDocumentDetected();
//            return;
//        }

        try {

            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri2));

        } catch (Exception e) {
        }

        if (pdfURIs.size() > 0 && imageURIs.size() < 1) {
            uri2 = data.getData();
            uploadPDFDoc(pdfURIs, locationToBE);
//            String path = Utilities.getPath(context, uri2);
//            if (path != null && !path.isEmpty()) {
//                openPreviewImage(path, locationToBE);
//            }
            return;
        }

        if (pdfURIs.size() > 0 && imageURIs.size() > 0) {
            uploadPDFDoc(pdfURIs, locationToBE);
            uploadFiles(locationToBE);
            return;
        }

        if (pdfURIs.size() == 0 && imageURIs.size() > 1) {
            uploadFiles(locationToBE);
            return;
        }

//        if(uri != null) {
//        }
        if (data.getData() != null) {
            uri = data.getData();
        } else {
            uri = data.getClipData().getItemAt(0).getUri();
        }
        if (pdfURIs.size() == 0 && imageURIs.size() > 0) {
            Duke.imageStoragePath = Utilities.getPath(context, uri);
            if (Duke.imageStoragePath != null && !Duke.imageStoragePath.isEmpty() && (Duke.imageStoragePath.toLowerCase().endsWith(".jpg") || Duke.imageStoragePath.toLowerCase().endsWith(".jpeg")
                    || Duke.imageStoragePath.toLowerCase().endsWith(".png"))) {
                openPreviewImage(Duke.imageStoragePath, locationToBE, latitude, longitude);
            } else {
                Duke.imageStoragePath = "";
            }
        }

    }

    private boolean inavlidPDFDocumentCheck() {
        boolean areAllPDFDocssValid = true;

        for (int i = 0; i < pdfURIs.size(); i++) {
            try {
//                PdfReader reader = new PdfReader(RealPathUtil.getRealPath(this, uriList.get(i)));
                PdfReader reader = new PdfReader(Utilities.getPath(this, uriList.get(i)));
                String textFromPage = PdfTextExtractor.getTextFromPage(reader, 1);
                System.out.println(textFromPage);
            } catch (Exception e) {
                // handle exception
                areAllPDFDocssValid = false;
                invalidDocumentDetected();
                return areAllPDFDocssValid;
            }
        }
        return areAllPDFDocssValid;
    }

    String saveImagePath(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SavedImages");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "DukeFiles_" + timeStamp + ".jpg";
        File file = new File(myDir, fname);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root + "/SavedImages/" + fname;
    }

    private void rotateImageIfNecessary(String imagePath) {
        try {
            ExifInterface ei = null;
            ei = new ExifInterface(imagePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap rotatedBitmap = null;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            int rotation = 1;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = ExifInterface.ORIENTATION_ROTATE_90;
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = ExifInterface.ORIENTATION_ROTATE_180;
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = ExifInterface.ORIENTATION_ROTATE_270;
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            /**Rotate only when the image is not in correct orientation**/
            if (rotation != ExifInterface.ORIENTATION_NORMAL) {
                Utilities.saveBitmap(imagePath, rotatedBitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void invalidDocumentDetected() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(HomeFragment.IS_DOCUMENT_INVALID, true);
        NavigationFlowManager.openFragments(new HomeFragment(), bundle, DashboardActivity.this, R.id.dashboard_wrapper);
        return;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    private void openPreviewImage(String path, String address, String lat, String longi) {
        Bundle args = new Bundle();
        args.putString(AppConstants.UploadDocumentsConstants.BITMAP_IMAGE, path);
        args.putString("address", address);
        args.putString("lat", lat);
        args.putString("longi", longi);
        NavigationFlowManager.openFragments(UploadPreviewFragment.newInstance(), args, DashboardActivity.this, R.id.dashboard_wrapper);
    }

    private void openPreviewDocuments(ArrayList<String> pathList, String address) {
        Bundle args = new Bundle();
        args.putString(AppConstants.UploadDocumentsConstants.BITMAP_IMAGE, "none");
        args.putStringArrayList(AppConstants.UploadDocumentsConstants.MULTIPLE_DOCUMENTS, pathList);
        args.putString("address", address);
        NavigationFlowManager.openFragments(UploadPreviewFragment.newInstance(), args, DashboardActivity.this, R.id.dashboard_wrapper);
    }

    private void openPreviewImageAfterCapturingLocation(String path, String address) {
        Bundle args = new Bundle();
        args.putString(AppConstants.UploadDocumentsConstants.BITMAP_IMAGE, path);
        args.putString("address", address);
        NavigationFlowManager.openFragments(UploadPreviewFragment.newInstance(), args, DashboardActivity.this, R.id.dashboard_wrapper);
    }

    private void previewCapturedImage() {
        final CustomProgressLoader customProgressLoader = new CustomProgressLoader(this);
        customProgressLoader.showDialog();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            customProgressLoader.dismiss();
//                            openPreviewImage(Duke.imageStoragePath, "none");
//                        }
//                    });
//                } catch (Exception ignored) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            customProgressLoader.dismiss();
//                        }
//                    });
//                }
//            }
//        });
//        thread.run();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(DashboardActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.d("location fetched", addresses.get(0).getAddressLine(0));
                        if (addresses.get(0) != null && addresses.get(0).getAddressLine(0).length() > 0) {
                            address = addresses.get(0).getAddressLine(0);
                            latitude = String.valueOf(location.getLatitude());
                            longitude = String.valueOf(location.getLongitude());
                            System.out.println("Current Lat " + latitude + " Long " + longitude);
                            openPreviewImage(Duke.imageStoragePath, address, latitude, longitude);
                        }
                        customProgressLoader.dismiss();
                    } catch (Exception e) {
                        openPreviewImage(Duke.imageStoragePath, "none", "", "");
                        Log.d("location not fetched", e.getLocalizedMessage());
                        customProgressLoader.dismiss();
                    }
                } else {
                    openPreviewImage(Duke.imageStoragePath, "none", "", "");
                    customProgressLoader.dismiss();
                }
            }
        });
        Log.d("current address", address);
    }

    @Override
    public void uploadDocumentListener(Boolean isOpen) {
        if (isOpen) {
            uploadDocument = new UploadDocument(context, this, savedInstance, false, null, isOpen);
        } else {
            uploadDocument = new UploadDocument(context, this, savedInstance, false, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }

    private void uploadPDFDoc(final ArrayList<String> pdfURIList, String streetAddress) {
        customProgressLoader.showDialog();
        uploadFileViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> count = new ArrayList<>();
                for (int i = 0; i < pdfURIList.size(); i++) {
                    count.clear();
                    count.add(1);
                    MultipartBody.Part[] list = getMultipartBody(pdfURIList, uriList.get(i), screenWidth, screenHeight);
                    MultipartBody.Part fileCount = DashboardActivity.getFileCountArray(count);
                    MultipartBody.Part address = MultipartBody.Part.createFormData("address", streetAddress);

                    if (list != null && list.length <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customProgressLoader.hideDialog();
                            }
                        });
                        return;
                    }
                    uploadFileViewModel.uploadFile(list, fileCount, address).observe(DashboardActivity.this, new Observer<FileUploadSuccessModel>() {
                        @Override
                        public void onChanged(@Nullable FileUploadSuccessModel jsonElement) {
                            upload = null;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customProgressLoader.hideDialog();
                                }
                            });
                            if (jsonElement != null && jsonElement.getCode() != null && jsonElement.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, true);
                                NavigationFlowManager.openFragments(new HomeFragment(), bundle, DashboardActivity.this, R.id.dashboard_wrapper);
                            } else {
                                if (jsonElement != null && jsonElement.getMessage() != null) {
                                    upload = AppConstants.UploadConstants.UPLOAD_FAILURE;
                                    /**Dismiss Dialogs(If any)**/
                                    if (uploadStatusDialog != null)
                                        uploadStatusDialog.dismiss();
                                    /**Show dialog**/
                                    uploadStatusDialog = new UploadStatusDialog(DashboardActivity.this, 10, UploadStatusDialog.DialogType.UPLOAD_ERROR, jsonElement.getMessage(), new UploadStatusClickActions() {
                                        @Override
                                        public void onButtonCick(int dialogId, int type) {
                                            uploadStatusDialog.dismiss();
                                            Bundle bundle = new Bundle();
                                            bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, false);
                                            NavigationFlowManager.openFragments(new HomeFragment(), bundle, DashboardActivity.this, R.id.dashboard_wrapper);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    public static MultipartBody.Part getFileCountArray(ArrayList<Integer> pdfURLCount) {
        if (pdfURLCount != null && pdfURLCount.size() > 0) {
            String str = Arrays.toString(pdfURLCount.toArray());
            MultipartBody.Part body = MultipartBody.Part.createFormData(ApiConstants.UploadDocuments.FILE_COUNT, str);
            return body;
        }
        return null;
    }

    private MultipartBody.Part[] getMultipartBody(ArrayList<String> pdfURIList, Uri uri, int width, int height) {

        String resultBase64Encoded = "";

        MultipartBody.Part[] list = new MultipartBody.Part[1];
//        for (int i = 0; i < pdfURIs.size(); i++) {

        try {
//                for (int i = 0; i < pdfURIList.size(); i++) {

//                    Uri fileNm = Uri.parse(pdfURIList.get(i));

//                    File newFile = new File(imagePath, "default_image.jpg");
//                    Uri contentUri = getUriForFile(DashboardActivity.this, "com.mydomain.fileprovider", newFile);

            InputStream in = getContentResolver().openInputStream(uri);
            byte[] bytes = getBytes(in);
            resultBase64Encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), resultBase64Encoded);
            String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            String fileName = timeStamp + ".pdf";
//
            MultipartBody.Part body = MultipartBody.Part.createFormData(fileName, fileName, requestFile);
            list[0] = body;
//                }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }

//        }
        return list;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void uploadFiles(String streetAddress) {
        Log.d("location", streetAddress);
        customProgressLoader.showDialog();
        uploadFileViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                bitmapsCount.clear();
                ArrayList<Integer> count = new ArrayList<>();
                for (int i = 0; i < imageUriList.size(); i++) {
                    resetFileUploads();
                    count.clear();
                    count.add(1);

                    Duke.imageStoragePath = Utilities.getPath(context, imageUriList.get(i));
                    imageBitmap = Utilities.getPortraitResizedBitmap(BitmapFactory.decodeFile(Duke.imageStoragePath), 2160, 1440); /**Temp. Solution**/
                    Duke.imageStoragePath = saveImagePath(imageBitmap);
                    Duke.uploadingImagesList.add(imageBitmap);
                    Duke.uploadingImageStoragePaths.add(Duke.imageStoragePath);

                    MultipartBody.Part[] list = new MultipartBody.Part[Duke.uploadingImageStoragePaths.size()];
                    list = Utilities.getMultipartBody(Duke.uploadingImageStoragePaths, false, screenWidth, screenHeight);

                    MultipartBody.Part fileCount = Utilities.getFileCountArray(count);

                    MultipartBody.Part address = MultipartBody.Part.createFormData("address", streetAddress);

                    if (list != null && list.length <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customProgressLoader.hideDialog();
                            }
                        });
                        return;
                    }

                    uploadFileViewModel.uploadFile(list, fileCount, address).observe(DashboardActivity.this, new Observer<FileUploadSuccessModel>() {
                        @Override
                        public void onChanged(@Nullable FileUploadSuccessModel jsonElement) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customProgressLoader.hideDialog();
                                }
                            });
                            if (jsonElement != null && jsonElement.getCode() != null && jsonElement.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
//                                resetFileUploads();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, true);
                                NavigationFlowManager.openFragments(new HomeFragment(), bundle, DashboardActivity.this, R.id.dashboard_wrapper);
                            } else {
                                if (jsonElement != null && jsonElement.getMessage() != null) {
                                    /**Dismiss Dialogs(If any)**/
                                    if (uploadStatusDialog != null)
                                        uploadStatusDialog.dismiss();
                                    /**Show dialog**/
                                    uploadStatusDialog = new UploadStatusDialog(DashboardActivity.this, 10, UploadStatusDialog.DialogType.UPLOAD_ERROR, jsonElement.getMessage(), new UploadStatusClickActions() {
                                        @Override
                                        public void onButtonCick(int dialogId, int type) {
                                            uploadStatusDialog.dismiss();
                                            Bundle bundle = new Bundle();
                                            bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, false);
                                            NavigationFlowManager.openFragments(new HomeFragment(), bundle, DashboardActivity.this, R.id.dashboard_wrapper);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }

            }
        });

    }

    private void getCurrentLocation(Intent data) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(DashboardActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.d("location fetched", addresses.get(0).getAddressLine(0));
                        if (addresses.get(0) != null && addresses.get(0).getAddressLine(0).length() > 0) {
                            address = addresses.get(0).getAddressLine(0);
                            latitude = String.valueOf(location.getLatitude());
                            longitude = String.valueOf(location.getLongitude());
                            performUploadTasks(data, address, latitude, longitude);
                        }
                    } catch (Exception e) {
                        performUploadTasks(data, "none", "", "");
                        Log.d("location not fetched", e.getLocalizedMessage());
                    }
                } else {
                    performUploadTasks(data, "none", "", "");
                }
            }
        });
        Log.d("current address", address);
    }

    public void resetFileUploads() {
        Duke.imageStoragePath = "";
        Duke.uploadingImageStoragePaths = new ArrayList<>();
        Duke.uploadingImagesList = new ArrayList<>();
        Duke.sortedUploadingImageStoragePaths = new ArrayList<>();
        Duke.sortedImagesList = new ArrayList<>();
        Duke.uploadingImageCount = new ArrayList<>();
    }

    public static boolean checkImgCorrupted(String filePath) {
        BitmapFactory.Options options = null;
        if (options == null) {
            options = new BitmapFactory.Options();
        }//  ww w. j  av a 2 s  . c om
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);
        if (options.mCancel || options.outWidth == -1
                || options.outHeight == -1) {
            return true;
        }
        return false;
    }

}
