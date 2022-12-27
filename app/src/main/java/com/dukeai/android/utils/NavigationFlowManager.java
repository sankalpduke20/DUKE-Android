package com.dukeai.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dukeai.android.R;
import com.dukeai.android.ui.activities.DashboardActivity;
import com.dukeai.android.ui.activities.MainActivity;

import java.util.ArrayList;

public class NavigationFlowManager {
    public static void openFragments(Fragment fragment, Bundle bundle, FragmentActivity fragmentActivity, int id) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public static void openFragments(Fragment fragment, Bundle bundle, FragmentActivity fragmentActivity, int id, String tag) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(id, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
//        fragmentManager.executePendingTransactions();
    }

    /*public static void openFragmentsNull(Fragment fragment, Bundle bundle, FragmentActivity fragmentActivity, int id, String tag) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(id, fragment, tag);
        fragmentTransaction.addToBackStack(AppConstants.StringConstants.LOADS);
        fragmentTransaction.commit();
//        fragmentManager.executePendingTransactions();
    }*/


    public static void openFragmentsWithOutBackStack(Fragment fragment, Bundle bundle, FragmentActivity fragmentActivity, int id, String tag) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public static void openMainActivity(Activity context, Boolean toLogin) {
        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.putExtra(AppConstants.LoginConstants.TO_LOGIN, toLogin);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivity);
        context.finish();
    }

    public static void openMainActivity(Activity context, Boolean toLogin, Boolean toVerification) {
        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.putExtra(AppConstants.LoginConstants.TO_LOGIN, toLogin);
        mainActivity.putExtra(AppConstants.LoginConstants.TO_VERIFICATION, toVerification);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivity);
        context.finish();
    }

    public static void openDashboardActivity(Activity context) {
        Intent dashboard = new Intent(context, DashboardActivity.class);
        dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dashboard);
        context.finish();
    }


}
