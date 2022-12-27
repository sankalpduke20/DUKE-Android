package com.dukeai.android.ui.fragments;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.AssetListAdapter;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.DeleteAsset;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.AssetDeductionModel;
import com.dukeai.android.models.AssetDeductionResponseModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.ui.activities.AddTractor;
import com.dukeai.android.ui.activities.AssetDeduction;
import com.dukeai.android.ui.activities.EditTractor;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.AssetDeductionViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TractorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TractorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TractorFragment extends Fragment implements PopupActions {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public AssetDeduction activity;
    AssetListAdapter assetListAdapter;
    @BindView(R.id.asets_recycler_view11)
    RecyclerView rv;
    @BindView(R.id.fab)
    FloatingActionButton btn;
    Context context;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    Activity previousContext = null;
    @BindView(R.id.no_tractor_item)
    TextView noItem;
    AssetDeductionViewModel assetDeductionViewModel;
    ArrayList<AssetDeductionModel.DataListModel> dataModels = new ArrayList<>();
    PopupActions popupActions;
    String vinIdfromIntent = "";
    Float vinValfromIntent;
    String descfromIntent = "";
    String assetTypefromIntent = "";
    String internalIdfromIntent = "";
    Float modelfromIntent;
    String purchasessDatefromIntent = "";
    boolean deleteFlag = false;
    private AssetDeductionModel.DataListModel[] listdata;
    DeleteAsset deleteAsset = new DeleteAsset() {
        @Override
        public void deleteItem(int position) {
            Log.i("postioon", String.valueOf(position));


            assetTypefromIntent = "truck";
            vinIdfromIntent = listdata[position].getId();
            purchasessDatefromIntent = listdata[position].getPurchaseDate();
            descfromIntent = listdata[position].getDescription();
            modelfromIntent = Float.parseFloat(listdata[position].getModel());
            internalIdfromIntent = listdata[position].getInternalId();
            vinValfromIntent = Float.parseFloat(listdata[position].getValueVal().toString());
            showConfiramtionDailog();
        }
    };
    public BroadcastReceiver statusOkReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            getData();
        }
    };
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener = null;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public TractorFragment() {
        // Required empty public constructor
    }

    public static TractorFragment newInstance(String param1, String param2) {
        TractorFragment fragment = new TractorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static TractorFragment newInstance() {
        TractorFragment fragment = new TractorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(statusOkReciever,
                new IntentFilter("statusOk"));
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View tractorView = inflater.inflate(R.layout.fragment_tractor, container, false);
        ButterKnife.bind(this, tractorView);
        popupActions = this;
        customProgressLoader = new CustomProgressLoader(getContext());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAsset(view);
            }
        });
        noItem.setVisibility(View.GONE);
        setCurrentTheme();
        getData();

        return tractorView;

    }

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        btn.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getData();
    }

    public void getData() {
        final AssetDeductionViewModel assetDeductionViewModel = ViewModelProviders.of(this).get(AssetDeductionViewModel.class);

        assetDeductionViewModel.getAssetDeductionModelLiveData().observe(this, new Observer<AssetDeductionModel>() {
            @Override
            public void onChanged(@Nullable AssetDeductionModel assetDeductionModel) {

                if (assetDeductionModel != null) {
                    int j = -1, count = 0;
                    int len = assetDeductionModel.getDataListModel().length;
                    for (int i = 0; i < len; i++) {
                        if (assetDeductionModel.getDataListModel()[i].getAssetType().equals("truck")) {
                            count++;
                        }
                    }
                    listdata = new AssetDeductionModel.DataListModel[count];

                    if (count < 1) {

                        noItem.setVisibility(View.VISIBLE);
                    } else {
                        noItem.setVisibility(View.GONE);
                    }
                    for (int i = 0; i < len; i++) {

                        AssetDeductionModel.DataListModel index = assetDeductionModel.getDataListModel()[i];
                        if (assetDeductionModel.getDataListModel()[i].getAssetType().equals("truck")) {
                            j++;
                            listdata[j] = new AssetDeductionModel.DataListModel(index.getPurchaseDate(), index.getDescription(), index.getModel(), index.getInternalId(), index.getId(), index.getValueVal(), index.getDelete(), index.getAssetType());

                        }

                    }

                    AssetListAdapter adapter = new AssetListAdapter(listdata, deleteAsset);
                    rv.setHasFixedSize(true);
                    rv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                    rv.setAdapter(adapter);
                } else {
                    noItem.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        previousContext = this.getActivity();
//        mListener = null;
    }

    public void addAsset(View view) {
        Bundle params = new Bundle();
        params.putString("Add", "tractor");
        mFirebaseAnalytics.logEvent("Tax_Info", params);

        Intent intent = new Intent(this.getActivity(), AddTractor.class);
        intent.putExtra("assetType", "tractor");
        startActivityForResult(intent, 1);

    }

    public void editAsset(int position) {

        Log.i("getactivity", String.valueOf(this.getActivity()));
        Intent intent1;

        if (previousContext != null) {
            intent1 = new Intent(previousContext, EditTractor.class);
        } else {
            intent1 = new Intent(this.getActivity(), EditTractor.class);
        }
        intent1.putExtra("vinId", listdata[position].getId());
        intent1.putExtra("vinVal", listdata[position].getValueVal().toString());
        intent1.putExtra("desc", listdata[position].getDescription());
        intent1.putExtra("assetType", listdata[position].getAssetType());
        intent1.putExtra("internalId", listdata[position].getInternalId());
        intent1.putExtra("model", listdata[position].getModel());
        intent1.putExtra("delete", listdata[position].getDelete());
        intent1.putExtra("purchasessDate", listdata[position].getPurchaseDate());

//                getActivity().startActivity(intent1);
        if (previousContext != null) {
            previousContext.startActivityForResult(intent1, 1);
        } else {
            startActivityForResult(intent1, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK)) {
            // recreate your fragment here
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.detach(this).attach(this).commit();
            getData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    public void showDialog(String statusMssg) {
        String status = "";
        String message = "";
        if (statusMssg.toLowerCase().equals("success")) {
            status = "Success";
            message = "Asset deleted successfully";
        } else {
            status = "Error";
            message = "Something went wrong, Please try again later!";
        }
        confirmationComponent = new ConfirmationComponent(this.getContext(), status, message, true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
            }
        }, 1);
        getData();

    }

    public void showConfiramtionDailog() {
        confirmationComponent = new ConfirmationComponent(this.getContext(), "Warning", "Do you really want to delete the record?", true, "Yes", "No", popupActions, 1);
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        dismissExistingComponentIfAny();
        if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
            /**User chose to stop tracking the trip**/
            deleteFlag = true;
            calldelete();
        } else {
            /**Turn Trip tracking button ON**/
            deleteFlag = false;
        }
    }

    private void dismissExistingComponentIfAny() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    public void calldelete() {
        JsonObject jsonObject = InputParams.assetInfoData(internalIdfromIntent, vinIdfromIntent, vinValfromIntent, purchasessDatefromIntent, descfromIntent, assetTypefromIntent, modelfromIntent, deleteFlag);
        customProgressLoader.showDialog();

        assetDeductionViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(AssetDeductionViewModel.class);

        assetDeductionViewModel.postAssetDeductionModelLiveData(jsonObject).observe(Objects.requireNonNull(getActivity()), new Observer<AssetDeductionResponseModel>() {
            @Override
            public void onChanged(@Nullable AssetDeductionResponseModel assetDeductionResponseModel) {
                customProgressLoader.hideDialog();

                if (assetDeductionResponseModel != null) {
                    String statusMssg = assetDeductionResponseModel.getMessage();
                    showDialog(statusMssg);

                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
