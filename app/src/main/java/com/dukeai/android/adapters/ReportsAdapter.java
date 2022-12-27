package com.dukeai.android.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.FinancialSummaryModel;
import com.dukeai.android.models.SubscriptionPlan;
import com.dukeai.android.utils.AppConstants;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.MyViewHolder> {

    ArrayList<FinancialSummaryModel.DataModel.ReportsModel> list = new ArrayList<>();
    Context context;
    int resource;
    HashSet<SubscriptionPlan.MemberStatus> podPlans = new HashSet<>();
    private OnListItemClickListener onListItemClickListener;

    public ReportsAdapter(Context context, int resource, OnListItemClickListener listItemClickListener) {
        this.list = list;
        this.context = context;
        this.resource = resource;
        this.onListItemClickListener = listItemClickListener;
        podPlans.add(SubscriptionPlan.MemberStatus.POD);
        podPlans.add(SubscriptionPlan.MemberStatus.FREE_POD);
        podPlans.add(SubscriptionPlan.MemberStatus.BASIC_POD);
        podPlans.add(SubscriptionPlan.MemberStatus.ESSENTIAL_POD);
        podPlans.add(SubscriptionPlan.MemberStatus.PREMIUM_POD);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final FinancialSummaryModel.DataModel.ReportsModel dataModel = list.get(position);

        if (Duke.subscriptionPlan.isFeatureIncluded(dataModel.getType())) {
            holder.disabledInfoView.setVisibility(View.GONE);
            holder.disabledLayer.setBackground(context.getDrawable(R.color.colorTransparent));
            holder.downloadReport.setForeground(context.getDrawable(R.color.colorTransparent));
        }  else if(dataModel.getType().equalsIgnoreCase(AppConstants.ReportConstants.BALANCESHEET)) {
//            holder.disabledInfoView.setVisibility(View.VISIBLE);
//            holder.disabledLayer.setClickable(false);
//            TextView text = holder.disabledInfoView.findViewById(R.id.disable_reason);
//            text.setText("Coming Soon");
//            holder.amount.setVisibility(View.GONE);
//            holder.reportType.setGravity(Gravity.CENTER_VERTICAL);
//            holder.disabledLayer.setBackground(context.getDrawable(R.drawable.disabled_bg_rounded_corners));
//            holder.downloadReport.setVisibility(View.GONE);
//            holder.reportIcon.setVisibility(View.GONE);
        } else {
            holder.disabledInfoView.setVisibility(View.VISIBLE);
            holder.disabledLayer.setBackground(context.getDrawable(R.drawable.disabled_bg_rounded_corners));
            holder.downloadReport.setForeground(context.getDrawable(R.drawable.disabled_download_button_circle));
        }

        if (String.valueOf(dataModel.getAmount()).toLowerCase().equals("nan")) {
            holder.amount.setText(R.string.no_data_available);
            holder.units.setVisibility(View.GONE);

        } else {
            holder.amount.setText(String.valueOf(dataModel.getAmount()));
            holder.units.setVisibility(View.VISIBLE);
        }

        if (dataModel.getType() != null)
            holder.reportType.setText(dataModel.getType());

        setIconBasedOnType(dataModel, holder);

        holder.reportsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListItemClickListener != null) {
                    if (Duke.subscriptionPlan.isFeatureIncluded(list.get(position).getType()))
                        onListItemClickListener.onListItemClick(position, dataModel);
                }
            }
        });

        holder.downloadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListItemClickListener != null) {
                    if (Duke.subscriptionPlan.isFeatureIncluded(list.get(position).getType()))
                        onListItemClickListener.onListItemClick(position, dataModel);
                }
            }
        });
    }

    private void setIconBasedOnType(FinancialSummaryModel.DataModel.ReportsModel dataModel, MyViewHolder holder) {
        if (dataModel.getType() != null) {
            String type = dataModel.getType();
            if (type.equalsIgnoreCase(AppConstants.ReportConstants.BALANCESHEET)) {
                holder.reportIcon.setImageResource(R.drawable.ic_settlments);
                holder.reportType.setText(AppConstants.ReportConstants.BALANCE_SHEET);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.EXPENSES))
                holder.reportIcon.setImageResource(R.drawable.ic_expences);
            else if (type.equalsIgnoreCase(AppConstants.ReportConstants.YTD)) {
                holder.reportIcon.setImageResource(R.drawable.ic_net_income);
                holder.reportType.setText(AppConstants.ReportConstants.YEARTODATETAXLIABILITY);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.PL)) {
                holder.reportIcon.setImageResource(R.drawable.ic_liablities);
                holder.reportType.setText(AppConstants.ReportConstants.PROFITANDLOSS);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.IFTA)) {
                holder.reportIcon.setImageResource(R.drawable.ic_ifta);
                holder.reportType.setText(AppConstants.ReportConstants.IFTATAXFILINGS);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.POD)) {
                holder.reportIcon.setImageResource(R.drawable.ic_pod);
                holder.reportType.setText(AppConstants.ReportConstants.PROOFOFDELIVERY);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.SELFTAX)) {
                holder.reportIcon.setImageResource(R.drawable.ic_self_tax);
                holder.reportType.setText(AppConstants.ReportConstants.SELFESTIMATOR);
            } else if (type.equalsIgnoreCase(AppConstants.ReportConstants.FEDTAX)) {
                holder.reportIcon.setImageResource(R.drawable.ic_fed_tax);
                holder.reportType.setText(AppConstants.ReportConstants.FEDESTIMATOR);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateDataList(ArrayList<FinancialSummaryModel.DataModel.ReportsModel> dataList) {
        if (list != null) {
            list.clear();

//            /**To Show POD on TOP**/
            //Uncomment when needed
//            if (podPlans.contains(Duke.subscriptionPlan.getMemberStatus())) {
//                int i = dataList.size() - 1;
//                for (; i >= 0; --i) {
//                    if (dataList.get(i).type.contains("POD"))
//                        break;
//                }
//                if (i>=0){
//                    list.add(0, dataList.get(i));
//                    dataList.remove(i);
//                }
//            }

            list.addAll(dataList);
            list.remove(list.size()-1);
            notifyDataSetChanged();
        }
    }

    public interface OnListItemClickListener {
        void onListItemClick(int pos, FinancialSummaryModel.DataModel.ReportsModel dataModel);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container)
        RelativeLayout container;

        @BindView(R.id.report_item_layout)
        View reportsView;

        @BindView(R.id.amount)
        TextView amount;

        @BindView(R.id.report_type)
        TextView reportType;

        @BindView(R.id.report_icon)
        ImageView reportIcon;

        @BindView(R.id.download_report)
        ImageButton downloadReport;

        @BindView(R.id.amount_units)
        TextView units;

        @BindView(R.id.disabledLayer)
        RelativeLayout disabledLayer;

        @BindView(R.id.disabled_info_view)
        FrameLayout disabledInfoView;

        public MyViewHolder(View itemView) {
            super(itemView);
            reportsView = itemView;
            ButterKnife.bind(this, itemView);
            ChangeThemeModel changeThemeModel = new ChangeThemeModel();
            amount.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
            units.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
            reportIcon.setColorFilter(Color.parseColor(changeThemeModel.getFontColor()));
            downloadReport.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
//            downloadReport.setBackgroundColor(Color.parseColor(changeThemeModel.getFontColor()));
        }
    }
}
