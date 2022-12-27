package com.dukeai.android.apiUtils.respositories;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.ApiUtils;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.ByteStreamResponseModel;
import com.dukeai.android.models.DocumentDetailsModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.DownloadReportModel;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.FileDeleteSuccessModel;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.SubscriptionPlan;
import com.dukeai.android.models.UpdatePaymentModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileStatusRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;

    public FileStatusRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<FileStatusModel> getFileStatus(final String numberOfDocs) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<FileStatusModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                JsonObject jsonObject = InputParams.fileStatus(Utilities.getReportCurrentDate(), Utilities.getReportLastQuarterDate(), numberOfDocs);
                jsonObject.addProperty("app_build", "300");
                jsonObject.addProperty("app_version", "2.3.5");
                Call<FileStatusModel> call = dukeApi.getFileStatus(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<FileStatusModel>() {
                    @Override
                    public void onResponse(Call<FileStatusModel> call, Response<FileStatusModel> response) {
                        if (response.isSuccessful()) {
                            if (response != null && response.body() != null) {

                                Log.i("responsebodyy", response.body().getMemberStatus());
                                if(response.body().getUniqueUserId().length() > 0) {
                                    Duke.uniqueUserId = response.body().getUniqueUserId();
                                }
                                data.setValue(response.body());
                                if(checkMemberStatus(response.body().getMemberStatus())) {
                                    Duke.subscriptionPlan.setMemberStatus(SubscriptionPlan.getSubscriptionPlanType(response.body().getMemberStatus()));
                                } else {
                                    Duke.subscriptionPlan.setMemberStatus(SubscriptionPlan.MemberStatus.NONE);
                                }
//                                Duke.subscriptionPlan.setMemberStatus(SubscriptionPlan.getSubscriptionPlanType(response.body().getMemberStatus()));
                            } else {
                                FileStatusModel fileStatusModel = new FileStatusModel();
                                try {
                                    fileStatusModel.setMessage(Utilities.getStrings(Duke.getInstance(), R.string.got_empty_response));
                                    data.setValue(fileStatusModel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    data.setValue(null);
                                }
                            }
                        } else {
                            FileStatusModel fileStatusModel = new FileStatusModel();
                            if (response != null && response.errorBody() != null) {
                                try {
                                    fileStatusModel.setMessage(ApiUtils.getApiError(response.errorBody().string()));
                                    data.setValue(fileStatusModel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    data.setValue(null);
                                }
                            } else {
                                data.setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FileStatusModel> call, Throwable t) {
                        FileStatusModel fileStatusModel = new FileStatusModel();
                        try {
                            fileStatusModel.setMessage(ApiUtils.getFailureErrorString(t));
                            data.setValue(fileStatusModel);
                        } catch (Exception e) {
                            data.setValue(null);
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

    public LiveData<DownloadImageModel> downloadPdfFile(final Activity activity, final String fileName) {
        final Bitmap bm = null;
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DownloadImageModel> data = new MutableLiveData<>();
        final String content_type = ApiConstants.DownloadFile.CONTENT_TYPE;
        final String accept = ApiConstants.DownloadFile.ACCEPT;
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseBody> call = dukeApi.downloadFile(jwtToken, content_type, accept, userDataModel.getUserEmail(), fileName);
                final Bitmap finalBm = bm;
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                   new AsyncTask<Void, Void, Void>() {
                                       @Override
                                       protected Void doInBackground(Void... voids) {
                                           if (!fileName.equals("POD.pdf")) {
                                               boolean writtenToDisk = Utilities.writeResponseBodyToDisk(activity, response.body(), fileName);

                                           } else {
                                               boolean writtenToDisk = Utilities.writeResponseBodyToDisk(activity, response.body());
                                           }
                                           return null;
                                       }

                                       @Override
                                       protected void onPostExecute(Void aVoid) {
                                           DownloadImageModel model = new DownloadImageModel();
                                           model.setBitmap(finalBm);
                                           data.setValue(model);
                                           super.onPostExecute(aVoid);
                                       }
                                   }.execute();

                            }
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DownloadImageModel model = new DownloadImageModel();
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

    public LiveData<ByteStreamResponseModel> downloadPodDocFile(final String fileName) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<ByteStreamResponseModel> data = new MutableLiveData<>();
        final String content_type = ApiConstants.DownloadFile.CONTENT_TYPE;
        final String accept = ApiConstants.DownloadFile.ACCEPT;
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseBody> call = dukeApi.downloadFile(jwtToken, content_type, accept, userDataModel.getUserEmail(), fileName);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        ByteStreamResponseModel byteStreamResponseModel = new ByteStreamResponseModel();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    byteStreamResponseModel.setBytes(response.body().bytes());
                                    byteStreamResponseModel.setFileName(fileName);
                                    data.setValue(byteStreamResponseModel);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            byteStreamResponseModel.setBytes(null);
                            byteStreamResponseModel.setFileName(null);
                            data.setValue(byteStreamResponseModel);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        ByteStreamResponseModel byteStreamResponseModel = new ByteStreamResponseModel();
                        try {
                            byteStreamResponseModel.setBytes(null);
                            byteStreamResponseModel.setFileName(ApiUtils.getFailureErrorString(t));
                            data.setValue(byteStreamResponseModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

    public LiveData<DownloadImageModel> downloadInnerFiles(final String fileName, final int screenWidth, final int screenHeight) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DownloadImageModel> data = new MutableLiveData<>();
        final String content_type = ApiConstants.DownloadFile.CONTENT_TYPE;
        final String accept = ApiConstants.DownloadFile.ACCEPT;
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseBody> call = dukeApi.downloadFile(jwtToken, content_type, accept, userDataModel.getUserEmail(), fileName);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            DownloadImageModel model = new DownloadImageModel();
                            Bitmap bm = Utilities.decodeSampledBitmapFromResource(response.body().byteStream(), screenWidth, screenHeight);
                            model.setBitmap(bm);
                            data.setValue(model);
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DownloadImageModel model = new DownloadImageModel();
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

    public LiveData<DownloadImageModel> downloadImage(final String page) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DownloadImageModel> data = new MutableLiveData<>();
        final String content_type = ApiConstants.DownloadFile.CONTENT_TYPE;
        final String accept = ApiConstants.DownloadFile.ACCEPT;
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseBody> call = dukeApi.downloadFile(jwtToken, content_type, accept, userDataModel.getUserEmail(), page);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            DownloadImageModel model = new DownloadImageModel();
                            Bitmap bm = BitmapFactory.decodeStream(response.body().byteStream());
                            model.setBitmap(bm);
                            data.setValue(model);
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DownloadImageModel model = new DownloadImageModel();
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

    public LiveData<DownloadImageModel> downloadFile(final String fileName, final int screenWidth, final int screenHeight) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DownloadImageModel> data = new MutableLiveData<>();
        final String content_type = ApiConstants.DownloadFile.CONTENT_TYPE;
        final String accept = ApiConstants.DownloadFile.ACCEPT;
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<ResponseBody> call = dukeApi.downloadFile(jwtToken, content_type, accept, userDataModel.getUserEmail(), fileName);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            DownloadImageModel model = new DownloadImageModel();
                            Bitmap bm = Utilities.decodeSampledBitmapFromResource(response.body().byteStream(), screenWidth, screenHeight);
                            Bitmap resized = Utilities.getResizedBitmap(bm, 200);
                            model.setBitmap(resized);
                            data.setValue(model);
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DownloadImageModel model = new DownloadImageModel();
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

    public LiveData<FileDeleteSuccessModel> deleteFile(final String sha1) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<FileDeleteSuccessModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FileDeleteSuccessModel> call = dukeApi.deleteFile(jwtToken, userDataModel.getUserEmail(), sha1);
                call.enqueue(new Callback<FileDeleteSuccessModel>() {
                    @Override
                    public void onResponse(Call<FileDeleteSuccessModel> call, Response<FileDeleteSuccessModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<FileDeleteSuccessModel> call, Throwable t) {
                        FileDeleteSuccessModel model = new FileDeleteSuccessModel();
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

//    public LiveData<DocumentDetailsModel> fetchDocumentDetails(final String sha1) {
//        userDataModel = userConfig.getUserDataModel();
//        final MutableLiveData<DocumentDetailsModel> data = new MutableLiveData<>();
//        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
//            @Override
//            public void onSuccess(String jwtToken) {
//                Call<DocumentDetailsModel> call = dukeApi.fetchDocumentDetails(jwtToken, userDataModel.getUserEmail(), sha1);
//                call.enqueue(new Callback<DocumentDetailsModel>() {
//                    @Override
//                    public void onResponse(Call<DocumentDetailsModel> call, Response<DocumentDetailsModel> response) {
//                        if (response.isSuccessful()) {
//                            JsonObject obj = new JsonObject();
//                            DocumentDetailsModel documentDetailsModel = new DocumentDetailsModel();
//                            documentDetailsModel = response.body();
//                            data.setValue(response.body());
//                        } else {
//                            data.setValue(null);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<DocumentDetailsModel> call, Throwable t) {
//                        DocumentDetailsModel model = new DocumentDetailsModel();
//                        try {
//                            data.setValue(model);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });
//        return data;
//    }

    public LiveData<DocumentDetailsModel> fetchDocumentDetails(final String sha1) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DocumentDetailsModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<DocumentDetailsModel> call = dukeApi.fetchDocumentDetails(jwtToken, userDataModel.getUserEmail(), sha1);
                call.enqueue(new Callback<DocumentDetailsModel>() {
                    @Override
                    public void onResponse(Call<DocumentDetailsModel> call, Response<DocumentDetailsModel> response) {
                        if (response.isSuccessful()) {
                            JsonObject obj = new JsonObject();
                            JSONObject object = new JSONObject();
                            DocumentDetailsModel documentDetailsModel = new DocumentDetailsModel();
                            documentDetailsModel = response.body();
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<DocumentDetailsModel> call, Throwable t) {
                        DocumentDetailsModel model = new DocumentDetailsModel();
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

    public LiveData<UpdatePaymentModel> memberStatusChanged(JsonObject object) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<UpdatePaymentModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<UpdatePaymentModel> call = dukeApi.memberStatusUpdate(jwtToken, userDataModel.getUserEmail(), object);
                call.enqueue(new Callback<UpdatePaymentModel>() {
                    @Override
                    public void onResponse(Call<UpdatePaymentModel> call, Response<UpdatePaymentModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdatePaymentModel> call, Throwable t) {
                        UpdatePaymentModel updatePaymentModel = new UpdatePaymentModel();
                        try {
                            updatePaymentModel.setMsg(ApiUtils.getFailureErrorString(t));
                            data.setValue(updatePaymentModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

    public LiveData<UpdatePaymentModel> updateDocumentSignature(String sha1, String signature, String is_scan) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<UpdatePaymentModel> data = new MutableLiveData<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sha1", sha1);
        jsonObject.addProperty("signature", signature);
        jsonObject.addProperty("is_scan", is_scan);
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<UpdatePaymentModel> call = dukeApi.updateDocumentSignature(jwtToken, userDataModel.getUserEmail(), jsonObject);

                call.enqueue(new Callback<UpdatePaymentModel>() {
                    @Override
                    public void onResponse(Call<UpdatePaymentModel> call, Response<UpdatePaymentModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdatePaymentModel> call, Throwable t) {
                        UpdatePaymentModel updatePaymentModel = new UpdatePaymentModel();
                        try {
                            updatePaymentModel.setMsg(ApiUtils.getFailureErrorString(t));
                            data.setValue(updatePaymentModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return data;
    }

    private boolean checkMemberStatus(String memberStatus) {
        boolean isMemberStatusValid = false;

        if(EnumUtils.isValidEnum(SubscriptionPlan.MemberStatus.class, memberStatus)){
            isMemberStatusValid = true;
        } else {
            isMemberStatusValid = false;
        }

        return isMemberStatusValid;
    }

    public LiveData<DownloadReportModel> downloadReports( final JsonObject object) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<DownloadReportModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<DownloadReportModel> call = dukeApi.downloadReport(jwtToken,  userDataModel.getUserEmail(), object);
                call.enqueue(new Callback<DownloadReportModel>() {
                    @Override
                    public void onResponse(Call<DownloadReportModel> call, Response<DownloadReportModel> response) {
                        if (response.body() != null) {
                            data.setValue(response.body());
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                DownloadReportModel responseModel = new DownloadReportModel();
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
                    public void onFailure(Call<DownloadReportModel> call, Throwable t) {
                        data.setValue(new DownloadReportModel());
                    }
                });
            }
        });
        return data;
    }


}
