package com.dukeai.android.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dukeai.android.R;

public class AssetDeductionTabs extends Fragment {


    int position;
    private TextView textView;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        AssetDeductionTabs tabFragment = new AssetDeductionTabs();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    public static AssetDeductionTabs newInstance() {
        AssetDeductionTabs fragment = new AssetDeductionTabs();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asset_deduction_tabs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        if(position == 0){
//            textView = (TextView) view.findViewById(R.id.textView);
//
//            textView.setText("Fragment " + (position + 1));
//            TractorFragment tractorFragment = new TractorFragment();
//            FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frameLayout,tractorFragment);
//            fragmentTransaction.commit();

        Fragment fragment = new TractorFragment();
        FragmentManager manager = this.getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
//            NavigationFlowManager.openFragments( TractorFragment.newInstance(), null, this.getActivity(), R.id.frameLayout,"");


//        }else if(position == 1){
//            textView = (TextView) view.findViewById(R.id.textView);
//
//            textView.setText("Fragment " + (position + 1));
//            TrailerFrgament trailerFrgament = new TrailerFrgament();
//            FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frameLayout,trailerFrgament);
//            fragmentTransaction.commit();
//            Fragment fragment = new TrailerFrgament();
//            FragmentManager manager = this.getActivity().getSupportFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.replace(R.id.frameLayout,fragment );
//            transaction.commit();
//            NavigationFlowManager.openFragments(TrailerFrgament.newInstance(), null, this.getActivity(), R.id.frameLayout,"");

//        }

    }
}

