package com.dukeai.android.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup

public class PreviewFragment extends Fragment implements HeaderActions, PopupActions {

    View preview;
    @BindView(R.id.custom_header)
    CustomHeader customHeader;
    @BindView(R.id.preview_image)
    ImageView previewImage;
    Bitmap imageBitmap;
    UploadDocumentInterface uploadDocumentInterface;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int rotation = 0;
    Boolean isPermissionGranted = false;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public PreviewFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static PreviewFragment newInstance() {
        PreviewFragment fragment = new PreviewFragment();
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
        preview = inflater.inflate(R.layout.fragment_preview, container, false);
        ButterKnife.bind(this, preview);
        popupActions = this;
        return preview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCustomHeader();
        getArgumentsData();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    private void getArgumentsData() {
        Bundle args = getArguments();
        if (args != null) {
            imageBitmap = args.getParcelable(AppConstants.UploadDocumentsConstants.BITMAP_IMAGE);
            imageBitmap = getRotatedBitmap(imageBitmap);
            previewImage.setImageBitmap(imageBitmap);
        }
    }

    private void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.preview_caps));
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadDocumentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uploadDocumentInterface = null;
        popupActions = null;
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (id) {
            case AppConstants.PopupConstants.POSITIVE:
                confirmationComponent.dismiss();
                resetSelectionOfImages();
                if (uploadDocumentInterface != null) {
                    uploadDocumentInterface.uploadDocumentListener(true);
                }
                break;
            case AppConstants.PopupConstants.NEGATIVE:
                confirmationComponent.dismiss();
                break;
        }
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
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

    @OnClick(R.id.retake)
    void onClickRetake() {
        // Firebase: Send click retake button event
//        Bundle params = new Bundle();
//        params.putString("Button", "Retake");
//        mFirebaseAnalytics.logEvent("UploadProcess", params);
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.warning), getString(R.string.confirm_to_delete), false, getString(R.string.yes), getString(R.string.no), popupActions, 1);
    }

    @OnClick(R.id.preview_done)
    void onClickPreviewDone() {
        // Firebase: Send click okay button event
//        Bundle params = new Bundle();
//        params.putString("Button", "okay");
//        mFirebaseAnalytics.logEvent("UploadProcess", params);
        if (Duke.imageStoragePath != null && !Duke.imageStoragePath.isEmpty()) {
            imageBitmap = getRotatedBitmap(imageBitmap);
            if (isPermissionGranted) {
                proceedToUploadPreview();
            } else {
                checkWritePermissions();
            }
        } else {
            NavigationFlowManager.openFragments(UploadPreviewFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper);
            resetSelectionOfImages();
        }
    }

    private void resetSelectionOfImages() {
        Duke.imageStoragePath = "";
    }

    void proceedToUploadPreview() {
        Duke.imageStoragePath = saveImagePath(imageBitmap);
        Duke.uploadingImagesList.add(imageBitmap);
        Duke.uploadingImageStoragePaths.add(Duke.imageStoragePath);
        NavigationFlowManager.openFragments(UploadPreviewFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper);
        resetSelectionOfImages();
    }

    void checkWritePermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermissionGranted = true;
                        proceedToUploadPreview();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        isPermissionGranted = false;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_LONG).show();
            }
        }).onSameThread()
                .check();
    }

    String saveImagePath(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SavedImages");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "DukeFiles_" + timeStamp + ".jpg";
        File file = new File(myDir, fname);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root + "/SavedImages/" + fname;
    }

    @OnClick(R.id.rotate)
    void onClickRotate() {
        rotation += 90;
        rotation %= 360;
        Bitmap bitmap = getRotatedBitmap(imageBitmap);
        previewImage.setImageBitmap(bitmap);
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        if (rotation % 360 == 0) {
            return bitmap;
        }
        matrix.postRotate(rotation, bitmap.getWidth(),
                bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    @OnClick(R.id.add_picture)
    void onClickCamera() {
        // Firebase: Send click upload button event
//        Bundle params = new Bundle();
//        params.putString("Page", "Preview");
//        mFirebaseAnalytics.logEvent("AddDocument", params);

        if (Duke.imageStoragePath != null && !Duke.imageStoragePath.isEmpty()) {
            Duke.uploadingImagesList.add(imageBitmap);
            Duke.uploadingImageStoragePaths.add(Duke.imageStoragePath);
            resetSelectionOfImages();
        }

        if (uploadDocumentInterface != null) {
            rotation = 0;
            uploadDocumentInterface.uploadDocumentListener(false);
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
