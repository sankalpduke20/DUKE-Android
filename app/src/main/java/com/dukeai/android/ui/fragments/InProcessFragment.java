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
import com.dukeai.android.adapters.InProcessDataAdapter;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateTabCount;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.InProcessDocumentsModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InProcessFragment extends ViewLifecycleFragment implements InProcessDataAdapter.OnListItemClickListener, PopupActions {

    public static final String TAG = InProcessFragment.class.getSimpleName();
    public static InProcessDataAdapter adapter;
    Context context;
    View inProcessView;
    ArrayList<InProcessDocumentsModel> dataModels = new ArrayList<>();
    FileStatusViewModel fileStatusViewModel;
    @BindView(R.id.in_process_view)
    RecyclerView recyclerView;
    @BindView(R.id.in_process_empty_card)
    RelativeLayout emptyCardLayout;
    CustomProgressLoader customProgressLoader;
    UpdateTabCount updateTabCount;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int screenWidth, screenHeight;
    private OnFragmentInteractionListener mListener;

    public InProcessFragment() {
        // Required empty public constructor
    }

    public static InProcessFragment newInstance(String param1, String param2) {
        InProcessFragment fragment = new InProcessFragment();
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
        inProcessView = inflater.inflate(R.layout.fragment_in_process, container, false);
        ButterKnife.bind(this, inProcessView);
        popupActions = this;
        updateTabCount = (UpdateTabCount) getParentFragment();
        setInitials();
        return inProcessView;
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    private void setInitials() {
        dataModels.clear();
        getWindowWidthAndHeight();
        customProgressLoader = new CustomProgressLoader(getContext());
        fileStatusViewModel = ViewModelProviders.of(getActivity()).get(FileStatusViewModel.class);
        setRecyclerAdapter();
    }

    private void setDataToAdapter() {
        dataModels.clear();
        dataModels.addAll(Duke.fileStatusModel.getInProcessDocumentsModels());
        if (dataModels.size() <= 0) {
            return;
        }
        adapter.updateDataList(dataModels);
    }

    private void setRecyclerAdapter() {
        adapter = new InProcessDataAdapter(context, R.layout.layout_in_process_rejected_item, false, InProcessFragment.this, fileStatusViewModel, this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);
    }

    public void onListItemClick(int pos, InProcessDocumentsModel inProcessDataModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.StringConstants.INPROCESS_DATA_MODEL, inProcessDataModel);
        NavigationFlowManager.openFragmentsWithOutBackStack(new DocumentDetailsFragment(), bundle, getActivity(), R.id.dashboard_wrapper, InProcessFragment.TAG);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
                    if (Duke.fileStatusModel != null && Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getInProcessDocumentsModels().size() > 0) {
                        setDataToAdapter();
                        if (updateTabCount != null) {
                            updateTabCount.UpdateTabCount();
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyCardLayout.setVisibility(View.GONE);
                        customProgressLoader.hideDialog();
                    } else {
                        customProgressLoader.hideDialog();
                        recyclerView.setVisibility(View.GONE);
                        emptyCardLayout.setVisibility(View.VISIBLE);
                        TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                        textView.setText(getString(R.string.no_in_process_documents_to_list));

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

                    if (Duke.fileStatusModel != null && Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getInProcessDocumentsModels().size() == 0) {
                        if (updateTabCount != null) {
                            updateTabCount.UpdateTabCount();
                        }
                    }
                } else {
                    customProgressLoader.hideDialog();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
    public void onPopupActions(String id, int dialogId) {
        confirmationComponent.dismiss();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
