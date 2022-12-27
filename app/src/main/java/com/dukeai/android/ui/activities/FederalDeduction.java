package com.dukeai.android.ui.activities;

import android.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.FederalDeductionModel;
import com.dukeai.android.models.FederalDeductionResponseModel;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FederalDeductionViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup

public class FederalDeduction extends BaseActivity implements HeaderActions, AdapterView.OnItemSelectedListener {
    AlertDialog.Builder builder;
    String fillingStatus;
    Double taxAmount = -1.0;
    Double deductionAmount = -1.0;
    Double otherAmount = -1.0;
    @BindView(R.id.federal_deduction_header)
    CustomHeader customHeader;
    FederalDeductionViewModel federalDeductionViewModel = new FederalDeductionViewModel();
    Context context;
    @BindView(R.id.item_deduction_amount)
    CustomEditInputField itemDeductionAmountField;
    @BindView(R.id.other_income_amount)
    CustomEditInputField otherIncomeAmountField;
    @BindView(R.id.tax_amount)
    CustomEditInputField taxAmountField;
    ConfirmationComponent confirmationComponent;
    CustomProgressLoader customProgressLoader;
    @BindView(R.id.pre_tax_deduction)
    RadioButton preTaxDeductionButton;
    @BindView(R.id.parent_layout)
    RelativeLayout parentLayout;
    private RadioGroup radioGroup, radioGroup2;
    private RadioButton radioBtn;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    EditText editText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_federal_deduction);
        customProgressLoader = new CustomProgressLoader(this);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final List<String> categories = new ArrayList<String>();
        categories.add("Married File Jointly");
        categories.add("Married File Separately");
        categories.add("Single");
        categories.add("Head of Household");
        preTaxDeductionButton.setChecked(true);
        TextView tv = findViewById(R.id.selected_tax_text);
        tv.setText("Pre Tax Deduction");
        setCurrentTheme();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this));
         editText = findViewById(R.id.tax_amount);

        setCustomHeader();
        federalDeductionViewModel = ViewModelProviders.of(this).get(FederalDeductionViewModel.class);

//        new Thread() {
//            @Override
//            public void run() {
        customProgressLoader.showDialog();
        federalDeductionViewModel.getFederalDeductionModelLiveData().observe(FederalDeduction.this, new Observer<FederalDeductionModel>() {
            @Override
            public void onChanged(@Nullable FederalDeductionModel federalDeductionModel) {
                customProgressLoader.hideDialog();
                Log.i("federalDeuction", "got");

                if (federalDeductionModel != null) {

                    fillingStatus = federalDeductionModel.getFederalDeductionDataModel().getFillingStatus();
                    switch (fillingStatus) {
                        case "S":
                            fillingStatus = "Single";
                            break;
                        case "MFJ":
                            fillingStatus = "Married File Jointly";
                            break;
                        case "MFS":
                            fillingStatus = "Married File Separately";
                            break;
                        case "HOH":
                            fillingStatus = "Head of Household";
                            break;
                    }

                    Spinner spinner = findViewById(R.id.spinner);
                    //        // Spinner Drop down elements

                    int position = categories.indexOf(fillingStatus);
                    spinner.setSelection(position);

                    Boolean checked = federalDeductionModel.getFederalDeductionDataModel().getPreTaxDeductionData().getStatus();
                    RadioButton radioButton = findViewById(R.id.pre_tax_deduction);
                    radioButton.setChecked(checked);
                    if (checked) {
                        taxAmount = federalDeductionModel.getFederalDeductionDataModel().getPreTaxDeductionData().getAmount();
                    }
                    checked = federalDeductionModel.getFederalDeductionDataModel().getTaxCreditDeductionModelData().getStatus();
                    radioButton = findViewById(R.id.tax_credit);
                    radioButton.setChecked(checked);
                    if (checked) {
                        taxAmount = federalDeductionModel.getFederalDeductionDataModel().getTaxCreditDeductionModelData().getAmount();
                    }
                    checked = federalDeductionModel.getFederalDeductionDataModel().getStandardDeductionModelData().getStatus();
                    radioButton = findViewById(R.id.standard_deduction);
                    radioButton.setChecked(checked);
                    if (checked) {
                        otherAmount = federalDeductionModel.getFederalDeductionDataModel().getOtherIncomeModel().getAmount();
                    }
                    checked = federalDeductionModel.getFederalDeductionDataModel().getItemizeDeductionModelData().getStatus();
                    radioButton = findViewById(R.id.item_deduction);
                    radioButton.setChecked(checked);
                    if (checked) {
                        deductionAmount = federalDeductionModel.getFederalDeductionDataModel().getItemizeDeductionModelData().getAmount();
                        otherAmount = federalDeductionModel.getFederalDeductionDataModel().getOtherIncomeModel().getAmount();
                    }
                    if (taxAmount != -1.0) {
                        taxAmount = Math.round(taxAmount * 100.0) / 100.0;
                        editText.setText(taxAmount.toString());
                    }
                    getSelectedRadioButton();

                }
            }
        });
//            }
//        }.start();
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup2 = findViewById(R.id.deductionRadioGroup);
//        getSelectedRadioButton();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int selectedId = radioGroup.getCheckedRadioButtonId();
                setSelectedButton(selectedId);
            }
        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int selectedId = radioGroup2.getCheckedRadioButtonId();
                setSelectedButton(selectedId);
            }
        });
        // Spinner element
        Spinner spinner = findViewById(R.id.spinner);
        //        // Spinner Drop down elements
//        List<String> categories = new ArrayList<String>();
//        categories.add("Married File Jointly");
//        categories.add("Married File Separately");
//        categories.add("Single");
//        categories.add("Head of Household");
//        int position = categories.indexOf(fillingStatus);
//        spinner.setSelection(position);
//        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
//

//
//        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

//        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//

//        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

    }

    public void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.tax_deductions));
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

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
//        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void getSelectedRadioButton() {

        int selectedId = radioGroup.getCheckedRadioButtonId();
        setSelectedButton(selectedId);
        selectedId = radioGroup2.getCheckedRadioButtonId();
        setSelectedButton(selectedId);

    }

    public void setSelectedButton(int selectedId) {

        // find the radiobutton by returned id
        radioBtn = findViewById(selectedId);
        TextView tv = findViewById(R.id.selected_tax_text);
        EditText editText = findViewById(R.id.tax_amount);

        RelativeLayout layout = findViewById(R.id.other_income_layout);
        TextView itemDeduction = findViewById(R.id.item_deduction_value);
        EditText itemDeductionAmount = findViewById(R.id.item_deduction_amount);
        EditText otherIncomeAmount = findViewById(R.id.other_income_amount);

        String selectedButtonValue = (String) radioBtn.getText();
        Log.i("radioValue", selectedButtonValue);

        if (selectedButtonValue.equals("Pre Tax Deduction")) {
            tv.setText("Pre Tax Deduction");
          /*  if (taxAmount != -1.0) {
                taxAmount = Math.round(taxAmount * 100.0) / 100.0;
                editText.setText(taxAmount.toString());
            }*/
        } else if (selectedButtonValue.equals("Tax Credit")) {
            tv.setText("Tax Credit");
            /*if (taxAmount != -1.0) {
                taxAmount = Math.round(taxAmount * 100.0) / 100.0;
                editText.setText(taxAmount.toString());
            }
*/
        } else if (selectedButtonValue.equals("Standard Deduction")) {
            layout.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.GONE);
            itemDeduction.setText("Other Income");
            if (otherAmount != -1.0) {
                otherAmount = Math.round(otherAmount * 100.0) / 100.0;

                itemDeductionAmount.setText(otherAmount.toString());
            } else {
                itemDeductionAmount.setText("");

            }

        } else if (selectedButtonValue.equals("Itemize Deduction")) {
            layout.setVisibility(View.VISIBLE);
            itemDeduction.setText("Item Deduction Value");
            if (otherAmount != -1.0) {
                otherAmount = Math.round(otherAmount * 100.0) / 100.0;

                otherIncomeAmount.setText(otherAmount.toString());
            } else {
                otherIncomeAmount.setText("");
            }

            if (deductionAmount != -1.0) {
                deductionAmount = Math.round(deductionAmount * 100.0) / 100.0;

                itemDeductionAmount.setText(deductionAmount.toString());
            } else {
                itemDeductionAmount.setText("");
            }

        }
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

    public void postTaxInfo(View view) {
        EditText otherIncomeEditText = findViewById(R.id.other_income_amount);
        EditText itemDeductionEditText = findViewById(R.id.item_deduction_amount);
        RelativeLayout otherIncomeLayout = findViewById(R.id.other_income_layout);
        EditText editText = findViewById(R.id.tax_amount);

        boolean isTaxCreditFieldValid = taxAmountField.validateField(taxAmountField);
        boolean isOtherIcomeFieldValid = otherIncomeAmountField.validateField(otherIncomeAmountField);
        boolean isItemizeDeducitonValid = itemDeductionAmountField.validateField(itemDeductionAmountField);

        if (otherIncomeLayout.getVisibility() == View.INVISIBLE || otherIncomeLayout.getVisibility() == View.GONE) {
            isOtherIcomeFieldValid = true;
        }
        if (isTaxCreditFieldValid && isOtherIcomeFieldValid && isItemizeDeducitonValid) {

            Spinner mySpinner = findViewById(R.id.spinner);
            String fillingStatusValue = mySpinner.getSelectedItem().toString();
            switch (fillingStatusValue) {
                case "Single":
                    fillingStatusValue = "S";
                    break;
                case "Married File Jointly":
                    fillingStatusValue = "MFJ";
                    break;
                case "Married File Separately":
                    fillingStatusValue = "MFS";
                    break;
                case "Head of Household":
                    fillingStatusValue = "HOH";
                    break;
            }

            TextView textView = findViewById(R.id.selected_tax_text);
            textView.getText();

            Boolean preTaxDeductionStatus = false, taxCreditStatus = false, itemizeStatus = false, standardDeductionStatus = false;
            Double preTaxDeductionAmount = 0.0, taxCreditAmount = 0.0, itemizeDeductionAmount = 0.0, otherIncomeAmount = 0.0;

            if (textView.getText().toString().equals("Pre Tax Deduction")) {
                preTaxDeductionStatus = true;
                preTaxDeductionAmount = (Double.parseDouble(editText.getText().toString()));
            } else if (textView.getText().toString().equals("Tax Credit")) {
                taxCreditStatus = true;
                taxCreditAmount = (Double.parseDouble(editText.getText().toString()));
            }

            if (otherIncomeLayout.getVisibility() == View.VISIBLE) {
                otherIncomeAmount = (Double.parseDouble(otherIncomeEditText.getText().toString()));
                itemizeDeductionAmount = (Double.parseDouble(itemDeductionEditText.getText().toString()));
                itemizeStatus = true;
            } else {
                standardDeductionStatus = true;
                otherIncomeAmount = (Double.parseDouble(itemDeductionEditText.getText().toString()));
            }

            JsonObject preTaaxDeduction = new JsonObject();
            preTaaxDeduction.addProperty("amount", preTaxDeductionAmount);
            preTaaxDeduction.addProperty("status", preTaxDeductionStatus);

            JsonObject itemizeDeduction = new JsonObject();
            itemizeDeduction.addProperty("amount", itemizeDeductionAmount);
            itemizeDeduction.addProperty("status", itemizeStatus);

            JsonObject standardDeduction = new JsonObject();
            standardDeduction.addProperty("status", standardDeductionStatus);

            JsonObject taxCredit = new JsonObject();
            taxCredit.addProperty("amount", taxCreditAmount);
            taxCredit.addProperty("status", taxCreditStatus);

            JsonObject otherIncomeObj = new JsonObject();
            otherIncomeObj.addProperty("amount", otherIncomeAmount);
            JsonObject jsonObject = InputParams.federalInfoData(fillingStatusValue, preTaaxDeduction, itemizeDeduction, standardDeduction, taxCredit, otherIncomeObj);
            customProgressLoader.showDialog();

            federalDeductionViewModel.postFederalDeductionModelLiveData(jsonObject).observe(FederalDeduction.this, new Observer<FederalDeductionResponseModel>() {
                @Override
                public void onChanged(@Nullable FederalDeductionResponseModel federalDeductionModel) {
                    customProgressLoader.hideDialog();

                    if (federalDeductionModel != null) {
                        String statusMssg = federalDeductionModel.getMessage();
                        showDialog(statusMssg);
                    }
                }
            });
            Bundle params = new Bundle();
            params.putString("Fed_deduct_submit", "success");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        } else {
            Bundle params = new Bundle();
            params.putString("Fed_deduct_submit", "fail");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        }
    }

    public void showDialog(String statusMssg) {

        String status = "";
        String message = "";
        if (statusMssg.toLowerCase().equals("success")) {
            status = "Success";
            message = "Federal Deduction updated successfully";
        } else {
            status = "Error";
            message = "Something went wrong, Please try again later!";
        }


        confirmationComponent = new ConfirmationComponent(this, status, message, true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
                finish();
            }
        }, 1);
//        finish();

    }

    @OnClick(R.id.tax_amount)
    void onClick() {
        taxAmountField.setCursorVisible(true);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }
}
