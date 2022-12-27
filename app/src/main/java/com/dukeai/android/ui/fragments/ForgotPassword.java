package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.interfaces.CommonHeaderInterface;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ArrayResponseModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.activities.MainActivity;
import com.dukeai.android.utils.AlertDialogWithVerticalOptions;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.viewModel.UserRegistrationViewModel;
import com.dukeai.android.views.CommonHeader;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomTextInputLayout;
import com.dukeai.awsauth_duke.Auth;
import com.dukeai.awsauth_duke.AuthUserSession;
import com.dukeai.awsauth_duke.handlers.AuthHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ForgotPassword extends Fragment implements CommonHeaderInterface, PopupActions {

    public static final String COGNITO = "cognito";
    public static final String APPLE = "apple";
    public static final String GOOGLE = "google";
    public static final String FACEBOOK = "facebook";

    private static final int NAVIGATE_TO_REGISTER = 76;
    private static final int OPEN_HOSTED_UI = 79;
    private static final int SELECT_LOGIN_METHOD = 99;
    private static final int SELECT_LOGIN_METHOD_ON_RESEND = 89;
    View forgotPwView;
    @BindView(R.id.header)
    CommonHeader header;
    @BindView(R.id.reset_password_text)
    Button resetPasswordText;
    @BindView(R.id.horizontal_line)
    View horizontalLine;
    @BindView(R.id.email_layout)
    CustomTextInputLayout emailLayout;
    @BindView(R.id.email)
    CustomEditInputField forgotEmail;
    @BindView(R.id.code_layout)
    CustomTextInputLayout codeLayout;
    @BindView(R.id.code)
    CustomEditInputField verificationCode;
    @BindView(R.id.password_layout)
    CustomTextInputLayout passwordLayout;
    @BindView(R.id.password)
    CustomEditInputField newPassword;
    @BindView(R.id.verify)
    Button verifyButton;
    @BindView(R.id.send_verification_code)
    Button sendVerificationCode;
    @BindView(R.id.resend)
    Button resend;
    @BindView(R.id.back_to_login)
    Button backToLogin;
    @BindView(R.id.verification_code_text)
    TextView verificationCodeText;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.form_layout)
    RelativeLayout formLayout;
    Boolean isNewPasswordSet = false, isResendPassword = false;
    Auth auth;
    int btnClick = 0;
    ForgotPasswordHandler forgotPasswordHandler;
    ForgotPasswordContinuation forgotPasswordContinuation;
    CustomProgressLoader customProgressLoader;
    CommonHeader commonHeader;
    Boolean hasArguments = false;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    UserRegistrationViewModel userRegistrationViewModel;
    AlertDialogWithVerticalOptions dialogWithVerticalOptions;
    boolean shouldDismissProgressLoader = false;
    private OnFragmentInteractionListener mListener;

    public ForgotPassword() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ForgotPassword newInstance(String param1, String param2) {
        ForgotPassword fragment = new ForgotPassword();
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
        forgotPwView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        ButterKnife.bind(this, forgotPwView);
        customProgressLoader = new CustomProgressLoader(getContext());
        commonHeader = new CommonHeader(getContext());
        popupActions = this;
        setCurrentTheme();
        isNewPasswordSet = false;
        getArgumentsData();
        setForgotPasswordHandler();
        userRegistrationViewModel = ViewModelProviders.of(this).get(UserRegistrationViewModel.class);
        return forgotPwView;
    }

    private void getArgumentsData() {
        Bundle args = getArguments();
        if (args != null && args.get(AppConstants.ForgotEmail.EMAIL) != null) {
            hasArguments = true;
            forgotEmail.setText(args.get(AppConstants.ForgotEmail.EMAIL).toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * Dismiss the Progress indicator from MainActivity
         * **/
        if (shouldDismissProgressLoader)
            mListener.onFragmentInteraction(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void setForgotPasswordHandler() {
//        btnClick = 0;
        forgotPasswordHandler = new ForgotPasswordHandler() {
            @Override
            public void onSuccess() {
                customProgressLoader.hideDialog();
                isNewPasswordSet = true;
                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.success), getString(R.string.new_password_is_set), false, getString(R.string.ok), popupActions, 1);
            }

            @Override
            public void getResetCode(ForgotPasswordContinuation continuation) {
                customProgressLoader.hideDialog();
                if (isResendPassword) {
                    String msg = getString(R.string.code_sent_to) + " " + continuation.getParameters().getDestination();
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.code_resent), msg, false, getString(R.string.ok), popupActions, 1);
                }
                getForgotPasswordCode(continuation);
            }

            @Override
            public void onFailure(Exception exception) {
                customProgressLoader.hideDialog();
                String errorMessage = "";
                if (exception.getMessage().contains(AppConstants.ForgotEmail.NOT_REGISTERED_WITH_US)) {
                    errorMessage = getString(R.string.email_not_registered);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.register_text), getString(R.string.close), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.ForgotEmail.COMBINATION_NOT_FOUND)) {
                    errorMessage = getString(R.string.email_not_registered);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.register_text), getString(R.string.close), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.ForgotEmail.INVALID_VERIFICATION_CODE)) {
                    errorMessage = getString(R.string.correct_verification_code);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.ok), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.ForgotEmail.ATTEMPT_LIMIT_EXCEEDED)) {
                    errorMessage = getString(R.string.attempt_limit_exceeded);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.ok), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.ForgotEmail.CAN_NOT_RESET_PASSWORD)) {
                    errorMessage = getString(R.string.no_registered_email_phone);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.ok), popupActions, 1);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    errorMessage = getString(R.string.no_internet);
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.ok), popupActions, 1);
                } else {
                    errorMessage = exception.getMessage();
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), errorMessage, false, getString(R.string.ok), popupActions, 1);
                }
            }
        }

        ;
    }

    @OnClick(R.id.verify)
    void verifyPassword() {
        boolean isCodeValid = verificationCode.validateField(verificationCode);
        boolean isPasswordValid = newPassword.validateField(newPassword);
        if (isCodeValid && isPasswordValid) {

            btnClick++;
            String newPass = newPassword.getText().toString();
            String code = verificationCode.getText().toString();
            forgotPasswordContinuation.setPassword(newPass);
            forgotPasswordContinuation.setVerificationCode(code);
            forgotPasswordContinuation.continueTask();
            verifyButton.setEnabled(false);

        }

    }

    private void getForgotPasswordCode(ForgotPasswordContinuation continuation) {
        this.forgotPasswordContinuation = continuation;
        verificationCodeText.setText(String.format("%s %s", getString(R.string.enter_the_verification_code_sent_at), forgotEmail.getText().toString()));
        resetPasswordText.setVisibility(View.GONE);
        horizontalLine.setVisibility(View.GONE);
        emailLayout.setVisibility(View.GONE);
        sendVerificationCode.setVisibility(View.GONE);
        backToLogin.setVisibility(View.GONE);
        header.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.VISIBLE);
        passwordLayout.setVisibility(View.VISIBLE);
        verifyButton.setVisibility(View.VISIBLE);
        resend.setVisibility(View.VISIBLE);
        verificationCodeText.setVisibility(View.VISIBLE);
        formLayout.setPadding(formLayout.getPaddingLeft(), formLayout.getPaddingTop(), formLayout.getPaddingRight(), formLayout.getPaddingBottom() + Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15f,//Add 15dp to existing padding bottom
                getResources().getDisplayMetrics()
        )));
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 90, getResources()
                        .getDisplayMetrics());
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) logo.getLayoutParams();
        params.setMargins(params.leftMargin, marginInDp, params.rightMargin, params.bottomMargin);
    }

    void validateUserRegistrationType(final Boolean isResend) {
        boolean isEmailValid = forgotEmail.validateField(forgotEmail);
        if (isEmailValid) {
            customProgressLoader.showDialog();
            userRegistrationViewModel.forgotPasswordUserStatus(forgotEmail.getText().toString()).observe(this, new Observer<ArrayResponseModel>() {
                @Override
                public void onChanged(@Nullable ArrayResponseModel responseModel) {
                    if (responseModel != null && responseModel.getCode().equalsIgnoreCase(ApiConstants.ERRORS.SUCCESS) && responseModel.getMessage() != null) {
                        /**Show options to choose from**/
                        String[] loginMethods = responseModel.getMessage();
                        switch (loginMethods.length) {
                            case 0:
                                customProgressLoader.dismiss();
                                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.new_user), getString(R.string.you_are_not_yet_registered_with_us), false, getString(R.string.ok), popupActions, NAVIGATE_TO_REGISTER);
                                break;
                            case 1:
                                if (loginMethods[0].equalsIgnoreCase(COGNITO)) {
                                    sendVerificationCode(isResend);
                                } else {
                                    customProgressLoader.dismiss();
                                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.existing_user), getString(R.string.you_ve_registered_with) + responseModel.getMessage()[0].toUpperCase() + getString(R.string.please_try_login), false, getString(R.string.ok), getString(R.string.cancel), popupActions, OPEN_HOSTED_UI);
                                }
                                break;
                            case 2:
                            case 3:
                                List<String> optionsList = new ArrayList<>();
                                List<String> list = Arrays.asList(loginMethods);
                                if (list.contains(COGNITO)) {
                                    optionsList.add(getString(R.string.verification_code));
                                }
                                if (list.contains(APPLE) || list.contains(GOOGLE) || list.contains(FACEBOOK)) {
                                    optionsList.add(getString(R.string.apple_google_facebook));
                                }
                                customProgressLoader.dismiss();
                                dialogWithVerticalOptions = new AlertDialogWithVerticalOptions(getContext(), getString(R.string.multiple_login_methods), getString(R.string.you_seem_to_have_multipe_login_methods), optionsList.get(0), optionsList.size() > 1 ? optionsList.get(1) : "", optionsList.size() != 1, popupActions, !isResend ? SELECT_LOGIN_METHOD : SELECT_LOGIN_METHOD_ON_RESEND);
                                break;
                            default:
                                customProgressLoader.dismiss();
                                showErrorDialog();
                        }
                    } else {
                        /**Something Went Wrong, Show Error Dialog**/
                        customProgressLoader.dismiss();
                        showErrorDialog();
                    }
                }
            });
        }
    }

    private void showErrorDialog() {
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, NAVIGATE_TO_REGISTER);
    }

    private void sendVerificationCode(Boolean isResend) {
        isResendPassword = isResend;
        customProgressLoader.showDialog();
        String username = forgotEmail.getText().toString().toUpperCase();
        AppHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }

    @OnClick(R.id.send_verification_code)
    void onClickSendVerificationCode() {
        validateUserRegistrationType(false);
    }

    @OnClick(R.id.back_to_login)
    public void backToLogin() {
        if (hasArguments) {
            navigateToSignup();
        } else {
            NavigationFlowManager.openFragments(new LoginFragment(), null, getActivity(), R.id.main_wrapper);
        }
    }

    @OnClick(R.id.resend)
    public void resend() {
        validateUserRegistrationType(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.common_back)
    public void backNavigation() {
        resetPasswordText.setVisibility(View.VISIBLE);
        horizontalLine.setVisibility(View.VISIBLE);
        emailLayout.setVisibility(View.VISIBLE);
        sendVerificationCode.setVisibility(View.VISIBLE);
        backToLogin.setVisibility(View.VISIBLE);
        header.setVisibility(View.GONE);
        codeLayout.setVisibility(View.GONE);
        passwordLayout.setVisibility(View.GONE);
        verifyButton.setVisibility(View.GONE);
        resend.setVisibility(View.GONE);
        verificationCodeText.setVisibility(View.GONE);
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
                        .getDisplayMetrics());
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) logo.getLayoutParams();
        params.setMargins(params.leftMargin, marginInDp, params.rightMargin, params.bottomMargin);
    }

    @Override
    public void onBackClicked(ImageView imageView) {

    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (dialogId) {
            case NAVIGATE_TO_REGISTER:
                dismissConfirmationComponent();
                navigateToSignup();
                break;
            case OPEN_HOSTED_UI:
                dismissConfirmationComponent();
                switch (id) {
                    case AppConstants.PopupConstants.POSITIVE:
                        showHostedSignInUI();
                        break;
                    default:
                        Log.d("#@#@:", "onPopupActions: ");
                }
                break;
            case SELECT_LOGIN_METHOD:
                dismissAlertDialogWithVerticalOptions();
                if (id.equalsIgnoreCase(getString(R.string.verification_code))) {
                    sendVerificationCode(false);
                } else {
                    showHostedSignInUI();
                }
                break;
            case SELECT_LOGIN_METHOD_ON_RESEND:
                dismissAlertDialogWithVerticalOptions();
                if (id.equalsIgnoreCase(getString(R.string.verification_code))) {
                    sendVerificationCode(true);
                } else {
                    showHostedSignInUI();
                }
                break;
            default:
                dismissConfirmationComponent();
                if (!isNewPasswordSet) {
                    switch (id) {
                        case AppConstants.PopupConstants.POSITIVE:
                            navigateToSignup();
                            break;
                        case AppConstants.PopupConstants.NEGATIVE:
                            Log.d("TAG", "onPopupActions: ");
                            break;
                        case AppConstants.PopupConstants.NEUTRAL:
                            Log.d("TAG", "onPopupActions: ");
                            break;
                        default:
                            Log.d("TAG", "onPopupActions: ");
                    }
                } else {
                    NavigationFlowManager.openFragments(new LoginFragment(), null, getActivity(), R.id.main_wrapper);
                }
                verifyButton.setEnabled(true);
        }
    }

    private void dismissAlertDialogWithVerticalOptions() {
        if (dialogWithVerticalOptions != null && dialogWithVerticalOptions.isShowing())
            dialogWithVerticalOptions.dismiss();
    }

    private void dismissConfirmationComponent() {
        if (confirmationComponent != null && confirmationComponent.isShowing())
            confirmationComponent.dismiss();
    }

    private void showHostedSignInUI() {
        try {
            auth = Duke.getAuth(Duke.appContext, new AuthHandler() {
                @Override
                public void onSuccess(AuthUserSession session) {
                    Log.d("FORGOT_PASSWORD", "onSuccess: ");
                    /**
                     * The Control should return to onSuccess of Main Activity by default due to Redirection
                     * And will proceed towards Dashboard
                     * Mark:- In the rarest of rare case, System may retain a session and returns control here.
                     * When Control Comes here, It Should save user data and navigate to Dashboard.
                     * */
                    UserDataModel userDataModel = new UserDataModel(session);
                    UserConfig.getInstance().setUserDataModel(userDataModel);
                    new SaveUserPreferences().execute(userDataModel);
                    NavigationFlowManager.openDashboardActivity(getActivity());
                }

                @Override
                public void onSignout() {
                    Log.d("FORGOT_PASSWORD", "onSuccess: ");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("FORGOT_PASSWORD", "onSuccess: ");
                }
            });
            shouldDismissProgressLoader = true;
            MainActivity.shouldShowProgressLoader = true;
            auth.getSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToSignup() {
        NavigationFlowManager.openFragments(new SignUpFragment(), null, getActivity(), R.id.main_wrapper);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//        loginTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
//        registerTab.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
//        registerUnderline.setBackgroundColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        logo.setImageResource(changeThemeModel.getLogo());
        resetPasswordText.setTextColor(Color.parseColor(changeThemeModel.getTitlecolor()));
        horizontalLine.setBackgroundColor(Color.parseColor(changeThemeModel.getTitlecolor()));

        Drawable email = forgotEmail.getResources().getDrawable(R.drawable.ic_email);
        Drawable drawable3 = DrawableCompat.wrap(email);
        DrawableCompat.setTint(drawable3, ContextCompat.getColor(Objects.requireNonNull(getContext()), changeThemeModel.getPswdIconcolor()));

        if (!AppConstants.currentTheme.equals("duke")) {
            logo.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String data);
    }
}
