package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiUtils;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.GenericResponseWithJSONModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFileRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;

    public UploadFileRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<FileUploadSuccessModel> uploadFile(final MultipartBody.Part[] files) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<FileUploadSuccessModel> data = new MutableLiveData<>();

        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FileUploadSuccessModel> call = dukeApi.uploadMultipleFiles(jwtToken, userDataModel.getUserEmail(), files);
                call.enqueue(new Callback<FileUploadSuccessModel>() {
                    @Override
                    public void onResponse(Call<FileUploadSuccessModel> call, Response<FileUploadSuccessModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            FileUploadSuccessModel model = new FileUploadSuccessModel();
                            try {
                                model.setMessage(ApiUtils.getApiError(response.errorBody().string()));
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FileUploadSuccessModel> call, Throwable t) {
                        FileUploadSuccessModel model = new FileUploadSuccessModel();
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


    public LiveData<FileUploadSuccessModel> uploadFile(final MultipartBody.Part[] files, final MultipartBody.Part fileCount, final MultipartBody.Part address) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<FileUploadSuccessModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FileUploadSuccessModel> call = dukeApi.uploadMultipleFilesWithCount(jwtToken, userDataModel.getUserEmail(), files, fileCount, address);
                call.enqueue(new Callback<FileUploadSuccessModel>() {
                    @Override
                    public void onResponse(Call<FileUploadSuccessModel> call, Response<FileUploadSuccessModel> response) {
                        System.out.println("Address in Multi Upload "+new Gson().toJson(address));
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            FileUploadSuccessModel model = new FileUploadSuccessModel();
                            try {
                                model.setMessage(ApiUtils.getApiError(response.errorBody().string()));
                                data.setValue(model);
                            } catch (Exception e) {
                                model.setMessage(Utilities.getStrings(Duke.getInstance(), R.string.failed_to_upload));
                                data.setValue(model);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FileUploadSuccessModel> call, Throwable t) {
                        FileUploadSuccessModel model = new FileUploadSuccessModel();
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

    public LiveData<GenericResponseWithJSONModel> getManifest() {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenericResponseWithJSONModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenericResponseWithJSONModel> call = dukeApi.generateManifest(jwtToken, userDataModel.getUserEmail());
                call.enqueue(new Callback<GenericResponseWithJSONModel>() {
                    @Override
                    public void onResponse(Call<GenericResponseWithJSONModel> call, Response<GenericResponseWithJSONModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponseWithJSONModel> call, Throwable t) {
                        try {
                            data.setValue(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

    public LiveData<UploadFileResponseModel> upload(MultipartBody.Part address, MultipartBody.Part load_flag, MultipartBody.Part signature, MultipartBody.Part is_scan, MultipartBody.Part manifest, MultipartBody.Part[] file, MultipartBody.Part[] pdfFiles,MultipartBody.Part coordinates) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<UploadFileResponseModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<UploadFileResponseModel> call = dukeApi.upload(jwtToken, userDataModel.getUserEmail(), address, signature, is_scan, load_flag, coordinates,manifest, file, pdfFiles);
                call.enqueue(new Callback<UploadFileResponseModel>() {
                    @Override
                    public void onResponse(Call<UploadFileResponseModel> call, Response<UploadFileResponseModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                            System.out.println("Scan Response  => "+new Gson().toJson(response.body()));
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<UploadFileResponseModel> call, Throwable t) {
                        try {
                            data.setValue(null);
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
