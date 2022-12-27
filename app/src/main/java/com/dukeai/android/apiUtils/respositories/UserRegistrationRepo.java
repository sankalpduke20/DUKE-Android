package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.ArrayResponseModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRegistrationRepo {
    DukeApi dukeApi;
    UserDataModel userDataModel;
    UserConfig userConfig = UserConfig.getInstance();

    public UserRegistrationRepo() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<ResponseModel> deleteUnconfirmedUser(final String emailId) {
        final MutableLiveData<ResponseModel> data = new MutableLiveData<>();
        Call<ResponseModel> call = dukeApi.deleteUser(emailId);
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

    public LiveData<ResponseModel> updateUserGroup(final String emailId, final JsonObject payLoad) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<ResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseModel> call = dukeApi.updateUserGroup(jwtToken, emailId, payLoad);
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
            }
        });
        return data;
    }

    public LiveData<ArrayResponseModel> forgotPassword(final String emailId) {
        final MutableLiveData<ArrayResponseModel> data = new MutableLiveData<>();
        Call<ArrayResponseModel> call = dukeApi.forgotPassword(emailId);
        call.enqueue(new Callback<ArrayResponseModel>() {
            @Override
            public void onResponse(Call<ArrayResponseModel> call, Response<ArrayResponseModel> response) {
                if (response.body() != null) {
                    data.setValue(response.body());
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        ArrayResponseModel responseModel = new ArrayResponseModel();
                        responseModel.setMessage(jsonObject.getJSONArray("Message"));
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
            public void onFailure(Call<ArrayResponseModel> call, Throwable t) {
                data.setValue(new ArrayResponseModel());
            }
        });
        return data;
    }

}
