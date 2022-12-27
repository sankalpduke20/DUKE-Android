package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.TripIdModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IFTARepository {

    DukeApi dukeApi;
    UserDataModel userDataModel;
    UserConfig userConfig = UserConfig.getInstance();

    public IFTARepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<TripIdModel> getTripId() {
        final MutableLiveData<TripIdModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<TripIdModel> call = dukeApi.getTripId(jwtToken, userDataModel.getUserEmail(), ApiConstants.IFTA.ACCEPT);
                call.enqueue(new Callback<TripIdModel>() {
                    @Override
                    public void onResponse(Call<TripIdModel> call, Response<TripIdModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            TripIdModel tripIdModel = new TripIdModel();
                            try {
                                tripIdModel.setErrorMessage(response.errorBody().string());
                                data.setValue(tripIdModel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TripIdModel> call, Throwable t) {
                        data.setValue(new TripIdModel(t.getMessage()));
                    }
                });
            }
        });
        return data;
    }


    public LiveData<TripIdModel> getLastTripStatus() {
        final MutableLiveData<TripIdModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<TripIdModel> call = dukeApi.getLastTripStatus(jwtToken, userDataModel.getUserEmail(), ApiConstants.IFTA.ACCEPT);
                call.enqueue(new Callback<TripIdModel>() {
                    @Override
                    public void onResponse(Call<TripIdModel> call, Response<TripIdModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            TripIdModel tripIdModel = new TripIdModel();
                            try {
                                tripIdModel.setErrorMessage(response.errorBody().string());
                                data.setValue(tripIdModel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TripIdModel> call, Throwable t) {
                        data.setValue(new TripIdModel(t.getMessage()));
                    }
                });
            }
        });
        return data;
    }

    public LiveData<ErrorModel> sendTripData(JsonArray locations, String tripId, String status) {
        final MutableLiveData<ErrorModel> data = new MutableLiveData<>();
        userDataModel = userConfig.getUserDataModel();

        final JsonObject requestBody = new JsonObject();
        if (locations == null) {
            requestBody.add("positionList", new JsonArray());
        } else {
            requestBody.add("positionList", locations);
        }
        requestBody.addProperty("tripId", tripId);
        requestBody.addProperty("status", status);

        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ErrorModel> call = dukeApi.sendTripData(jwtToken, userDataModel.getUserEmail(), requestBody);
                call.enqueue(new Callback<ErrorModel>() {
                    @Override
                    public void onResponse(Call<ErrorModel> call, Response<ErrorModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            TripIdModel tripIdModel = new TripIdModel();
                            try {
                                tripIdModel.setErrorMessage(response.errorBody().string());
                                data.setValue(tripIdModel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ErrorModel> call, Throwable t) {
                        data.setValue(new TripIdModel(t.getMessage()));
                    }
                });
            }
        });
        return data;
    }
}
