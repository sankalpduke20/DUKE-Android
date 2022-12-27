package com.dukeai.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.dukeai.android.ui.fragments.TractorFragment;
import com.dukeai.android.ui.fragments.TrailerFrgament;

public class AssetsDeductionAdapter extends FragmentStatePagerAdapter {

    int tabCount;
    private String[] title = {"Tractor", "Trailer"};

    public AssetsDeductionAdapter(FragmentManager manager, int tabCount) {
        super(manager);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TractorFragment tractorFragment = new TractorFragment();
                return tractorFragment;
            case 1:
                TrailerFrgament trailerFrgament = new TrailerFrgament();
                return trailerFrgament;
            default:
                return null;
        }
//        return AssetDeductionTabs.getInstance(position);
    }

    @Override
    public int getCount() {
        return tabCount;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
