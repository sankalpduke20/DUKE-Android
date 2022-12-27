package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.activities.DocumentViewerActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.DeviceTokenViewModel;
import com.dukeai.android.viewModel.VerifyUserViewModel;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class UserVerificationFragment extends Fragment implements PopupActions, HeaderActions {
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.back_btn)
    ImageView backBtn;
    View view;
    UserConfig userConfig = UserConfig.getInstance();
    VerifyUserViewModel verifyUserViewModel;
    CustomProgressLoader customProgressLoader;
    String email = "", password = "", promoCode = "";
    AuthenticationHandler authenticationHandler;
    ConfirmationComponent confirmationComponent;
    Boolean isFirstTimeLogin = true, isIncorrectCredentials = false, isUserVerified = false;
    DeviceTokenViewModel deviceTokenViewModel;
    AuthenticationDetails authenticationDetails;
    PopupActions popupActions;
    VerificationHandler resendConfCodeHandler;
    private static final int DELETE_LOAD_DIALOG_ID = 20;

    public UserVerificationFragment() {

    }

    // TODO: Rename and change types and number of parameters
    public static UserVerificationFragment newInstance() {
        UserVerificationFragment fragment = new UserVerificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_verification, container, false);
        ButterKnife.bind(this, view);
        ImageView backBtn = view.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationFlowManager.openFragments(new SignUpFragment(), null, getActivity(), R.id.main_wrapper);
            }
        });
        customProgressLoader = new CustomProgressLoader(getContext());
        setInitials();
        setCurrentTheme();
        return view;
    }

    private void setInitials() {
        verifyUserViewModel = ViewModelProviders.of(this).get(VerifyUserViewModel.class);
        deviceTokenViewModel = ViewModelProviders.of(this).get(DeviceTokenViewModel.class);
        popupActions = this;
//        setCustomHeader();
        setResendConfirmationHandler();
        setAuthenticationHandler();
        getArgumentsData();
    }

    private void setResendConfirmationHandler() {
        resendConfCodeHandler = new VerificationHandler() {
            @Override
            public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium) {
                customProgressLoader.hideDialog();
                String str = getString(R.string.code_sent_to) + " " + verificationCodeDeliveryMedium.getDestination();
                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.code_resent), str, false, getResources().getString(R.string.ok), popupActions, 1);
            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                String msg = exception.getMessage();
                if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    msg = getString(R.string.no_internet);
                }
                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getResources().getString(R.string.ok), popupActions, 1);
            }
        };
    }

    public void setCustomHeader() {
//
//        customHeader.showHideProfileImage(View.GONE);
//        customHeader.showHideHeaderTitle(View.GONE);
//        customHeader.setHeaderActions(this);
//        customHeader.setToolbarTitle("");
//
//        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
    }

    private void getArgumentsData() {
        if (getArguments() != null) {
            Bundle args = getArguments();
            email = args.getString(AppConstants.SignUpDataConstants.EMAIL);
            password = args.getString(AppConstants.SignUpDataConstants.PASSWORD);
            promoCode = args.getString(AppConstants.SignUpDataConstants.PROMO_CODE);
            UserDataModel userDataModel = new UserDataModel(email, password, promoCode, false);
            userConfig.setUserDataModel(userDataModel);
            new SaveUserPreferences().execute(userDataModel);
            PreferenceManager.saveString(Duke.getInstance(), AppConstants.UserPreferencesConstants.LOGGED_IN, null);
        }
        if (Duke.isFromSplash) {
            UserDataModel userDataModel = userConfig.getUserDataModel();
            if (userDataModel != null && userDataModel.getUserEmail() != null) {
                email = userDataModel.getUserEmail();
                password = userDataModel.getUserPassword();
                verifyUserVerification(email);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.go)
    void onClickUserVerification() {
        if (!isUserVerified) {
            verifyUserVerification(email);
        }
    }


    private void verifyUserVerification(String email) {
        Activity context = getActivity();
        confirmationComponent = new ConfirmationComponent(getContext(), "Before you proceed!", "Be sure to check your spam or junk folder and mark it as not spam?", true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
                proceedToUserVerfication();
            }
        }, 1);
    }

    private void proceedToUserVerfication() {
        verifyUserViewModel.verifyUser(email).observe(this, new Observer<ResponseModel>() {
            @Override
            public void onChanged(@Nullable ResponseModel responseModel) {
                if (responseModel != null && responseModel.getCode() != null && responseModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                    customProgressLoader.hideDialog();
                    isUserVerified = true;
                    loginUser();
                } else {
                    customProgressLoader.hideDialog();
                    confirmationComponent = new ConfirmationComponent(getContext(), "Verify account", "Your email is not yet verified!", false, "ok", popupActions, 1);
                }
            }
        });
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loginUser() {
        AppHelper.setUser(email);
        AppHelper.setPasswordForFirstTimeLogin(password);
        AppHelper.getPool().getUser(email).getSessionInBackground(authenticationHandler);
    }

    private void setAuthenticationHandler() {
        authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                if (isFirstTimeLogin) {
                    AppHelper.setCurrSession(userSession);
                    UserDataModel userDataModel = new UserDataModel(userSession, email, password);
                    userConfig.setUserDataModel(userDataModel);
                    new SaveUserPreferences().execute(userDataModel);
                    loginSecondTime();
                } else {
                    AppHelper.setCurrSession(userSession);
                    Duke.isFromLogin = true;
                    Duke.isFromSignUp = true;
                    UserDataModel userDataModel = new UserDataModel(userSession, email, password);
//                    Duke.isFirebaseSetUp = true;
//                    if (Duke.isFirebaseSetUp) {
//                        // Firebase: Setup
//                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
//                        // Firebase: Send just registered user information
//                        Bundle params = new Bundle();
//                        params.putString("UserEmail", userDataModel.getUserName());
//                        mFirebaseAnalytics.logEvent("JustRegistered", params);
//                        Duke.isFirebaseSetUp = false;
//                    }
                    new SaveUserPreferences().execute(userDataModel);
                    customProgressLoader.hideDialog();
                    NavigationFlowManager.openDashboardActivity(getActivity());
                }
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                getUserAuthentication(authenticationContinuation);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                String msg = exception.getMessage();
                String buttonLabel = getString(R.string.ok);
                if (exception.getMessage().contains(AppConstants.LoginConstants.USER_NOT_CONFIRMED)) {
                    msg = getString(R.string.user_not_confirmed);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    msg = getString(R.string.no_internet);
                } else {
                    msg = getString(R.string.password_invalid_please_login);
                    buttonLabel = getString(R.string.login);
                    isIncorrectCredentials = true;
                }
                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, buttonLabel, popupActions, 1);
            }
        };
    }

    private void getUserAuthentication(AuthenticationContinuation authenticationContinuation) {
        authenticationDetails = new AuthenticationDetails(email, password, null);
        authenticationContinuation.setAuthenticationDetails(authenticationDetails);
        authenticationContinuation.continueTask();
    }

    private void loginSecondTime() {
        JsonObject jsonObject = InputParams.updateDeviceToken("none", Duke.referralId);
        deviceTokenViewModel.updateDeviceToken(jsonObject).observe(this, new Observer<DeviceTokenModel>() {
            @Override
            public void onChanged(@Nullable DeviceTokenModel deviceTokenModel) {
                if (deviceTokenModel != null && deviceTokenModel.getCode() != null && deviceTokenModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CognitoUser user = AppHelper.getPool().getUser(AppHelper.getCurrUser());
                            user.signOut();
                            AppHelper.setUser(email);
                            isFirstTimeLogin = false;
                            //Login from here
                            AppHelper.getPool().getUser(email).getSessionInBackground(authenticationHandler);
                        }
                    }, 6000);
                } else {
                    //Something went wrong - Handle this case.
                    customProgressLoader.hideDialog();
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.your_email_verified_successfully), false, getString(R.string.login), popupActions, 1);
                    isIncorrectCredentials = true;
                }
            }
        });
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
        }
    }

    @OnClick(R.id.resend)
    void resendVerificationCode() {
        customProgressLoader.showDialog();
        resendConfirmationLinkInBackground();
    }

    private void resendConfirmationLinkInBackground() {
        if (!email.equals("") && resendConfCodeHandler != null) {
            AppHelper.getPool().getUser(email).resendConfirmationCodeInBackground(resendConfCodeHandler);
        } else {
            confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, 1);
        }
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        Log.i("backclicked", "backed");
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//
//
//        // Get fragment one if exist.
//        Fragment fragmentOne = new SignUpFragment();
//
//        fragmentTransaction.replace(R.id.verfication_layout, fragmentOne, "Fragment One");
//
//        // Do not add fragment three in back stack.
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
        NavigationFlowManager.openFragments(new SignUpFragment(), null, getActivity(), R.id.main_wrapper);


    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        logo.setImageResource(changeThemeModel.getLogo());
        backBtn.setColorFilter(Color.parseColor(changeThemeModel.getHeaderBackButtonColor()));

        if (!AppConstants.currentTheme.equals("duke")) {
            logo.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
