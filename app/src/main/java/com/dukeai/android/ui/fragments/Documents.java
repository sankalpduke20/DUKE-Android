package com.dukeai.android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.dukeai.android.ui.activities.PaymentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.DocumentsViewPagerAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.UpdateTabCount;
import com.dukeai.android.interfaces.UpdateTabStylesListener;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dukeai.android.Duke.isDocumentAddingToLoad;
import static com.dukeai.android.Duke.isLoadDocument;

// Firebase: Setup

public class Documents extends Fragment implements HeaderActions, UpdateTabStylesListener, UpdateTabCount {

    String TAG = Documents.class.getSimpleName();
    View documentsView;
    @BindView(R.id.view_pager_documents)
    ViewPager viewPager;
    @BindView(R.id.tab_layout_documents)
    TabLayout tabView;
    @BindView(R.id.documents_header)
    CustomHeader customHeader;
    @BindView(R.id.upload_button)
    FloatingActionButton uploadButton;
    UploadDocumentInterface uploadDocumentInterface;
    DocumentsViewPagerAdapter documentsViewPagerAdapter;
    InProcessFragment inProcessFragment;
    ProcessedFragment processedFragment;
    RejectedDocumentsFragment rejectedDocumentsFragment;
    String toFragment = AppConstants.StringConstants.IN_PROCESS;
    String currentFragment = "";
    private OnFragmentInteractionListener mListener;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public Documents() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Documents newInstance() {
        Documents fragment = new Documents();
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
        documentsView = inflater.inflate(R.layout.fragment_documents, container, false);
        ButterKnife.bind(this, documentsView);
        setButtonThemeColor();
        return documentsView;
    }

    private void getArgumentsData() {
        Bundle args = getArguments();
        if (args != null && args.getString(AppConstants.StringConstants.NAVIGATE_TO) != null) {
            toFragment = args.getString(AppConstants.StringConstants.NAVIGATE_TO);
        }
        if(args != null && args.getString(AppConstants.StringConstants.DELETE_DOC_FROM_LOAD) != null) {
            Toast.makeText(getContext(), args.getString(AppConstants.StringConstants.DELETE_DOC_FROM_LOAD), Toast.LENGTH_LONG).show();
        }
        if(args != null && args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD) != null) {
            Toast.makeText(getContext(), args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD), Toast.LENGTH_LONG).show();
        }
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorTransparent));
    }

    private void setCustomTabView() {
        for (int i = 0; i < tabView.getTabCount(); i++) {
            TabLayout.Tab tab = tabView.getTabAt(i);
            assert tab != null;
            tab.setCustomView(documentsViewPagerAdapter.getTabView(i));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCustomHeader();
        setToolbarColor();
        getArgumentsData();
        setUpViewPager(viewPager);
        Duke.PDFDocFilenames.clear();
        Duke.PDFDocURIs.clear();
        tabView.setupWithViewPager(viewPager);
        // ITERATE OVER ALL TABS AND SET THE CUSTOM VIEW
        setCustomTabView();
        tabView.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Firebase: Send click in_process button event
                    Bundle params = new Bundle();
                    params.putString("Type", "In_process");
                    mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
                    currentFragment = AppConstants.StringConstants.IN_PROCESS;
                } else if (tab.getPosition() == 1) {
                    // Firebase: Send click processed button event
                    Bundle params = new Bundle();
                    params.putString("Type", "Processed");
                    mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
                    currentFragment = AppConstants.StringConstants.PROCESS;
                } else if (tab.getPosition() == 2) {
                    // Firebase: Send click rejected button event
                    Bundle params = new Bundle();
                    params.putString("Type", "Rejected");
                    mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
                    currentFragment = AppConstants.StringConstants.REJECT;
                }
                setCustomView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setCustomView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setCustomView(tab, true);
            }
        });
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        if (toFragment.equals(AppConstants.StringConstants.REJECT)) {
            // Firebase: Send click rejected button event
            Bundle params = new Bundle();
            params.putString("Type", "Rejected");
            mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
            currentFragment = AppConstants.StringConstants.REJECT;
            tabView.getTabAt(2).select();
        } else if (toFragment.equals(AppConstants.StringConstants.PROCESS)) {
            // Firebase: Send click processed button event
            Bundle params = new Bundle();
            params.putString("Type", "Processed");
            mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
            currentFragment = AppConstants.StringConstants.PROCESS;
            tabView.getTabAt(1).select();
        } else {
            // Firebase: Send click in_process button event
            Bundle params = new Bundle();
            params.putString("Type", "In_process");
            mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
            currentFragment = AppConstants.StringConstants.IN_PROCESS;
            tabView.getTabAt(0).select();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.title_activity_documents));
        customHeader.showHideProfileImage(View.VISIBLE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
        if (Duke.ProfileImage != null) {
            customHeader.setHeaderImage(Duke.ProfileImage);
        }
    }


    private void setUpViewPager(ViewPager viewPager) {
        documentsViewPagerAdapter = new DocumentsViewPagerAdapter(getContext(), getChildFragmentManager(), this);
        inProcessFragment = new InProcessFragment();
        processedFragment = new ProcessedFragment();
        rejectedDocumentsFragment = new RejectedDocumentsFragment();
        documentsViewPagerAdapter.addFragment(inProcessFragment, getString(R.string.in_process));
        documentsViewPagerAdapter.addFragment(processedFragment, getString(R.string.processed));
        documentsViewPagerAdapter.addFragment(rejectedDocumentsFragment, getString(R.string.rejected));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setSaveFromParentEnabled(false);
        viewPager.setAdapter(documentsViewPagerAdapter);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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


    public void setCustomView(TabLayout.Tab tab, boolean isSelected) {
        View view = tab.getCustomView();
        if (view == null) {
            return;
        }
        TextView textView = view.findViewById(R.id.document_tab_item);
        if (isSelected) {
            textView.setTextSize(13);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlack));
            setHighlightedColor(textView);
        } else {
            textView.setTextSize(12);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorCrete));
            setCurrentTheme(textView);

        }

        String inProcessString = Utilities.getStrings(Duke.getInstance(), R.string.in_process);
        String rejectedString = Utilities.getStrings(Duke.getInstance(), R.string.rejected);
        String processString = Utilities.getStrings(Duke.getInstance(), R.string.processed);
        if (Duke.fileStatusModel != null) {
            if (Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getInProcessDocumentsModels().size() > 0) {
                inProcessString = Utilities.getStrings(Duke.getInstance(), R.string.in_process) + "(" + Duke.fileStatusModel.getInProcessDocumentsModels().size() + ")";
            }
            if (Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getRejectedDocumentsModels().size() > 0) {
                rejectedString = Utilities.getStrings(Duke.getInstance(), R.string.rejected) + "(" + Duke.fileStatusModel.getRejectedDocumentsModels().size() + ")";
            }
        }
        if (tab.getPosition() == 0) {
            textView.setText(inProcessString);
        } else if (tab.getPosition() == 1) {
            textView.setText(processString);
        } else {
            textView.setText(rejectedString);
        }

        View line = view.findViewById(R.id.item_indicator);
        setCurrentLineTheme(line);
        line.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
    }

    private void setCurrentLineTheme(View line) {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        line.setBackgroundColor(Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
    }

    @Override
    public void UpdateTabStyles() {
        updateTabText();
    }

    void updateTabText() {
        TabLayout.Tab tab = tabView.getTabAt(tabView.getSelectedTabPosition());
        setCustomTabView();
        setCustomView(tab, true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.upload_button)
    void onClickUpload() {
        // Firebase: Send click upload button event
        if( ( (Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD") ) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() == 0.0) ||
                ((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD") ) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() < 0.0)) {
            loadPaywall();
        } else {
            Bundle params = new Bundle();
            if (currentFragment == "REJECT") {
                params.putString("Page", "Document_rejected");
            } else if (currentFragment == "PROCESS") {
                params.putString("Page", "Document_processed");
            } else {
                params.putString("Page", "Document_" + currentFragment.toLowerCase());
            }
            mFirebaseAnalytics.logEvent("AddDocument", params);
            if (uploadDocumentInterface != null) {
                Utilities.resetFileData();
                if(isLoadDocument) {
                    Duke.isNewLoadBeingCreated = false;
                    isDocumentAddingToLoad = true;
                }
                uploadDocumentInterface.uploadDocumentListener(false);
            }
        }
    }

    private void loadPaywall() {
        Intent intent = new Intent(requireActivity(), PaymentActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onClickProfile() {
        Utilities.navigateToProfilePage(getActivity());
    }

    @Override
    public void onBackClicked() {
        if(Duke.isLoadDocument) {
            Duke.DocsOfALoad.clear();
            NavigationFlowManager.openFragments(Duke.loadsFragment, null, getActivity(), R.id.dashboard_wrapper);
        } else {
            NavigationFlowManager.openFragments(HomeFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.HOME);
        }
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    private void replaceTabsTitle() {
        String inProcessString = getString(R.string.in_process);
        String rejectedString = getString(R.string.rejected);
        if (Duke.fileStatusModel != null) {
            if (Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getInProcessDocumentsModels().size() > 0) {
                inProcessString = getString(R.string.in_process) + "(" + Duke.fileStatusModel.getInProcessDocumentsModels().size() + ")";
            }
            if (Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getRejectedDocumentsModels().size() > 0) {
                rejectedString = getString(R.string.rejected) + "(" + Duke.fileStatusModel.getRejectedDocumentsModels().size() + ")";
            }
        }
        documentsViewPagerAdapter.setTitleList(0, inProcessString);
        documentsViewPagerAdapter.setTitleList(2, rejectedString);
    }

    @Override
    public void UpdateTabCount() {
        TabLayout.Tab tab = tabView.getTabAt(tabView.getSelectedTabPosition());
        setCustomView(tab, true);
        replaceTabsTitle();
    }

    private void setCurrentTheme(TextView textView) {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        textView.setTextColor(Color.parseColor(changeThemeModel.getTabTextColor()));
    }

    private void setHighlightedColor(TextView textView) {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        textView.setTextColor(Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
    }

    private void setButtonThemeColor() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        uploadButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        uploadButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
