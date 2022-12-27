package com.dukeai.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.models.InProcessDocumentsModel;
import com.dukeai.android.models.LoadsListModel;
import com.dukeai.android.models.ProcessedDocumentsModel;
import com.dukeai.android.models.UserLoadsModel;
import com.dukeai.android.utils.DateFormatter;
import com.dukeai.android.utils.Utilities;

import java.util.ArrayList;

public class LoadsListAdapter extends RecyclerView.Adapter<LoadsListAdapter.LoadsListHolder> {


    Context context;
    private ArrayList<UserLoadsModel> listdata;
    private OnListItemClickListener onListItemClickListener;
    DateFormatter dateFormatter = new DateFormatter();
    int green = Color.parseColor("#34C759");
    int lightGray = Color.parseColor("#AAAAAA");
    int lightRed = Color.parseColor("#FF453A");

    // RecyclerView recyclerView;
    public LoadsListAdapter(ArrayList<UserLoadsModel> listdata, OnListItemClickListener onListItemClickListener) {
        this.listdata = listdata;
        this.onListItemClickListener = onListItemClickListener;
    }

    @Override
    public LoadsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.loads_list_item, parent, false);
        LoadsListAdapter.LoadsListHolder viewHolder = new LoadsListAdapter.LoadsListHolder(listItem);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LoadsListHolder holder, int position) {
        UserLoadsModel currentItem = listdata.get(position);

        //TODO this code is written for differentiate cards on the basis of their statu_
        if(currentItem.getStatus()==null){
            holder.loadListItem.setBackgroundColor(0);
        }else if(currentItem.getStatus().equals("invoiced")){
            holder.loadListItem.setBackgroundColor(lightGray);
        }else if(currentItem.getStatus().equals("paid")){
            holder.loadListItem.setBackgroundColor(green);
        }else if(currentItem.getStatus().equals("rejected")){
            holder.loadListItem.setBackgroundColor(lightRed);
        }else{
            holder.loadListItem.setBackgroundColor(0);
        }

        holder.loadId.setText(currentItem.getLoadId());
//        holder.uploadDate.setText(currentItem.getCreatedDate());
//        holder.uploadDate.setText(dateFormatter.getFormattedDate(setRandomDates(position)));
        holder.uploadDate.setText(dateFormatter.getFormattedDate(currentItem.getDateCreated()));
//        holder.loadAmount.setText("NA");
        if (currentItem.getAmount()==null || currentItem.getAmount().equals("")) {
            holder.loadAmount.setText("NA");
        } else {
            holder.loadAmount.setText(String.valueOf(currentItem.getAmount()));
        }
        holder.isSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if(checked) {
                    holder.isSelected.setPressed(false);
                    if(!Duke.selectedLoadsForTransmission.contains(currentItem.getLoadUuid())){
                        Duke.selectedLoadsForTransmission.add(currentItem.getLoadUuid());
                    }
                } else {
                    holder.isSelected.setPressed(true);
                    if(Duke.selectedLoadsForTransmission.contains(currentItem.getLoadUuid())){
                        Duke.selectedLoadsForTransmission.remove(currentItem.getLoadUuid());
                    }
                }
                if(checked) {
                    if(!Duke.selectedLoadsForTransmission.contains(currentItem.getLoadUuid())){
                        Duke.selectedLoadsForTransmission.add(currentItem.getLoadUuid());
                    }
                } else {
                    if(Duke.selectedLoadsForTransmission.contains(currentItem.getLoadUuid())){
                        Duke.selectedLoadsForTransmission.remove(currentItem.getLoadUuid());
                    }
                }
            }
        });

        if(position == getItemCount()-1) {
            holder.separatorLine.setVisibility(View.GONE);
        }else{
            holder.separatorLine.setVisibility(View.VISIBLE);
        }

        holder.loadListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListItemClickListener.onListItemClick(position, currentItem);
            }
        });
    }

    private String setRandomDates(int position) {
        switch (position) {
            case 0 | 2 | 4:
                return "2021-12-18T12:59:59Z";
            case 1 | 3 :
                return "2021-08-10T12:59:59Z";
            case 7 | 6:
                return "2021-09-01T12:59:59Z";
            case 8:
                return "2020-12-02T12:59:59Z";
            case 9:
                return "2020-10-13T12:59:59Z";
            default:
                return "2020-08-09T12:59:59Z";
        }
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public void updateDataList(ArrayList<UserLoadsModel> dataList) {
        if (listdata != null) {
            listdata.clear();
            listdata.addAll(dataList);
            notifyDataSetChanged();
        }
    }


    public static class LoadsListHolder extends RecyclerView.ViewHolder {
        TextView loadId;
        TextView uploadDate;
        TextView loadAmount;
        CheckBox isSelected;
        LinearLayout loadListItem;
        View separatorLine;

        public LoadsListHolder(View itemView) {
            super(itemView);
            loadId = itemView.findViewById(R.id.load_id);
            uploadDate = itemView.findViewById(R.id.upload_date);
            loadAmount = itemView.findViewById(R.id.load_amount);
            isSelected = itemView.findViewById(R.id.is_load_selected);
            loadListItem = itemView.findViewById(R.id.load_list_item);
            separatorLine = itemView.findViewById(R.id.separator_line);
        }

    }

    public interface OnListItemClickListener {
        void onListItemClick(int pos, UserLoadsModel dataModel);
    }
}
