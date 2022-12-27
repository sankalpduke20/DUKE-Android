package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.models.AppUpdateStatusModel;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppUpdateRepository {

    DukeApi dukeApi;

    public AppUpdateRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<AppUpdateStatusModel> getAppUpdateStatus(JsonObject headerParams) {
        final MutableLiveData<AppUpdateStatusModel> data = new MutableLiveData<>();
        Call<AppUpdateStatusModel> call = dukeApi.getAppUpdateStatus(headerParams);
        call.enqueue(new Callback<AppUpdateStatusModel>() {
            @Override
            public void onResponse(Call<AppUpdateStatusModel> call, Response<AppUpdateStatusModel> response) {
                if (response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<AppUpdateStatusModel> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }
}
