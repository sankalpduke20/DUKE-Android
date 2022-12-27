package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.BalanceSheetModel;
import com.dukeai.android.models.BalanceSheetResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BalanceSheetRepository {
    DukeApi dukeApi;
    UserDataModel userDataModel;
    UserConfig userConfig = UserConfig.getInstance();

    public BalanceSheetRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<BalanceSheetModel> getBalanceSheetData() {
        final MutableLiveData<BalanceSheetModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<BalanceSheetModel> call = dukeApi.getBalanceSheetData(jwtToken, userDataModel.getUserEmail(), ApiConstants.IFTA.ACCEPT);
                call.enqueue(new Callback<BalanceSheetModel>() {
                    @Override
                    public void onResponse(Call<BalanceSheetModel> call, final Response<BalanceSheetModel> response) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //code to do the HTTP request
                                if (response.isSuccessful()) {
                                    data.postValue(response.body());
                                } else {
//                            data.setValue(null);
                                    data.postValue(response.body());

                                }
                            }
                        });
                        thread.start();


                    }

                    @Override
                    public void onFailure(Call<BalanceSheetModel> call, Throwable t) {
                        BalanceSheetModel model = new BalanceSheetModel();
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

    public LiveData<BalanceSheetResponseModel> postBalanceSheetData(final JsonObject jsonObject) {
        final MutableLiveData<BalanceSheetResponseModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<BalanceSheetResponseModel> call = dukeApi.postBalanceSheetData(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<BalanceSheetResponseModel>() {
                    @Override
                    public void onResponse(Call<BalanceSheetResponseModel> call, final Response<BalanceSheetResponseModel> response) {

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
                    public void onFailure(Call<BalanceSheetResponseModel> call, Throwable t) {
                        BalanceSheetResponseModel model = new BalanceSheetResponseModel();
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
