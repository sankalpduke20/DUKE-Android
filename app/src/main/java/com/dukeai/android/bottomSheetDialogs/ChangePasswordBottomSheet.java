package com.dukeai.android.bottomSheetDialogs;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.AppHelper;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.InputValidators;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.DeviceTokenViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordBottomSheet extends BottomSheetDialog implements PopupActions {
    View changePasswordView;
    @BindView(R.id.old_password_field)
    TextInputEditText oldPassword;
    @BindView(R.id.new_password_field)
    TextInputEditText newPassword;
    GenericHandler genericHandler;
    Context context;
    String oldPw = "", newPw = "";
    @BindView(R.id.old_password_error)
    TextView oldPasswordError;
    @BindView(R.id.new_password_error)
    TextView newPasswordError;
    CustomProgressLoader customProgressLoader;
    Activity activity;
    DeviceTokenViewModel deviceTokenViewModel;
    LifecycleOwner lifecycleOwner;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    Boolean isPasswordChanged = false;
    int buttonClickCount = 0;
    @BindView(R.id.old_password)
    TextInputLayout oldplayout;
    @BindView(R.id.change_password)
    TextView changePasswordText;
    @BindView(R.id.close)
    ImageView closeBtn;

    public ChangePasswordBottomSheet(@NonNull Context context, Activity activity, DeviceTokenViewModel viewModel, LifecycleOwner owner) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.lifecycleOwner = owner;
        this.deviceTokenViewModel = viewModel;
        this.popupActions = this;
        isPasswordChanged = false;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setInitials();
        setCurrentThemeIcons();
        initiateCallBack();
        setTextChangeListeners();
    }

    private void setTextChangeListeners() {
        oldPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    oldPw = oldPassword.getText().toString();
                    boolean isOldPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, oldPw, AppConstants.StringConstants.OLD_PASSWORD);
                    if (isOldPasswordValid) {
                        oldPasswordError.setVisibility(View.GONE);
                    } else {
                        oldPasswordError.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        newPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    newPw = newPassword.getText().toString();
                    boolean isNewPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, newPw, AppConstants.StringConstants.NEW_PASSWORD);
                    if (isNewPasswordValid) {
                        newPasswordError.setVisibility(View.GONE);
                    } else {
                        newPasswordError.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    oldPw = oldPassword.getText().toString();
                    boolean isOldPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, oldPw, AppConstants.StringConstants.OLD_PASSWORD);
                    if (isOldPasswordValid) {
                        oldPasswordError.setVisibility(View.GONE);
                    }
                }
                changePasswordView.findViewById(R.id.done).setEnabled(true);
                buttonClickCount = 0;

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    newPw = newPassword.getText().toString();
                    boolean isNewPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, newPw, AppConstants.StringConstants.NEW_PASSWORD);
                    if (isNewPasswordValid) {
                        newPasswordError.setVisibility(View.GONE);
                    }
//                    changePasswordView.findViewById(R.id.done).setEnabled(true);
                    buttonClickCount = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initiateCallBack() {
        genericHandler = new GenericHandler() {
            @Override
            public void onSuccess() {
                dismissDialog();
                isPasswordChanged = true;
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(context, R.string.success), Utilities.getStrings(context, R.string.password_changed_please_login), false, Utilities.getStrings(context, R.string.ok), popupActions, 1);
            }

            @Override
            public void onFailure(Exception exception) {
                String message = exception.getMessage();
                if (exception.getMessage().contains(AppConstants.ChangePasswordSheet.INCORRECT_STATEMENT)) {
                    message = Utilities.getStrings(Duke.getInstance(), R.string.invalid_username_password);
                } else if (exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)) {
                    message = Utilities.getStrings(Duke.getInstance(), R.string.no_internet);
                }
                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(context, R.string.error), message, false, Utilities.getStrings(context, R.string.ok), popupActions, 1);
            }
        };
    }

    private void setInitials() {
        changePasswordView = getLayoutInflater().inflate(R.layout.change_password_layout, null);
        setContentView(changePasswordView);
        customProgressLoader = new CustomProgressLoader(context);
        ButterKnife.bind(this, changePasswordView);
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

    @OnClick(R.id.done)
    void onClickDoneButton() {
        if (buttonClickCount < 1) {
            buttonClickCount++;
            oldPw = oldPassword.getText().toString();
            newPw = newPassword.getText().toString();
            boolean isOldPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, oldPw, AppConstants.StringConstants.OLD_PASSWORD);
            boolean isNewPasswordValid = validatePassword(AppConstants.StringConstants.OLD_PASSWORD, newPw, AppConstants.StringConstants.NEW_PASSWORD);
            if (isOldPasswordValid && isNewPasswordValid) {
                if (!oldPw.equals(newPw)) {
                    AppHelper.getPool().getUser(AppHelper.getCurrUser()).changePasswordInBackground(oldPw, newPw, genericHandler);
                } else {
                    String message = Utilities.getStrings(Duke.getInstance(), R.string.new_old_password);
                    confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(context, R.string.error), message, false, Utilities.getStrings(context, R.string.ok), popupActions, 1);
                }
            }

        } else {
            changePasswordView.findViewById(R.id.done).setEnabled(false);
        }
    }

    private boolean validatePassword(String text, String pw, String type) {
        Boolean isValid = InputValidators.validateInput(text, pw);
        if (isValid) {
            if (type.equals(AppConstants.StringConstants.OLD_PASSWORD)) {
                oldPasswordError.setVisibility(View.GONE);
            } else {
                newPasswordError.setVisibility(View.GONE);
            }
        } else {
            if (type.equals(AppConstants.StringConstants.OLD_PASSWORD)) {
                oldPasswordError.setVisibility(View.VISIBLE);
            } else {
                newPasswordError.setVisibility(View.VISIBLE);
            }
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
        dismissExistingComponentIfAny();
        changePasswordView.findViewById(R.id.done).setEnabled(true);
        buttonClickCount = 0;
        if (isPasswordChanged) {
//            confirmationComponent.dismiss();
            dismissExistingComponentIfAny();

            Utilities.logoutUser(context, activity, deviceTokenViewModel, lifecycleOwner);
        } else {
            dismissExistingComponentIfAny();
//            confirmationComponent.dismiss();
        }
    }

    private void dismissExistingComponentIfAny() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    private void setCurrentThemeIcons() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        Drawable pswdIcon = activity.getResources().getDrawable(R.drawable.ic_password);
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

        changePasswordText.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        closeBtn.setColorFilter(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
    }
}
