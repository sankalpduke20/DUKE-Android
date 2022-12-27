package com.dukeai.android.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.bottomSheetDialogs.ImageSelection;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadImageActions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

//import static butterknife.internal.Utils.arrayOf;
import static com.dukeai.android.ui.activities.BaseActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.dukeai.android.ui.activities.BaseActivity.IMAGE_EXTENSION;
import static com.dukeai.android.ui.activities.BaseActivity.KEY_IMAGE_STORAGE_PATH;
import static com.dukeai.android.ui.activities.BaseActivity.MEDIA_TYPE_IMAGE;
import static com.dukeai.android.ui.activities.BaseActivity.PICK_IMAGE_REQUEST;

public class
UploadDocument implements UploadImageActions, PopupActions {
    boolean isPermissionAccepted = false;
    ConfirmationComponent confirmationComponent;
    boolean fromProfile = false;
    Fragment fragment;
    Boolean isCameraOpenByDefault = false;
    private Context baseContext;
    private Activity baseActivity;
    private ImageSelection imageSelection;
    private Bundle savedInstance;

    public UploadDocument(Context context, Activity activity, Bundle savedInstanceState, Boolean isFromProfile, Fragment fragment) {
        this.baseContext = context;
        this.baseActivity = activity;
        this.savedInstance = savedInstanceState;
        imageSelection = new ImageSelection(context, this, isFromProfile);
        imageSelection.showDialog();
        fromProfile = isFromProfile;
        this.fragment = fragment;
        isCameraOpenByDefault = false;
        this.isPermissionAccepted = false;
    }

    public UploadDocument(Context context, Activity activity, Bundle savedInstanceState, Boolean isFromProfile, Fragment fragment, Boolean isCameraOpen) {
        this.baseContext = context;
        this.baseActivity = activity;
        this.savedInstance = savedInstanceState;
        imageSelection = new ImageSelection(context, this, isFromProfile);
        fromProfile = isFromProfile;
        this.fragment = fragment;
        isCameraOpenByDefault = isCameraOpen;
        if (isCameraOpenByDefault) {
            openCamera();
        } else {
            imageSelection.showDialog();
        }
        this.isPermissionAccepted = false;
    }

    private void requestCameraPermission(final int type, final String from) {
        Dexter.withActivity(baseActivity)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (type == MEDIA_TYPE_IMAGE) {
                            if (report.areAllPermissionsGranted()) {
                                isPermissionAccepted = true;
                                if (from.equals(AppConstants.UploadDocumentsConstants.FROM_GALLERY)) {
                                    openGallery();
                                } else {
                                    captureImage();
                                }
                            } else if (report.isAnyPermissionPermanentlyDenied()) {
                                isPermissionAccepted = false;
                                showSettingsDialog();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
            }
        }).onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        confirmationComponent = new ConfirmationComponent(baseContext, Utilities.getStrings(Duke.getInstance(), R.string.need_permissions), Utilities.getStrings(Duke.getInstance(), R.string.few_permissions_needed), false, Utilities.getStrings(Duke.getInstance(), R.string.goto_settings), Utilities.getStrings(Duke.getInstance(), R.string.cancel), this, 1);
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(AppConstants.UploadDocumentsConstants.PACKAGE, baseActivity.getPackageName(), null);
        intent.setData(uri);
        if (fromProfile) {
            fragment.startActivityForResult(intent, 101);
        } else {
            baseActivity.startActivityForResult(intent, 101);
        }
    }

    private void captureImage() {
        closeBottomSheetDialog();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = null;
        try {
            file = CameraUtils.createImageFile(baseActivity);
        } catch (IOException ignored) {
        }
        if (file != null) {
            if (fromProfile) {
                Duke.profileImageStoragePath = file.getAbsolutePath();
            } else {
                Duke.imageStoragePath = file.getAbsolutePath();
            }
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(baseActivity.getApplicationContext(), file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (fromProfile) {
            fragment.startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        } else {
            baseActivity.startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    private void closeBottomSheetDialog() {
        imageSelection.dismissDialog();
    }

    private void openGallery() {
        closeBottomSheetDialog();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            intent.setDataAndType(MediaStore.Downloads.EXTERNAL_CONTENT_URI, "image/*|application/pdf");
//        }
//        intent.setType("image/jpeg");
        String[] mimeTypes = {"image/*", "application/pdf"};
//        String mimeTypes[] = arrayOf("image/*", "application/pdf");
        intent.setType("image/*|application/pdf");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (fromProfile) {
            fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        } else {
            baseActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    }

    void openCamera() {
        if (isPermissionAccepted) {
            closeBottomSheetDialog();
            setInitialsForCameraCapture();
            restoreFromBundle(savedInstance);
        } else {
            requestCameraPermission(MEDIA_TYPE_IMAGE, AppConstants.UploadDocumentsConstants.FROM_CAMERA);
        }
    }

    @Override
    public void openGalleryImages() {
        if (isPermissionAccepted) {
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
//                if (Environment.isExternalStorageManager()) {
//                    //todo when permission is granted
//                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    String[] mimetypes = {"image/*", "application/*"};
//                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
////                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, path);
//                    if (fromProfile) {
//                        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                    } else {
//                        baseActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                    }
//                } else {
//                    //request for the permission
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                    Uri uri = Uri.fromParts("package", baseActivity.getPackageName(), null);
//                    intent.setData(uri);
//                    if (fromProfile) {
//                        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                    } else {
//                        baseActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                    }
//                }
//            } else {
//                openGallery();
//            }
            openGallery();
        } else {
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
//                if (Environment.isExternalStorageManager()) {
//                    //todo when permission is granted
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    String[] mimetypes = {"image/*", "application/pdf"};
//                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//                    baseActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                } else {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    String[] mimetypes = {"image/*", "application/pdf"};
//                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//                    baseActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//                }
//            } else {
//                requestCameraPermission(MEDIA_TYPE_IMAGE, AppConstants.UploadDocumentsConstants.FROM_GALLERY);
//            }
            requestCameraPermission(MEDIA_TYPE_IMAGE, AppConstants.UploadDocumentsConstants.FROM_GALLERY);
        }
    }

    @Override
    public void openCameraImages() {
        openCamera();
    }

    private void setInitialsForCameraCapture() {
        if (!CameraUtils.isDeviceSupportCamera(baseActivity.getApplicationContext())) {
            Toast.makeText(baseActivity.getApplicationContext(),
                    Utilities.getStrings(Duke.getInstance(), R.string.sorry_device_does_not_support),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                if (fromProfile) {
                    Duke.profileImageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                    if (!TextUtils.isEmpty(Duke.profileImageStoragePath)) {
                        if (Duke.profileImageStoragePath.substring(Duke.profileImageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                            closeBottomSheetDialog();
                        }
                    }
                } else {
                    Duke.imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                    if (!TextUtils.isEmpty(Duke.imageStoragePath)) {
                        if (Duke.imageStoragePath.substring(Duke.imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                            closeBottomSheetDialog();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (id) {
            case AppConstants.PopupConstants.POSITIVE:
                confirmationComponent.dismiss();
                openSettings();
                break;
            case AppConstants.PopupConstants.NEGATIVE:
                confirmationComponent.dismiss();
                break;
        }
    }
}
