package com.dukeai.android.apiUtils.respositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.AssetDeductionModel;
import com.dukeai.android.models.AssetDeductionResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetDeductionRepository {
    DukeApi dukeApi;
    UserDataModel userDataModel;
    UserConfig userConfig = UserConfig.getInstance();

    public AssetDeductionRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<AssetDeductionModel> getAssetDeductionData() {
        final MutableLiveData<AssetDeductionModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();

        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<AssetDeductionModel> call = dukeApi.getAssetDeductionData(jwtToken, userDataModel.getUserEmail(), ApiConstants.IFTA.ACCEPT);

                call.enqueue(new Callback<AssetDeductionModel>() {
                    @Override
                    public void onResponse(Call<AssetDeductionModel> call, final Response<AssetDeductionModel> response) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //code to do the HTTP request
                                if (response.isSuccessful()) {
                                    data.postValue(response.body());
                                    Log.e("Assetdeduction Josn ", new Gson().toJson(response.body()));
                                } else {
                                    data.postValue(response.body());
                                }
                            }
                        });
                        thread.start();


                    }

                    @Override
                    public void onFailure(Call<AssetDeductionModel> call, Throwable t) {
                        AssetDeductionModel model = new AssetDeductionModel();
                        try {
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

    public LiveData<AssetDeductionResponseModel> postAssetDeductionData(final JsonObject jsonObject) {
        final MutableLiveData<AssetDeductionResponseModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<AssetDeductionResponseModel> call = dukeApi.postAssetDeductionData(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<AssetDeductionResponseModel>() {
                    @Override
                    public void onResponse(Call<AssetDeductionResponseModel> call, final Response<AssetDeductionResponseModel> response) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //code to do the HTTP request
                                if (response.isSuccessful()) {
                                    data.postValue(response.body());
                                } else {
                                    data.setValue(null);
                                }
                            }
                        });
                        thread.start();


                    }

                    @Override
                    public void onFailure(Call<AssetDeductionResponseModel> call, Throwable t) {
                        AssetDeductionResponseModel model = new AssetDeductionResponseModel();
                        try {
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
