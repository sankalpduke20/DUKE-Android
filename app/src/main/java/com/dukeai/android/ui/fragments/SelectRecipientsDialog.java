package com.dukeai.android.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.SelectRecipientAdapter;
import com.dukeai.android.models.RecipientDataModel;
import com.dukeai.android.models.SelectRecipientDataModel;
import com.google.gson.Gson;


import java.util.ArrayList;

public class SelectRecipientsDialog extends DialogFragment implements SelectRecipientAdapter.OnListItemClickListener {

    TextView clearAll;
    TextView cancel;
    Button selectBtn;
    RecyclerView globalRecipientsRecyclerView;
    RecyclerView recipientsAddedByYouRecyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    TextView noGlobalRecipients;
    TextView noCustomRecipients;
    SelectRecipientDataModel selectRecipientDataModel;
    ArrayList<SelectRecipientDataModel> selectRecipientDataForGlobalList = new ArrayList<>();
    ArrayList<SelectRecipientDataModel> selectRecipientDataCustomList = new ArrayList<>();
    boolean isCanceled = false;
    ArrayList<String> alreadySelectedRecipients = new ArrayList<>();
//    private OnDismissListener onDialogDismiss;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_select_recipient_dialog, null);
        builder.setView(view);

        globalRecipientsRecyclerView = view.findViewById(R.id.available_to_all_recyclerView);
        recipientsAddedByYouRecyclerView = view.findViewById(R.id.added_by_you_recyclerView);
        noGlobalRecipients = view.findViewById(R.id.no_global_recipients);
        noCustomRecipients = view.findViewById(R.id.no_custom_recipients);
        cancel = view.findViewById(R.id.cancel_btn);
        clearAll = view.findViewById(R.id.clear_all_btn);
        selectBtn = view.findViewById(R.id.ok_btn);

        if(Duke.selectedRecipients.size()>0) {
            alreadySelectedRecipients.addAll(Duke.selectedRecipients);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCanceled = true;
                if(alreadySelectedRecipients.size() != Duke.selectedRecipients.size() && Duke.selectedRecipients.size()>alreadySelectedRecipients.size()) {
                    ArrayList<String> latestSelectedItems = new ArrayList<>();
                    latestSelectedItems.addAll(Duke.selectedRecipients);
                    for(String recipient: latestSelectedItems) {
                        if(!alreadySelectedRecipients.contains(recipient)) {
                            Duke.selectedRecipients.remove(recipient);
                        }
                    }
                }
                selectRecipientDataForGlobalList.clear();
                selectRecipientDataCustomList.clear();
                dismiss();
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRecipientDataForGlobalList.clear();
                selectRecipientDataCustomList.clear();
                Duke.selectedRecipients.clear();
                setGlobalRecipientLists();
                setCustomRecipientLists();
                Log.d("Selected recipients:", Duke.selectedRecipients.toArray().toString());
            }
        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Selected recipients:", Duke.selectedRecipients.toArray().toString());
                Log.d("Selected recipients:", Duke.selectedRecipients.toArray().toString());
                dismiss();
            }
        });
//        if(Duke.selectRecipientDataForGlobalList.size()==0 || Duke.selectRecipientDataCustomList.size()==0 ){
            selectRecipientDataForGlobalList.clear();
            selectRecipientDataCustomList.clear();
            setGlobalRecipientLists();
            setCustomRecipientLists();

      /*  }else{
            setRecipientLists(globalRecipientsRecyclerView, Duke.selectRecipientDataForGlobalList);
            setRecipientLists(recipientsAddedByYouRecyclerView, Duke.selectRecipientDataCustomList);
        }*/


        return builder.create();
    }

    private void setGlobalRecipientLists() {
        for (RecipientDataModel recipientDataModel: Duke.globalRecipientsList) {
            if(Duke.selectedRecipients.size() > 0 && Duke.selectedRecipients.contains(recipientDataModel.getEmail())) {
                selectRecipientDataForGlobalList.add(new SelectRecipientDataModel(true, recipientDataModel.getEmail()));
            } else {
                selectRecipientDataForGlobalList.add(new SelectRecipientDataModel(false, recipientDataModel.getEmail()));
            }
        }
        if(selectRecipientDataForGlobalList.size()>0) {
            globalRecipientsRecyclerView.setVisibility(View.VISIBLE);
            noGlobalRecipients.setVisibility(View.INVISIBLE);
            Duke.selectRecipientDataForGlobalList = selectRecipientDataForGlobalList;
            setRecipientLists(globalRecipientsRecyclerView, selectRecipientDataForGlobalList);
        } else {
            globalRecipientsRecyclerView.setVisibility(View.INVISIBLE);
            noGlobalRecipients.setVisibility(View.VISIBLE);
        }
    }

    private void setCustomRecipientLists() {
//        selectRecipientDataModels.clear();
        for (RecipientDataModel recipientDataModel: Duke.customRecipientsList) {
            if(Duke.selectedRecipients.size() > 0 && Duke.selectedRecipients.contains(recipientDataModel.getEmail())) {
                selectRecipientDataCustomList.add(new SelectRecipientDataModel(true, recipientDataModel.getEmail()));
            } else {
                selectRecipientDataCustomList.add(new SelectRecipientDataModel(false, recipientDataModel.getEmail()));
            }
        }
        if(selectRecipientDataCustomList.size()>0) {
            recipientsAddedByYouRecyclerView.setVisibility(View.VISIBLE);
            noCustomRecipients.setVisibility(View.INVISIBLE);
            Duke.selectRecipientDataCustomList = selectRecipientDataCustomList;
            setRecipientLists(recipientsAddedByYouRecyclerView, selectRecipientDataCustomList);
        } else {
            recipientsAddedByYouRecyclerView.setVisibility(View.INVISIBLE);
            noCustomRecipients.setVisibility(View.VISIBLE);
        }
    }

    private void setRecipientLists(RecyclerView recyclerView, ArrayList<SelectRecipientDataModel> selectRecipientDataModelArrayList) {
        layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        adapter = new SelectRecipientAdapter(getActivity(), selectRecipientDataModelArrayList, SelectRecipientsDialog.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onListItemClick(int pos, SelectRecipientDataModel dataModel) {
        System.out.println("Selected Recipient "+new Gson().toJson(dataModel));
        if(dataModel.isSelected) {
            if(!Duke.selectedRecipients.contains(dataModel.getRecipientEmail())){
                Duke.selectedRecipients.add(dataModel.getRecipientEmail());
            }
        } else {
            if(Duke.selectedRecipients.contains(dataModel.getRecipientEmail())){
                Duke.selectedRecipients.remove(dataModel.getRecipientEmail());
            }
        }
    }

    interface DismissListener {
        void onDismiss(boolean hasDate);
    }

    private DismissListener listener = null;

    public void setDismissListener(DismissListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onDismiss(isCanceled);
        }
    }
}
