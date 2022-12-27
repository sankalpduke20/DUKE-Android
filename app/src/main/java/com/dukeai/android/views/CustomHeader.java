package com.dukeai.android.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.utils.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomHeader extends LinearLayout {
    View headerView;
    Context headerContext;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_image)
    ImageView headerImage;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_back)
    ImageView backButton;
    @BindView(R.id.profile_text)
    TextView profileLabel;
    HeaderActions headerActions;
    @BindView(R.id.header_layout)
    RelativeLayout headerLayout;

    public CustomHeader(Context context) {
        super(context);
        this.headerContext = context;
        initView();
    }

    public CustomHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.headerContext = context;
        initView();
    }

    public CustomHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.headerContext = context;
        initView();
    }

    public void setImageTintColor(int color) {
        backButton.setColorFilter(color);
    }

    public void setHeaderActions(HeaderActions actions) {
        this.headerActions = actions;
    }

    public void showHideProfileImage(int flag) {
        this.headerImage.setVisibility(flag);
        this.profileLabel.setVisibility(flag);
    }

    public void setProfileTextGone() {
        this.profileLabel.setVisibility(GONE);
    }

    public void showHideHeaderTitle(int flag) {
        this.headerTitle.setVisibility(flag);
    }

    public void setHeaderTitle(String title) {
        this.headerTitle.setText(title);
    }

    public void setHeaderImage(Bitmap bm) {
        this.headerImage.setImageBitmap(bm);
    }

    public void setToolbarTitle(String title) {
        this.toolbarTitle.setText(title);
    }

    public void showHideBackButton(int flag) {
        this.backButton.setVisibility(flag);
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) headerContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        headerView = inflater.inflate(R.layout.custom_header, this);
        ButterKnife.bind(this, headerView);
        setCurrentTheme();
        setHeaderImageBitmap();
    }

    private void setHeaderImageBitmap() {
        if (Duke.ProfileImage != null) {
            headerImage.setImageBitmap(Duke.ProfileImage);
        } else {
            headerImage.setImageDrawable(Utilities.getDrawable(Duke.getInstance(), R.drawable.header_profile));
        }
    }

    @OnClick(R.id.header_image)
    public void navigateToProfile() {
        if (headerActions != null) {
            headerActions.onClickProfile();
        }
    }

    @OnClick(R.id.toolbar_back)
    public void onClickBackIcon() {
        if (headerActions != null) {
            headerActions.onBackClicked();
        } else {
            ((Activity) headerContext).onBackPressed();
        }
    }

    @OnClick(R.id.toolbar_title)
    public void onClickToolbarTitle() {
        if (headerActions != null) {
            headerActions.onClickToolbarTitle();
        }
    }

    @OnClick(R.id.header_title)
    public void onClickHeaderTitle() {
        if (headerActions != null) {
            headerActions.onClickHeaderTitle();
        }
    }

    public void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        backButton.setColorFilter(Color.parseColor(changeThemeModel.getHeaderTitleColor()));
        toolbarTitle.setTextColor(Color.parseColor(changeThemeModel.getHeaderTitleColor()));
        headerLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getHeaderBackgroundColor()));
    }

}
