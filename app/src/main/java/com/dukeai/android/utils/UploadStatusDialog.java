package com.dukeai.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.UploadStatusClickActions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadStatusDialog extends Dialog {

    /**
     * Interface for passing the click action
     */
    UploadStatusClickActions uploadStatusClickActions;
    /**
     * This is the Dialog Id which could be used to identify the action coming from it
     */
    int id;
    /**
     * Dialog type distinguishes the type of Content and message to be shown
     **/
    int type;
    @BindView(R.id.upload_status_alert_imageView)
    ImageView imageView;
    @BindView(R.id.upload_status_alert_title)
    TextView titleTextView;
    @BindView(R.id.upload_status_alert_description)
    TextView descriptionTextView;
    @BindView(R.id.upload_status_alert_button)
    Button buttonView;

    /**
     * Public Constructor for instantiating a dialog
     **/
    public UploadStatusDialog(Context context, int dialogId, int type, String message, UploadStatusClickActions actions) {
        super(context);
        this.id = dialogId;
        this.type = type;
        this.uploadStatusClickActions = actions;
        View view = View.inflate(getContext(), R.layout.alert_view_document_upload_status, null);
        setContentView(view);
        ButterKnife.bind(this, view);
        switch (type) {
            case DialogType.UPLOAD_SUCCESS:
                //This case is not required
                break;
            case DialogType.UPLOAD_ERROR:
                //Initialize fields
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_upload_failure));
                titleTextView.setText(R.string.oh_snap_capitals);
                descriptionTextView.setText(message);
                buttonView.setText(R.string.re_upload);
                break;
            case DialogType.NETWORK_ERROR:
                //Initialize fields
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_upload_failure));
                titleTextView.setText("Error!");
                descriptionTextView.setText(message);
                buttonView.setText(R.string.okay);
                break;
        }
        this.setCancelable(false);
        if (!this.isShowing()) {
            this.show();
        }
//        showPopup();
    }

    private void showPopup() {
        if (!this.isShowing()) {
            this.show();
        }
    }

    @OnClick(R.id.upload_status_alert_button)
    void onButtonClick() {
        uploadStatusClickActions.onButtonCick(id, type);
    }

    /**
     * Dialog Types
     **/
    public static class DialogType {
        public static final int UPLOAD_SUCCESS = 0;
        public static final int UPLOAD_ERROR = 1;
        public static final int NETWORK_ERROR = 2;
    }
}
