package com.dukeai.android.bottomSheetDialogs;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.ui.activities.AssetDeduction;
import com.dukeai.android.ui.activities.BalanceSheet;
import com.dukeai.android.ui.activities.FederalDeduction;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup

public class TaxInfoSheet extends BottomSheetDialog implements PopupActions {

    View taxInfoview;
    CustomProgressLoader customProgressLoader;
    Activity activity;
    Context context;

    LifecycleOwner lifecycleOwner;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    Boolean isPasswordChanged = false;
    @BindView(R.id.federal_deduction_layout)
    RelativeLayout federal_deduction_layout;
    @BindView(R.id.asset_deduction_layout)
    RelativeLayout asset_deduction_laayout;
    @BindView(R.id.balance_sheet_layout)
    RelativeLayout balance_sheet_layout;
    @BindView(R.id.close_icon)
    ImageView closeIcon;
    @BindView(R.id.federal_deduction_icon)
    ImageView federalDeductionIcon;
    @BindView(R.id.assets_deduction_icon)
    ImageView assetDeductionIcon;
    @BindView(R.id.balance_sheet_icon)
    ImageView balanceSheetIcon;
    @BindView(R.id.tax_info_title)
    TextView taxTitle;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    public TaxInfoSheet(@NonNull Context context, final Activity activity, LifecycleOwner owner) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.lifecycleOwner = owner;
        this.popupActions = this;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setInitials();
        setCurrentThemeIcons();
        RelativeLayout federal_deduction_layout = findViewById(R.id.federal_deduction_layout);
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this.activity));
        federal_deduction_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("button", "federal_deduction");
                mFirebaseAnalytics.logEvent("Tax_Info", params);
                Intent intent = new Intent(activity, FederalDeduction.class);
                activity.startActivity(intent);
                dismissDialog();

            }
        });
        RelativeLayout asset_deduciton_layout = findViewById(R.id.asset_deduction_layout);
        asset_deduciton_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("button", "assets_deduction");
                mFirebaseAnalytics.logEvent("Tax_Info", params);
                Intent intent = new Intent(activity, AssetDeduction.class);
                activity.startActivity(intent);
                dismissDialog();

            }
        });
        RelativeLayout balance_sheet = findViewById(R.id.balance_sheet_layout);
        balance_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("button", "balance_sheet");
                mFirebaseAnalytics.logEvent("Tax_Info", params);
                Intent intent = new Intent(activity, BalanceSheet.class);
                activity.startActivity(intent);
                dismissDialog();
            }
        });
    }

    private void setInitials() {
        taxInfoview = getLayoutInflater().inflate(R.layout.tax_info_pop_up, null);
        setContentView(taxInfoview);
        customProgressLoader = new CustomProgressLoader(context);
        ButterKnife.bind(this, taxInfoview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(60, 0, 0, 0)));
    }


    @OnClick(R.id.close_icon)
    void onClickCloseIcon() {
        this.dismiss();
    }


    @OnClick(R.id.asset_deduction_layout)
    void OnClickAssetDeduction() {

    }


    public void showDialog() {
        this.show();
    }

    public void dismissDialog() {
        this.dismiss();
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        confirmationComponent.dismiss();
    }

    private void setCurrentThemeIcons() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();

        federalDeductionIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        assetDeductionIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        balanceSheetIcon.setColorFilter(Color.parseColor(changeThemeModel.getInputFieldIconColor()));
        closeIcon.setColorFilter(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
        taxTitle.setTextColor(Color.parseColor(changeThemeModel.getPopupHeadingTextColor()));
    }
}
