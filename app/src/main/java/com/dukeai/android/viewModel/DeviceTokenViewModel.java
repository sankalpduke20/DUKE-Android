package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.DeviceTokenRepository;
import com.dukeai.android.models.DeviceTokenModel;
import com.google.gson.JsonObject;

public class DeviceTokenViewModel extends ViewModel {
    DeviceTokenRepository deviceTokenRepository;
    LiveData<DeviceTokenModel> deviceTokenModelLiveData;
    LiveData<DeviceTokenModel> deleteDeviceTokenModelData;

    public DeviceTokenViewModel() {
        deviceTokenRepository = new DeviceTokenRepository();
    }

    public LiveData<DeviceTokenModel> updateDeviceToken(JsonObject jsonObject) {
        deviceTokenModelLiveData = deviceTokenRepository.updateDeviceToken(jsonObject);
        return deviceTokenModelLiveData;
    }

    public LiveData<DeviceTokenModel> getDeleteDeviceTokenModelData(JsonObject jsonObject) {
        deleteDeviceTokenModelData = deviceTokenRepository.deleteDeviceToken(jsonObject);
        return deleteDeviceTokenModelData;
    }
}
