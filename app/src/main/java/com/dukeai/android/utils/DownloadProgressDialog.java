package com.dukeai.android.utils;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadProgressDialog extends Dialog {

    @BindView(R.id.dialog_title)
    TextView titleTextView;
    @BindView(R.id.dialog_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.dialog_progress_text)
    TextView progressTextView;
    @BindView(R.id.dialog_description)
    TextView descriptionTextView;

    Context context;
    String title;
    String description;
    int progress;

    public DownloadProgressDialog(@NonNull Context context, String title, String message, String progressMsg) {
        super(context);
        View view = View.inflate(context, R.layout.layout_progress_dialog, null);
        this.context = context;
        this.title = title;
        this.description = message;
        this.progress = 0;
        setContentView(view);
        ButterKnife.bind(this, view);
        /**Setting Initial Text to progressTextView**/
        this.setCanceledOnTouchOutside(false);
        this.progressTextView.setText(progressMsg);
        initUI();
    }

    private void initUI() {
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        progressBar.setProgress(progress);
    }

    public void showDialog() {
        if (!this.isShowing()) {
            this.show();
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        progressTextView.setText(progress + context.getString(R.string.percentage_symbol));
        progressBar.setProgress(progress);
    }

    @Override
    public void onBackPressed() {
        Toast toast = Toast.makeText(getContext(),
                "Please wait while the POD document gets transmitted!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 180);
        toast.show();
    }
}
