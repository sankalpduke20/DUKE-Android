package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.FederalDeductionModel;
import com.dukeai.android.models.FederalDeductionResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaxInfoRepository {
    DukeApi dukeApi;
    UserDataModel userDataModel;
    UserConfig userConfig = UserConfig.getInstance();

    public TaxInfoRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<FederalDeductionModel> getFederalDeductionData() {
        final MutableLiveData<FederalDeductionModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FederalDeductionModel> call = dukeApi.getFederalDeductionData(jwtToken, userDataModel.getUserEmail(), ApiConstants.IFTA.ACCEPT);
                call.enqueue(new Callback<FederalDeductionModel>() {
                    @Override
                    public void onResponse(Call<FederalDeductionModel> call, final Response<FederalDeductionModel> response) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //code to do the HTTP request
                                if (response.isSuccessful()) {
                                    data.postValue(response.body());
                                } else {
                                    data.postValue(response.body());

                                }
                            }
                        });
                        thread.start();


                    }

                    @Override
                    public void onFailure(Call<FederalDeductionModel> call, Throwable t) {
                        FederalDeductionModel model = new FederalDeductionModel();
                        try {
//                    model.setMessage(ApiUtils.getFailureErrorString(t));
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

    public LiveData<FederalDeductionResponseModel> postFederalDeductionData(final JsonObject jsonObject) {
        final MutableLiveData<FederalDeductionResponseModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FederalDeductionResponseModel> call = dukeApi.postFederalDeductionData(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<FederalDeductionResponseModel>() {
                    @Override
                    public void onResponse(Call<FederalDeductionResponseModel> call, final Response<FederalDeductionResponseModel> response) {

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
                    public void onFailure(Call<FederalDeductionResponseModel> call, Throwable t) {
                        FederalDeductionResponseModel model = new FederalDeductionResponseModel();
                        try {
//                    model.setMessage(ApiUtils.getFailureErrorString(t));
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
