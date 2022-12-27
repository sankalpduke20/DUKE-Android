package com.dukeai.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.DeleteAsset;
import com.dukeai.android.interfaces.RecyclerViewClickListener;
import com.dukeai.android.models.AssetDeductionModel;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.ui.activities.EditTractor;

public class AssetListAdapter extends RecyclerView.Adapter<AssetListAdapter.ViewHolder> {
    Context context;
    DeleteAsset deleteAsset;
    private RecyclerViewClickListener mListener;
    private AssetDeductionModel.DataListModel[] listdata;


    public AssetListAdapter(AssetDeductionModel.DataListModel[] listdata, DeleteAsset deleteAsset) {
        this.listdata = listdata;
        this.deleteAsset = deleteAsset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.tractor_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem, deleteAsset);
        context = parent.getContext();
        Log.i("contentxsfsdf", String.valueOf(context));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Intent intent, intent1;
        final AssetDeductionModel.DataListModel myListData = listdata[position];
        holder.vinId.setText(listdata[position].getId());
        holder.vinVal.setText(listdata[position].getValueVal().toString());
        holder.description.setText(listdata[position].getDescription());

        intent = new Intent(this.context, EditTractor.class);

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
                intent.putExtra("id", String.valueOf(position));
                intent.putExtra("intentType", "edit");
                intent.putExtra("vinId", listdata[position].getId());
                intent.putExtra("vinVal", listdata[position].getValueVal().toString());
                intent.putExtra("desc", listdata[position].getDescription());
                intent.putExtra("assetType", listdata[position].getAssetType());
                intent.putExtra("internalId", listdata[position].getInternalId());
                intent.putExtra("model", listdata[position].getModel());
                intent.putExtra("delete", listdata[position].getDelete().toString());
                intent.putExtra("purchasessDate", listdata[position].getPurchaseDate());
                Activity origin = (Activity) context;
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public interface OnItemClickListener {
        void onItemClick(AssetDeductionModel.DataListModel item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView vinId;
        public LinearLayout layout;
        public TextView vinVal;
        public TextView description;
        ImageView delete;
        ImageView edit;
        DeleteAsset mdeleteAsset;
        private RecyclerViewClickListener mListener;


        public ViewHolder(View itemView, DeleteAsset deleteAsset) {
            super(itemView);
            this.vinId = itemView.findViewById(R.id.vin_val);
            this.vinVal = itemView.findViewById(R.id.value_val);
            this.description = itemView.findViewById(R.id.desc);
            layout = itemView.findViewById(R.id.assets_view11);
            delete = itemView.findViewById(R.id.delete_icon);
            edit = itemView.findViewById(R.id.edit_icon);

            mdeleteAsset = deleteAsset;
            delete.setOnClickListener(this);

            ChangeThemeModel changeThemeModel = new ChangeThemeModel();
            delete.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor()));
            edit.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor()));
        }

        @Override
        public void onClick(View view) {

            mdeleteAsset.deleteItem(getAdapterPosition());
        }

    }
}