package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.models.ResponseModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserVerificationRepository {
    DukeApi dukeApi;

    public UserVerificationRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<ResponseModel> verifyUser(final String emailId) {
        final MutableLiveData<ResponseModel> data = new MutableLiveData<>();
        Call<ResponseModel> call = dukeApi.verifyUser(emailId);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null) {
                    data.setValue(response.body());
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        ResponseModel responseModel = new ResponseModel();
                        responseModel.setMessage(jsonObject.getString("Message"));
                        responseModel.setCode(jsonObject.getString("Code"));
                        data.setValue(responseModel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                data.setValue(new ResponseModel());
            }
        });
        return data;
    }
}
