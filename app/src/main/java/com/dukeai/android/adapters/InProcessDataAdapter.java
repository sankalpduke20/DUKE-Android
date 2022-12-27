package com.dukeai.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dukeai.android.R;
import com.dukeai.android.models.InProcessDocumentsModel;
import com.dukeai.android.utils.DateFormatter;
import com.dukeai.android.utils.URLBuilder;
import com.dukeai.android.viewModel.FileStatusViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InProcessDataAdapter extends RecyclerView.Adapter<InProcessDataAdapter.MyViewHolder> {
    ArrayList<InProcessDocumentsModel> list = new ArrayList<>();
    Context context;
    int resource;
    boolean showRejected;
    FileStatusViewModel fileStatusViewModel;
    Fragment parentFragment;
    DateFormatter dateFormatter;
    int[] profileImg = {R.drawable.fashion_image, R.drawable.fashion_image, R.drawable.fashion_image};
    private OnListItemClickListener onListItemClickListener;

    public InProcessDataAdapter(Context context, int resource, boolean showRejected, OnListItemClickListener onListItemClickListener, FileStatusViewModel viewModel, Fragment fragment) {
        this.context = context;
        this.resource = resource;
        this.showRejected = showRejected;
        this.fileStatusViewModel = viewModel;
        this.parentFragment = fragment;
        this.onListItemClickListener = onListItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final InProcessDocumentsModel dataModel = list.get(position);
        dateFormatter = new DateFormatter();
        if (dataModel.getUploadDate() != null)
            holder.documentDate.setText(dateFormatter.getFormattedDate(dataModel.getUploadDate()));
//            holder.documentDate.setText(dataModel.getUploadDate());


        if (showRejected)
            holder.rejectedIcon.setVisibility(View.VISIBLE);
        else
            holder.rejectedIcon.setVisibility(View.GONE);

        GlideUrl imageURL = URLBuilder.getGlideUrl(list.get(position).getThumbnail());

//        //Async loading
        Glide.with(context)
                .asBitmap()
                .load(imageURL)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(BitmapTransitionOptions.withCrossFade())
                .listener(new RequestListener() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        // Log the GlideException here (locally or with a remote logging framework):
                        Log.e("glide error:", "Load failed", e);

                        // You can also log the individual causes:
                        for (Throwable t : e.getRootCauses()) {
                            Log.e("glide error:", "Caused by", t);
                        }
                        // Or, to log all root causes locally, you can use the built in helper method:
                        e.logRootCauses("glide error:");

                        return false; // Allow calling onLoadFailed on the Target.
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                        // Log successes here or use DataSource to keep track of cache hits and misses.

                        return false; // Allow calling onResourceReady on the Target.
                    }
        })
                .error(Glide
                        .with(context)
                        .asBitmap()
                        .load(imageURL)
                        .placeholder(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transition(BitmapTransitionOptions.withCrossFade()))
                .into(holder.documentImage);

//        Glide.with(context).asBitmap().load(imageURL).centerCrop().fitCenter().into(holder.documentImage);

        holder.documentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListItemClickListener.onListItemClick(position, dataModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateDataList(ArrayList<InProcessDocumentsModel> dataList) {
        if (list != null) {
            list.clear();
            list.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    public interface OnListItemClickListener {
        void onListItemClick(int pos, InProcessDocumentsModel dataModel);
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
