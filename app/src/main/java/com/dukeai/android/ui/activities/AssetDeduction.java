package com.dukeai.android.ui.activities;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dukeai.android.R;
import com.dukeai.android.adapters.AssetsDeductionAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.ui.fragments.TractorFragment;
import com.dukeai.android.ui.fragments.TrailerFrgament;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.viewModel.AssetDeductionViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

// Firebase: Setup

public class AssetDeduction extends BaseActivity implements TractorFragment.OnFragmentInteractionListener, TrailerFrgament.OnFragmentInteractionListener, HeaderActions {
    Uri link;
    @BindView(R.id.asset_deduction_header)
    CustomHeader customHeader;
    int currentPosition;
    AssetDeductionViewModel assetDeductionViewModel = new AssetDeductionViewModel();
    CustomProgressLoader customProgressLoader;
    @BindView(R.id.parent_layout)
    RelativeLayout parentLayout;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_deduction);
        ButterKnife.bind(this);
        customProgressLoader = new CustomProgressLoader(this);

        setCustomHeader();
        setCurrentTheme();
        assetDeductionViewModel = ViewModelProviders.of(this).get(AssetDeductionViewModel.class);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Tractor"));

        tabLayout.addTab(tabLayout.newTab().setText("Trailer"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = findViewById(R.id.viewpager);
        AssetsDeductionAdapter assetsDeductionAdapter = new AssetsDeductionAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(assetsDeductionAdapter);

        tabLayout.setupWithViewPager(viewPager);
//        for(int i=0;i<tabLayout.getTabCount();i++) {
//            TextView tv = (TextView)tabLayout.getTabAt(i).getCustomView();
//            Log.i("text1",tv.getText().toString());
//        }
        final ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        tabLayout.setTabTextColors(Color.parseColor(changeThemeModel.getTabTextColor()), Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                currentPosition = tab.getPosition();
//                setCurrentThemeTabHighlightedTextColor(tab);
                tabLayout.setTabTextColors(Color.parseColor(changeThemeModel.getTabTextColor()), Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
                tabLayout.setSelectedTabIndicatorColor(Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                setCurrentThemeTabTextColor(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        link = uri;
    }

    public void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.asset_deductions));
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
//        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
        TextView title = customHeader.findViewById(R.id.toolbar_title);
//        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        finish();
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    public void addAsset(View view) {
        if (currentPosition == 0) {
//            Bundle params = new Bundle();
//            params.putString("Add", "truck");
//            mFirebaseAnalytics.logEvent("Tax_Info", params);

            Intent intent = new Intent(this, AddTractor.class);
            intent.putExtra("assetType", "tractor");
            startActivity(intent);


        } else if (currentPosition == 1) {
//            Bundle params = new Bundle();
//            params.putString("Add", "trailer");
//            mFirebaseAnalytics.logEvent("Tax_Info", params);

            Intent intent = new Intent(this, AddTractor.class);
            intent.putExtra("assetType", "trailer");
            startActivity(intent);

        }
    }

    //    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }
    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        parentLayout.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }
//private void setCurrentThemeTabTextColor(TabLayout.Tab tab) {
//        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//        TextView tabView = (TextView)tab.getCustomView();
//        tabView.setTextColor(Color.parseColor(changeThemeModel.getHighlightedTabTextColor()));
//}
//private void setCurrentThemeTabHighlightedTextColor(TabLayout.Tab tab) {
//    ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//    TextView tabView = (TextView)tab.getCustomView();
//    tabView.setTextColor(Color.parseColor(changeThemeModel.getTabTextColor()));
//}
}
