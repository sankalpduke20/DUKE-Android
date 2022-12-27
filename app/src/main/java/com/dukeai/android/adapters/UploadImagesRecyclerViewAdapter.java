package com.dukeai.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.UploadImagePreviewClickListener;
import com.dukeai.android.utils.Utilities;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadImagesRecyclerViewAdapter extends RecyclerView.Adapter<UploadImagesRecyclerViewAdapter.ViewHolder> {

    Context context;
    int selectedPosition = 0;
    ViewHolder result;
    UploadImagePreviewClickListener uploadImagePreviewClickListener;
    private ArrayList<Bitmap> imagesList;
    private int resource;

    public UploadImagesRecyclerViewAdapter(ArrayList<Bitmap> imagesList, int resource, Context context, UploadImagePreviewClickListener listener) {
        this.imagesList = imagesList;
        this.resource = resource;
        this.context = context;
        this.uploadImagePreviewClickListener = listener;
        this.selectedPosition = imagesList.size() - 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(this.resource, viewGroup, false);
        result = new ViewHolder(view);
        return new UploadImagesRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Bitmap data = imagesList.get(i);

        viewHolder.itemImage.setImageBitmap(data);
        if (selectedPosition == i) {
            viewHolder.view.setBackground(Utilities.getDrawable(context, R.drawable.selected_item_border));
        } else {
            viewHolder.view.setBackground(null);
        }
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadImagePreviewClickListener != null) {
                    selectedPosition = i;
                    uploadImagePreviewClickListener.onUploadImagePreviewClickListener(data);
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        @BindView(R.id.item_image)
        ImageView itemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            ButterKnife.bind(this, view);
        }


    }
}
