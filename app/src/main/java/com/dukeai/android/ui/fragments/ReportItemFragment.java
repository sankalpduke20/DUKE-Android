package com.dukeai.android.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dukeai.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportItemFragment extends Fragment {
    View reportItemView;
    @BindView(R.id.report_icon)
    ImageView reportIcon;
    private OnFragmentInteractionListener mListener;

    public ReportItemFragment() {
        // Required empty public constructor
    }

    public static ReportItemFragment newInstance() {
        ReportItemFragment fragment = new ReportItemFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reportItemView = inflater.inflate(R.layout.view_report_list_item, container, false);
        ButterKnife.bind(this, reportItemView);
        setCurrentTheme();
        return reportItemView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setCurrentTheme() {
//        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
//        TextView tv = reportItemView.findViewById(R.id.amount_units);
//                tv.setTextColor(Color.parseColor("#000000"));
//        tv = reportItemView.findViewById(R.id.amount);
//        tv.setTextColor(Color.parseColor(changeThemeModel.getFontColor()));
//        reportIcon.setColorFilter(Color.parseColor(changeThemeModel.getFontColor()));
//        reportItemView.findViewById(R.id.download_report).setBackgroundColor(Color.parseColor(changeThemeModel.getFontColor()));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
