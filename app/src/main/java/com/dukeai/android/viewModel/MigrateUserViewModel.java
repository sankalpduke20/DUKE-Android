package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.MigrateUserRepository;
import com.dukeai.android.models.DeviceTokenModel;

public class MigrateUserViewModel extends ViewModel {
    MigrateUserRepository migrateUserRepository;
    LiveData<DeviceTokenModel> deviceTokenModelLiveData;

    public MigrateUserViewModel() {
        migrateUserRepository = new MigrateUserRepository();
    }

    public LiveData<DeviceTokenModel> migrateUserToCognito() {
        deviceTokenModelLiveData = migrateUserRepository.migrateUser();
        return deviceTokenModelLiveData;
    }
}
