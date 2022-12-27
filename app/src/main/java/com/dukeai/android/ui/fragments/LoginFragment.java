package com.dukeai.android.ui.fragments;

import static com.dukeai.android.Duke.accessToken;
import static com.dukeai.android.Duke.api_key;
import static com.dukeai.android.Duke.client_id;
import static com.dukeai.android.Duke.cust_id;
import static com.dukeai.android.Duke.idToken;
import static com.dukeai.android.Duke.refreshToken;
import static com.dukeai.android.ui.fragments.ForgotPassword.APPLE;
import static com.dukeai.android.ui.fragments.ForgotPassword.COGNITO;
import static com.dukeai.android.ui.fragments.ForgotPassword.FACEBOOK;
import static com.dukeai.android.ui.fragments.ForgotPassword.GOOGLE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ArrayResponseModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.InputValidators;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UIMessages;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.viewModel.UserRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends Fragment implements PopupActions {

    private static final int GENERIC_ERROR = 997;
    private static final int SHOW_SOCIAL_LOGIN_UI = 543;
    String username, password;
    View loginView;
    Context context;
    @BindView(R.id.email_field)
    TextInputEditText emailField;
    @BindView(R.id.password_field)
    TextInputEditText passwordField;
    @BindView(R.id.login_tab)
    Button loginTab;
    @BindView(R.id.register_tab)
    Button registerTab;
    @BindView(R.id.login_underline)
    View loginUnderline;
    @BindView(R.id.new_register_tab)
    Button newRegister;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.social_login_info_text)
    TextView socialLoginInfoTextView;
    @BindView(R.id.privacy_policy)
    TextView privacyPolicy;
    AuthenticationHandler authenticationHandler;
    AuthenticationDetails authenticationDetails;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    UserConfig userConfig = UserConfig.getInstance();
    PopupActions popupActions;
    Boolean userNotConfirmed = false;
    boolean userNotRegistered = false;
    UserRegistrationViewModel userRegistrationViewModel;
    private OnSocialLoginInteractionListener mListener;


    public LoginFragment() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        loginView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, loginView);
        Duke.isWithOutToken = false;
        context = this.getActivity();
        setCurrentTheme();
        socialLoginInfoTextView.setText(R.string.or_login_with);
        userNotConfirmed = false;
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setAuthenticationHandler();
        getBundleArguments();
        popupActions = this;
        customProgressLoader = new CustomProgressLoader(getContext());
        userRegistrationViewModel = ViewModelProviders.of(this).get(UserRegistrationViewModel.class);
        return loginView;
    }


    private void getBundleArguments() {
        Bundle args = getArguments();
        if (args != null && args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL) != null) {
            username = args.getString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL);
            emailField.setText(username);
        }
    }

    private void getUserAuthentication(AuthenticationContinuation continuation) {
        username = emailField.getText().toString().toUpperCase();
        password = passwordField.getText().toString();
        authenticationDetails = new AuthenticationDetails(username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    private void setAuthenticationHandler() {
        authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                customProgressLoader.hideDialog();
                AppHelper.setCurrSession(userSession);
                Duke.isFromLogin = true;
                Duke.isFromSignUp = false;
                UserDataModel userDataModel = new UserDataModel(userSession, username, password);

                userConfig.setUserDataModel(userDataModel);
                new SaveUserPreferences().execute(userDataModel);
                Duke.isFirebaseSetUp = true;
                NavigationFlowManager.openDashboardActivity(getActivity());
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                getUserAuthentication(authenticationContinuation);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                System.out.print("MFA Code---");
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                System.out.print("Authentication Challenge---");
            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                String msg = exception.getMessage();
                if (exception.getMessage().contains(AppConstants.LoginConstants.USER_NOT_CONFIRMED)) {
                    userNotConfirmed = true;
                    msg = getString(R.string.user_not_confirmed);
                } else if (exception.getMessage().contains(AppConstants.LoginConstants.NO_INTERNET)) {
                    msg = getString(R.string.no_internet);
                } else if (exception.getMessage().contains(AppConstants.LoginConstants.USER_DOES_NOT_EXIST)) {
                    msg = getString(R.string.you_are_not_registered_with_us);
                    userNotRegistered = true;
                } else {
                    msg = getString(R.string.incorrect_username_password);
                }
                if (userNotConfirmed) {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getResources().getString(R.string.ok), getString(R.string.cancel), popupActions, 1);
                } else if (userNotRegistered) {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getResources().getString(R.string.register_text), getString(R.string.cancel), popupActions, 1);
                } else {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), msg, false, getResources().getString(R.string.ok), popupActions, 1);
                }
            }
        };
    }

    @OnClick(R.id.privacy_policy)
    public void checkPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://duke.ai/private_policy_updated"));
        startActivity(browserIntent);
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        confirmationComponent.dismiss();
        switch (dialogId) {
            case SHOW_SOCIAL_LOGIN_UI:
                /**
                 * Show Social login UI
                 * */
                if (id.equals(AppConstants.PopupConstants.POSITIVE))
                    mListener.onSocialLoginButtonPress();
            default:
                String email = emailField.getText().toString().toUpperCase();
                String password = passwordField.getText().toString();

                if (userNotConfirmed && id.equals(AppConstants.PopupConstants.POSITIVE)) {
                    Bundle args = new Bundle();
                    args.putString(AppConstants.SignUpDataConstants.EMAIL, email);
                    args.putString(AppConstants.SignUpDataConstants.PASSWORD, password);
                    args.putString(AppConstants.SignUpDataConstants.PROMO_CODE, "none");
                    NavigationFlowManager.openFragments(new UserVerificationFragment(), args, getActivity(), R.id.main_wrapper);
                } else if (userNotRegistered && id.equals(AppConstants.PopupConstants.POSITIVE)) {
                    Bundle args = new Bundle();
                    args.putString(AppConstants.VerificationCodeConstants.LOGIN_EMAIL, email);
                    NavigationFlowManager.openFragments(new SignUpFragment(), args, getActivity(), R.id.main_wrapper);
                }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @OnClick(R.id.login_buton)
    public void login(View v) {

        String email = emailField.getText().toString();
        String pswd = passwordField.getText().toString();

//        boolean isEmailValid = emailField.validateField(emailField);
        boolean isEmailValid = validateFieldEmail(emailField);
        boolean isPasswordValid = validateFieldPass(passwordField);
//        boolean isPasswordValid = passwordField.validateField(passwordField);

        if (isNetworkAvailable()) {
            if (isEmailValid && isPasswordValid) {
                customProgressLoader.showDialog();
                userRegistrationViewModel.forgotPasswordUserStatus(emailField.getText().toString()).observe(this, new Observer<ArrayResponseModel>() {
                    @Override
                    public void onChanged(@Nullable ArrayResponseModel responseModel) {
                        if (responseModel != null && responseModel.getCode().equalsIgnoreCase(ApiConstants.ERRORS.SUCCESS) && responseModel.getMessage() != null) {
                            /**Show options to choose from**/
                            String[] loginMethods = responseModel.getMessage();
                            switch (loginMethods.length) {
                                case 0:
                                    /**New User:
                                     * Show Alert Message
                                     * Take User to Registration Screen
                                     * **/
                                    customProgressLoader.dismiss();
                                    userNotRegistered = true;
                                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.you_are_not_registered_with_us), false, getResources().getString(R.string.register_text), getString(R.string.cancel), popupActions, 1);
                                    break;
                                case 1:
                                    /**Registered User:
                                     * Cognito: -> Validate User
                                     * Social Login: -> Show Message to User -> Try login using Social Login Methods
                                     * **/
                                    if (loginMethods[0].equalsIgnoreCase(COGNITO)) {
                                        /**Cognito User**/
                                        cognitoLogin();
                                    } else {
                                        /**Social Login User**/
                                        customProgressLoader.dismiss();
                                        showAlertForSocialLoginUI(loginMethods[0].toUpperCase() + getString(R.string.space) + getString(R.string.login), getString(R.string.you_seem_to_have_registered_with_space) + loginMethods[0].toUpperCase() + getString(R.string.please_try_login_with) + loginMethods[0] + ".");
                                    }
                                    break;
                                case 2:
                                case 3:
                                    List<String> list = Arrays.asList(loginMethods);
                                    if (list.contains(COGNITO)) {
                                        /**Multiple Login Methods:
                                         * Validate Cognito User Credentials
                                         * **/
                                        cognitoLogin();
                                    } else if (list.contains(APPLE) || list.contains(GOOGLE) || list.contains(FACEBOOK)) {
                                        String methodsString = new String();
                                        for (int i = 0; i < loginMethods.length; ++i) {
                                            if (i != loginMethods.length - 1)
                                                methodsString += loginMethods[i] + "/";
                                            else
                                                methodsString += loginMethods[i];
                                        }
                                        customProgressLoader.dismiss();
                                        showAlertForSocialLoginUI(getString(R.string.multiple_login_methods),
                                                getString(R.string.you_seem_to_have_registered_with_space) + methodsString.toUpperCase() +
                                                        getString(R.string.please_try_login_with) + methodsString + ".");
                                    }
                                    break;
                                default:
                                    /**Validate Cognito User*/
                                    cognitoLogin();
                            }
                        } else {
                            /**Something Went Wrong, Show Error Dialog**/
                            customProgressLoader.dismiss();
                            showErrorDialog();
                        }
                    }
                });
            }
        } else {
            confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.no_internet_connectivity), false, getString(R.string.ok), popupActions, GENERIC_ERROR);

        }
    }

    private void showAlertForSocialLoginUI(String title, String message) {
        confirmationComponent = new ConfirmationComponent(getContext(), title, message, false, getString(R.string.okay), getString(R.string.cancel), popupActions, SHOW_SOCIAL_LOGIN_UI);
    }

    private void cognitoLogin() {
        if (customProgressLoader != null && !customProgressLoader.isShowing())
            customProgressLoader.showDialog();
        username = emailField.getText().toString().toUpperCase();
        password = passwordField.getText().toString();
        AppHelper.setUser(username);
        AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
    }

    private void showErrorDialog() {
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, GENERIC_ERROR);
    }

    @OnClick({R.id.register_tab, R.id.new_register_tab})
    void onClickRegister() {
        NavigationFlowManager.openFragments(new SignUpFragment(), null, getActivity(), R.id.main_wrapper);
    }

    @OnClick(R.id.forgot_password)
    void onClickForgotPassword() {
        NavigationFlowManager.openFragments(new ForgotPassword(), null, getActivity(), R.id.main_wrapper);
    }

    @OnClick(R.id.social_icon_section)
    void onSocialLoginClick() {
        mListener.onSocialLoginButtonPress();
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        loginTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        loginUnderline.setBackgroundColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        registerTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        newRegister.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        logo.setImageResource(changeThemeModel.getLogo());


        Drawable password = passwordField.getResources().getDrawable(R.drawable.ic_password);
        Drawable drawable1 = DrawableCompat.wrap(password);
        DrawableCompat.setTint(drawable1, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        Drawable email = emailField.getResources().getDrawable(R.drawable.ic_email);
        Drawable drawable = DrawableCompat.wrap(email);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        if (!AppConstants.currentTheme.equals("duke")) {
            logo.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
//        Drawable pswdIcon = context.getResources().getDrawable(R.drawable.ic_password);
//        Drawable drawable = DrawableCompat.wrap(pswdIcon);
//
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));
//
//
//        Drawable emailIcon = context.getResources().getDrawable(R.drawable.ic_email);
//        Drawable drawable1 = DrawableCompat.wrap(emailIcon);
//
//        DrawableCompat.setTint(drawable1, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));

//        Drawable pswdIcon = activity.getResources().getDrawable(R.drawable.ic_password);
//        Drawable drawable = DrawableCompat.wrap(pswdIcon);
//
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Interface with the Activity.
     */
    public interface OnSocialLoginInteractionListener {
        void onSocialLoginButtonPress();
    }


    public boolean validateFieldEmail(View v) {
        String viewTag = v.getTag().toString();
        Boolean isValid = InputValidators.validateInput(viewTag, ((TextInputEditText) v).getText().toString());
        String msg;
        if (isValid) {
            msg = null;
        } else {
            msg = "! " + UIMessages.getMessage(AppConstants.StringConstants.ERROR, viewTag);
        }

//        if(textInputLayout.getError() == null)

        if (emailField != null) {
            emailField.setError(msg);
        }

        return isValid;
    }

    public boolean validateFieldPass(View v) {
        String viewTag = v.getTag().toString();
        Boolean isValid = InputValidators.validateInput(viewTag, ((TextInputEditText) v).getText().toString());
        String msg;
        if (isValid) {
            msg = null;
        } else {
            msg = "! " + UIMessages.getMessage(AppConstants.StringConstants.ERROR, viewTag);
        }

//        if(textInputLayout.getError() == null)

        if (passwordField != null) {
            passwordField.setError(msg);
        }

        return isValid;
    }


}
