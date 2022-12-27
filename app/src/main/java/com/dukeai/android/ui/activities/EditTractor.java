package com.dukeai.android.ui.activities;

import android.app.DatePickerDialog;

import androidx.lifecycle.Observer;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.AssetDeductionResponseModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.AssetDeductionViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.lang.Float.parseFloat;

// Firebase: Setup
public class EditTractor extends BaseActivity implements HeaderActions {
    final Calendar myCalendar = Calendar.getInstance();
    @BindView(R.id.add_tractor_header)
    CustomHeader customHeader;
    String assetType;
    @BindView(R.id.vin_value)
    CustomEditInputField vinValue;
    @BindView(R.id.value_amount)
    CustomEditInputField valueAmount;
    @BindView(R.id.description_value)
    CustomEditInputField description;
    /*@BindView(R.id.service_date_value)
    CustomEditInputField dateValue;*/
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    String selectedBtn;
    @BindView(R.id.na)
    RadioButton naButton;
    @BindView(R.id.service_date_layout)
    RelativeLayout relativeLayout;
    @BindView(R.id.parent_layout)
    LinearLayout parentLayout;
    @BindView(R.id.tvServiceDate)
    TextView tvServiceDate;
    ConfirmationComponent confirmationComponent;
    AssetDeductionViewModel assetDeductionViewModel = new AssetDeductionViewModel();
    CustomProgressLoader customProgressLoader;


    boolean clearDate = false;

    int frgamentPos = 0;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tractor);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this));

        assetType = getIntent().getStringExtra("assetType");
        Log.i("asset", assetType);
        ButterKnife.bind(this);
        customProgressLoader = new CustomProgressLoader(this);
        setCustomHeader();
        setCurrentTheme();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextView year = findViewById(R.id.yrline);
        RadioButton radioButton = findViewById(R.id.na);

        vinValue.setText(getIntent().getStringExtra("vinId"));
        description.setText(getIntent().getStringExtra("desc"));
        if (getIntent().getStringExtra("purchasessDate").equals("none")) {
//            dateValue.setText("");
            tvServiceDate.setText("");
            clearDate = true;
        } else {
//            dateValue.setText(getIntent().getStringExtra("purchasessDate"));
            tvServiceDate.setText(getIntent().getStringExtra("purchasessDate"));
        }
        valueAmount.setText(getIntent().getStringExtra("vinVal"));

        int index = 0;
        EditText description = findViewById(R.id.description_value);
        description.setText(getIntent().getStringExtra("desc"));
        EditText vinValue = findViewById(R.id.value_amount);
        float amount = Float.parseFloat(getIntent().getStringExtra("vinVal"));
        vinValue.setText(String.valueOf(amount));
        Float model = Float.parseFloat(getIntent().getStringExtra("model"));
        if (model == 0.0) {
            RadioButton na = findViewById(R.id.seciton179);
            na.setChecked(true);
            relativeLayout.setVisibility(View.INVISIBLE);
        } else if (model == 1.0) {
            RadioButton na = findViewById(R.id.yrline);
            na.setChecked(true);
//            EditText service_date_value = findViewById(R.id.service_date_value);
            TextView service_date_value = findViewById(R.id.tvServiceDate);
            String dateVal = getIntent().getStringExtra("purchasessDate");

            service_date_value.setText(dateVal);
            year.setText("3 year straight line");

            relativeLayout.setVisibility(View.VISIBLE);
        } else if (model == 2.0) {
            RadioButton na = findViewById(R.id.yrline);
            na.setChecked(true);
//            EditText service_date_value = findViewById(R.id.service_date_value);
            TextView service_date_value = findViewById(R.id.tvServiceDate);
            String dateVal = getIntent().getStringExtra("purchasessDate");

            service_date_value.setText(dateVal);
            year.setText("5 year straight line");

            relativeLayout.setVisibility(View.VISIBLE);

        } else {
            RadioButton na = findViewById(R.id.na);
            na.setChecked(true);
            relativeLayout.setVisibility(View.INVISIBLE);
        }


        if (assetType.equals("truck")) {
            year.setText("3 year straight line");
            naButton.setVisibility(View.VISIBLE);
            TextView tv = findViewById(R.id.emptyView);
            tv.setVisibility(View.GONE);
//            radioButton.setVisibility(View.INVISIBLE);

        } else if (assetType.equals("trailer")) {
            year.setText("5 year straight line");
            naButton.setVisibility(View.GONE);
            TextView tv = findViewById(R.id.emptyView);
            tv.setVisibility(View.VISIBLE);

//            radioButton.setVisibility(View.INVISIBLE);
        }
//        EditText serviceDatePicker = findViewById(R.id.service_date_value);
        TextView service_date_value = findViewById(R.id.tvServiceDate);

        int selectedId = radioGroup.getCheckedRadioButtonId();
        selectedBtn = setSelectedButton(selectedId);
        RadioButton yrline = findViewById(R.id.yrline);
        yrline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.VISIBLE);
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                EditText serviceDatePicker = findViewById(R.id.service_date_value);
                TextView serviceDatePicker = findViewById(R.id.tvServiceDate);
//                serviceDatePicker.setText((new StringBuilder().append(dayOfMonth).append("-").append(monthOfYear+1).append("-").append(year)));
                serviceDatePicker.setText(Utilities.getFormattedDate(myCalendar));

            }

        };


        tvServiceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    //
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvServiceDate.setText(Utilities.getFormattedDate(myCalendar));

                    }

                };

                DatePickerDialog dpDialog = new DatePickerDialog(EditTractor.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dpDialog.show();

            }
        });

        /*serviceDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditTractor.this, "clicked", Toast.LENGTH_SHORT).show();
                Calendar calendar;
                DatePickerDialog dpDialog = new DatePickerDialog(EditTractor.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dpDialog.getDatePicker().setMaxDate(new Date().getTime());
                dpDialog.show();
            }
        });
*/
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int selectedId = radioGroup.getCheckedRadioButtonId();
                selectedBtn = setSelectedButton(selectedId);

            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//        EditText serviceDatePicker = findViewById(R.id.service_date_value);
        TextView serviceDatePicker = findViewById(R.id.tvServiceDate);
        final Calendar myCalendars = Calendar.getInstance();
        serviceDatePicker.setText(sdf.format(myCalendars.getTime()));
    }

    public void setCustomHeader() {
        if (assetType.equals("truck")) {
            customHeader.setToolbarTitle(getString(R.string.edit_tractor));
        } else if (assetType.equals("trailer")) {
            customHeader.setToolbarTitle(getString(R.string.edit_trailer));
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

    @Override
    public void onClickProfile() {

    }

    public void addTractorToList(View view) {
//        int selectedId = radioGroup.getCheckedRadioButtonId();
//        selectedBtn = setSelectedButton(selectedId);

        Boolean isValidVinVal = vinValue.validateField(vinValue);
        Boolean isValidValAmount = valueAmount.validateField(valueAmount);
        Boolean isValidateDesc = description.validateField(description);
        Boolean isValiddateVal = false;
//        String dateVal = dateValue.getText().toString();
        String dateVal = tvServiceDate.getText().toString();
        float model;
        switch (selectedBtn) {
            case "Section 179":
                model = 0;
                dateVal = "none";
                isValiddateVal = true;

                break;
            case "3 year straight line":
                model = 1;
//                isValiddateVal = dateValue.validateField(dateValue);
                isValiddateVal =true;

                break;
            case "5 year straight line":
                model = 2;
//                isValiddateVal = dateValue.validateField(dateValue);
                isValiddateVal = true;

                break;
            case "N/A":
                model = 3;
                dateVal = "none";
                isValiddateVal = true;

                break;
            default:
                model = 3;
        }

        if (isValidateDesc && isValiddateVal && isValidValAmount && isValidVinVal) {

            String uniqueId = getIntent().getStringExtra("internalId");
            String vinVal = vinValue.getText().toString();
            Float valAmount = parseFloat(valueAmount.getText().toString());
            String desc = description.getText().toString().trim();


            JsonObject jsonObject = InputParams.assetInfoData(uniqueId, vinVal, valAmount, dateVal, desc, assetType, model, false);
            customProgressLoader.showDialog();

            assetDeductionViewModel.postAssetDeductionModelLiveData(jsonObject).observe(this, new Observer<AssetDeductionResponseModel>() {
                @Override
                public void onChanged(@Nullable AssetDeductionResponseModel assetDeductionResponseModel) {
                    customProgressLoader.hideDialog();

                    if (assetDeductionResponseModel != null) {
                        String statusMssg = assetDeductionResponseModel.getMessage();
                        showDialog(statusMssg);

                    }
                }
            });
            Bundle params = new Bundle();
            params.putString(assetType + "_submit", "success");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        } else {
            Bundle params = new Bundle();
            params.putString(assetType + "_submit", "fail");
            mFirebaseAnalytics.logEvent("Tax_Info", params);
        }
    }

    public String setSelectedButton(int selectedId) {

        // find the radiobutton by returned id
        RadioButton radioBtn = findViewById(selectedId);


        String selectedButtonValue = (String) radioBtn.getText();
        if ((selectedButtonValue.equals("3 year straight line")) || (selectedButtonValue.equals("5 year straight line"))) {

            if (clearDate == true) {
//                dateValue.setText("");
                tvServiceDate.setText("");
            }
        } else {
            relativeLayout.setVisibility(View.GONE);
        }

        return selectedButtonValue;
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

    public void showDialog(String statusMssg) {

        String status = "";
        String message = "";
        if (statusMssg.equals("Success") || statusMssg.equals("success")) {
            status = "Success";
            message = "Asset editted successfully";
        } else {
            status = "Error";
            message = "Something went wrong, Please try again later!";
        }
        confirmationComponent = new ConfirmationComponent(this, status, message, true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                confirmationComponent.dismiss();
                setResult(RESULT_OK, null);

//                getFragmentManager().popBackStack();
                finish();
            }
        }, 1);
//        finish();

    }

    @OnClick(R.id.vin_value)
    void clickVinid() {
        vinValue.setCursorVisible(true);
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }
}
