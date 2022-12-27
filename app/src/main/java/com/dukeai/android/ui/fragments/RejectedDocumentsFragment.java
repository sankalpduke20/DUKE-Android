package com.dukeai.android.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.RejectedAdapter;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateTabCount;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.RejectedDocumentsModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RejectedDocumentsFragment extends ViewLifecycleFragment implements RejectedAdapter.OnListItemClickListener, PopupActions {

    public static final String TAG = RejectedDocumentsFragment.class.getSimpleName();
    public RejectedAdapter adapter;
    View rejectedView;
    Context context;
    ArrayList<RejectedDocumentsModel> dataModels = new ArrayList<>();
    @BindView(R.id.process_documents_view)
    RecyclerView recyclerView;
    @BindView(R.id.rejected_empty_card)
    RelativeLayout emptyCardLayout;
    CustomProgressLoader customProgressLoader;
    FileStatusViewModel fileStatusViewModel;
    UpdateTabCount updateTabCount;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int screenWidth, screenHeight;
    private OnFragmentInteractionListener mListener;

    public RejectedDocumentsFragment() {
        // Required empty public constructor
    }

    public static RejectedDocumentsFragment newInstance(String param1, String param2) {
        RejectedDocumentsFragment fragment = new RejectedDocumentsFragment();
        return fragment;
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rejectedView = inflater.inflate(R.layout.fragment_rejected_documents, container, false);
        ButterKnife.bind(this, rejectedView);
        popupActions = this;
        updateTabCount = (UpdateTabCount) getParentFragment();
        setInitials();
        return rejectedView;
    }

    private void setInitials() {
        getWindowWidthAndHeight();
        dataModels.clear();
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        setRecyclerAdapter();
    }

    private void setAdapterData() {
        dataModels.clear();
        dataModels.addAll(Duke.fileStatusModel.getRejectedDocumentsModels());
        if (dataModels == null || dataModels.size() <= 0) {
            return;
        }
        adapter.updateDataList(dataModels);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customProgressLoader != null) {
            customProgressLoader.hideDialog();
        }
        dataModels.clear();
        if (!getUserVisibleHint()) {
            return;
        }
        updateDocumentsData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && customProgressLoader != null && isResumed()) {
            customProgressLoader.hideDialog();
            onResume();
        }
    }

    private void updateDocumentsData() {
        if (customProgressLoader == null || fileStatusViewModel == null) {
            return;
        }
        String numberOfDocs = "";
        customProgressLoader.showDialog();
        fileStatusViewModel.getFileStatusModelLiveData(numberOfDocs).observe(this, new Observer<FileStatusModel>() {
            @Override
            public void onChanged(@Nullable FileStatusModel fileStatusModel) {
                if (fileStatusModel != null) {
                    Duke.fileStatusModel = fileStatusModel;
                    if (Duke.fileStatusModel != null && Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getRejectedDocumentsModels().size() > 0) {
                        if (updateTabCount != null) {
                            updateTabCount.UpdateTabCount();
                        }
                        setAdapterData();
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyCardLayout.setVisibility(View.GONE);
                        customProgressLoader.hideDialog();
                    } else {
                        customProgressLoader.hideDialog();
                        recyclerView.setVisibility(View.GONE);
                        emptyCardLayout.setVisibility(View.VISIBLE);
                        TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                        textView.setText(getString(R.string.no_rejected_documents_to_list));

                        String message = "";
                        if (fileStatusModel != null && fileStatusModel.getMessage() != null) {
                            if (fileStatusModel.getMessage().contains(AppConstants.HomePageConstants.NO_GROUPS)) {
                                message = getString(R.string.not_part_of_any_group);
                            } else {
                                message = fileStatusModel.getMessage();
                            }
                            confirmationComponent = new ConfirmationComponent(getContext(), getResources().getString(R.string.error), message, false, getResources().getString(R.string.ok), popupActions, 1);
                        }
                    }
                } else {
                    customProgressLoader.hideDialog();
                }
            }
        });
    }

    private void setRecyclerAdapter() {
        adapter = new RejectedAdapter(context, R.layout.layout_in_process_rejected_item, true, RejectedDocumentsFragment.this, fileStatusViewModel, this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);
    }

    public void onListItemClick(int pos, RejectedDocumentsModel rejectedDocumentsModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.StringConstants.REJECTED_DATA_MODEL, rejectedDocumentsModel);
        NavigationFlowManager.openFragmentsWithOutBackStack(new RejectedDocumentDetailsFragment(), bundle, getActivity(), R.id.dashboard_wrapper, RejectedDocumentsFragment.TAG);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.hideDialog();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.hideDialog();
        }
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        confirmationComponent.dismiss();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
