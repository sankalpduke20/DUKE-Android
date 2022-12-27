package com.dukeai.android.bottomSheetDialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.UploadImageActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.Utilities;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup

public class ImageSelection extends BottomSheetDialog {
    UploadImageActions uploadImageActions;
    View sheetView;
    Boolean isFromProfile;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.camera_image)
    ImageView cameraIcon;
    @BindView(R.id.gallery_image)
    ImageView galleryIcon;
    @BindView(R.id.close)
    ImageView closeIcon;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;


    public ImageSelection(@NonNull Context context, UploadImageActions uploadImageInterface, Boolean fromProfile) {
        super(context);
        this.uploadImageActions = uploadImageInterface;
        this.isFromProfile = fromProfile;
        sheetView = getLayoutInflater().inflate(R.layout.image_selection_bottom_sheet, null);
        setContentView(sheetView);
        ButterKnife.bind(this, sheetView);
        setTitle();
        changeTheme();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private void setTitle() {
        if (isFromProfile) {
            title.setText(Utilities.getStrings(Duke.getInstance(), R.string.upload_photo));
        } else {
            title.setText(Utilities.getStrings(Duke.getInstance(), R.string.add_a_document));
        }
    }

    @OnClick(R.id.camera)
    void openCameraOptions() {
        // Firebase: Send click Camera button event
        Bundle params = new Bundle();
        params.putString("Type", "Camera");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        uploadImageActions.openCameraImages();
    }

    @OnClick(R.id.gallery)
    void openGalleryOptions() {
        // Firebase: Send click Gallery button event
        Bundle params = new Bundle();
        params.putString("Type", "Gallery");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        uploadImageActions.openGalleryImages();
    }

    @OnClick(R.id.close)
    void onClickCloseIcon() {
        Bundle params = new Bundle();
        params.putString("Type", "Cancel");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        this.dismiss();
    }

    public void showDialog() {
        this.show();
    }

    public void dismissDialog() {
        this.dismiss();
    }

    public void changeTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        cameraIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        galleryIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        title.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        closeIcon.setColorFilter(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
    }
}
