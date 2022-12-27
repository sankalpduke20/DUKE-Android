package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.ReportsRepository;
import com.dukeai.android.models.FinancialSummaryModel;
import com.dukeai.android.models.GenerateReportModel;
import com.google.gson.JsonObject;

public class ReportsViewModel extends ViewModel {
    ReportsRepository reportsRepository;
    LiveData<FinancialSummaryModel> financialSummaryModelLiveData;
    LiveData<GenerateReportModel> generateReportModelLiveData;

    public ReportsViewModel() {
        reportsRepository = new ReportsRepository();
    }

    public LiveData<FinancialSummaryModel> getFinancialSummaryModelLiveData(JsonObject jsonObject) {
        financialSummaryModelLiveData = reportsRepository.getReportsData(jsonObject);
        return financialSummaryModelLiveData;
    }

    public LiveData<GenerateReportModel> getGenerateReportModelLiveData(JsonObject jsonObject) {
        generateReportModelLiveData = reportsRepository.generateReportData(jsonObject);
        return generateReportModelLiveData;
    }
}
