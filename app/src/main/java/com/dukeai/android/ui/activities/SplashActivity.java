package com.dukeai.android.ui.activities;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dukeai.android.Duke;
import com.dukeai.android.OnClearFromRecentService;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.AppUpdateStatusModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.AppUpdateViewModel;
import com.dukeai.android.viewModel.VerifyUserViewModel;
import com.google.gson.Gson;

import butterknife.BindView;
import pl.droidsonroids.gif.GifImageView;

public class SplashActivity extends BaseActivity implements PopupActions {

    String userLoggedIn;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;
    VerifyUserViewModel verifyUserViewModel;
    Activity activity;
    ConfirmationComponent confirmationComponent;
    ProgressBar progressBar;
    @BindView(R.id.splash_layout)
    RelativeLayout splashLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.launcher_progressBar);
        setCurrentTheme();
        setInitials();
//        setCurrentTheme();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**Check App Update*/
        checkAppUpdates();
    }

    private void setInitials() {
        verifyUserViewModel = ViewModelProviders.of(this).get(VerifyUserViewModel.class);
        activity = this;
        setFullPage();
        splash();
        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
    }

    private void setFullPage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void splash() {
        int SPLASH_DISPLAY_LENGTH = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void checkAppUpdates() {
        progressBar.setVisibility(View.VISIBLE);
        AppUpdateViewModel appUpdateViewModel = new AppUpdateViewModel();
        appUpdateViewModel.getAppUpdateStatus().observe(SplashActivity.this, new Observer<AppUpdateStatusModel>() {
            @Override
            public void onChanged(@Nullable AppUpdateStatusModel appUpdateStatusModel) {
                progressBar.setVisibility(View.GONE);
                if (appUpdateStatusModel != null && appUpdateStatusModel.getUpdateType() != null) {
                    switch (appUpdateStatusModel.getUpdateType()) {
                        case "FORCE":
                            dismissConfirmationComponent();
                            confirmationComponent = new ConfirmationComponent(SplashActivity.this, "Update Required", "We've released a new version of Duke.ai. This version is no longer supported, please download the update from the Play Store.", false, "Open Store", SplashActivity.this, 15);
                            break;
                        case "OPTIONAL":
                            dismissConfirmationComponent();
                            confirmationComponent = new ConfirmationComponent(SplashActivity.this, "Update Available", "We've released a new version of Duke.ai. Download the update from the Play Store.", false, "Update", "Cancel", SplashActivity.this, 16);
                            break;
                        default:
                            Log.d("SPLASH", "onChanged: ");
                            navigateUser();
                            break;
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    /**Something Went Wrong with API call**/
                    navigateUser();
                }
            }
        });
    }

    private void navigateUser() {

        userLoggedIn = PreferenceManager.getString(Duke.getInstance(), AppConstants.UserPreferencesConstants.LOGGED_IN, null);
        Duke.isFromSignUp = false;
        if (userLoggedIn == null) {
            /**First Time User - Login/Registration required**/
            Duke.isFromLogin = true;
            Duke.isFromSplash = true;
            if (userConfig != null && userConfig.getUserDataModel() != null) {
                userDataModel = userConfig.getUserDataModel();
            } else {
                String userSessionString = PreferenceManager.getString(this, AppConstants.UserPreferencesConstants.USER_SESSION, null);
                userDataModel = new Gson().fromJson(userSessionString, UserDataModel.class);
            }
            userConfig.setUserDataModel(userDataModel);
            if (userDataModel != null && userDataModel.getUserEmail() != null) {
                NavigationFlowManager.openMainActivity(activity, true, true);
            } else {
                NavigationFlowManager.openMainActivity(this, false);
            }
        } else {
            /**User Already logged in**/
            Duke.isFromLogin = false;
            if (userConfig != null && userConfig.getUserDataModel() != null) {
                userDataModel = userConfig.getUserDataModel();
            } else {
                String userSessionString = PreferenceManager.getString(this, AppConstants.UserPreferencesConstants.USER_SESSION, null);
                userDataModel = new Gson().fromJson(userSessionString, UserDataModel.class);
            }
            if (userDataModel == null || userDataModel.getUserEmail() == null) {
                /**Something is not going in the right direction.
                 * Clear user data.
                 * Logout User. Show Login screen.
                 * **/
                Utilities.clearUserData(SplashActivity.this);
                NavigationFlowManager.openMainActivity(this, true);
                return;
            }
            Duke.deviceToken = PreferenceManager.getString(Duke.getInstance(), AppConstants.UserPreferencesConstants.DEVICE_TOKEN, null);
            Duke.referralId = PreferenceManager.getString(Duke.getInstance(), AppConstants.UserPreferencesConstants.REFERRAL_ID, "none");
            Duke.isOpenWithHome = PreferenceManager.getString(Duke.getInstance(), AppConstants.UserPreferencesConstants.OPEN_WITH, null);
            userConfig.setUserDataModel(userDataModel);
            NavigationFlowManager.openDashboardActivity(this);
        }
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        dismissConfirmationComponent();
        if (id.equals(AppConstants.PopupConstants.NEUTRAL) || id.equals(AppConstants.PopupConstants.POSITIVE)) {
            try {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            } catch (Exception e) {
                //Handle with Alert View
            }
        } else {
            navigateUser();
        }
    }

    private void dismissConfirmationComponent() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//        findViewById(R.id.parent_layout).setBackgroundColor(Color.BLACK);
        findViewById(R.id.splash_layout).setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
        ImageView logo = findViewById(R.id.logo);
        logo.setImageResource(changeThemeModel.getLogo());
        GifImageView gifView = findViewById(R.id.gifImageView);
        if (!AppConstants.currentTheme.equals("duke")) {
            logo.setVisibility(View.VISIBLE);
            logo.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            gifView.setImageResource(R.drawable.splash_screen_truck);
        }
    }
}
