package com.dukeai.android.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.interfaces.CommonHeaderInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommonHeader extends LinearLayout {

    Context commonHeaderContext;
    View commonHeaderView;
    @BindView(R.id.common_back)
    ImageView headerImage;
    @BindView(R.id.common_header)
    TextView headerTitle;
    CommonHeaderInterface commonHeaderInterface;

    public CommonHeader(Context context) {
        super(context);
        this.commonHeaderContext = context;
        initView();
    }


    public CommonHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.commonHeaderContext = context;
        initView();
    }

    public void setHeader(String headerName) {
        this.headerTitle.setText(headerName);
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) commonHeaderContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        commonHeaderView = inflater.inflate(R.layout.common_header, this);
        ButterKnife.bind(this, commonHeaderView);
    }

    @OnClick(R.id.common_back)
    void onClickBack() {
        if (commonHeaderInterface != null) {
            commonHeaderInterface.onBackClicked(headerImage);
        } else {
            ((Activity) commonHeaderContext).onBackPressed();
        }
    }
}
