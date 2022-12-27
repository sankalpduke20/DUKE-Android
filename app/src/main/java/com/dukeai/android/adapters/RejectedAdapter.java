package com.dukeai.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.dukeai.android.R;
import com.dukeai.android.models.RejectedDocumentsModel;
import com.dukeai.android.utils.DateFormatter;
import com.dukeai.android.utils.URLBuilder;
import com.dukeai.android.viewModel.FileStatusViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RejectedAdapter extends RecyclerView.Adapter<RejectedAdapter.MyViewHolder> {

    ArrayList<RejectedDocumentsModel> list = new ArrayList<>();
    Context context;
    int resource;
    boolean showRejected;
    FileStatusViewModel fileStatusViewModel;
    Fragment parentFragment;
    DateFormatter dateFormatter;
    private OnListItemClickListener onListItemClickListener;

    public RejectedAdapter(Context context, int resource, boolean showRejected, OnListItemClickListener onListItemClickListener, FileStatusViewModel viewModel, Fragment fragment) {
        this.context = context;
        this.resource = resource;
        this.showRejected = showRejected;
        this.fileStatusViewModel = viewModel;
        this.parentFragment = fragment;
        this.onListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(resource, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        final RejectedDocumentsModel dataModel = list.get(i);
        dateFormatter = new DateFormatter();

        if (dataModel.getUploadDate() != null)
            myViewHolder.documentDate.setText(dateFormatter.getFormattedDate(dataModel.getUploadDate()));

        if (showRejected)
            myViewHolder.rejectedIcon.setVisibility(View.VISIBLE);
        else
            myViewHolder.rejectedIcon.setVisibility(View.GONE);

        GlideUrl imageURL = URLBuilder.getGlideUrl(list.get(i).getThumbnail());
        //Async loading
        Glide.with(context)
                .asBitmap()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(BitmapTransitionOptions.withCrossFade())
                .error(Glide
                        .with(context)
                        .asBitmap()
                        .load(imageURL)
                        .placeholder(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transition(BitmapTransitionOptions.withCrossFade()))
                .into(myViewHolder.documentImage);

        myViewHolder.documentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListItemClickListener.onListItemClick(i, dataModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateDataList(ArrayList<RejectedDocumentsModel> dataList) {
        if (list != null) {
            list.clear();
            list.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    public interface OnListItemClickListener {
        void onListItemClick(int pos, RejectedDocumentsModel dataModel);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.documents_list_view)
        View documentsView;
        @BindView(R.id.document_image)
        ImageView documentImage;
        @BindView(R.id.document_date)
        TextView documentDate;
        @BindView(R.id.rejected_icon)
        ImageView rejectedIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            documentsView = itemView;
            ButterKnife.bind(this, itemView);

        }
    }
}
