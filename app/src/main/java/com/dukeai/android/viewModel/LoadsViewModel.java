package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.LoadsRepository;
import com.dukeai.android.models.CreateLoadModel;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.LoadDocumentModel;
import com.dukeai.android.models.LoadsListModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.RecipientsListModel;
import com.dukeai.android.models.SingleLoadModel;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class LoadsViewModel extends ViewModel {
    LoadsRepository loadsRepository;
    LiveData<CreateLoadModel> createLoadModelLiveData;
    LiveData<RecipientsListModel> recipientsListModelLiveData;
    LiveData<GenericResponseModel> updateRecipientsLiveData;
    LiveData<LoadsListModel> loadsListModelLiveData;
    LiveData<LoadsTransmitModel> transmitLoadsModelLiveData;
    LiveData<LoadsTransmitModel> transmitProcessedDocsLiveData;
    LiveData<SingleLoadModel> getLoadDetailLiveData;
    LiveData<GenericResponseModel> deleteLoadObjectLiveData;
    LiveData<GenericResponseModel> deleteFromLoadLiveData;

    public LoadsViewModel() {
        loadsRepository = new LoadsRepository();
    }

    public LiveData<CreateLoadModel> createLoad() {
        createLoadModelLiveData = loadsRepository.createLoad();
        return createLoadModelLiveData;
    }

    public LiveData<LoadsListModel> getLoadsListLiveData(JsonObject jsonObject) {
        loadsListModelLiveData = loadsRepository.getUserLoads(jsonObject);
        return loadsListModelLiveData;
    }

    public LiveData<RecipientsListModel> getRecipientsList(String flag) {
        recipientsListModelLiveData = loadsRepository.getRecipientsList(flag);
        return recipientsListModelLiveData;
    }

    public LiveData<GenericResponseModel> getUpdateRecipientsLiveData(JsonObject jsonObject) {
        updateRecipientsLiveData = loadsRepository.updateRecipient(jsonObject);
        return updateRecipientsLiveData;
    }

    public LiveData<LoadsTransmitModel> getTransmitLoadsModelLiveData(ArrayList<String> loadUUIDs, ArrayList<String> recipientsList) {
        transmitLoadsModelLiveData = loadsRepository.transmitLoads(loadUUIDs, recipientsList);
        return transmitLoadsModelLiveData;
    }

    public LiveData<LoadsTransmitModel> getTransmitProcessedDocsLiveData(ArrayList<String> docSHA1s) {
        transmitProcessedDocsLiveData = loadsRepository.transmitProcessedDocs(docSHA1s);
        return transmitProcessedDocsLiveData;
    }

    public LiveData<SingleLoadModel> getGetLoadDetailLiveData(String loadUUID) {
        getLoadDetailLiveData = loadsRepository.getLoadDetail(loadUUID);
        return getLoadDetailLiveData;
    }

    public LiveData<GenericResponseModel> getDeleteLoadObjectLiveData(String loadUUID) {
        deleteLoadObjectLiveData = loadsRepository.deleteLoadObject(loadUUID);
        return deleteLoadObjectLiveData;
    }

    public LiveData<GenericResponseModel> getDeleteFromLoadLiveData(String loadUUID, String sha1) {
        deleteFromLoadLiveData = loadsRepository.deleteFromLoad(loadUUID, sha1);
        return deleteFromLoadLiveData;
    }
}
