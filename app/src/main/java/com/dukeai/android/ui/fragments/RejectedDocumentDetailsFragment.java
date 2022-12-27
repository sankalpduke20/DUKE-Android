package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ImageView;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.adapters.ProcessedDocumentsFlipperAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateDocumentNavigationIcons;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.FileDeleteSuccessModel;
import com.dukeai.android.models.RejectedDocumentsModel;
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

// Firebase: Setup

public class RejectedDocumentDetailsFragment extends Fragment implements HeaderActions, PopupActions, UpdateDocumentNavigationIcons {

    View rejectedDetailsPageView;
    RejectedDocumentsModel data;
    @BindView(R.id.header)
    CustomHeader customHeader;
    @BindView(R.id.reason)
    TextView reason;
    @BindView(R.id.rejected_flipper)
    AdapterViewFlipper rejectedFlipper;
    @BindView(R.id.previous_button)
    ImageView prevButton;
    @BindView(R.id.next_button)
    ImageView nextButton;
    UploadDocumentInterface uploadDocumentInterface;
    FileStatusViewModel fileStatusViewModel;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    ProcessedDocumentsFlipperAdapter processedDocumentsFlipperAdapter;
    ArrayList<DownloadImageModel> dataModel = new ArrayList<>();
    int screenWidth, screenHeight;
    UpdateDocumentNavigationIcons updateDocumentNavigationIcons;
    int apiCount = 0;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public RejectedDocumentDetailsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RejectedDocumentDetailsFragment newInstance(String param1, String param2) {
        RejectedDocumentDetailsFragment fragment = new RejectedDocumentDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rejectedDetailsPageView = inflater.inflate(R.layout.fragment_rejected_document_details, container, false);
        ButterKnife.bind(this, rejectedDetailsPageView);
        popupActions = this;
        updateDocumentNavigationIcons = this;
        setInitials();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        return rejectedDetailsPageView;
    }

    private void setInitials() {
        apiCount = 0;
        getWindowWidthAndHeight();
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        customHeader.setToolbarTitle(getString(R.string.rejected));
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.showHideProfileImage(View.GONE);
        customHeader.setHeaderActions(this);
        getArgumentsData();
    }

    private void getArgumentsData() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.get(AppConstants.StringConstants.REJECTED_DATA_MODEL) != null) {
            data = (RejectedDocumentsModel) bundle.getSerializable(AppConstants.StringConstants.REJECTED_DATA_MODEL);
            if (data != null)
                updateDataInFragment(data);
        }
    }

    private void updateDataInFragment(RejectedDocumentsModel data) {
        reason.setText(data.getDescription());
        setImagesInFlipper(data.getPages());
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    private void setImagesInFlipper(final ArrayList<String> pages) {
        dataModel.clear();
        if (pages != null && pages.size() > 0) {
            customProgressLoader.showDialog();
        }
        apiCount = 0;
        loadDocuments(pages);
    }


    public void loadDocuments(final ArrayList<String> pages) {
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
                        rejectedFlipper.setAdapter(processedDocumentsFlipperAdapter);
                    } else {
                        customProgressLoader.hideDialog();
                    }
                }
                apiCount++;
                if (apiCount < pages.size()) {
                    loadDocuments(pages);
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @OnClick(R.id.next_button)
    public void showNextImage() {
        // Firebase: Send click right button event
        Bundle params = new Bundle();
        params.putString("Page", "Rejected Detail");
        mFirebaseAnalytics.logEvent("ScrolltoRightBt", params);
        rejectedFlipper.showNext();
    }

    @OnClick(R.id.previous_button)
    public void showPreviousImage() {
        // Firebase: Send click left button event
        Bundle params = new Bundle();
        params.putString("Page", "Rejected Detail");
        mFirebaseAnalytics.logEvent("ScrolltoLeftBt", params);
        rejectedFlipper.showPrevious();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uploadDocumentInterface = null;
    }

    @OnClick(R.id.close_fab_icon)
    void removeRejectedDocument() {
        // Firebase: Send click delete button event
        Bundle params = new Bundle();
        params.putString("Page", "Rejected Detail");
        mFirebaseAnalytics.logEvent("DeleteButtonClick", params);
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.warning), getString(R.string.picture_will_not_be_there), false, getString(R.string.yes), getString(R.string.no), popupActions, 1);
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
                confirmationComponent.dismiss();
                customProgressLoader.hideDialog();
                if (deviceTokenModel != null) {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.success), getString(R.string.delete_success), false, getString(R.string.ok), popupActions, 1);
                }
            }
        });
    }

    void navigateToRejects() {
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.REJECT);
        NavigationFlowManager.openFragments(Documents.newInstance(), args, getActivity(), R.id.dashboard_wrapper);
    }

    @OnClick(R.id.upload_button)
    void uploadDocument() {
        // Firebase: Send click upload button event
        Bundle params = new Bundle();
        params.putString("Page", "Rejected Detail");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        if (uploadDocumentInterface != null) {
            uploadDocumentInterface.uploadDocumentListener(false);
        }
    }


    @Override
    public void onClickProfile() {
        Utilities.navigateToProfilePage(getActivity());
    }

    @Override
    public void onBackClicked() {
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.REJECT);
        NavigationFlowManager.openFragments(Documents.newInstance(), args, getActivity(), R.id.dashboard_wrapper);
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
                navigateToRejects();
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
