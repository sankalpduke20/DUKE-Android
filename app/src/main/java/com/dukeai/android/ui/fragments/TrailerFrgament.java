package com.dukeai.android.ui.fragments;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TrailerFrgament.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TrailerFrgament#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrailerFrgament extends Fragment implements PopupActions {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AssetListAdapter assetListAdapter;
    @BindView(R.id.asets_recycler_view11)
    RecyclerView rv;
    @BindView(R.id.fab)
    FloatingActionButton btn;
    Context context;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    @BindView(R.id.no_tractor_item)
    TextView noItem;
    PopupActions popupActions;
    Activity previousContext = null;
    String vinIdfromIntent = "";
    Float vinValfromIntent;
    String descfromIntent = "";
    String assetTypefromIntent = "";
    String internalIdfromIntent = "";
    Float modelfromIntent;
    String purchasessDatefromIntent = "";
    boolean deleteFlag = false;
    AssetDeductionViewModel assetDeductionViewModel;
    ArrayList<AssetDeductionModel.DataListModel> dataModels = new ArrayList<>();
    private AssetDeductionModel.DataListModel[] listdata;
    DeleteAsset deleteAsset = new DeleteAsset() {
        @Override
        public void deleteItem(int position) {
            Log.i("postioon", String.valueOf(position));

            assetTypefromIntent = "trailer";
            vinIdfromIntent = listdata[position].getId();
            purchasessDatefromIntent = listdata[position].getPurchaseDate();
            descfromIntent = listdata[position].getDescription();
            modelfromIntent = Float.parseFloat(listdata[position].getModel());
            internalIdfromIntent = listdata[position].getInternalId();
            vinValfromIntent = Float.parseFloat(listdata[position].getValueVal().toString());
            showConfiramtionDailog();
        }
    };
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener = null;

    public TrailerFrgament() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrailerFrgament.
     */
    // TODO: Rename and change types and number of parameters
    public static TrailerFrgament newInstance(String param1, String param2) {
        TrailerFrgament fragment = new TrailerFrgament();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static TrailerFrgament newInstance() {
        TrailerFrgament fragment = new TrailerFrgament();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK)) {
            // recreate your fragment here
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.detach(this).attach(this).commit();
            getData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View trailerView = inflater.inflate(R.layout.fragment_trailer_frgament, container, false);

        ButterKnife.bind(this, trailerView);
        popupActions = this;
        noItem.setVisibility(View.GONE);

        customProgressLoader = new CustomProgressLoader(getContext());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAsset(view);
            }
        });
        setCurrentTheme();
        return trailerView;
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
//        mListener = null;
        previousContext = this.getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getData();
    }

    public void getData() {
        final AssetDeductionViewModel assetDeductionViewModel = ViewModelProviders.of(this).get(AssetDeductionViewModel.class);
        customProgressLoader.showDialog();

        assetDeductionViewModel.getAssetDeductionModelLiveData().observe(this, new Observer<AssetDeductionModel>() {
            @Override
            public void onChanged(@Nullable AssetDeductionModel assetDeductionModel) {
                customProgressLoader.hideDialog();

                if (assetDeductionModel != null) {
                    int j = -1, count = 0;
                    int len = assetDeductionModel.getDataListModel().length;
                    for (int i = 0; i < len; i++) {
                        if (assetDeductionModel.getDataListModel()[i].getAssetType().equals("trailer")) {
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
                        if (assetDeductionModel.getDataListModel()[i].getAssetType().equals("trailer")) {
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

    public void addAsset(View view) {
        Bundle params = new Bundle();
        params.putString("Add", "trailer");
        mFirebaseAnalytics.logEvent("Tax_Info", params);
        Intent intent = new Intent(this.getActivity(), AddTractor.class);
        intent.putExtra("assetType", "trailer");
        startActivityForResult(intent, 1);
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

        confirmationComponent = new ConfirmationComponent(Objects.requireNonNull(this.getContext()), "Warning", "Do you really want to delete the record?", true, "Yes", "No", popupActions, 1);

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

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        btn.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
