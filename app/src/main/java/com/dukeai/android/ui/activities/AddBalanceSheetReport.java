package com.dukeai.android.ui.activities;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.BalanceSheetResponseModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.BalanceSheetViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddBalanceSheetReport extends BaseActivity implements HeaderActions, AdapterView.OnItemSelectedListener, PopupActions {

    @BindView(R.id.submit)
    Button submitBtn;
    @BindView(R.id.tax_amount)
    CustomEditInputField taxAmountField;
    @BindView(R.id.description_value)
    CustomEditInputField descriptionField;
    ConfirmationComponent confirmationComponent;
    @BindView(R.id.report_header)
    CustomHeader customHeader;
    String typeOfSheet = "add";
    @BindView(R.id.delete_icon)
    RelativeLayout deleteIcon;
    String internald = "";
    Boolean deleteFlag = false;
    CustomProgressLoader customProgressLoader;
    PopupActions popupActions;
    @BindView(R.id.parent_layout)
    RelativeLayout parentLayout;
    @BindView(R.id.del_icon)
    ImageView delIcon;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_balance_sheet_report);
        ButterKnife.bind(this);
        popupActions = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        customProgressLoader = new CustomProgressLoader(this);
        setCurrentTheme();

        final List<String> categories = new ArrayList<String>();
        categories.add("Cash");
        categories.add("Accounts Receivable");
        categories.add("Current Other Assets");
        categories.add("Other Fixed Assets");
        categories.add("Other Assets");
        categories.add("Accounts Payable");
        categories.add("Current Other Liabilities");
        categories.add("Other Liabilities");
        categories.add("Other Equity");
        deleteIcon.setVisibility(View.INVISIBLE);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
//        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        typeOfSheet = getIntent().getStringExtra("typeOfSheet");

        if (typeOfSheet != null || typeOfSheet != "") {
            if (typeOfSheet.equals("edit")) {
                deleteIcon.setVisibility(View.VISIBLE);

                String descripiton = getIntent().getStringExtra("description");
                String accountType = getIntent().getStringExtra("account_type");
                String cashvalue = getIntent().getStringExtra("cashvalue");
                internald = getIntent().getStringExtra("internal_id");

                Log.i("cashvalue", cashvalue);
                Log.i("des", descripiton);

                switch (accountType) {
                    case "cash":
                        accountType = "Cash";
                        break;
                    case "AR":
                        accountType = "Accounts Receivable";
                        break;
                    case "current_other_assets":
                        accountType = "Current Other Assets";
                        break;
                    case "other_fixed_assets":
                        accountType = "Other Fixed Assets";
                        break;
                    case "other_assets":
                        accountType = "Other Assets";
                        break;
                    case "AP":
                        accountType = "Accounts Payable";
                        break;
                    case "current_other_liabilities":
                        accountType = "Current Other Liabilities";
                        break;
                    case "other_liabilities":
                        accountType = "Other Liabilities";
                        break;
                    case "other_equity":
                        accountType = "Other Equity";
                        break;

                }
                int pos = categories.indexOf(accountType);
                spinner.setSelection(pos);
                EditText description_value = findViewById(R.id.description_value);
                description_value.setText(descripiton);
//            EditText tax_amount = (EditText) findViewById(R.id.tax_amount);
//            tax_amount.setText(cashvalue);
                taxAmountField.setText(cashvalue);
            }
        } else {
            typeOfSheet = "add";
        }
        if (deleteIcon.getVisibility() == View.VISIBLE) {
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    showConfiramtionDailog();

                    showConfiramtionDailog();
//                        deleteFlag = true;
//                     if(deleteFlag){
//                         postTaxInfo(v);
//
//                     }

                }
            });
        }
        setCustomHeader();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postTaxInfo();
            }
        });
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this));
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setCustomHeader() {
        if (typeOfSheet.equals("edit")) {
            customHeader.setToolbarTitle(getString(R.string.edit_report));
        } else {
            customHeader.setToolbarTitle(getString(R.string.add_report));
        }
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
//        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
//        TextView title = customHeader.findViewById(R.id.toolbar_title);
//        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), changeThemeModel.getProfileHeaderBackButtonColor()));
        TextView title = customHeader.findViewById(R.id.toolbar_title);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), changeThemeModel.getProfileHeaderTextColor()));
        customHeader.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), changeThemeModel.getProfileHeaderBackgroundColor()));

    }

    public void postTaxInfo() {

        boolean taxAmountValid = taxAmountField.validateField(taxAmountField);
        boolean isDescValid = descriptionField.validateField(descriptionField);
        if (taxAmountValid && isDescValid) {

            Spinner mySpinner = findViewById(R.id.spinner);
            String categoryVal = mySpinner.getSelectedItem().toString();
            switch (categoryVal) {
                case "Cash":
                    categoryVal = "cash";
                    break;
                case "Accounts Receivable":
                    categoryVal = "AR";
                    break;
                case "Current Other Assets":
                    categoryVal = "current_other_assets";
                    break;
                case "Other Fixed Assets":
                    categoryVal = "other_fixed_assets";
                    break;
                case "Other Assets":
                    categoryVal = "other_assets";
                    break;
                case "Accounts Payable":
                    categoryVal = "AP";
                    break;
                case "Current Other Liabilities":
                    categoryVal = "current_other_liabilities";
                    break;
                case "Other Liabilities":
                    categoryVal = "other_liabilities";
                    break;
                case "Other Equity":
                    categoryVal = "other_equity";
                    break;
            }
            String uniqueId;


            if (typeOfSheet.equals("edit")) {
                uniqueId = internald;

                Log.i("internalid", uniqueId);
            } else {
                uniqueId = UUID.randomUUID().toString();
            }
            Float amount = Float.parseFloat(((EditText) findViewById(R.id.tax_amount)).getText().toString());
            String desc = ((EditText) findViewById(R.id.description_value)).getText().toString();

            customProgressLoader.showDialog();
            Log.i("category", categoryVal);
            JsonObject jsonObject = InputParams.balanceSheetInfo(amount, desc, uniqueId, categoryVal, deleteFlag);
            BalanceSheetViewModel balanceSheetViewModel = ViewModelProviders.of(this).get(BalanceSheetViewModel.class);
            balanceSheetViewModel.postBalanceSheetData(jsonObject).observe(AddBalanceSheetReport.this, new Observer<BalanceSheetResponseModel>() {
                @Override
                public void onChanged(@Nullable BalanceSheetResponseModel balanceSheetResponseModel) {
                    customProgressLoader.hideDialog();
                    if (balanceSheetResponseModel != null) {
                        String statusMssg = balanceSheetResponseModel.getMessage();
                        showDialog(statusMssg);
                    }
                }

            });
            Bundle params = new Bundle();
            params.putString("Balance_Sheet_Report_submit", "success");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        } else {
            Bundle params = new Bundle();
            params.putString("Balance_Sheet_Report_submit", "fail");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        }
    }

    public void showConfiramtionDailog() {

        confirmationComponent = new ConfirmationComponent(this, "Warning", "Do you really want to delete the record?", true, "Yes", "No", popupActions, 1);

    }

    public void showDialog(String statusMssg) {

        String status = "";
        String message = "";
        if (typeOfSheet.equals("add")) {
            message = "Record Added successfully";
        } else if (typeOfSheet.equals("edit")) {
            message = "Record editted successfully";
        }
        if (deleteFlag == true) {
            message = "Record deleted successfully";
        }
        if (statusMssg.toLowerCase().equals("success")) {
            status = "Success";
        } else {
            status = "Error";
            message = "Something went wrong, Please try again later!";
        }
        confirmationComponent = new ConfirmationComponent(this, status, message, true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
                setResult(RESULT_OK, null);
                finish();
            }
        }, 1);

    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        dismissExistingComponentIfAny();
        if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
            /**User chose to stop tracking the trip**/
            deleteFlag = true;
            postTaxInfo();
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

    @OnClick(R.id.tax_amount)
    void clickAmountField() {
        taxAmountField.setCursorVisible(true);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
        deleteIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        delIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));

    }
//    public void setHeaderColor(){
//
//        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//        backButton.setColorFilter(Color.parseColor(changeThemeModel.));
//        toolbarTitle.setTextColor(Color.parseColor(changeThemeModel.));
//    }
}
