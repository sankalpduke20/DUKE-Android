package com.dukeai.android.apiUtils.respositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.CreateLoadModel;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.LoadDocumentModel;
import com.dukeai.android.models.LoadsListModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.RecipientsListModel;
import com.dukeai.android.models.SingleLoadModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

public class LoadsRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;
    Response<LoadsTransmitModel> responseReceived = null;

    public LoadsRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<CreateLoadModel> createLoad() {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<CreateLoadModel> data = new MutableLiveData<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("app_version", "2.3.7");
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<CreateLoadModel> call = dukeApi.createLoad(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<CreateLoadModel>() {
                    @Override
                    public void onResponse(Call<CreateLoadModel> call, Response<CreateLoadModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            CreateLoadModel model = new CreateLoadModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateLoadModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<RecipientsListModel> getRecipientsList(String flag) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<RecipientsListModel> data = new MutableLiveData<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("app_version", "2.3.7");
        jsonObject.addProperty("flag", flag);
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<RecipientsListModel> call = dukeApi.getRecipientsList(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<RecipientsListModel>() {
                    @Override
                    public void onResponse(Call<RecipientsListModel> call, Response<RecipientsListModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            RecipientsListModel model = new RecipientsListModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipientsListModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> addRecipient(JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.addRecipient(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> updateRecipient(JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.updateRecipientDetail(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> deleteRecipient(JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.deleteRecipient(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<LoadsListModel> getUserLoads(JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<LoadsListModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<LoadsListModel> call = dukeApi.getUserLoads(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<LoadsListModel>() {
                    @Override
                    public void onResponse(Call<LoadsListModel> call, Response<LoadsListModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            LoadsListModel model = new LoadsListModel("", "", "");
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoadsListModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> deleteFromLoad(String loadUUID, String sha1) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("app_version", "2.3.7");
        jsonObject.addProperty("doc_sha", sha1);
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.deleteFromLoad(jwtToken, userDataModel.getUserEmail(), loadUUID, jsonObject);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> addToLoad(String loadUUID) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.addToLoad(jwtToken, userDataModel.getUserEmail(), loadUUID);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<SingleLoadModel> getLoadDetail(String loadUUID) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<SingleLoadModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<SingleLoadModel> call = dukeApi.getLoadDetail(jwtToken, userDataModel.getUserEmail(), loadUUID);

                call.enqueue(new Callback<SingleLoadModel>() {
                    @Override
                    public void onResponse(Call<SingleLoadModel> call, Response<SingleLoadModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            SingleLoadModel model = new SingleLoadModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleLoadModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<LoadsTransmitModel> transmitLoads(ArrayList<String> loadUUIDs, ArrayList<String> recipientList) {
        userDataModel = userConfig.getUserDataModel();
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("app_version", "2.3.7");
//        jsonObject.addProperty("recipient", recipientList);

        Map<String, Object> jsonParams = new HashMap<>();
        jsonParams.put("app_version", "2.3.7");
        jsonParams.put("recipient", recipientList);
        jsonParams.put("load_uuid", loadUUIDs);

        Log.d("json param: ", jsonParams.toString());

        final MutableLiveData<LoadsTransmitModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<LoadsTransmitModel> call = dukeApi.transmitLoads(jwtToken, userDataModel.getUserEmail(), jsonParams);

                call.enqueue(new Callback<LoadsTransmitModel>() {
                    @Override
                    public void onResponse(Call<LoadsTransmitModel> call, Response<LoadsTransmitModel> response) {
                        responseReceived = response;
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            LoadsTransmitModel model = new LoadsTransmitModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoadsTransmitModel> call, Throwable t) {
                        LoadsTransmitModel model = new LoadsTransmitModel();
                        if(responseReceived != null) {
                            model.setMessage(responseReceived.message());
                        } else {
                            model.setMessage("Error: Something unexpected happened!");
                        }
                        data.setValue(model);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<LoadsTransmitModel> transmitProcessedDocs(ArrayList<String> docSHA1s) {
        userDataModel = userConfig.getUserDataModel();

        Map<String, Object> jsonParams = new HashMap<>();
        jsonParams.put("doc_sha_array", docSHA1s);

        Log.d("json param: ", jsonParams.toString());

        final MutableLiveData<LoadsTransmitModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<LoadsTransmitModel> call = dukeApi.transmitProcessedDocs(jwtToken, userDataModel.getUserEmail(), jsonParams);

                call.enqueue(new Callback<LoadsTransmitModel>() {
                    @Override
                    public void onResponse(Call<LoadsTransmitModel> call, Response<LoadsTransmitModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            LoadsTransmitModel model = new LoadsTransmitModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoadsTransmitModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

    public LiveData<GenericResponseModel> deleteLoadObject(String loadUUID) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseModel> call = dukeApi.deleteLoadObject(jwtToken, userDataModel.getUserEmail(), loadUUID);

                call.enqueue(new Callback<GenericResponseModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseModel> call, Response<GenericResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            GenericResponseModel model = new GenericResponseModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }

}
