package com.dukeai.android.bottomSheetDialogs;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;

import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;

public class CustomFilterSheet extends BottomSheetDialog implements PopupActions {

    public String fromDate = "";
    public String toDate = "";
    PopupActions popupActions;
    Context context;
    Activity activity;
    LifecycleOwner lifecycleOwner;
    ConfirmationComponent confirmationComponent;
    //    @BindView(R.id.close_icon)
//    ImageView closeIcon;
//    @BindView(R.id.from_date_error)
//    TextView fromDateError;
//    @BindView(R.id.to_date_error)
//    TextView toDateError;
//    @BindView(R.id.submit)
//    Button submit;
//    @BindView(R.id.to_date_value)
//    TextInputEditText to_date_value;
//    @BindView(R.id.from_date_value)
//    TextInputEditText from_date_value;
//     Calendar myCalendar = Calendar.getInstance();
    View customFilterView;
    CustomProgressLoader customProgressLoader;
    //    public CustomFilterSheet(@NonNull final Context context, final Activity activity, LifecycleOwner owner) {
//        super(context);
//        this.context = context;
//        this.activity = activity;
//        this.lifecycleOwner = owner;
//        this.popupActions = this;
//        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        setInitials();
//        final CustomEditInputField serviceDatePicker = (CustomEditInputField) findViewById(R.id.to_date_value);
//
//        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//
//            @Override
//            public void onDateSet(DatePicker view, int year, int monthOfYear,
//                                  int dayOfMonth) {
//                // TODO Auto-generated method stub
//                myCalendar.set(Calendar.YEAR, year);
//                myCalendar.set(Calendar.MONTH, monthOfYear);
//                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
////                updateLabel();
////                String myFormat = "dd/MM/yyyy"; //In which you need put here
////                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//////                final Calendar myCalendars = myCalendar.getInstance();
//                serviceDatePicker.setText((new StringBuilder().append(year).append("-").append(monthOfYear).append("-").append(dayOfMonth)));
//
//            }
//
//        };
////        DatePickerDialog dpDialog = new DatePickerDialog(this,R.style.DialogThemeColor, date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
////        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//
//        serviceDatePicker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Calendar calendar;
//                DatePickerDialog dpDialog =  new DatePickerDialog(activity, date, myCalendar
//                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                        myCalendar.get(Calendar.DAY_OF_MONTH));
//                dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//                dpDialog.show();
//            }
//        });
//    }
//
//    private void setInitials() {
//        customFilterView = getLayoutInflater().inflate(R.layout.custom_filter, null);
//        setContentView(customFilterView);
//        customProgressLoader = new CustomProgressLoader(context);
//        ButterKnife.bind(this, customFilterView);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(60, 0, 0, 0)));
//    }
//
//
//
//
//    @OnClick(R.id.close_icon)
//    void onClickCloseIcon() {
//        this.dismiss();
//    }
//    public void showDialog() {
//        this.show();
//    }
//
//    public void dismissDialog() {
//        this.dismiss();
//    }
//
//    @Override
//    public void onPopupActions(String id, int dialogId) {
//        confirmationComponent.dismiss();
//    }
    String toDateValue = "";
    String fromDateValue = "";

    int fromDateFlag = 0, toDateFlag = 0;

    int tomonthVal, todateVal, toyearVal;
    int frommonthVal, fromdateVal, fromyearVal;


    public CustomFilterSheet(@NonNull Context context, Activity activity, LifecycleOwner owner) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.lifecycleOwner = owner;
        this.popupActions = this;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        setInitials();
//        initiateCallBack();
//        setTextChangeListeners();
    }

    //    private void setTextChangeListeners() {
//        to_date_value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    toDateValue = to_date_value.getText().toString();
//                    boolean toDateValid = validateDate(AppConstants.StringConstants.NON_EMPTY, toDateValue, AppConstants.StringConstants.NON_EMPTY);
//                    if (toDateValid) {
//                        toDateError.setVisibility(View.GONE);
//                    } else {
//                        toDateError.setVisibility(View.VISIBLE);
//                    }
//                    if((!toDateValue.isEmpty())&&(toDateValid) ){
//                        toDateFlag = 1;
//                    }
//                }
//                else {
//
//
//                    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//
//                        @Override
//                        public void onDateSet(DatePicker view, int year, int monthOfYear,
//                                              int dayOfMonth) {
//                            // TODO Auto-generated method stub
//                            myCalendar.set(Calendar.YEAR, year);
//                            myCalendar.set(Calendar.MONTH, monthOfYear);
//                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
////                updateLabel();
////                String myFormat = "dd/MM/yyyy"; //In which you need put here
////                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//////                final Calendar myCalendars = myCalendar.getInstance();
////                            if(monthOfYear == 12){
////                                monthOfYear = 11;
////                            }
//                            tomonthVal = monthOfYear+1;
//                            toyearVal = year;
//                            todateVal = dayOfMonth;
//                            to_date_value.setText((new StringBuilder().append(year).append("-").append(monthOfYear+1).append("-").append(dayOfMonth)));
//
//                            Log.i("monthtodate",String.valueOf(monthOfYear+1));
//                        }
//
//                    };
////        DatePickerDialog dpDialog = new DatePickerDialog(this,R.style.DialogThemeColor, date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
////        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//
//                    to_date_value.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                    Calendar calendar;
//                    DatePickerDialog dpDialog =  new DatePickerDialog(activity, date, myCalendar
//                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                            myCalendar.get(Calendar.DAY_OF_MONTH));
//                    if(fromDateFlag == 0){
//                        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//
//                    }else{
//                        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
////                        fromDateValue = from_date_value.getText().toString();
////
////                        String sDate1=fromDateValue;
//
//                        String sDate1 = ""+fromyearVal+"-"+frommonthVal+"-"+fromdateVal;
//
//                        long minDate=0;
//                        try {
//                            Date fromDate1=new SimpleDateFormat("yyyy-mm-dd").parse(sDate1);
//                            minDate = fromDate1.getTime();
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        dpDialog.getDatePicker().setMinDate(minDate);
//                        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//                    }
//                    dpDialog.show();
//                        }
//                    });
//
//                }
//            }
//        });
//        from_date_value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    fromDateValue = from_date_value.getText().toString();
//
//                    boolean fromDateValid = validateDate(AppConstants.StringConstants.NON_EMPTY, fromDateValue, AppConstants.StringConstants.NON_EMPTY);
//                    if (fromDateValid) {
//                        fromDateError.setVisibility(View.GONE);
//                    } else {
//                        fromDateError.setVisibility(View.VISIBLE);
//                    }
//                    if((!fromDateValue.isEmpty())&&(fromDateValid) ){
//                        fromDateFlag = 1;
//                    }
//                }else {
//
//                    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
//
//                        @Override
//                        public void onDateSet(DatePicker view, int year, int monthOfYear,
//                                              int dayOfMonth) {
//                            // TODO Auto-generated method stub
//                            myCalendar.set(Calendar.YEAR, year);
//                            myCalendar.set(Calendar.MONTH, monthOfYear);
//                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
////                updateLabel();
////                String myFormat = "dd/MM/yyyy"; //In which you need put here
////                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//////                final Calendar myCalendars = myCalendar.getInstance();
////                            if(monthOfYear == 12){
////                                monthOfYear = 11;
////                            }
//                            fromyearVal = year;
//                            frommonthVal = monthOfYear+1;
//                            fromdateVal = dayOfMonth;
//                            from_date_value.setText((new StringBuilder().append(year).append("-").append(monthOfYear+1).append("-").append(dayOfMonth)));
//                            Log.i("monthfromDate",String.valueOf(monthOfYear+1));
//
//                        }
//
//                    };
////        DatePickerDialog dpDialog = new DatePickerDialog(this,R.style.DialogThemeColor, date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
////        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//
//                    from_date_value.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                    Calendar calendar;
//                    DatePickerDialog dpDialog =  new DatePickerDialog(activity, date, myCalendar
//                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
//                            myCalendar.get(Calendar.DAY_OF_MONTH));
//                    if(toDateFlag == 0){
//                        dpDialog.getDatePicker().setMaxDate(new Date().getTime());
//
//                    }else{
////                        toDateValue = to_date_value.getText().toString();
////                        String sDate1=toDateValue;
//
//                                                String sDate1 = ""+toyearVal+"-"+tomonthVal+"-"+todateVal;
//
//                        long maxDate=0;
//                        try {
//                             Date toDate1=new SimpleDateFormat("yyyy-mm-dd").parse(sDate1);
//                             Log.i("todate1",toDate1.toString());
//                            maxDate = toDate1.getTime();
//                            Log.i("maxDate1",String.valueOf(maxDate));
//
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        dpDialog.getDatePicker().setMaxDate(maxDate);
//                    }
//                    dpDialog.show();
//                        }
//                    });
//                }
//            }
//        });
//        to_date_value.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) {
//                    toDateValue = to_date_value.getText().toString();
//                    boolean toDateValid = validateDate(AppConstants.StringConstants.OLD_PASSWORD, toDateValue, AppConstants.StringConstants.OLD_PASSWORD);
//                    if (toDateValid) {
//                        toDateError.setVisibility(View.GONE);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        from_date_value.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) {
//                    fromDateValue = from_date_value.getText().toString();
//                    boolean isNewPasswordValid = validateDate(AppConstants.StringConstants.OLD_PASSWORD, fromDateValue, AppConstants.StringConstants.NEW_PASSWORD);
//                    if (isNewPasswordValid) {
//                        fromDateError.setVisibility(View.GONE);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//    }
//
////    private void initiateCallBack() {
////        genericHandler = new GenericHandler() {
////            @Override
////            public void onSuccess() {
////                dismissDialog();
////                isPasswordChanged = true;
////                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(context, R.string.success), Utilities.getStrings(context, R.string.password_changed_please_login), false, Utilities.getStrings(context, R.string.ok), popupActions,1);
////            }
////
////            @Override
////            public void onFailure(Exception exception) {
////                String message = exception.getMessage();
////                if (exception.getMessage().contains(AppConstants.ChangePasswordSheet.INCORRECT_STATEMENT)) {
////                    message = Utilities.getStrings(Duke.getInstance(), R.string.invalid_username_password);
////                } else if(exception.getMessage().contains(AppConstants.StringConstants.NO_INTERNET)){
////                    message = Utilities.getStrings(Duke.getInstance(), R.string.no_internet);
////                }
////                confirmationComponent = new ConfirmationComponent(context, Utilities.getStrings(context, R.string.error), message, false, Utilities.getStrings(context, R.string.ok), popupActions,1);
////            }
////        };
////    }
//
//        private void setInitials() {
//        customFilterView = getLayoutInflater().inflate(R.layout.custom_filter, null);
//        setContentView(customFilterView);
//        customProgressLoader = new CustomProgressLoader(context);
//        ButterKnife.bind(this, customFilterView);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(60, 0, 0, 0)));
//    }
//
//    @OnClick(R.id.close_icon)
//    void onClickCloseIcon() {
//        this.dismiss();
//    }
//
////    @OnClick(R.id.submit)
////    void onClickDoneButton() {
////        toDateValue = to_date_value.getText().toString();
////        fromDateValue = from_date_value.getText().toString();
////        boolean toDateValid = validateDate(AppConstants.StringConstants.NON_EMPTY, toDateValue, AppConstants.StringConstants.NON_EMPTY);
////        boolean fromDateValid = validateDate(AppConstants.StringConstants.NON_EMPTY, fromDateValue, AppConstants.StringConstants.NON_EMPTY);
////        if (toDateValid && fromDateValid) {
////
////            fromDate = fromDateValue;
////            toDate = toDateValue;
////
////            Log.i("fromdate",fromDate);
////            Log.i("toDate",toDate);
//////            ReportsFragment fragment = new ReportsFragment();
//////            fragment.getReportsData(4);
//////            fragment.getReportsDataForCustomFilter(fromDate,toDate);
////
////        }
////    }
//
//    private boolean validateDate(String text, String pw, String type) {
//        Boolean isValid = InputValidators.validateInput(text, pw);
//        if (isValid) {
//            if (type.equals(AppConstants.StringConstants.NON_EMPTY)) {
//                toDateError.setVisibility(View.GONE);
//            } else {
//                fromDateError.setVisibility(View.GONE);
//            }
//        } else {
//            if (type.equals(AppConstants.StringConstants.NON_EMPTY)) {
//                toDateError.setVisibility(View.VISIBLE);
//            } else {
//                fromDateError.setVisibility(View.VISIBLE);
//            }
//        }
//        return isValid;
//    }
//
//    public void showDialog() {
//        this.show();
//    }
//
//    public void dismissDialog() {
//        this.dismiss();
//    }
//
    @Override
    public void onPopupActions(String id, int dialogId) {
//        if(isPasswordChanged){
//            confirmationComponent.dismiss();
//            Utilities.logoutUser(context, activity, deviceTokenViewModel, lifecycleOwner);
//        } else {
//            confirmationComponent.dismiss();
//        }
    }
}
