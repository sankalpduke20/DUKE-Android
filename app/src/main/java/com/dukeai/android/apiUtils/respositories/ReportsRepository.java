package com.dukeai.android.apiUtils.respositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.ApiUtils;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.FinancialSummaryModel;
import com.dukeai.android.models.GenerateReportModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;

    public ReportsRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<FinancialSummaryModel> getReportsData(final JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<FinancialSummaryModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<FinancialSummaryModel> call = dukeApi.getFinancialSummary(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<FinancialSummaryModel>() {
                    @Override
                    public void onResponse(Call<FinancialSummaryModel> call, Response<FinancialSummaryModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<FinancialSummaryModel> call, Throwable t) {
                        FinancialSummaryModel model = new FinancialSummaryModel();
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

    public LiveData<GenerateReportModel> generateReportData(final JsonObject jsonObject) {
        userDataModel = userConfig.getUserDataModel();
        final MutableLiveData<GenerateReportModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<GenerateReportModel> call = dukeApi.generateReport(jwtToken, userDataModel.getUserEmail(), jsonObject);
                call.enqueue(new Callback<GenerateReportModel>() {
                    @Override
                    public void onResponse(Call<GenerateReportModel> call, Response<GenerateReportModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            data.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<GenerateReportModel> call, Throwable t) {
                        GenerateReportModel model = new GenerateReportModel();
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
}
