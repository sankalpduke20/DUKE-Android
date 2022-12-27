package com.dukeai.android.bottomSheetDialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.InputValidators;
import com.dukeai.android.utils.SaveUserPreferences;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePhoneNumberBottomSheet extends BottomSheetDialog implements PopupActions {
    View view;
    @BindView(R.id.password_field)
    TextInputEditText passwordField;
    @BindView(R.id.verify_password_wrapper)
    RelativeLayout verifyPasswordWrapper;
    @BindView(R.id.phone_number_wrapper)
    RelativeLayout phoneNumberWrapper;
    @BindView(R.id.phone_field)
    TextInputEditText phoneField;
    @BindView(R.id.password_error)
    TextView passwordError;
    @BindView(R.id.phone_number_error)
    TextView phoneNumberError;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.close)
    ImageView closeBtn;

    String password = "", userPassword, phoneNumber = "", email;
    UserConfig userConfig = UserConfig.getInstance();
    CognitoUserDetails userDetails;
    CognitoUserAttributes userAttributes;
    GetDetailsHandler detailsHandler;
    UpdateAttributesHandler handler;
    Context context;
    AuthenticationHandler authenticationHandler;
    AuthenticationDetails authenticationDetails;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int buttonClickCount = 0;
    int verifyBtnCount = 0;

    public ChangePhoneNumberBottomSheet(@NonNull Context context) {
        super(context);
        this.context = context;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view = getLayoutInflater().inflate(R.layout.change_phone_number_layout, null);
        setContentView(view);
        popupActions = this;
        ButterKnife.bind(this, view);
        setInitials();
        setCurrentThemeIcons();
        setTextChangeListeners();
    }

    private void setInitials() {
        email = userConfig.getUserDataModel().getUserEmail().toUpperCase();
        verifyPasswordWrapper.setVisibility(View.VISIBLE);
        phoneNumberWrapper.setVisibility(View.GONE);
        setDetailsHandler();
        getUserDetails();
        setUserAttributesHandler();
        setAuthenticationHandler();
    }

    private void setAuthenticationHandler() {
        authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                String message = Utilities.getStrings(context, R.string.verify_password);
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.success), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
                AppHelper.setCurrSession(userSession);
                UserDataModel userDataModel = new UserDataModel(userSession, email, userPassword);
                userConfig.setUserDataModel(userDataModel);
                new SaveUserPreferences().execute(userDataModel);
                showPhoneNumberScreen();
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                getAuthenticationDetailsOfUser(authenticationContinuation);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                String message = Utilities.getStrings(context, R.string.incorrect_username_password);
                if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    message = Utilities.getStrings(Duke.getInstance(), R.string.no_internet);
                }
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.error), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);

            }
        };
    }

    private void showPhoneNumberScreen() {
        verifyPasswordWrapper.setVisibility(View.GONE);
        phoneNumberWrapper.setVisibility(View.VISIBLE);
        setPhoneIcon();

    }

    private void getAuthenticationDetailsOfUser(AuthenticationContinuation authenticationContinuation) {
        authenticationDetails = new AuthenticationDetails(email, userPassword, null);
        authenticationContinuation.setAuthenticationDetails(authenticationDetails);
        authenticationContinuation.continueTask();
    }

    private void setDetailsHandler() {
        detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                userAttributes = cognitoUserDetails.getAttributes();
            }

            @Override
            public void onFailure(Exception exception) {
                String message = Utilities.getStrings(context, R.string.failed_to_fetch_user_details);
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.error), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
            }
        };
    }

    private void setTextChangeListeners() {

        phoneField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                buttonClickCount = 0;
                view.findViewById(R.id.done).setEnabled(true);

            }

            @Override
            public void afterTextChanged(Editable s) {

                view.findViewById(R.id.done).setEnabled(true);
                buttonClickCount = 0;
            }
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                view.findViewById(R.id.verify).setEnabled(true);

                verifyBtnCount = 0;

            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyBtnCount = 0;
                view.findViewById(R.id.verify).setEnabled(true);

            }
        });

    }

    private void setUserAttributesHandler() {
        handler = new UpdateAttributesHandler() {
            @Override
            public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
                String message = Utilities.getStrings(context, R.string.phone_number_changed);
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.success), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
                dismissDialog();
            }

            @Override
            public void onFailure(Exception exception) {
                String message = exception.getMessage();
                if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    message = Utilities.getStrings(Duke.getInstance(), R.string.no_internet);
                }
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.error), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
            }
        };
    }

    private void getUserDetails() {
        String userName = AppHelper.getCurrUser().toUpperCase();
        AppHelper.getPool().getUser(userName).getDetailsInBackground(detailsHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(60, 0, 0, 0)));
    }

    @OnClick(R.id.close)
    void onClickCloseIcon() {
        this.dismiss();
    }

    @OnClick(R.id.verify)
    void verifyUserPassword() {
        if (verifyBtnCount < 1) {
            verifyBtnCount++;
            if (validateUserPassword()) {
                password = passwordField.getText().toString();
                userPassword = userConfig.getUserDataModel().getUserPassword();
                if (password.equals(userPassword)) {
                    passwordError.setVisibility(View.GONE);
                    AppHelper.setUser(email);
                    AppHelper.getPool().getUser(email).getSessionInBackground(authenticationHandler);
                } else {
                    String message = Utilities.getStrings(context, R.string.incorrect_username_password);
                    confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(Duke.getInstance(), R.string.error), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
                }
            }
        } else {
            view.findViewById(R.id.verify).setEnabled(false);
        }

    }

    private Boolean validateUserPassword() {
        password = passwordField.getText().toString();
        Boolean isValid = InputValidators.validateInput(AppConstants.StringConstants.PASSWORD, password);
        if (isValid) {
            passwordError.setVisibility(View.GONE);
        } else {
            passwordError.setText(Utilities.getStrings(Duke.getInstance(), R.string.invalid_password));
            passwordError.setVisibility(View.VISIBLE);
        }
        return isValid;
    }

    void changePhoneNumber() {
        phoneNumber = phoneField.getText().toString();
        if (userAttributes != null) {
            userAttributes.getAttributes().remove(AppConstants.UserAttributesConstants.SUB);
            userAttributes.getAttributes().remove(AppConstants.UserAttributesConstants.EMAIL_VERIFIED);
            userAttributes.getAttributes().remove(AppConstants.UserAttributesConstants.PHONE_NUMBER_VERIFIED);
            userAttributes.getAttributes().put(AppConstants.UserAttributesConstants.PHONE_NUMBER, phoneNumber);
            AppHelper.getPool().getCurrentUser().updateAttributesInBackground(userAttributes, handler);
        }
    }

    @OnClick(R.id.done)
    void onClickDone() {
        if (buttonClickCount < 1) {
            buttonClickCount++;
            if (validatePhoneNumber()) {
                changePhoneNumber();
            }
        } else {
            view.findViewById(R.id.done).setEnabled(false);

        }
    }

    private Boolean validatePhoneNumber() {
        phoneNumber = phoneField.getText().toString();
        Boolean isValid = InputValidators.validateInput(AppConstants.StringConstants.PHONE, phoneNumber);
        if (isValid) {
            phoneNumberError.setVisibility(View.GONE);
        } else {
            phoneNumberError.setVisibility(View.VISIBLE);
        }
        return isValid;
    }

    public void showDialog() {
        this.show();
    }

    public void dismissDialog() {
        this.dismiss();
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        confirmationComponent.dismiss();
        view.findViewById(R.id.done).setEnabled(true);
        view.findViewById(R.id.verify).setEnabled(true);
        buttonClickCount = 0;
        verifyBtnCount = 0;
    }

    private void setCurrentThemeIcons() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        Drawable pswdIcon = context.getResources().getDrawable(R.drawable.ic_password);
        Drawable drawable = DrawableCompat.wrap(pswdIcon);

        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));

//        Drawable hidePswd = oldplayout.getResources().getDrawable(R.drawable.ic_hide);
//        Drawable drawable1 = DrawableCompat.wrap(hidePswd);
//
//        DrawableCompat.setTint(drawable1,ContextCompat.getColor(getContext(),changeThemeModel.getPcolor()));
//
//        Drawable showPswd = oldplayout.getResources().getDrawable(R.drawable.ic_eye);
//        Drawable drawable2 = DrawableCompat.wrap(showPswd);
//
//        DrawableCompat.setTint(drawable2,ContextCompat.getColor(getContext(),changeThemeModel.getPcolor()));

        title.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        closeBtn.setColorFilter(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        Drawable phoneIcon = context.getResources().getDrawable(R.drawable.ic_phone);
        Drawable drawable1 = DrawableCompat.wrap(phoneIcon);

        DrawableCompat.setTint(drawable1, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));
    }

    public void setPhoneIcon() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        title.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        closeBtn.setColorFilter(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        Drawable phoneIcon = context.getResources().getDrawable(R.drawable.ic_phone);
        Drawable drawable1 = DrawableCompat.wrap(phoneIcon);

        DrawableCompat.setTint(drawable1, ContextCompat.getColor(getContext(), changeThemeModel.getPswdIconcolor()));
    }
}
