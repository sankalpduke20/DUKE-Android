package com.dukeai.android.ui.activities;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.dukeai.android.BuildConfig;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.fragments.ForgotPassword;
import com.dukeai.android.ui.fragments.LoginFragment;
import com.dukeai.android.ui.fragments.SignUpFragment;
import com.dukeai.android.ui.fragments.UserVerificationFragment;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.DeviceTokenViewModel;
import com.dukeai.android.viewModel.UserRegistrationViewModel;
import com.dukeai.awsauth_duke.Auth;
import com.dukeai.awsauth_duke.AuthUserSession;
import com.dukeai.awsauth_duke.handlers.AuthHandler;
import com.facebook.FacebookSdk;
import com.google.gson.JsonObject;

public class MainActivity
        extends BaseActivity
        implements LoginFragment.OnSocialLoginInteractionListener, SignUpFragment.OnSocialLoginInteractionListener, ForgotPassword.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    public static boolean shouldShowProgressLoader = false;
    Context context;
    Boolean toLogin = false, toVerification = false;
    CustomProgressLoader progressLoader;
    UserRegistrationViewModel registrationViewModel;
    ConfirmationComponent alertDialog;
    boolean shouldExecuteFirstTime = true;
    DeviceTokenViewModel deviceTokenViewModel;
    private Auth auth;
    private Uri appRedirect;

    //    @BindView(R.id.parent_layout)
//    RelativeLayout parentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCognito();
        FacebookSdk.setAutoLogAppEventsEnabled(true);
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();
//        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        progressLoader = new CustomProgressLoader(MainActivity.this);
        context = this;
        getIntentData();
        loadFragments();
        setCurrentTheme();
        registrationViewModel = ViewModelProviders.of(this).get(UserRegistrationViewModel.class);
        deviceTokenViewModel = ViewModelProviders.of(this).get(DeviceTokenViewModel.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent activityIntent = getIntent();
        //  -- Call Auth.getTokens() to get Cognito JWT --
        if (activityIntent.getData() != null &&
                appRedirect.getHost().equals(activityIntent.getData().getHost())) {
            auth.getTokens(activityIntent.getData());
        }
        /**
         * Intended for Showing activity indicator.
         * To Show Progress indicator after the user is navigated back to App (From Social Login)
         * **/
        if (progressLoader != null && shouldShowProgressLoader) {
            shouldShowProgressLoader = false;
            progressLoader.showDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressIndicator();
    }

    private void dismissProgressIndicator() {
        if (progressLoader != null && progressLoader.isShowing()) {
            progressLoader.dismiss();
        }
    }

    /**
     * Setup authentication with Cognito.
     */
    void initCognito() {
        try {
            //  -- Create an instance of Auth --
            auth = Duke.getAuth(getApplicationContext(), new AuthHandler() {
                @Override
                public void onSuccess(final AuthUserSession session) {
                    shouldShowProgressLoader = true;
                    if (shouldExecuteFirstTime) {
                        shouldExecuteFirstTime = false;
                        UserDataModel userDataModel = new UserDataModel(session);
                        UserConfig.getInstance().setUserDataModel(userDataModel);
                        new SaveUserPreferences().execute(userDataModel);
                        AppHelper.setUser(userDataModel.getUserEmail());
                        JsonObject payload = InputParams.updateUserGroup(session.getUsername());
                        registrationViewModel.updateUserGroup(userDataModel.getUserEmail(), payload).observe(MainActivity.this, new Observer<ResponseModel>() {
                            @Override
                            public void onChanged(@Nullable ResponseModel responseModel) {
                                if (responseModel != null && responseModel.getCode() != null && responseModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                                    /**Mark:- Success
                                     * Refresh The Access Token
                                     * */
                                    auth.getRefreshSession();
                                } else {
                                    /**Failure**/
                                    showErrorDialog();
                                }
                            }
                        });
                    } else {
                        /**This Section Should be Executed Second Time With new JWT Token
                         * Update Device Token
                         * Take User to Home Screen
                         * **/
                        UserDataModel userDataModel = new UserDataModel(session);
                        UserConfig.getInstance().setUserDataModel(userDataModel);
                        new SaveUserPreferences().execute(userDataModel);
                        JsonObject jsonObject = InputParams.updateDeviceToken("none", null);
                        deviceTokenViewModel.updateDeviceToken(jsonObject).observe(MainActivity.this, new Observer<DeviceTokenModel>() {
                            @Override
                            public void onChanged(@Nullable DeviceTokenModel deviceTokenModel) {
                                if (deviceTokenModel != null && deviceTokenModel.getCode() != null && deviceTokenModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                                    Log.d(TAG, "onChanged: ");
                                    NavigationFlowManager.openDashboardActivity(MainActivity.this);
                                } else {
                                    //Something went wrong - Handle this case.
                                    Log.d(TAG, "onChanged: ");
                                    showErrorDialog();
                                }
                            }
                        });
                    }
//                    AsyncTask.execute(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
                }

                @Override
                public void onSignout() {
                    Log.d(TAG, "onSignout: ");
                    shouldShowProgressLoader = false;
                    dismissProgressIndicator();
                    Utilities.clearUserData(MainActivity.this);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "onFailure: ");
                    Toast.makeText(MainActivity.this, e+"", Toast.LENGTH_SHORT).show();
                    shouldShowProgressLoader = false;
                    dismissProgressIndicator();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            appRedirect = Uri.parse(BuildConfig.appRedirectScheme);
        }catch (Exception ex){
            Log.e("MainActivity-201: Exception: ",ex+"");
        }
    }

    private void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                                            progressLoader.hideDialog();
                alertDialog = new ConfirmationComponent(getBaseContext(), "Error!", getResources().getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, "ok", new PopupActions() {
                    @Override
                    public void onPopupActions(String id, int dialogId) {
                        alertDialog.dismiss();
                    }
                }, 1);
            }
        });
    }


    private void getIntentData() {
        Intent intent = getIntent();
        toLogin = intent.getBooleanExtra(AppConstants.LoginConstants.TO_LOGIN, false);
        toVerification = intent.getBooleanExtra(AppConstants.LoginConstants.TO_VERIFICATION, false);
    }

    private void loadFragments() {
        if (toLogin) {
            if (toVerification) {
                NavigationFlowManager.openFragments(new UserVerificationFragment(), null, this, R.id.main_wrapper);
            } else {
                NavigationFlowManager.openFragments(new LoginFragment(), null, this, R.id.main_wrapper);
            }
        } else {
            SignUpFragment signUpFragment = new SignUpFragment();
            NavigationFlowManager.openFragments(signUpFragment, null, this, R.id.main_wrapper);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackLength = fm.getBackStackEntryCount();
        Fragment topFragment = getSupportFragmentManager().findFragmentById(R.id.main_wrapper);
        if ((topFragment instanceof LoginFragment) || (topFragment instanceof SignUpFragment)) {
            closeApp();
        } else if (backStackLength > 1) {
            fm.popBackStack();
        }
    }

    private void closeApp() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        findViewById(R.id.parent_layout).setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }

    @Override
    public void onSocialLoginButtonPress() {
        if (auth != null) {
            this.auth.getSession();
            shouldShowProgressLoader = true;
        }
    }

    @Override
    public void onFragmentInteraction(String data) {
        /**
         * Response from Forgot Password Fragment to dismiss the Progress Indicator
         * */
        if (progressLoader != null && progressLoader.isShowing()) {
            progressLoader.dismiss();
            shouldShowProgressLoader = false;
        }
    }
}
