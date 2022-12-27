package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.UserVerificationRepository;
import com.dukeai.android.models.ResponseModel;

public class VerifyUserViewModel extends ViewModel {
    LiveData<ResponseModel> responseModelLiveData;
    UserVerificationRepository userVerificationRepository;

    public VerifyUserViewModel() {
        userVerificationRepository = new UserVerificationRepository();
    }

    public LiveData<ResponseModel> verifyUser(String email) {
        responseModelLiveData = userVerificationRepository.verifyUser(email);
        return responseModelLiveData;
    }
}
