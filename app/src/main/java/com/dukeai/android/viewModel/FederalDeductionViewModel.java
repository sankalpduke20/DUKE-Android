package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.TaxInfoRepository;
import com.dukeai.android.models.FederalDeductionModel;
import com.dukeai.android.models.FederalDeductionResponseModel;
import com.google.gson.JsonObject;

public class FederalDeductionViewModel extends ViewModel {
    TaxInfoRepository taxInfoRepository;
    LiveData<FederalDeductionModel> federalDeductionModelLiveData;
    LiveData<FederalDeductionResponseModel> postFederalDeductionModelLiveData;

    public FederalDeductionViewModel() {
        taxInfoRepository = new TaxInfoRepository();
    }

    public LiveData<FederalDeductionModel> getFederalDeductionModelLiveData() {
        federalDeductionModelLiveData = taxInfoRepository.getFederalDeductionData();
        return federalDeductionModelLiveData;
    }

    public LiveData<FederalDeductionResponseModel> postFederalDeductionModelLiveData(JsonObject jsonObject) {
        postFederalDeductionModelLiveData = taxInfoRepository.postFederalDeductionData(jsonObject);
        return postFederalDeductionModelLiveData;
    }
}
