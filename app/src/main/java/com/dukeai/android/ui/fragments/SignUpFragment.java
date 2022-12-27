package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
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
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.viewModel.PromoCodeViewModel;
import com.dukeai.android.viewModel.UserRegistrationViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomTextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SignUpFragment extends Fragment implements PopupActions {

    OnSocialLoginInteractionListener mListener;
    SignUpHandler signUpHandler;
    View signInView;
    String theme = AppConstants.currentTheme;

    @BindView(R.id.username_field)
    TextInputEditText usernameField;
    @BindView(R.id.phone_field)
    CustomEditInputField phoneField;
    @BindView(R.id.email_field)
    CustomEditInputField emailField;
    @BindView(R.id.password_field)
    CustomEditInputField passwordField;
    @BindView(R.id.promo_code_field)
    CustomEditInputField referralIdField;
    @BindView(R.id.promo)
    CustomTextInputLayout referralLayout;
    @BindView(R.id.login_tab)
    Button loginTab;
    @BindView(R.id.register_tab)
    Button registerTab;
    @BindView(R.id.register_underline)
    View registerUnderline;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.social_login_info_text)
    TextView socialLoginInfoTextView;

    String email = "", phoneNumber = "", password = "", username = "", referralId = "";
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent, promoCodeComponent;
    PopupActions popupActions;
    UserRegistrationViewModel userRegistrationViewModel;
    boolean isInWaitingPeriod = false;
    PromoCodeViewModel promoCodeViewModel;

    public SignUpFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
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
        signInView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        ButterKnife.bind(this, signInView);
        popupActions = this;
        setInitials();
        getBundleArguments();
        setCurrentTheme();
        socialLoginInfoTextView.setText(R.string.or_register_with);
        return signInView;
    }

    private void setInitials() {
        customProgressLoader = new CustomProgressLoader(getContext());
        setSignUpHandler();
        setTextChangeListener();
        userRegistrationViewModel = ViewModelProviders.of(getActivity()).get(UserRegistrationViewModel.class);
        promoCodeViewModel = ViewModelProviders.of(getActivity()).get(PromoCodeViewModel.class);
    }

    private void getBundleArguments() {
        Bundle args = getArguments();
        if (args != null && args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL) != null) {
            email = args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL);
            emailField.setText(email);
        }
    }

    private void setTextChangeListener() {
        phoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String phone = phoneField.getText().toString().trim();
                if (hasFocus) {
                    if (phone.isEmpty()) {
                        phoneField.setText("+1");
                    }
                } else {
                    if (phone.equals("+1")) {
                        phoneField.setText("");
                    }
                }
            }
        });
    }

    private void setSignUpHandler() {
        signUpHandler = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                if (signUpConfirmationState) {
                    NavigationFlowManager.openDashboardActivity(getActivity());
                } else {
                    // First time user sign up - Take user to Verification Screen
                    confirmSignUp(cognitoUserCodeDeliveryDetails);
                }
                customProgressLoader.hideDialog();
            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                if (exception.getMessage().contains(getString(R.string.already_exists_user))) {
                    //User is trying to register again - IF it is an Unconfirmed user, delete account and register again.
                    deleteUnconfirmedUser();
                } else if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    String msg = getString(R.string.no_internet);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.ok), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.PASSWORD_DID_NOT_CONFIRM)) {
                    String msg = getString(R.string.invalid_password);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.ok), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.INVALID_PHONE_NUMBER_FORMAT)) {
                    String msg = getString(R.string.invalid_phone_number_format);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.ok), popupActions, 1);
                } else {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), exception.getMessage(), false, getString(R.string.ok), popupActions, 1);
                }
            }
        };
    }

    private void deleteUnconfirmedUser() {
        customProgressLoader.showDialog();
        userRegistrationViewModel.deleteUnconfirmedUser(email).observe(this, new Observer<ResponseModel>() {
            @Override
            public void onChanged(@Nullable ResponseModel responseModel) {
                customProgressLoader.hideDialog();
                if (responseModel != null && responseModel.getMessage() != null) {
                    if (responseModel.getCode().equals("Success") && responseModel.getMessage().contains("Deleted unconfirmed user")) {
                        setUpUserAttributes();
                    } else if (responseModel.getMessage().contains("User is still in period")) {
                        isInWaitingPeriod = true;
                        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getContext().getString(R.string.you_ve_tried_registration_recently), false, getString(R.string.verify), getString(R.string.cancel), popupActions, 1);
                    } else if (responseModel.getMessage().contains("User Not Found")) {
                        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getContext().getString(R.string.something_went_wrong_please_try_after_some_time), false, getString(R.string.ok), popupActions, 1);
                    } else if (responseModel.getMessage().contains("User is confirmed")) {
                        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.account_exists), getContext().getString(R.string.you_already_have_an_existing_account) + email + getContext().getString(R.string.please_try_login), false, getString(R.string.ok), popupActions, 1);
                    }
                } else if (responseModel != null && responseModel.getMessage() == null) {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getContext().getString(R.string.something_went_wrong_please_try_after_some_time), false, getString(R.string.ok), popupActions, 1);
                } else {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getContext().getString(R.string.something_went_wrong_please_try_after_some_time), false, getString(R.string.ok), popupActions, 1);
                }
            }
        });
    }

    private void confirmSignUp(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
        Bundle args = new Bundle();
        args.putString(AppConstants.SignUpDataConstants.PROMO_CODE, referralId);
        args.putString(AppConstants.SignUpDataConstants.EMAIL, email);
        args.putString(AppConstants.SignUpDataConstants.USER_NAME, username);
        args.putString(AppConstants.SignUpDataConstants.PHONE_NUMBER, phoneNumber);
        args.putString(AppConstants.SignUpDataConstants.PASSWORD, password);
        Duke.isFromSplash = false;
        NavigationFlowManager.openFragments(new UserVerificationFragment(), args, getActivity(), R.id.main_wrapper);
    }

    private void setUserValues() {
        username = usernameField.getText().toString().trim();
        email = emailField.getText().toString().toUpperCase().trim();
        phoneNumber = phoneField.getText().toString().trim();
        password = passwordField.getText().toString().trim();
        if (username.equals("")) {
            username = email;
        }
    }

    public String getAppType() {
        String appType = "";
        if (theme.equals("duke")) {
            appType = "DUKE";
        } else if (theme.equals("truck")) {
            appType = "TruckerTaxTools";
        }
        return appType;
    }

    private void setUpUserAttributes() {
        setUserValues();
        customProgressLoader.showDialog();
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute(AppHelper.getSignUpFieldsC2O().get(ApiConstants.SignUpApi.NAME), username);
        userAttributes.addAttribute(AppHelper.getSignUpFieldsC2O().get(ApiConstants.SignUpApi.EMAIL), email);
        userAttributes.addAttribute(AppHelper.getSignUpFieldsC2O().get(ApiConstants.SignUpApi.PHONE_NUMBER), phoneNumber);
        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("dev"))
            userAttributes.addAttribute(ApiConstants.SignUpApi.APP_TYPE, getAppType());
        AppHelper.getPool().signUpInBackground(email, password, userAttributes, null, signUpHandler);
    }

    private boolean validatePhone(String num) {
        if(!Pattern.matches("[a-zA-Z]+", num)) {
            return num.length() > 6 && num.length() <= 13;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSocialLoginInteractionListener) {
            mListener = (OnSocialLoginInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        if (confirmationComponent != null) {
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    if (isInWaitingPeriod) {
                        isInWaitingPeriod = false;
                        Bundle args = new Bundle();
                        args.putString(AppConstants.SignUpDataConstants.EMAIL, emailField.getText().toString().toUpperCase());
                        args.putString(AppConstants.SignUpDataConstants.PASSWORD, passwordField.getText().toString());
                        args.putString(AppConstants.SignUpDataConstants.PROMO_CODE, referralIdField.getText().toString());
                        NavigationFlowManager.openFragments(new UserVerificationFragment(), args, getActivity(), R.id.main_wrapper);
                    } else {
                        Bundle args = new Bundle();
                        email = emailField.getText().toString();
                        args.putString(AppConstants.ForgotEmail.EMAIL, email);
                        NavigationFlowManager.openFragments(new ForgotPassword(), args, getActivity(), R.id.main_wrapper);
                    }
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    break;
                case AppConstants.PopupConstants.NEUTRAL:
                    break;
            }
            confirmationComponent.dismiss();
        }

        if (promoCodeComponent != null) {
            promoCodeComponent.dismiss();
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    setUpUserAttributes();
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    break;
                case AppConstants.PopupConstants.NEUTRAL:
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
        NavigationFlowManager.openFragments(new LoginFragment(), null, getActivity(), R.id.main_wrapper);
    }

    @OnClick(R.id.login_tab)
    public void displayLoginFragment() {
        NavigationFlowManager.openFragments(new LoginFragment(), null, getActivity(), R.id.main_wrapper);
    }

    @OnClick(R.id.register_send_button)
    public void sendVerificationCode() {
        boolean isEmailValid = emailField.validateField(emailField);
        boolean isPasswordValid = passwordField.validateField(passwordField);
        boolean isPhoneNumberValid = phoneField.validateField(phoneField);
        boolean isReferralIdValid = referralIdField.validateField(referralIdField);
        String id = referralIdField.getText().toString();

        if (isReferralIdValid && isEmailValid && isPasswordValid && isPhoneNumberValid) {
            if (id != null && !id.isEmpty()) {
                verifyReferralId();
            } else {
                setUpUserAttributes();
            }
        }
    }

    @OnClick(R.id.social_icon_section)
    void onSocialLoginClick() {
        mListener.onSocialLoginButtonPress();
    }

    public void verifyReferralId() {
        customProgressLoader.showDialog();
        final String promoCode = referralIdField.getText().toString();
        JsonObject jsonObject = InputParams.updatePromoCode(promoCode);
        Duke.isWithOutToken = true;
        promoCodeViewModel.updateReferralId(email.toUpperCase(), jsonObject).observe(this, new Observer<DeviceTokenModel>() {
            @Override
            public void onChanged(@Nullable DeviceTokenModel deviceTokenModel) {
                if (deviceTokenModel != null && deviceTokenModel.getCode() != null && deviceTokenModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                    Duke.referralId = promoCode;
                    referralId = promoCode;
                    PreferenceManager.saveString(getContext(), AppConstants.UserPreferencesConstants.REFERRAL_ID, Duke.referralId);
                    setUpUserAttributes();
                } else if (deviceTokenModel != null && deviceTokenModel.getMessage() != null) {
                    String msg = deviceTokenModel.getMessage();
                    if (deviceTokenModel.getMessage().contains(getString(R.string.Promocode_not_valid))) {
                        msg = getString(R.string.referral_id_not_valid);
                        promoCodeComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.proceed), getString(R.string.try_again), popupActions, 1);

                        customProgressLoader.dismiss();
                    }else if (deviceTokenModel.getMessage().contains(getString(R.string.Invalid_PromoCode))) {
                        msg = getString(R.string.Invalid_PromoCode);
                        promoCodeComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.proceed), getString(R.string.try_again), popupActions, 1);

                        customProgressLoader.dismiss();
                    }else {
                        promoCodeComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getString(R.string.ok), popupActions, 1);

                        customProgressLoader.dismiss();
                    }
                }
            }
        });

    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        loginTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        registerTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        registerUnderline.setBackgroundColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        logo.setImageResource(changeThemeModel.getLogo());


        Drawable referal = referralIdField.getResources().getDrawable(R.drawable.ic_referral);
        Drawable drawable = DrawableCompat.wrap(referal);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        Drawable username = usernameField.getResources().getDrawable(R.drawable.ic_username);
        Drawable drawable1 = DrawableCompat.wrap(username);
        DrawableCompat.setTint(drawable1, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        Drawable phone = phoneField.getResources().getDrawable(R.drawable.ic_phone);
        Drawable drawable2 = DrawableCompat.wrap(phone);
        DrawableCompat.setTint(drawable2, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        Drawable email = emailField.getResources().getDrawable(R.drawable.ic_email);
        Drawable drawable3 = DrawableCompat.wrap(email);
        DrawableCompat.setTint(drawable3, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        Drawable password = passwordField.getResources().getDrawable(R.drawable.ic_password);
        Drawable drawable4 = DrawableCompat.wrap(password);
        DrawableCompat.setTint(drawable4, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        if (!AppConstants.currentTheme.equals("duke")) {
            logo.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface OnSocialLoginInteractionListener {
        void onSocialLoginButtonPress();
    }
}
