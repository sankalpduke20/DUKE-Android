package com.dukeai.android.utils;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.PopupActions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertDialogWithVerticalOptions extends Dialog {

    PopupActions popupActions;
    @BindView(R.id.confirmation_title)
    TextView confirmationTitle;
    @BindView(R.id.confirmation_message)
    TextView confirmationMessage;
    @BindView(R.id.opt1_button)
    Button opt1Button;
    @BindView(R.id.opt2_button)
    Button opt2Button;

    int dialogId;

    public AlertDialogWithVerticalOptions(@NonNull Context context, String title, String message, String opt1ButtonText, String opt2ButtonText, Boolean opt2Visibility, PopupActions clickActions, int dialogId) {
        super(context);
        View view = View.inflate(context, R.layout.layout_alert_dialog_vertical_buttons, null);
        setContentView(view);
        ButterKnife.bind(this, view);
        setConfirmationMessage(message);
        setConfirmationTitle(title);
        setOpt1Button(opt1ButtonText);
        setOpt2Button(opt2ButtonText, opt2Visibility);
        setCancelable(false);
        popupActions = clickActions;
        this.dialogId = dialogId;
        show();
    }

    public void setConfirmationTitle(String confirmationTitle) {
        this.confirmationTitle.setText(confirmationTitle);
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage.setText(confirmationMessage);
    }

    public void setOpt1Button(String opt1ButtonText) {
        this.opt1Button.setText(opt1ButtonText);
    }

    public void setOpt2Button(String opt2ButtonText, boolean shouldShow) {
        if (shouldShow)
            this.opt2Button.setText(opt2ButtonText);
        else
            opt2Button.setVisibility(View.GONE);
    }

    @OnClick(R.id.opt1_button)
    void onOpt1Click() {
        popupActions.onPopupActions(opt1Button.getText().toString(), dialogId);
    }

    @OnClick(R.id.opt2_button)
    void onOpt2Click() {
        popupActions.onPopupActions(opt2Button.getText().toString(), dialogId);
    }
}
