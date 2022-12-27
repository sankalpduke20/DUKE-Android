package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.UserRegistrationRepo;
import com.dukeai.android.models.ArrayResponseModel;
import com.dukeai.android.models.ResponseModel;
import com.google.gson.JsonObject;

public class UserRegistrationViewModel extends ViewModel {

    LiveData<ResponseModel> responseModelLiveData;
    UserRegistrationRepo userRegistrationRepo;
    LiveData<ArrayResponseModel> arrayResponseModelLiveData;

    public UserRegistrationViewModel() {
        userRegistrationRepo = new UserRegistrationRepo();
    }

    public LiveData<ResponseModel> deleteUnconfirmedUser(String emailId) {
        responseModelLiveData = userRegistrationRepo.deleteUnconfirmedUser(emailId);
        return responseModelLiveData;
    }

    public LiveData<ArrayResponseModel> forgotPasswordUserStatus(String emailId) {
        arrayResponseModelLiveData = userRegistrationRepo.forgotPassword(emailId);
        return arrayResponseModelLiveData;
    }

    public LiveData<ResponseModel> updateUserGroup(String emailId, JsonObject payload) {
        responseModelLiveData = userRegistrationRepo.updateUserGroup(emailId, payload);
        return responseModelLiveData;
    }
}
