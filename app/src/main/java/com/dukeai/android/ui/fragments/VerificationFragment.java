package com.dukeai.android.ui.fragments;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.InputValidators;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.viewModel.DeviceTokenViewModel;
import com.dukeai.android.viewModel.PromoCodeViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup


public class VerificationFragment extends Fragment implements PopupActions {

    View sendVerificationView;
    GenericHandler confHandler;
    @BindView(R.id.send_code)
    CustomEditInputField confirmCode;
    @BindView(R.id.verification_code_text)
    TextView verificationCodeText;
    @BindView(R.id.promo_code_field)
    TextInputEditText refferalIdField;
    @BindView(R.id.promo_code_error)
    TextView refferalIdError;
    @BindView(R.id.logo)
    ImageView logo;
    AuthenticationHandler authenticationHandler;
    AuthenticationDetails authenticationDetails;
    String userName, email, phoneNumber, password;
    CustomProgressLoader customProgressLoader;
    VerificationHandler resendConfCodeHandler;
    UserConfig userConfig = UserConfig.getInstance();
    ConfirmationComponent confirmationComponent, promoCodeComponent;
    PopupActions popupActions;
    Boolean isFirstTimeLogin = true;
    DeviceTokenViewModel deviceTokenViewModel;
    LifecycleOwner owner;
    PromoCodeViewModel promoCodeViewModel;
    String referralID = "none";
    boolean isIncorrectCredentials = false;
    boolean isFromSignup = false;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    private OnFragmentInteractionListener mListener;

    public VerificationFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VerificationFragment newInstance(String param1, String param2) {
        VerificationFragment fragment = new VerificationFragment();
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
        sendVerificationView = inflater.inflate(R.layout.fragment_send_verification, container, false);
        ButterKnife.bind(this, sendVerificationView);
        customProgressLoader = new CustomProgressLoader(getContext());
        deviceTokenViewModel = ViewModelProviders.of(this).get(DeviceTokenViewModel.class);
        promoCodeViewModel = ViewModelProviders.of(this).get(PromoCodeViewModel.class);
        popupActions = this;
        owner = this;
        setCurrentTheme();
        setAuthenticationHandler();
        setConfirmationHandler();
        setResendConfirmationHanlder();
        getBundleArguments();
        setTextOnChangeListener();
        return sendVerificationView;
    }

    private void setTextOnChangeListener() {
        refferalIdField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String promoCode = refferalIdField.getText().toString();
                    if (promoCode.isEmpty() || promoCode.length() == 10) {
                        refferalIdError.setVisibility(View.GONE);
                    }
                }
            }
        });
        refferalIdField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String promoCode = refferalIdField.getText().toString();
                if (promoCode.isEmpty() || promoCode.length() == 10) {
                    refferalIdError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setResendConfirmationHanlder() {
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


    private void getUserAuthentication(AuthenticationContinuation continuation) {
        authenticationDetails = new AuthenticationDetails(email, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    private void loginSecondTime() {

        JsonObject jsonObject = InputParams.updateDeviceToken("none", Duke.referralId);
        deviceTokenViewModel.updateDeviceToken(jsonObject).observe(owner, new Observer<DeviceTokenModel>() {
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

    private void getBundleArguments() {
        Bundle args = getArguments();
        if (args != null && args.getString(AppConstants.SignUpDataConstants.EMAIL) != null) {
            isFromSignup = true;
            email = args.getString(AppConstants.SignUpDataConstants.EMAIL).toUpperCase();
            //Setting Textview from here
            verificationCodeText.setText(String.format("%s %s", getString(R.string.enter_the_verification_code_sent_at), email));
            userName = args.getString(AppConstants.SignUpDataConstants.USER_NAME);
            phoneNumber = args.getString(AppConstants.SignUpDataConstants.PHONE_NUMBER);
            password = args.getString(AppConstants.SignUpDataConstants.PASSWORD);
        } else if (args != null && args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL) != null) {
            email = args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL).toUpperCase();
            verificationCodeText.setText(String.format("%s %s", getString(R.string.enter_the_verification_code_sent_at), email));
            password = args.getString(AppConstants.VerificationCodeConstants.LOGIN_PASSWORD);
//            resendConfirmationCodeInBackground();
        }
    }

    private void resendConfirmationCodeInBackground() {
        if (!email.equals("") && resendConfCodeHandler != null) {
            AppHelper.getPool().getUser(email).resendConfirmationCodeInBackground(resendConfCodeHandler);
        } else {
            confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, 1);
        }
    }

    private void setConfirmationHandler() {
        confHandler = new GenericHandler() {
            @Override
            public void onSuccess() {
                customProgressLoader.hideDialog();
                loginUser();
            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                String msg = exception.getMessage();
                String buttonLabel = getString(R.string.ok);
                if (exception.getMessage().contains(AppConstants.VerificationCodeConstants.INVALID_VERIFICATION)) {
                    msg = getString(R.string.incorrect_verification);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    msg = getString(R.string.no_internet);
                } else if (exception.getMessage().contains("User cannot be confirm. Current status is CONFIRMED")) {
                    msg = "Your email is already verified. Please try login.";
                    isIncorrectCredentials = true;
                    buttonLabel = getString(R.string.login);
                }
                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, buttonLabel, popupActions, 1);
            }
        };

    }

    private void loginUser() {
        customProgressLoader.showDialog();
        AppHelper.setUser(email);
        AppHelper.setPasswordForFirstTimeLogin(password);
        AppHelper.getPool().getUser(email).getSessionInBackground(authenticationHandler);
    }

    private boolean validatePromoCode() {
        Boolean isValid = false;
        referralID = refferalIdField.getText().toString();
        if (referralID.isEmpty() || InputValidators.validateInput(AppConstants.StringConstants.EMAIL, referralID)) {
            refferalIdError.setVisibility(View.GONE);
            isValid = true;
        } else {
            refferalIdError.setVisibility(View.VISIBLE);
            isValid = false;
            Bundle params = new Bundle();
            params.putString("Description", "Referral ID is not valid");
            mFirebaseAnalytics.logEvent("Error", params);
        }
        return isValid;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    void confirmSignUpInBackground() {
        String code = confirmCode.getText().toString();
        customProgressLoader.showDialog();
        AppHelper.getPool().getUser(email).confirmSignUpInBackground(code, true, confHandler);
    }

    public void verifyReferralId() {
        customProgressLoader.showDialog();
        final String promoCode = refferalIdField.getText().toString();
        JsonObject jsonObject = InputParams.updatePromoCode(promoCode);
        Duke.isWithOutToken = true;
        promoCodeViewModel.updateReferralId(email.toUpperCase(), jsonObject).observe(this, new Observer<DeviceTokenModel>() {
            @Override
            public void onChanged(@Nullable DeviceTokenModel deviceTokenModel) {
                customProgressLoader.hideDialog();
                if (deviceTokenModel != null && deviceTokenModel.getCode() != null && deviceTokenModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                    Duke.referralId = promoCode;
                    referralID = promoCode;
                    PreferenceManager.saveString(getContext(), AppConstants.UserPreferencesConstants.REFERRAL_ID, Duke.referralId);
                    confirmSignUpInBackground();
                } else if (deviceTokenModel != null && deviceTokenModel.getMessage() != null) {
                    String msg = deviceTokenModel.getMessage();
                    if (deviceTokenModel.getMessage().contains(getString(R.string.Promocode_not_valid))) {
                        msg = getString(R.string.referral_id_not_valid);
                        promoCodeComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.proceed), getString(R.string.try_again), popupActions, 1);
                    } else {
                        promoCodeComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.ok), popupActions, 1);
                    }
                }
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
        }
        if (promoCodeComponent != null) {
            promoCodeComponent.dismiss();
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    confirmSignUpInBackground();
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    break;
                case AppConstants.PopupConstants.NEUTRAL:
                    ProceedToLogin();
                    break;
            }
        } else {
            switch (id) {
                case AppConstants.PopupConstants.NEUTRAL:
                    ProceedToLogin();
                    break;
            }
        }
    }

    private void ProceedToLogin() {
        if (isIncorrectCredentials) {
            isIncorrectCredentials = false;
            Bundle args = new Bundle();
            args.putString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL, email);
            NavigationFlowManager.openFragments(new LoginFragment(), args, getActivity(), R.id.main_wrapper);
        }
    }

    @OnClick(R.id.button)
    void doVerificationCode() {
        boolean isCodeValid = confirmCode.validateField(confirmCode);
        boolean isPromoCodeValid = validatePromoCode();
        if (isCodeValid && isPromoCodeValid) {
            if (InputValidators.validateInput(AppConstants.StringConstants.EMAIL, referralID)) {
                verifyReferralId();
            } else {
                confirmSignUpInBackground();
            }
        }
    }

    @OnClick(R.id.resend)
    void resendVerificationCode() {
        customProgressLoader.showDialog();
        resendConfirmationCodeInBackground();
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        logo.setImageResource(changeThemeModel.getLogo());

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
