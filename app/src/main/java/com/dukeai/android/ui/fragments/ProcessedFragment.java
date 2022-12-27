package com.dukeai.android.ui.fragments;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.ProcessedDocumentsAdapter;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateTabCount;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.LoadDocumentModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.ProcessedDocumentsModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.LoadsViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.dukeai.android.Duke.DocsOfALoad;
import static com.dukeai.android.Duke.isLoadDocument;

// Firebase: Setup
public class ProcessedFragment extends ViewLifecycleFragment implements ProcessedDocumentsAdapter.OnListItemClickListener, PopupActions {

    public static final String TAG = ProcessedFragment.class.getSimpleName();
    public ProcessedDocumentsAdapter adapter;
    Context context;
    View processedView;
    ArrayList<ProcessedDocumentsModel> dataModels = new ArrayList<>();
    @BindView(R.id.processed_documents_view)
    RecyclerView recyclerView;
    @BindView(R.id.processed_empty_card)
    RelativeLayout emptyCardLayout;
    CustomProgressLoader customProgressLoader;
    FileStatusViewModel fileStatusViewModel;
    UpdateTabCount updateTabCount;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    private OnFragmentInteractionListener mListener;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isDocsPresentInLoads = false;
    @BindView(R.id.search_frame)
    FrameLayout searchFrame;
    int loadAdapterCurrentItem = -1;
    ArrayList<ProcessedDocumentsModel> filteredDocumentsList = new ArrayList<>();

    LoadsViewModel loadsViewModel;

    public ProcessedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        processedView = inflater.inflate(R.layout.fragment_processed, container, false);
        ButterKnife.bind(this, processedView);
        popupActions = this;
        updateTabCount = (UpdateTabCount) getParentFragment();
        setInitials();
        setSwipeActionCallback();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        return processedView;
    }


    private void setInitials() {
        dataModels.clear();
        if(isLoadDocument) {
            searchFrame.setVisibility(View.GONE);
        } else {
            searchFrame.setVisibility(View.VISIBLE);
        }
        loadsViewModel = ViewModelProviders.of(getActivity()).get(LoadsViewModel.class);
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        customProgressLoader = new CustomProgressLoader(getContext());
        setRecyclerAdapter();
//        updateDocumentsData();
    }

    private void setDataToAdapter() {
        dataModels.clear();
        if (!customProgressLoader.isShowing()) {
            customProgressLoader.showDialog();
        }
//        dataModels.addAll(Duke.fileStatusModel.getAllProcessedDocuments());
        if(isLoadDocument) {
            if(Duke.DocsOfALoad.size()>0) {
                for(ProcessedDocumentsModel processedDocumentsModel: Duke.fileStatusModel.getAllProcessedDocuments()) {
                    for(LoadDocumentModel loadDocumentModel: DocsOfALoad) {
                        if(processedDocumentsModel.getSha1().equals(loadDocumentModel.getSha1())) {
                            dataModels.add(processedDocumentsModel);
                        }
                    }
                }
            }
        } else {
//            dataModels.addAll(Duke.fileStatusModel.getAllProcessedDocuments());
            for(ProcessedDocumentsModel doc: Duke.fileStatusModel.getAllProcessedDocuments()) {
                if(!doc.getProcessedData().getTitle().equals("Load Document")) {
                    dataModels.add(doc);
                }
            }
        }
        if (dataModels.size() <= 0) {
            return;
        }
        Log.d("Processed Doc :",new Gson().toJson(dataModels));
        adapter.updateDataList(dataModels);
        adapter.notifyDataSetChanged();
        customProgressLoader.hideDialog();
    }

    private void setRecyclerAdapter() {
        adapter = new ProcessedDocumentsAdapter(context, R.layout.layout_processed_document_item, ProcessedFragment.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);
    }

    public void onListItemClick(int pos, ProcessedDocumentsModel dataModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.StringConstants.PROCESSED_DATA_MODEL, dataModel);
        bundle.putBoolean("IS_FROM_SEARCH", false);
        Log.d("Processed Doc Selected Data ",new Gson().toJson(dataModel));
        NavigationFlowManager.openFragmentsWithOutBackStack(new ProcessedDocumentsDetailsFragment(), bundle, getActivity(), R.id.dashboard_wrapper, ProcessedFragment.TAG);
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
                    if (Duke.fileStatusModel != null && Duke.fileStatusModel.getInProcessDocumentsModels() != null && Duke.fileStatusModel.getAllProcessedDocuments().size() > 0) {
                        setDataToAdapter();
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyCardLayout.setVisibility(View.GONE);
                        if(Duke.isLoadDocument) {
                            if(Duke.DocsOfALoad.size()>0) {
                                if (updateTabCount != null) {
                                    updateTabCount.UpdateTabCount();
                                }
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyCardLayout.setVisibility(View.GONE);
                            } else {
                                customProgressLoader.hideDialog();
                                recyclerView.setVisibility(View.GONE);
                                emptyCardLayout.setVisibility(View.VISIBLE);
                                TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                                textView.setText(getString(R.string.no_docs_present_in_this_load));
                            }
                        } else {
                            if(dataModels.size()>0) {
                                if (updateTabCount != null) {
                                    updateTabCount.UpdateTabCount();
                                }
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyCardLayout.setVisibility(View.GONE);
                            } else {
                                customProgressLoader.hideDialog();
                                recyclerView.setVisibility(View.GONE);
                                emptyCardLayout.setVisibility(View.VISIBLE);
                                TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                                textView.setText(getString(R.string.no_processed_documents_to_list));
                            }
                        }
                    } else {
                        customProgressLoader.hideDialog();
                        recyclerView.setVisibility(View.GONE);
                        emptyCardLayout.setVisibility(View.VISIBLE);
                        TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                        textView.setText(getString(R.string.no_processed_documents_to_list));

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

    @OnClick(R.id.search_frame)
    public void onSearchFrameClick() {
        Bundle params = new Bundle();
        params.putString("Type", "Search");
        mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
        NavigationFlowManager.openFragments(new SearchProcessedDocumentsFragment(), null, getActivity(), R.id.dashboard_wrapper, ProcessedFragment.TAG);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setSwipeActionCallback() {

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Take action for the swiped item
                int position = viewHolder.getAdapterPosition();

                loadAdapterCurrentItem = position;
                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        Log.d(Duke.TAG, " swiped");
                        ArrayList<String> docSHA1s = new ArrayList<>();

                        docSHA1s.add(dataModels.get(loadAdapterCurrentItem).getSha1());

                        Log.d("sddf", docSHA1s.toString());

                        //Transmit Processed Docs API
                        customProgressLoader.showDialog();
                        loadsViewModel.getTransmitProcessedDocsLiveData(docSHA1s).observe(getActivity(), new Observer<LoadsTransmitModel>() {
                            @Override
                            public void onChanged(LoadsTransmitModel loadsTransmitModel) {
                                customProgressLoader.hideDialog();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), loadsTransmitModel.message.toString(), Toast.LENGTH_LONG).show();

                            }
                        });
                        break;
                    case ItemTouchHelper.RIGHT:
                        break;
                }
            }

            //Add icons for swipe on delete action

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlue))
                        .addSwipeLeftActionIcon(R.drawable.ic_mailshare)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}
