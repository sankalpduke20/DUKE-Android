package com.dukeai.android.bottomSheetDialogs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.UploadSelectionListener;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadSelectionBottomSheet extends BottomSheetDialog {

    View view;
    UploadSelectionListener uploadSelectionListener;

    @BindView(R.id.one_as_one_icon)
    ImageView oneAsOneIcon;
    @BindView(R.id.all_as_one_icon)
    ImageView allAsOneIcon;
    @BindView(R.id.manual_configuration_icon)
    ImageView manualConfigIcon;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.close)
    ImageView close;


    public UploadSelectionBottomSheet(@NonNull Context context, UploadSelectionListener listener) {
        super(context);
        view = getLayoutInflater().inflate(R.layout.layout_upload_selection_bottom_sheet, null);
        setContentView(view);
        ButterKnife.bind(this, view);
        this.uploadSelectionListener = listener;
        setCurrentTheme();
        showDialog();
    }

    public void showDialog() {
        this.show();
    }

    public void hideDialog() {
        this.dismiss();
    }

    @OnClick(R.id.one_as_one)
    void onClickOneAsOne() {
        if (uploadSelectionListener != null) {
            uploadSelectionListener.onUploadSelection(AppConstants.UploadSelectionConstants.ONE_AS_ONE);
        }
    }

    @OnClick(R.id.all_as_one)
    void onClickAllAsOne() {
        if (uploadSelectionListener != null) {
            uploadSelectionListener.onUploadSelection(AppConstants.UploadSelectionConstants.ALL_AS_ONE);
        }
    }

    @OnClick(R.id.manual_configuration)
    void onClickManualConfiguration() {
        if (uploadSelectionListener != null) {
            uploadSelectionListener.onUploadSelection(AppConstants.UploadSelectionConstants.MANUAL);
        }
    }

    @OnClick(R.id.close)
    void onClickClose() {
        hideDialog();
    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        oneAsOneIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        allAsOneIcon.setColorFilter((Color.parseColor(changeThemeModel.getInputFieldIconColor())));
        manualConfigIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        close.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getPopupHeadingTextColor())));
        title.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
    }
}
