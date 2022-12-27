package com.dukeai.android.ui.fragments;

import android.annotation.SuppressLint;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterViewFlipper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.R;
import com.dukeai.android.adapters.ProcessedDocumentsFlipperAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateDocumentNavigationIcons;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.FileDeleteSuccessModel;
import com.dukeai.android.models.InProcessDocumentsModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.INPUT_METHOD_SERVICE;

// Firebase: Setup

public class DocumentDetailsFragment extends Fragment implements HeaderActions, PopupActions, UpdateDocumentNavigationIcons {
    View inProcessView;
    InProcessDocumentsModel data;

    @BindView(R.id.header)
    CustomHeader customHeader;
    UploadDocumentInterface uploadDocumentInterface;
    @BindView(R.id.camera_fab_icon)
    FloatingActionButton cameraFabIcon;
    @BindView(R.id.previous_button)
    ImageButton prevButton;
    @BindView(R.id.next_button)
    ImageButton nextButton;
    FileStatusViewModel fileStatusViewModel;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    @BindView(R.id.in_process_flipper)
    AdapterViewFlipper inProcessFlipper;
    ProcessedDocumentsFlipperAdapter processedDocumentsFlipperAdapter;
    ArrayList<DownloadImageModel> dataModel = new ArrayList<>();
    int screenWidth, screenHeight;
    UpdateDocumentNavigationIcons updateDocumentNavigationIcons;
    int apiCount = 0;
    @BindView(R.id.close_fab_icon)
    FloatingActionButton closeFabIcon;
    @BindView(R.id.signature)
    FloatingActionButton signatureIcon;
    @BindView(R.id.submit)
    FloatingActionButton submitIcon;
    @BindView(R.id.submit_text)
    TextView submitText;
    @BindView(R.id.signature_text)
    EditText signature_text;
    @BindView(R.id.document_container)
    RelativeLayout documentContainer;
    @BindView(R.id.root)
    RelativeLayout rootLayout;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public DocumentDetailsFragment() {
        // Required empty public constructor
    }

    public static DocumentDetailsFragment newInstance() {
        DocumentDetailsFragment fragment = new DocumentDetailsFragment();
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
        inProcessView = inflater.inflate(R.layout.fragment_document_details, container, false);
        ButterKnife.bind(this, inProcessView);
        updateDocumentNavigationIcons = this;
        popupActions = this;
        setInitials();
        setCurrentTheme();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        documentContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                setSignatureInterfaceState();
            }
        });
        return inProcessView;
    }

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        closeFabIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        closeFabIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        signatureIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        signatureIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        submitIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        submitIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        cameraFabIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        cameraFabIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        prevButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        nextButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        prevButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        nextButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
    }

    private void setInitials() {
        apiCount = 0;
        getWindowWidthAndHeight();
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        setCustomHeader();
        setArgumentsData(getArguments());
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    @SuppressLint("RestrictedApi")
    public void setArgumentsData(Bundle bundle) {
        if (bundle != null && bundle.get(AppConstants.StringConstants.INPROCESS_DATA_MODEL) != null) {
            data = (InProcessDocumentsModel) bundle.getSerializable(AppConstants.StringConstants.INPROCESS_DATA_MODEL);
            if(data.getFileName().substring(0,3).equalsIgnoreCase("pdf")){
                cameraFabIcon.setVisibility(View.GONE);
            }
            if(data.getSignature() != null && data.getSignature().length()>0){
                signature_text.setText(data.getSignature());
            }
            customHeader.setToolbarTitle(getString(R.string.in_process));
            if (data != null)
                updateDataInFragment(data);
        }
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

    private void updateDataInFragment(InProcessDocumentsModel data) {
        setImagesInFlipper(data.getPages());
    }

    private void setImagesInFlipper(final ArrayList<String> pages) {
        dataModel.clear();
        if (pages != null && pages.size() > 0) {
            customProgressLoader.showDialog();
        }
        apiCount = 0;
        loadDocuments(pages);
    }

    private void loadDocuments(final ArrayList<String> pages) {
        fileStatusViewModel.downloadFile(pages.get(apiCount), screenWidth, screenHeight, true).observe(this, new Observer<DownloadImageModel>() {
            @Override
            public void onChanged(@Nullable DownloadImageModel downloadImageModel) {
                if (downloadImageModel != null) {
                    dataModel.add(downloadImageModel);
                    if ((pages.size()) == (dataModel.size())) {
                        customProgressLoader.hideDialog();
                        if (dataModel != null && dataModel.size() > 1) {
                            prevButton.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                        } else {
                            prevButton.setVisibility(View.GONE);
                            nextButton.setVisibility(View.GONE);
                        }
                        processedDocumentsFlipperAdapter = new ProcessedDocumentsFlipperAdapter(getContext(), R.layout.view_flipper_imageview, dataModel, updateDocumentNavigationIcons);
                        inProcessFlipper.setAdapter(processedDocumentsFlipperAdapter);
                    }
                } else {
                    customProgressLoader.hideDialog();
                }
                apiCount++;
                if (apiCount < pages.size()) {
                    loadDocuments(pages);
                }
            }
        });
    }


    @OnClick(R.id.next_button)
    public void showNextImage() {
        // Firebase: Send click right button event
        Bundle params = new Bundle();
        params.putString("Page", "In_process Detail");
        mFirebaseAnalytics.logEvent("ScrolltoRightBt", params);
        inProcessFlipper.showNext();
    }

    @OnClick(R.id.previous_button)
    public void showPreviousImage() {
        // Firebase: Send click left button event
        Bundle params = new Bundle();
        params.putString("Page", "In_process Detail");
        mFirebaseAnalytics.logEvent("ScrolltoLeftBt", params);
        inProcessFlipper.showPrevious();
    }

    private void setCustomHeader() {
        customHeader.showHideProfileImage(View.VISIBLE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uploadDocumentInterface = null;
    }

    @OnClick(R.id.camera_fab_icon)
    void onClickCameraIcon() {
        // Firebase: Send click upload button event
        Bundle params = new Bundle();
        params.putString("Page", "In_process Detail");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        if (uploadDocumentInterface != null) {
            uploadDocumentInterface.uploadDocumentListener(false);
        }
    }

    @OnClick(R.id.close_fab_icon)
    void onClickCloseIcon() {
        // Firebase: Send click delete button event
        Bundle params = new Bundle();
        params.putString("Page", "In_process Detail");
        mFirebaseAnalytics.logEvent("DeleteButtonClick", params);
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.warning), getString(R.string.picture_will_not_be_there), false, getString(R.string.yes), getString(R.string.no), popupActions, 1);
    }

    @OnClick(R.id.signature)
    void onClickSignatureIcon() {
        // Firebase: Send click delete button event
        documentContainer.setVisibility(View.GONE);
        closeFabIcon.setVisibility(View.GONE);
        cameraFabIcon.setVisibility(View.GONE);
        signatureIcon.setVisibility(View.GONE);
        submitIcon.setVisibility(View.VISIBLE);
        submitText.setVisibility(View.VISIBLE);

        if(signature_text.getText().toString().trim().matches("")) {
            signature_text.setText("");
        }

        signature_text.setSelection(signature_text.getText().length());
        signature_text.setVisibility(View.VISIBLE);
        signature_text.requestFocus();
        signature_text.setFocusableInTouchMode(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(signature_text, InputMethodManager.SHOW_FORCED);
    }

    @OnClick(R.id.submit)
    void onClickSubmitBtn() {
        String signature = signature_text.getText().toString();
        if(signature.length()>0 && !signature.trim().matches("")) {
            closeKeyboard();
            setSignatureInterfaceState();
            customProgressLoader.showDialog();
            Log.d("*********", signature + data.getSignature()+data.getSha1());
            String is_scan = "false";
            fileStatusViewModel.updateDocumentSignature(data.getSha1(), signature, is_scan).observe(this, new Observer<UpdatePaymentModel>() {
                @Override
                public void onChanged(@Nullable UpdatePaymentModel updatePaymentModel) {
                    customProgressLoader.hideDialog();
                    Toast toast=Toast.makeText(getContext(),"Document Signed Successfully!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 260); // last two args are X and Y are used for setting position
                    toast.setDuration(Toast.LENGTH_SHORT);//you can even use milliseconds to display toast
                    toast.show();
                }
            });
        } else {
            closeKeyboard();
            setSignatureInterfaceState();
        }
    }

    private void setSignatureInterfaceState() {
        documentContainer.setVisibility(View.VISIBLE);
        closeFabIcon.setVisibility(View.VISIBLE);
        cameraFabIcon.setVisibility(View.VISIBLE);
        signatureIcon.setVisibility(View.VISIBLE);
        submitIcon.setVisibility(View.GONE);
        submitText.setVisibility(View.GONE);
        signature_text.setVisibility(View.GONE);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void deleteFile() {
        if (data == null || data.getSha1() == null) {
            confirmationComponent.dismiss();
            return;
        }
        String sha1 = data.getSha1();
        if (sha1 == null) {
            confirmationComponent.dismiss();
            return;
        }
        customProgressLoader.showDialog();
        fileStatusViewModel.deleteFile(sha1).observe(this, new Observer<FileDeleteSuccessModel>() {
            @Override
            public void onChanged(@Nullable FileDeleteSuccessModel deviceTokenModel) {
                customProgressLoader.hideDialog();
                confirmationComponent.dismiss();
                if (deviceTokenModel != null) {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.success), getString(R.string.delete_success), false, getString(R.string.ok), popupActions, 1);
                }
            }
        });
    }

    void navigateToInProcess() {
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.IN_PROCESS);
        NavigationFlowManager.openFragments(new Documents(), args, getActivity(), R.id.dashboard_wrapper);
    }

    @Override
    public void onClickProfile() {
        Utilities.navigateToProfilePage(getActivity());
    }

    @Override
    public void onBackClicked() {
        closeKeyboard();
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.IN_PROCESS);
        NavigationFlowManager.openFragments(new Documents(), args, getActivity(), R.id.dashboard_wrapper);
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (id) {
            case AppConstants.PopupConstants.POSITIVE:
                deleteFile();
                break;
            case AppConstants.PopupConstants.NEGATIVE:
                confirmationComponent.dismiss();
                break;
            case AppConstants.PopupConstants.NEUTRAL:
                confirmationComponent.dismiss();
                navigateToInProcess();
                break;
        }
    }

    @Override
    public void UpdateDocumentNavigation(int position) {
        if (dataModel.size() > 1) {
            if (position == 0) {
                prevButton.setVisibility(View.GONE);
            } else {
                prevButton.setVisibility(View.VISIBLE);
            }
            if (position == dataModel.size() - 1) {
                nextButton.setVisibility(View.GONE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
