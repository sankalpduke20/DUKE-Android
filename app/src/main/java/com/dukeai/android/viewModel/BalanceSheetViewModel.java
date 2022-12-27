package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.BalanceSheetRepository;
import com.dukeai.android.models.BalanceSheetModel;
import com.dukeai.android.models.BalanceSheetResponseModel;
import com.google.gson.JsonObject;

public class BalanceSheetViewModel extends ViewModel {
    BalanceSheetRepository balanceSheetRepository;
    LiveData<BalanceSheetModel> balanceSheetModelLiveData;
    LiveData<BalanceSheetResponseModel> postBalanceSheetData;

    public BalanceSheetViewModel() {
        balanceSheetRepository = new BalanceSheetRepository();
    }

    public LiveData<BalanceSheetModel> getBalanceSheetModelLiveData() {
        balanceSheetModelLiveData = balanceSheetRepository.getBalanceSheetData();
        return balanceSheetModelLiveData;
    }

    public LiveData<BalanceSheetResponseModel> postBalanceSheetData(JsonObject jsonObject) {
        postBalanceSheetData = balanceSheetRepository.postBalanceSheetData(jsonObject);
        return postBalanceSheetData;
    }
}
