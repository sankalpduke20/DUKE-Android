package com.dukeai.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.models.ReportSpinnerDataModel;

import java.util.ArrayList;

public class ReportSpinnerAdapter extends ArrayAdapter implements SpinnerAdapter {

    Context context;
    int resource;
    ArrayList<ReportSpinnerDataModel> data;

    public ReportSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ReportSpinnerDataModel> data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReportSpinnerHolder reportSpinnerHolder;
        View view = convertView;
        if (view == null) {
            reportSpinnerHolder = new ReportSpinnerHolder();
            LayoutInflater itemInflator = LayoutInflater.from(getContext());
            convertView = itemInflator.inflate(this.resource, parent, false);
            reportSpinnerHolder.spinnerText = convertView.findViewById(R.id.report_dropdown_item);
            reportSpinnerHolder.bottomBorder = convertView.findViewById(R.id.bottom_border);

            convertView.setTag(reportSpinnerHolder);
        } else {
            reportSpinnerHolder = (ReportSpinnerHolder) convertView.getTag();
        }
        ReportSpinnerDataModel selectedItem = data.get(position);
        reportSpinnerHolder.spinnerText.setText(selectedItem.getSpinnerText());
        reportSpinnerHolder.bottomBorder.setVisibility(View.VISIBLE);

        return convertView;
    }

    @Override
    public View getDropDownView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReportSpinnerHolder reportSpinnerHolder = new ReportSpinnerHolder();
        ReportSpinnerDataModel dataModel = data.get(position);
        LayoutInflater itemInflater = LayoutInflater.from(getContext());
        convertView = itemInflater.inflate(this.resource, parent, false);
        reportSpinnerHolder.spinnerText = convertView.findViewById(R.id.report_dropdown_item);
        reportSpinnerHolder.bottomBorder = convertView.findViewById(R.id.bottom_border);
        reportSpinnerHolder.dropdown = convertView.findViewById(R.id.dropdown_icon);

        reportSpinnerHolder.spinnerText.setText(dataModel.getSpinnerText());
        reportSpinnerHolder.spinnerText.setPadding(15, 15, 20, 15);
        reportSpinnerHolder.dropdown.setVisibility(View.GONE);
        reportSpinnerHolder.bottomBorder.setVisibility(View.GONE);

        convertView.setTag(reportSpinnerHolder);
        return convertView;
    }

    public class ReportSpinnerHolder {
        TextView spinnerText;
        View bottomBorder;
        ImageView dropdown;
    }
}
