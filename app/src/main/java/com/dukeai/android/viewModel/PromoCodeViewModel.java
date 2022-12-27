package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.PromoCodeRepository;
import com.dukeai.android.models.DeviceTokenModel;
import com.google.gson.JsonObject;

public class PromoCodeViewModel extends ViewModel {
    PromoCodeRepository promoCodeRepository;
    LiveData<DeviceTokenModel> deviceTokenModelLiveData;

    public PromoCodeViewModel() {
        promoCodeRepository = new PromoCodeRepository();
    }

    public LiveData<DeviceTokenModel> updateReferralId(String email, JsonObject jsonObject) {
        deviceTokenModelLiveData = promoCodeRepository.updatePromoCode(email, jsonObject);
        return deviceTokenModelLiveData;
    }
}
