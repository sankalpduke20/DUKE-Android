package com.dukeai.android.apiUtils.respositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.models.DeviceTokenModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromoCodeRepository {
    DukeApi dukeApi;

    public PromoCodeRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<DeviceTokenModel> updatePromoCode(String email, JsonObject jsonObject) {
        final MutableLiveData<DeviceTokenModel> data = new MutableLiveData<>();
        Call<DeviceTokenModel> call = dukeApi.updatePromoCode(ApiConstants.PromoCode.PROMO_CODE_CHECK, jsonObject);
        call.enqueue(new Callback<DeviceTokenModel>() {
            @Override
            public void onResponse(Call<DeviceTokenModel> call, Response<DeviceTokenModel> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    DeviceTokenModel model = new DeviceTokenModel();
                    try {
                        model.setMessage(response.errorBody().string());
                        Log.d("PromoCode-37", "onResponse: " + new Gson().toJson(response.errorBody()));
                        data.setValue(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceTokenModel> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;

    }
}
