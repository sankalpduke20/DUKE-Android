package com.dukeai.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.models.BalanceSheetModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.ui.activities.AddBalanceSheetReport;

public class BalanceSheetAdapter extends RecyclerView.Adapter<BalanceSheetAdapter.ViewHolder> {

    Context context;
    private BalanceSheetModel.DataListModel[] listdata;

    // RecyclerView recyclerView;
    public BalanceSheetAdapter(BalanceSheetModel.DataListModel[] listdata) {
        this.listdata = listdata;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.balance_sheet_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BalanceSheetModel.DataListModel myListData = listdata[position];
        holder.textView.setText(listdata[position].getDescription());
        holder.amount.setText(String.valueOf(listdata[position].getCashValue()));
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "click on item: " + myListData.getDescription(), Toast.LENGTH_LONG).show();
            }
        });
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent("custom-message");
                Intent intent = new Intent(context, AddBalanceSheetReport.class);

                //            intent.putExtra("quantity",Integer.parseInt(quantity.getText().toString()));
                intent.putExtra("id", String.valueOf(position));
                intent.putExtra("category", listdata[position].getAccountType());
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                intent.putExtra("description", listdata[position].getDescription());
                intent.putExtra("account_type", listdata[position].getAccountType());
                intent.putExtra("internal_id", listdata[position].getInternalId());
                intent.putExtra("delete", false);
                intent.putExtra("cashvalue", String.valueOf(listdata[position].getCashValue()));
                intent.putExtra("typeOfSheet", "edit");

                ((Activity) context).startActivityForResult(intent, 1);

            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public RelativeLayout relativeLayout;
        public TextView amount;
        public ImageView edit;
        public TextView dollar;


        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.textv11);
            this.amount = itemView.findViewById(R.id.amount);
            relativeLayout = itemView.findViewById(R.id.rel11);
            this.edit = itemView.findViewById(R.id.next_button);
            dollar = itemView.findViewById(R.id.dollar);

            ChangeThemeModel changeThemeModel = new ChangeThemeModel();
            dollar.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
            amount.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
            edit.setColorFilter(Color.parseColor(changeThemeModel.getNextForwardButtonColor()));
        }
    }
}