package com.dukeai.android.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup


public class UploadSuccessFragment extends Fragment {


    View successView;
    @BindView(R.id.header)
    CustomHeader customHeader;
    @BindView(R.id.status)
    TextView statusText;
    private UploadDocumentInterface mListener;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public UploadSuccessFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static UploadSuccessFragment newInstance(String param1, String param2) {
        UploadSuccessFragment fragment = new UploadSuccessFragment();
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
        successView = inflater.inflate(R.layout.fragment_upload_success, container, false);
        ButterKnife.bind(this, successView);
        setCustomHeader();
        setUserName();
        successView.setFocusableInTouchMode(true);
        successView.requestFocus();
        successView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    clearDocumentsData();
                    NavigationFlowManager.openFragments(HomeFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper);
                    return true;
                }
                return false;
            }
        });
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        return successView;
    }

    private void setUserName() {
        if (Duke.userName != null) {
            statusText.setText(getString(R.string.hello_your_document_was_uploaded_we_will_get_back_to_you_if_we_are_unable_to_process_the_document).replaceFirst(AppConstants.StringConstants.HELLO, Duke.userName));
        }
    }

    private void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.document_uploaded));
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.showHideBackButton(View.GONE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            mListener = (UploadDocumentInterface) context;
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

    void clearDocumentsData() {
        Duke.imageStoragePath = "";
        Duke.uploadingImageStoragePaths = new ArrayList<>();
        Duke.uploadingImagesList = new ArrayList<>();
    }

    @OnClick(R.id.add_another_document)
    void onClickAnotherDocument() {
        // Firebase: Send click upload button event
//        Bundle params = new Bundle();
//        params.putString("Page", "Upload_Confirmation");
//        mFirebaseAnalytics.logEvent("AddDocument", params);

        if (mListener != null) {
            clearDocumentsData();
            mListener.uploadDocumentListener(false);
        }
    }

    public void onBackPressed() {
        NavigationFlowManager.openFragments(HomeFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper);
    }

    @OnClick(R.id.go_back_home)
    void onClickGoBackHome() {
        NavigationFlowManager.openFragments(new HomeFragment(), null, getActivity(), R.id.dashboard_wrapper);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
