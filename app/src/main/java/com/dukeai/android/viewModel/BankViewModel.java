package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.BankRepository;
import com.dukeai.android.models.BankModel;

public class BankViewModel extends ViewModel {
    BankRepository bankRepository;
    LiveData<BankModel> bankModelLiveData;

    public BankViewModel() {
        bankRepository = new BankRepository();
    }

    public LiveData<BankModel> getBankConnection() {
        bankModelLiveData = bankRepository.manageBank();
        return bankModelLiveData;
    }
}
