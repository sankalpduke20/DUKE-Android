package com.dukeai.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.UpdateDocumentNavigationIcons;
import com.dukeai.android.models.DownloadImageModel;
import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;

public class ProcessedDocumentsFlipperAdapter extends ArrayAdapter {
    ArrayList<DownloadImageModel> list = new ArrayList<>();
    Context context;
    int resource;
    UpdateDocumentNavigationIcons updateDocumentNavigationIcons;

    public ProcessedDocumentsFlipperAdapter(@NonNull Context context, int resource, ArrayList<DownloadImageModel> list, UpdateDocumentNavigationIcons navigationIcons) {
        super(context, resource);
        this.list = list;
        this.context = context;
        this.resource = resource;
        updateDocumentNavigationIcons = navigationIcons;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(resource, parent, false);
        }
        final DownloadImageModel dataModel = list.get(position);
        TouchImageView imageView = view.findViewById(R.id.flipper_document_image);
        imageView.setImageBitmap(dataModel.getBitmap());
        if (updateDocumentNavigationIcons != null) {
            updateDocumentNavigationIcons.UpdateDocumentNavigation(position);
        }
        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

}
