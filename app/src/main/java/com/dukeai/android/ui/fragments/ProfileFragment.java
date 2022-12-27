package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.LocationService;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.ApiUrls;
import com.dukeai.android.bottomSheetDialogs.ChangePasswordBottomSheet;
import com.dukeai.android.bottomSheetDialogs.ChangePhoneNumberBottomSheet;
import com.dukeai.android.bottomSheetDialogs.TaxInfoSheet;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.BankModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.ui.activities.AssetDeduction;
import com.dukeai.android.ui.activities.BalanceSheet;
import com.dukeai.android.ui.activities.FederalDeduction;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.CameraUtils;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.PreferenceManager;
import com.dukeai.android.utils.UploadDocument;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.BankViewModel;
import com.dukeai.android.viewModel.DeviceTokenViewModel;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.UploadFileViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.dukeai.android.ui.activities.BaseActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.dukeai.android.ui.activities.BaseActivity.PICK_IMAGE_REQUEST;

// Firebase: Setup

public class ProfileFragment extends Fragment implements HeaderActions, PopupActions, View.OnClickListener {
    private static final int SERVICE_RUNNING_LOGOUT_DIALOG_ID = 80;
    private static final int SERVICE_RUNNING_CHANGE_PWD_DIALOG_ID = 81;
    View profileView;
    ChangePasswordBottomSheet changePasswordBottomSheet;
    ChangePhoneNumberBottomSheet changePhoneNumberBottomSheet;
    TaxInfoSheet taxInfoSheet;
    DeviceTokenViewModel deviceTokenViewModel;
    UploadFileViewModel uploadFileViewModel;
    @BindView(R.id.profile_header)
    CustomHeader customHeader;
    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.name_text)
    TextView profileText;
    @BindView(R.id.open_with_camera)
    RelativeLayout openWithCamera;
    @BindView(R.id.open_with_home)
    RelativeLayout openWithHome;
    @BindView(R.id.image_layout)
    RelativeLayout profileImageHolderRelativeLayout;
    FileStatusViewModel fileStatusViewModel;
    CustomProgressLoader customProgressLoader;
    UploadDocument uploadDocument;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int screenWidth, screenHeight;
    Boolean isOpenWithHome = false;
    Boolean isOpenWithHomeCamera = false;
    @BindView(R.id.tax_information_layout)
    RelativeLayout tax_info_layout;
    @BindView(R.id.password_icon)
    ImageView passwordIcon;
    @BindView(R.id.phone_icon)
    ImageView phoneIcon;
    @BindView(R.id.tax_info_icon)
    ImageView taxInfoIcon;
    @BindView(R.id.logout_icon)
    ImageView logoutIcon;
    @BindView(R.id.open_with_camera_icon)
    ImageView cameraIcon;
    @BindView(R.id.open_with_home_icon)
    ImageView homeIcon;
    @BindView(R.id.change_password_layout)
    RelativeLayout changePasswordLayout;
    @BindView(R.id.change_phone_layout)
    RelativeLayout changePhoneLayout;
    @BindView(R.id.member_status)
    TextView memberStatus;
    private OnFragmentInteractionListener mListener;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    BankViewModel bankViewModel;

    public ProfileFragment() {
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CameraUtils.refreshGallery(getContext().getApplicationContext(), Duke.profileImageStoragePath);
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext().getApplicationContext(), getString(R.string.cancelled_image_capture), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext().getApplicationContext(), getString(R.string.failed_to_capture_image), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST) {
            Uri uri = null;
            try {
                uri = data.getData();
            } catch (Exception ignored) {

            }
            if (uri == null) {
                return;
            }

            Duke.profileImageStoragePath = Utilities.getPath(getContext(), uri);
            Bitmap mBitmapInsurance = BitmapFactory.decodeFile(Duke.profileImageStoragePath);
            Bitmap resized = Utilities.getPortraitResizedBitmap(mBitmapInsurance, 720, 540);
            openPreviewImage(resized);

        }
    }

    private void openPreviewImage(final Bitmap bm) {
        if (Duke.profileImageStoragePath == null || Duke.profileImageStoragePath.isEmpty()) {
            return;
        }
        ArrayList<String> paths = new ArrayList<>();
        paths.add(Duke.profileImageStoragePath);

        MultipartBody.Part[] list = Utilities.getMultipartBody(paths, true, screenWidth, screenHeight);
        if (list != null && list.length <= 0) {
            customProgressLoader.hideDialog();
            return;
        }
        customProgressLoader.showDialog();
        uploadFileViewModel.uploadFile(list).observe(this, new Observer<FileUploadSuccessModel>() {
            @Override
            public void onChanged(@Nullable FileUploadSuccessModel fileUploadSuccessModel) {
                customProgressLoader.hideDialog();
                if (fileUploadSuccessModel != null && fileUploadSuccessModel.getCode() != null && fileUploadSuccessModel.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                    Duke.profileImageStoragePath = "";
                    Duke.ProfileImage = bm;
                    profileImage.setImageBitmap(bm);
                    String message = Utilities.getStrings(Duke.getInstance(), R.string.profile_upload_success);
                    confirmationComponent = new ConfirmationComponent(getContext(), Utilities.getStrings(Duke.getInstance(), R.string.success), message, false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
                } else if (fileUploadSuccessModel != null && fileUploadSuccessModel.getMessage() != null) {
                    confirmationComponent = new ConfirmationComponent(getContext(), Utilities.getStrings(Duke.getInstance(), R.string.error), fileUploadSuccessModel.getMessage(), false, Utilities.getStrings(Duke.getInstance(), R.string.ok), popupActions, 1);
                }
            }
        });
    }

    private void previewCapturedImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(Duke.profileImageStoragePath);
            openPreviewImage(bitmap);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, profileView);
        popupActions = this;
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setInitials();

        setCurrentThemeIcons();
        RelativeLayout tv = profileView.findViewById(R.id.tax_information_layout);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickTaxInfo();
            }
        });

        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        if (UserConfig.getInstance().getUserDataModel().getLoginType() == UserDataModel.LoginType.Social) {
            changePasswordLayout.setVisibility(View.GONE);
            changePhoneLayout.setVisibility(View.GONE);
        }
        return profileView;

    }

    public void navigateToFederalDeduction(View v) {
        Intent intent = new Intent(getActivity(), FederalDeduction.class);
        getActivity().startActivity(intent);
    }

    public void navigateToAssetsDeduction(View v) {
        Intent intent = new Intent(getActivity(), AssetDeduction.class);
        getActivity().startActivity(intent);
    }

    public void navigateToBalanceSheet(View v) {
        Intent intent = new Intent(getActivity(), BalanceSheet.class);
        getActivity().startActivity(intent);
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }


    private void setInitials() {
        isOpenWithHome = false;
        isOpenWithHomeCamera = false;
        getWindowWidthAndHeight();
        deviceTokenViewModel = ViewModelProviders.of(this).get(DeviceTokenViewModel.class);
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        uploadFileViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);
        bankViewModel = new ViewModelProvider(this).get(BankViewModel.class);
        setCustomHeader();
        setToolbarColor();
        setProfileImage();
        getProfilePicture();
        setProfileName();
        setCameraHomeOptions();
        setMemberStatus();
    }

    private void setMemberStatus() {
        switch (Duke.subscriptionPlan.getMemberStatus().toString()) {
            case "MONTHLY_POD":
                memberStatus.setText("Member status - Monthly premium");
                break;
            case "ANNUALLY_POD":
                memberStatus.setText("Member status - Annual premium");
                break;
            default:
                memberStatus.setText("Member status - Not paid");
                break;
        }
    }

    private void setCameraHomeOptions() {
        if (Duke.isOpenWithHome != null && Duke.isOpenWithHome.equals(AppConstants.UserPreferencesConstants.OPEN_WITH_CAMERA)) {
            openWithHome.setVisibility(View.VISIBLE);
            openWithCamera.setVisibility(View.GONE);
        } else {
            openWithHome.setVisibility(View.GONE);
            openWithCamera.setVisibility(View.VISIBLE);
        }
    }

    private void setProfileName() {
        if (Duke.userName != null && !Duke.userName.isEmpty()) {
            String name = Duke.userName.split("@")[0];
            profileText.setText("Hello, " + name);
        }
    }

    private void setProfileImage() {
        if (Duke.ProfileImage != null) {
            profileImage.setImageBitmap(Duke.ProfileImage);
        } else {
            profileImage.setImageDrawable(Utilities.getDrawable(Duke.getInstance(), R.drawable.profile));
        }
    }

    private void getProfilePicture() {
        customProgressLoader.showDialog();
        String fileName = Utilities.getProfileImageName();
        fileStatusViewModel.downloadFile(fileName, screenWidth, screenHeight, true).observe(this, new Observer<DownloadImageModel>() {
            @Override
            public void onChanged(@Nullable DownloadImageModel downloadImageModel) {
                customProgressLoader.hideDialog();
                if (downloadImageModel != null && downloadImageModel.getBitmap() != null) {
                    Duke.ProfileImage = downloadImageModel.getBitmap();
                    setProfileImage();
                }

            }
        });
    }

    private void dismissExistingComponentIfAny() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    @OnClick(R.id.profile_edit)
    void onClickProfileEditIcon() {
        uploadDocument = new UploadDocument(getContext(), getActivity(), null, true, ProfileFragment.this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorBlack));
    }

    private void setCustomHeader() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        customHeader.setHeaderActions(this);
        customHeader.setToolbarTitle(getString(R.string.profile));
        customHeader.showHideProfileImage(View.INVISIBLE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setProfileTextGone();
//        customHeader.setImageTintColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
//        TextView title = customHeader.findViewById(R.id.toolbar_title);
//        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        customHeader.setImageTintColor(ContextCompat.getColor(getContext(), changeThemeModel.getProfileHeaderBackButtonColor()));
        TextView title = customHeader.findViewById(R.id.toolbar_title);
        title.setTextColor(ContextCompat.getColor(getContext(), changeThemeModel.getProfileHeaderTextColor()));
        customHeader.setBackgroundColor(ContextCompat.getColor(getContext(), changeThemeModel.getProfileHeaderBackgroundColor()));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (id) {
            case AppConstants.PopupConstants.POSITIVE:
                if (dialogId == SERVICE_RUNNING_LOGOUT_DIALOG_ID) {
                    try {
                        stopTrackingService();
                        //Logout
                        Utilities.logoutUser(getContext(), getActivity(), deviceTokenViewModel, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (dialogId == SERVICE_RUNNING_CHANGE_PWD_DIALOG_ID) {
                    try {
                        showChangePwdDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (isOpenWithHomeCamera) {
                    if (isOpenWithHome) {
                        Duke.isOpenWithHome = AppConstants.UserPreferencesConstants.OPEN_WITH_HOME;
                        PreferenceManager.saveString(Duke.getInstance(), AppConstants.UserPreferencesConstants.OPEN_WITH, AppConstants.UserPreferencesConstants.OPEN_WITH_HOME);
                    } else {
                        Duke.isOpenWithHome = AppConstants.UserPreferencesConstants.OPEN_WITH_CAMERA;
                        PreferenceManager.saveString(Duke.getInstance(), AppConstants.UserPreferencesConstants.OPEN_WITH, AppConstants.UserPreferencesConstants.OPEN_WITH_CAMERA);
                    }
                    toggleHomeCameraOption();
                }
                confirmationComponent.dismiss();
                break;
            case AppConstants.PopupConstants.NEGATIVE:
                confirmationComponent.dismiss();
                break;
            case AppConstants.PopupConstants.NEUTRAL:
                confirmationComponent.dismiss();
                break;
        }
    }

    private void stopTrackingService() {
        //Double Check - Stop service only if service is running
        if (Utilities.serviceIsRunningInForeground(getContext(), LocationService.class)) {
            HomeFragment.mService.removeLocationUpdates(getContext());
        }
    }

    private void toggleHomeCameraOption() {
        if (isOpenWithHome) {
            openWithCamera.setVisibility(View.VISIBLE);
            openWithHome.setVisibility(View.GONE);
        } else {
            openWithCamera.setVisibility(View.GONE);
            openWithHome.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        Duke.selectedRecipients.clear();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int backStackLength = fm.getBackStackEntryCount();
        if (backStackLength > 1) {
            fm.popBackStack();
        }
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    @Override
    public void onClick(View v) {
//        View popupView = getLayoutInflater().inflate(R.layout.tax_info_pop_up, null);
//        PopupWindow popupWindow = new PopupWindow(popupView,
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setFocusable(true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable());
//        int location[] = new int[2];
//
//        v.getLocationOnScreen(location);
//        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
//                location[0], location[1] + v.getHeight());
//        ImageView close = popupView.findViewById(R.id.close_icon);
    }

    @OnClick(R.id.change_password_layout)
    void onClickChangePassword() {

        UserConfig userConfig = UserConfig.getInstance();
        UserDataModel userDataModel;
        userDataModel = userConfig.getUserDataModel();
        Bundle params = new Bundle();
        params.putString("UserEmail", userDataModel.getUserEmail());
        mFirebaseAnalytics.logEvent("Password_Change", params);

        if (!Utilities.serviceIsRunningInForeground(getContext(), LocationService.class)) {
            showChangePwdDialog();
        } else {
            dismissExistingComponentIfAny();
            confirmationComponent = new ConfirmationComponent(getContext(), getContext().getString(R.string.active_trip), getContext().getString(R.string.procced_with_change_password), false, getContext().getString(R.string.yes), getContext().getString(R.string.no), popupActions, SERVICE_RUNNING_CHANGE_PWD_DIALOG_ID);
        }
    }

    private void showChangePwdDialog() {
        changePasswordBottomSheet = new ChangePasswordBottomSheet(getContext(), getActivity(), deviceTokenViewModel, this);
        if (!changePasswordBottomSheet.isShowing()) {
            changePasswordBottomSheet.showDialog();
        }
    }

    @OnClick(R.id.change_phone_layout)
    void onClickChangePhoneNumber() {
        changePhoneNumberBottomSheet = new ChangePhoneNumberBottomSheet(getContext());
        if (!changePhoneNumberBottomSheet.isShowing()) {
            changePhoneNumberBottomSheet.showDialog();
        }
    }

    @OnClick(R.id.open_with_camera)
    void onClickAppWithCamera() {
        Bundle params = new Bundle();
        params.putString("Type", "Open_With_Camera");
        mFirebaseAnalytics.logEvent("Open_App_Perference", params);

        isOpenWithHome = false;
        isOpenWithHomeCamera = true;
        confirmationComponent = new ConfirmationComponent(getContext(), Utilities.getStrings(Duke.getInstance(), R.string.confirm), Utilities.getStrings(Duke.getInstance(), R.string.do_you_want_to_open_with_camera), false, Utilities.getStrings(Duke.getInstance(), R.string.yes), Utilities.getStrings(Duke.getInstance(), R.string.no), popupActions, 1);
    }

    @OnClick(R.id.manage_bank_layout)
    void onClickManageBankConnections() {
      /*  Uri uriUrl;
        uriUrl = Uri.parse(ApiUrls.MANAGE_BANK_CONNECTIONS);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);*/

        bankViewModel.getBankConnection().observe(getActivity(), new Observer<BankModel>() {
            @Override
            public void onChanged(BankModel bankModel) {
                customProgressLoader.hideDialog();
                if(bankModel.getStatus().equals("SUCCESS")){
                    Uri uriUrl;
//                    uriUrl = Uri.parse(ApiUrls.MANAGE_BANK_CONNECTIONS);
                    uriUrl = Uri.parse(bankModel.getWebUrl());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            }
        });

    }

    @OnClick(R.id.open_with_home)
    void onClickAppWithHome() {
        Bundle params = new Bundle();
        params.putString("Type", "Open_With_Home");
        mFirebaseAnalytics.logEvent("Open_App_Perference", params);

        isOpenWithHome = true;
        isOpenWithHomeCamera = true;
        confirmationComponent = new ConfirmationComponent(getContext(), Utilities.getStrings(Duke.getInstance(), R.string.confirm), Utilities.getStrings(Duke.getInstance(), R.string.do_you_want_to_open_with_home), false, Utilities.getStrings(Duke.getInstance(), R.string.yes), Utilities.getStrings(Duke.getInstance(), R.string.no), popupActions, 1);
    }

    public void onClickTaxInfo() {
        Bundle params = new Bundle();
        params.putString("button", "tax_information");
        mFirebaseAnalytics.logEvent("Tax_Info", params);
        showTaxInfoDialog();
    }
//    @OnClick(R.id.tax_information_layout)
//    void onClickTaxInfo() {
//        if (!Utilities.serviceIsRunningInForeground(getContext(), LocationService.class)) {
//            showTaxInfoDialog();
//        } else {
//            dismissExistingComponentIfAny();
//            confirmationComponent = new ConfirmationComponent(getContext(), getContext().getString(R.string.active_trip), getContext().getString(R.string.procced_with_change_password), false, getContext().getString(R.string.yes), getContext().getString(R.string.no), popupActions, SERVICE_RUNNING_CHANGE_PWD_DIALOG_ID);
//        }
//    }

    private void showTaxInfoDialog() {
        taxInfoSheet = new TaxInfoSheet(getContext(), getActivity(), this);
        if (!taxInfoSheet.isShowing()) {
            taxInfoSheet.showDialog();
        }
    }

    @OnClick(R.id.logout_layout)
    void onClickLogout() {
        if (!Utilities.serviceIsRunningInForeground(getContext(), LocationService.class)) {
            Utilities.logoutUser(getContext(), getActivity(), deviceTokenViewModel, this);
        } else {
            dismissExistingComponentIfAny();
            confirmationComponent = new ConfirmationComponent(getContext(), getContext().getString(R.string.active_trip), getContext().getString(R.string.there_is_an_active_trip_running_do_you_want), false, getContext().getString(R.string.yes), getContext().getString(R.string.no), popupActions, SERVICE_RUNNING_LOGOUT_DIALOG_ID);
        }
    }

    private void setCurrentThemeIcons() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        passwordIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        phoneIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        taxInfoIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        logoutIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        cameraIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        homeIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
