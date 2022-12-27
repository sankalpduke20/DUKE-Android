package com.dukeai.android;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.LoadDocumentModel;
import com.dukeai.android.models.RecipientDataModel;
import com.dukeai.android.models.SelectRecipientDataModel;
import com.dukeai.android.models.SubscriptionPlan;
import com.dukeai.android.models.UserLoadsModel;
import com.dukeai.android.ui.fragments.LoadsFragment;
import com.dukeai.android.ui.fragments.SelectRecipientsDialog;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.awsauth_duke.Auth;
import com.dukeai.awsauth_duke.handlers.AuthHandler;
import com.revenuecat.purchases.Purchases;

import java.util.ArrayList;

import okhttp3.MultipartBody;

public class Duke extends Application implements LifecycleObserver {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static Duke appContext;
    public static Boolean isFromLogin = false;
    public static Boolean isFromSignUp = false;
    public static Boolean isWithOutToken = false;
    public static transient FileStatusModel fileStatusModel = new FileStatusModel();
    public static ArrayList<Bitmap> uploadingImagesList = new ArrayList<>();
    public static ArrayList<Bitmap> sortedImagesList = new ArrayList<>();
    public static ArrayList<Integer> uploadingImageCount = new ArrayList<>();
    public static ArrayList<UserLoadsModel> loadsDocuments = new ArrayList<>();
    public static ArrayList<String> uploadingImageStoragePaths = new ArrayList<>();
    public static ArrayList<String> uploadingPDFStoragePaths = new ArrayList<>();
    public static ArrayList<String> sortedUploadingImageStoragePaths = new ArrayList<>();
    public static ArrayList<String> podDocsStoragePaths = new ArrayList<>();
    public static ArrayList<RecipientDataModel> globalRecipientsList = new ArrayList<>();
    public static ArrayList<RecipientDataModel> customRecipientsList = new ArrayList<>();
    public static ArrayList<String> selectedRecipients = new ArrayList<>();
    public static ArrayList<String> selectedLoadsForTransmission = new ArrayList<>();
    public static String FileStoragePath = "";
    public static String PODDocumentsPath = "";
    public static boolean isPODReport = false;
    public static Bitmap ProfileImage;
    public static String deviceToken;
    public static String imageStoragePath = "";
    public static String profileImageStoragePath = "";
    public static String userName = "";
    public static String referralId;
    public static String isOpenWithHome = AppConstants.UserPreferencesConstants.OPEN_WITH_HOME;
    public static Boolean isFirebaseSetUp = false;
    public static String tripId = "";
    public static boolean isAutoIFTAAutoRestartInProgress = false;
    public static String tripSessionState = "";
    public static Boolean isFromSplash = false;
    public static SubscriptionPlan subscriptionPlan = new SubscriptionPlan(SubscriptionPlan.MemberStatus.FREE);
    public static Auth auth;
    public static boolean isInBackground = false;
    public static boolean isRecentCleared = false;
    public static String stateName = "";
    public static String isLoadingForFirstTime = "";
    public static String uniqueUserId = "";
    public static ArrayList<byte[]> podDocumentByteData = new ArrayList<>();
    public static ArrayList<byte[]> podDocumentByteDataImg = new ArrayList<>();
    public static boolean isLocationBeingFetched = false;
    public static boolean isLocationPermissionProvided = false;
    public static MultipartBody.Part address = MultipartBody.Part.createFormData("address", "none");
    public static boolean isLoadDocument = false;
    public static ArrayList<LoadDocumentModel> DocsOfALoad = new ArrayList<>();
    public static boolean isDocumentAddingToLoad = false;
    public static boolean isNewLoadBeingCreated = false;
    public static boolean isDocumentBeingScanned = false;
    public static String selectedLoadUUID = "";
    public static String selectedLoadSHA1 = "";
    public static ArrayList<Uri> PDFDocURIs = new ArrayList<>();
    public static ArrayList<String> PDFDocFilenames = new ArrayList<>();
    public static Uri FileStoragePathURI;
    public static LoadsFragment loadsFragment = null;
    public static Fragment fragment;
    public static SelectRecipientsDialog selectRecipientsDialog;

    public static ArrayList<SelectRecipientDataModel> selectRecipientDataForGlobalList = new ArrayList<>();
    public static ArrayList<SelectRecipientDataModel> selectRecipientDataCustomList = new ArrayList<>();
    public static String TAG = "PM=========>";

    public static String client_id = "";
    public static String api_key = "";
    public static String cust_id = "";
    public static CognitoIdToken idToken ;
    public static CognitoAccessToken accessToken;
    public static CognitoRefreshToken refreshToken ;

    public static synchronized Duke getInstance() {
        return appContext;
    }

    public static Auth getAuth(Context context, AuthHandler authHandler) {
        Auth.Builder builder = new Auth.Builder().setAppClientId(BuildConfig.appClientId)
                .setAppClientSecret(BuildConfig.appClientSecret)
                .setAppCognitoWebDomain(BuildConfig.appCognitoWebDomain)
                .setApplicationContext(context)
                .setSignInRedirect(BuildConfig.appRedirectScheme)
                .setSignOutRedirect(BuildConfig.appRedirectScheme)
                .setAuthHandler(authHandler);
        auth = builder.build();
        return auth;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AppHelper.init(appContext);
        createNotificationChannels();
        Purchases.setDebugLogsEnabled(true);
        if(BuildConfig.theme.equals("duke")){
            Purchases.configure(this, "aZcnnlsPEikLbYMmVvkRuIkKQiPBUJBB");
        } else {
            Purchases.configure(this, "YKqaPdxiTEihgMVTrkxgQZuectDrAENk");
        }
        //Duke official RevenueCat account API key for DUKE.AI aZcnnlsPEikLbYMmVvkRuIkKQiPBUJBB
        //Duke official RevenueCat account API key for TTT aZcnnlsPEikLbYMmVvkRuIkKQiPBUJBB

        //Personal RevenueCat account API key for DUKE.AI ZuKyGxJdRIierNaFpAOLyBpDFIZdxetZ
        //Personal RevenueCat account API key for TTT bWwRlLhjXvFiElltPnpZiVVIyoMFAIKA

//        Purchases.configure(this, "bWwRlLhjXvFiElltPnpZiVVIyoMFAIKA");
        //Personal RevenueCat account API key bWwRlLhjXvFiElltPnpZiVVIyoMFAIKA
//        for TTT - bWwRlLhjXvFiElltPnpZiVVIyoMFAIKA
//        for Duke - ZuKyGxJdRIierNaFpAOLyBpDFIZdxetZ
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
//        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN){
//            Log.d(TAG, "app went to background");
//            isInBackground = true;
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background
//        if(!isRecentCleared) {
//            isInBackground = true;
//            isRecentCleared = false;
//        }
    }

//   @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    public void onDestroy() {
//        Log.d("AppLog", "onDestroy");
//        isInBackground = false;
//    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("This is Channel 2");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}