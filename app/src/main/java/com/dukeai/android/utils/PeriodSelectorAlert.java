package com.dukeai.android.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.OnPeriodSubmitListener;
import com.dukeai.android.views.CustomEditInputField;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PeriodSelectorAlert extends Dialog {

    @BindView(R.id.close_icon)
    ImageView closeIcon;

    @BindView(R.id.submit)
    Button submitButton;

    @BindView(R.id.from_date_error)
    TextView fromDateError;

    @BindView(R.id.to_date_error)
    TextView toDateError;

    @BindView(R.id.from_date_value)
    CustomEditInputField fromDateField;

    @BindView(R.id.to_date_value)
    CustomEditInputField toDateField;

    @BindView(R.id.from_date_wrapper)
    TextInputLayout fromDateLayout;

    @BindView(R.id.tvFromDate)
    TextView tvFromDate;

    @BindView(R.id.tvToDate)
    TextView tvToDate;

    Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalender = Calendar.getInstance();

    boolean didSetFromDate = false;
    boolean didSetToDate = false;

    public PeriodSelectorAlert(final Context context, final OnPeriodSubmitListener periodSubmitListener, View.OnClickListener closeButtonClickListener) {
        super(context);
        View view = View.inflate(context, R.layout.custom_filter, null);
        setContentView(view);
        ButterKnife.bind(this, view);
        this.setCancelable(false);

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    //
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        fromCalendar.set(Calendar.YEAR, year);
                        fromCalendar.set(Calendar.MONTH, monthOfYear);
                        fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvFromDate.setText(Utilities.getFormattedDate(fromCalendar));

                        fromDateError.setVisibility(View.GONE);
                        didSetFromDate = true;
                    }

                };

                DatePickerDialog dpDialog = new DatePickerDialog(context, date, fromCalendar
                        .get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
                        fromCalendar.get(Calendar.DAY_OF_MONTH));
                if (didSetToDate) {
                    dpDialog.getDatePicker().setMaxDate(toCalender.getTimeInMillis());
                } else {
                    dpDialog.getDatePicker().setMaxDate(new Date().getTime());
                }
                dpDialog.show();
            }

        });

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    final DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            toCalender.set(Calendar.YEAR, year);
                            toCalender.set(Calendar.MONTH, monthOfYear);
                            toCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tvToDate.setText(Utilities.getFormattedDate(toCalender));


                            toDateError.setVisibility(View.GONE);
                            didSetToDate = true;
                        }

                    };

                    DatePickerDialog dpDialog = new DatePickerDialog(context, toDate, toCalender
                            .get(Calendar.YEAR), toCalender.get(Calendar.MONTH),
                            toCalender.get(Calendar.DAY_OF_MONTH));
                    if (didSetFromDate) {
                        dpDialog.getDatePicker().setMinDate(fromCalendar.getTimeInMillis());
                    }
                    dpDialog.getDatePicker().setMaxDate(new Date().getTime());
                    dpDialog.show();
                }
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (didSetFromDate && didSetToDate) {
                    periodSubmitListener.onSubmitClick(fromCalendar, toCalender);
                } else {

                    if (didSetToDate) {
                        toDateError.setVisibility(View.GONE);
                    } else {
                        toDateError.setVisibility(View.VISIBLE);
                    }

                    if (didSetFromDate) {
                        fromDateError.setVisibility(View.GONE);
                    } else {
                        fromDateError.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        closeIcon.setOnClickListener(closeButtonClickListener);



        toDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        toCalender.set(Calendar.YEAR, year);
                        toCalender.set(Calendar.MONTH, monthOfYear);
                        toCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        toDateField.setText(Utilities.getFormattedDate(toCalender));
                        Log.d("PM => ","To Date "+Utilities.getFormattedDate(toCalender));
                        toDateError.setVisibility(View.GONE);
                        didSetToDate = true;
                    }

                };

                DatePickerDialog dpDialog = new DatePickerDialog(context, toDate, toCalender
                        .get(Calendar.YEAR), toCalender.get(Calendar.MONTH),
                        toCalender.get(Calendar.DAY_OF_MONTH));
                if (didSetFromDate) {
                    dpDialog.getDatePicker().setMinDate(fromCalendar.getTimeInMillis());
                }
                dpDialog.getDatePicker().setMaxDate(new Date().getTime());
                dpDialog.show();
            }
        });

        fromDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    //
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        fromCalendar.set(Calendar.YEAR, year);
                        fromCalendar.set(Calendar.MONTH, monthOfYear);
                        fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        fromDateField.setText(Utilities.getFormattedDate(fromCalendar));
                        fromDateError.setVisibility(View.GONE);
                        didSetFromDate = true;
                    }

                };

                DatePickerDialog dpDialog = new DatePickerDialog(context, date, fromCalendar
                        .get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
                        fromCalendar.get(Calendar.DAY_OF_MONTH));
                if (didSetToDate) {
                    dpDialog.getDatePicker().setMaxDate(toCalender.getTimeInMillis());
                } else {
                    dpDialog.getDatePicker().setMaxDate(new Date().getTime());
                }
                dpDialog.show();
            }
        });
        showPopup();
    }

    @OnClick(R.id.from_date_wrapper)
    public void onClickFrom(View view) {
        Toast.makeText(getContext(), "PM-Clicked", Toast.LENGTH_SHORT).show();
    }

    private void showPopup() {
        if (!this.isShowing()) {
            this.show();
        }
    }
}
