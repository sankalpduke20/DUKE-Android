package com.dukeai.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.models.MyListData;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private MyListData[] listdata;

    // RecyclerView recyclerView;
    public MyListAdapter(MyListData[] listdata) {
        this.listdata = listdata;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MyListData myListData = listdata[position];
        holder.textView.setText(myListData.getDescription());


    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.tv11);
            relativeLayout = itemView.findViewById(R.id.rl11);
        }
    }
}
