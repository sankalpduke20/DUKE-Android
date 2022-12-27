package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiUtils;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceTokenRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;


    public DeviceTokenRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<DeviceTokenModel> updateDeviceToken(final JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DeviceTokenModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<DeviceTokenModel> call = dukeApi.updateDeviceToken(jwtToken, userConfig.getUserDataModel().getUserEmail(), jsonObject);
                call.enqueue(new Callback<DeviceTokenModel>() {
                    @Override
                    public void onResponse(Call<DeviceTokenModel> call, Response<DeviceTokenModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<DeviceTokenModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<DeviceTokenModel> deleteDeviceToken(final JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DeviceTokenModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<DeviceTokenModel> call = dukeApi.deleteDeviceToken(jwtToken, userConfig.getUserDataModel().getUserEmail(), jsonObject);
                call.enqueue(new Callback<DeviceTokenModel>() {
                    @Override
                    public void onResponse(Call<DeviceTokenModel> call, Response<DeviceTokenModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<DeviceTokenModel> call, Throwable t) {
                        DeviceTokenModel model = new DeviceTokenModel();
                        try {
                            model.setMessage(ApiUtils.getFailureErrorString(t));
                            data.setValue(model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

}
