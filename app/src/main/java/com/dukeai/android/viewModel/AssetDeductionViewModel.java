package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.AssetDeductionRepository;
import com.dukeai.android.models.AssetDeductionModel;
import com.dukeai.android.models.AssetDeductionResponseModel;
import com.google.gson.JsonObject;

public class AssetDeductionViewModel extends ViewModel {

    AssetDeductionRepository assetDeductionRepository;
    LiveData<AssetDeductionModel> assetDeductionModelLiveData;
    LiveData<AssetDeductionResponseModel> postAssetDeductionModelLiveData;

    public AssetDeductionViewModel() {
        assetDeductionRepository = new AssetDeductionRepository();
    }

    public LiveData<AssetDeductionModel> getAssetDeductionModelLiveData() {
        assetDeductionModelLiveData = assetDeductionRepository.getAssetDeductionData();
        return assetDeductionModelLiveData;
    }

    public LiveData<AssetDeductionResponseModel> postAssetDeductionModelLiveData(JsonObject jsonObject) {
        postAssetDeductionModelLiveData = assetDeductionRepository.postAssetDeductionData(jsonObject);
        return postAssetDeductionModelLiveData;
    }

}
