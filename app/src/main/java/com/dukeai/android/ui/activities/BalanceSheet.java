package com.dukeai.android.ui.activities;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.adapters.BalanceSheetAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.models.BalanceSheetModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.viewModel.BalanceSheetViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

// Firebase: Setup

public class BalanceSheet extends BaseActivity implements HeaderActions {
    BalanceSheetViewModel balanceSheetViewModel;
    @BindView(R.id.rview)
    RecyclerView rview;
    @BindView(R.id.coa_rv)
    RecyclerView coa_rv;
    @BindView(R.id.ofa_rv)
    RecyclerView ofa_rv;
    @BindView(R.id.col_rv)
    RecyclerView col_rv;
    @BindView(R.id.ar_rv)
    RecyclerView ar_rv;
    @BindView(R.id.ap_rv)
    RecyclerView ap_rv;
    @BindView(R.id.ol_rv)
    RecyclerView ol_rv;
    @BindView(R.id.oe_rv)
    RecyclerView oe_rv;
    @BindView(R.id.oa_rv)
    RecyclerView oa_rv;
    @BindView(R.id.balance_sheet_header)
    CustomHeader customHeader;
    CustomProgressLoader customProgressLoader;
    @BindView(R.id.no_tractor_item)
    TextView noItem;
    @BindView(R.id.balnce_sheet)
    RelativeLayout balanceShhet;
    @BindView(R.id.parent_layout)
    RelativeLayout parentLayout;

    private BalanceSheetModel.DataListModel[] cashListData;
    private BalanceSheetModel.DataListModel[] coaListData;
    private BalanceSheetModel.DataListModel[] arListData;
    private BalanceSheetModel.DataListModel[] ofaListData;
    private BalanceSheetModel.DataListModel[] oaListData;
    private BalanceSheetModel.DataListModel[] apListData;
    private BalanceSheetModel.DataListModel[] colListData;
    private BalanceSheetModel.DataListModel[] olListData;
    private BalanceSheetModel.DataListModel[] oeListData;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_sheet);
        ButterKnife.bind(this);
        setCustomHeader();
        setCurrentTheme();
        customProgressLoader = new CustomProgressLoader(this);
        noItem.setVisibility(View.GONE);
        balanceShhet.setVisibility(View.GONE);
        balanceSheetViewModel = ViewModelProviders.of(this).get(BalanceSheetViewModel.class);
        getData();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this));


//        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mMessageReceiver,
//                new IntentFilter("custom-message"));
//        MyListData[] myListData = new MyListData[] {
//                new MyListData("Email"),
//                new MyListData("Info"),
//                new MyListData("Delete")
//        };
//
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rview);
//        MyListAdapter adapter = new MyListAdapter(myListData);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        recyclerView.setAdapter(adapter);
    }

    //    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//
//                int position = Integer.parseInt(intent.getStringExtra("id"));
//                String category = intent.getStringExtra("category");
//
//                switch (category){
//                    case "cash": navigateToReport(cashListData,position);
//                    break;
//                    case "AR": navigateToReport(arListData,position);
//                    break;
//                    case "current_other_assets": navigateToReport(coaListData,position);
//                    break;
//                    case "other_fixed_assets":navigateToReport(ofaListData,position);
//                    break;
//                    case "other_assets":navigateToReport(oaListData,position);
//                    break;
//                    case "AP":navigateToReport(apListData,position);
//                    break;
//                    case "current_other_liabilities":navigateToReport(colListData,position);
//                    break;
//                    case "other_liabilities":navigateToReport(olListData,position);
//                    break;
//                    case "other_equity":navigateToReport(oeListData,position);
//                    break;
//                }
//
//        }
//    };
    public void navigateToReport(BalanceSheetModel.DataListModel[] listdata, int position) {
        Intent intent1 = new Intent(this, AddBalanceSheetReport.class);
        intent1.putExtra("description", listdata[position].getDescription());
        intent1.putExtra("account_type", listdata[position].getAccountType());
        intent1.putExtra("internal_id", listdata[position].getInternalId());
        intent1.putExtra("delete", false);
        intent1.putExtra("cashvalue", String.valueOf(listdata[position].getCashValue()));
        intent1.putExtra("typeOfSheet", "edit");

        Log.i("inId", listdata[position].getInternalId());
//                getActivity().startActivity(intent1);
        startActivityForResult(intent1, 1);
    }

    public void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.balance_sheet));
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
//        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
        TextView title = customHeader.findViewById(R.id.toolbar_title);
//        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
    }

    public void addReport(View view) {
        Bundle params = new Bundle();
        params.putString("Add", "Balance_Sheet_Report");
        mFirebaseAnalytics.logEvent("Tax_Info", params);

        Intent intent = new Intent(this, AddBalanceSheetReport.class);
        intent.putExtra("typeOfSheet", "add");
        startActivityForResult(intent, 1);

//        this.startActivity(intent);
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        finish();
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK)) {
            // recreate your fragment here
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.detach(this).attach(this).commit();
            getData();
        }
    }

    public void getData() {
        customProgressLoader.showDialog();

        balanceSheetViewModel.getBalanceSheetModelLiveData().observe(BalanceSheet.this, new Observer<BalanceSheetModel>() {
            @Override
            public void onChanged(@Nullable BalanceSheetModel balanceSheetModel) {
                customProgressLoader.hideDialog();

//                        if(balanceSheetModel != null){
//
//                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rview);
//                            BalanceSheetAdapter adapter = new BalanceSheetAdapter(balanceSheetModel.getDataListModel());
//                            recyclerView.setHasFixedSize(true);
//                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                            recyclerView.setAdapter(adapter);
//                        }

                if (balanceSheetModel != null) {
                    int cashCount = 0, arCount = 0, coaCount = 0, ofaCount = 0, oaCount = 0, apCount = 0, colCount = 0, olCount = 0, oeCount = 0;
                    int cashIndex = -1, arIndex = -1, coaIndex = -1, oaIndex = -1, ofaIndex = -1, apIndex = -1, colIndex = -1, olIndex = -1, oeIndex = -1;
                    int len = balanceSheetModel.getDataListModel().length;
                    if (len < 1) {
                        noItem.setVisibility(View.VISIBLE);
                        balanceShhet.setVisibility(View.GONE);

                    } else {
                        noItem.setVisibility(View.GONE);
                        balanceShhet.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < len; i++) {
                        switch (balanceSheetModel.getDataListModel()[i].getAccountType()) {
                            case "cash":
                                cashCount++;
                                break;
                            case "AR":
                                arCount++;
                                break;
                            case "current_other_assets":
                                coaCount++;
                                break;
                            case "other_fixed_assets":
                                ofaCount++;
                                break;
                            case "other_assets":
                                oaCount++;
                                break;
                            case "AP":
                                apCount++;
                                break;
                            case "current_other_liabilities":
                                colCount++;
                                break;
                            case "other_liabilities":
                                olCount++;
                                break;
                            case "other_equity":
                                oeCount++;
                                break;
                        }
                    }
                    cashListData = new BalanceSheetModel.DataListModel[cashCount];
                    arListData = new BalanceSheetModel.DataListModel[arCount];
                    coaListData = new BalanceSheetModel.DataListModel[coaCount];
                    ofaListData = new BalanceSheetModel.DataListModel[ofaCount];
                    oaListData = new BalanceSheetModel.DataListModel[oaCount];
                    apListData = new BalanceSheetModel.DataListModel[apCount];
                    colListData = new BalanceSheetModel.DataListModel[colCount];
                    olListData = new BalanceSheetModel.DataListModel[olCount];
                    oeListData = new BalanceSheetModel.DataListModel[oeCount];

                    for (int i = 0; i < len; i++) {

                        BalanceSheetModel.DataListModel index = balanceSheetModel.getDataListModel()[i];

                        switch (balanceSheetModel.getDataListModel()[i].getAccountType()) {
                            case "cash":
                                cashIndex++;
                                cashListData[cashIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());
                                break;
                            case "AR":
                                arIndex++;
                                arListData[arIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());
                                break;
                            case "current_other_assets":
                                coaIndex++;
                                coaListData[coaIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "other_fixed_assets":
                                ofaIndex++;
                                ofaListData[ofaIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "other_assets":
                                oaIndex++;
                                oaListData[oaIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "AP":
                                apIndex++;
                                apListData[apIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "current_other_liabilities":
                                colIndex++;
                                colListData[colIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "other_liabilities":
                                olIndex++;
                                olListData[olIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                            case "other_equity":
                                oeIndex++;
                                oeListData[oeIndex] = new BalanceSheetModel.DataListModel(index.getDescription(), index.getAccountType(), index.getCashValue(), index.getDelete(), index.getInternalId());

                                break;
                        }


                    }
//                            RecyclerViewClickListener listener = (view, position) -> {
//                                Toast.makeText(getContext(), "Position " + position, Toast.LENGTH_SHORT).show();
//                            };
                    BalanceSheetAdapter adapter;
                    if (cashCount > 0) {
                        TextView cash = findViewById(R.id.cash);
                        cash.setVisibility(View.VISIBLE);
                        rview.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(cashListData);
                        rview.setHasFixedSize(true);
                        rview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        rview.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.cash);
                        cash.setVisibility(View.GONE);
                        rview.setVisibility(View.GONE);
                    }
                    if (coaCount > 0) {
                        TextView cash = findViewById(R.id.coa);
                        cash.setVisibility(View.VISIBLE);
                        coa_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(coaListData);
                        coa_rv.setHasFixedSize(true);
                        coa_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        coa_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.coa);
                        cash.setVisibility(View.GONE);
                        coa_rv.setVisibility(View.GONE);
                    }
                    if (colCount > 0) {
                        TextView cash = findViewById(R.id.col);
                        cash.setVisibility(View.VISIBLE);
                        col_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(colListData);
                        col_rv.setHasFixedSize(true);
                        col_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        col_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.col);
                        cash.setVisibility(View.GONE);
                        col_rv.setVisibility(View.GONE);
                    }
                    if (ofaCount > 0) {
                        TextView cash = findViewById(R.id.ofa);
                        cash.setVisibility(View.VISIBLE);
                        ofa_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(ofaListData);
                        ofa_rv.setHasFixedSize(true);
                        ofa_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        ofa_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.ofa);
                        cash.setVisibility(View.GONE);
                        ofa_rv.setVisibility(View.GONE);
                    }
                    if (olCount > 0) {
                        TextView cash = findViewById(R.id.ol);
                        cash.setVisibility(View.VISIBLE);
                        adapter = new BalanceSheetAdapter(olListData);
                        ol_rv.setVisibility(View.VISIBLE);

                        ol_rv.setHasFixedSize(true);
                        ol_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        ol_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.ol);
                        cash.setVisibility(View.GONE);
                        ol_rv.setVisibility(View.GONE);
                    }
                    if (oeCount > 0) {
                        TextView cash = findViewById(R.id.oe);
                        cash.setVisibility(View.VISIBLE);
                        oe_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(oeListData);
                        oe_rv.setHasFixedSize(true);
                        oe_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        oe_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.oe);
                        cash.setVisibility(View.GONE);
                        oe_rv.setVisibility(View.GONE);
                    }
                    if (apCount > 0) {
                        TextView cash = findViewById(R.id.ap);
                        cash.setVisibility(View.VISIBLE);
                        ap_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(apListData);
                        ap_rv.setHasFixedSize(true);
                        ap_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        ap_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.ap);
                        cash.setVisibility(View.GONE);
                        ap_rv.setVisibility(View.GONE);
                    }
                    if (arCount > 0) {
                        TextView cash = findViewById(R.id.ar);
                        cash.setVisibility(View.VISIBLE);
                        ar_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(arListData);
                        ar_rv.setHasFixedSize(true);
                        ar_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        ar_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.ar);
                        cash.setVisibility(View.GONE);
                        ar_rv.setVisibility(View.GONE);
                    }
                    if (oaCount > 0) {
                        TextView cash = findViewById(R.id.oa);
                        cash.setVisibility(View.VISIBLE);
                        oa_rv.setVisibility(View.VISIBLE);

                        adapter = new BalanceSheetAdapter(oaListData);
                        oa_rv.setHasFixedSize(true);
                        oa_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        oa_rv.setAdapter(adapter);
                    } else {
                        TextView cash = findViewById(R.id.oa);
                        cash.setVisibility(View.GONE);
                        oa_rv.setVisibility(View.GONE);
                    }
                } else {
                    noItem.setVisibility(View.VISIBLE);
                    balanceShhet.setVisibility(View.GONE);
                }
            }

        });
    }

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
    }
}
