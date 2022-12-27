package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.IFTARepository;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.TripIdModel;
import com.google.gson.JsonArray;

public class IFTAViewModel extends ViewModel {

    IFTARepository iftaRepository;
    LiveData<TripIdModel> tripStatusModelLiveData;
    LiveData<ErrorModel> tripUpdateLiveData;

    public IFTAViewModel() {
        iftaRepository = new IFTARepository();
    }

    public LiveData<TripIdModel> getTripId() {
        tripStatusModelLiveData = iftaRepository.getTripId();
        return tripStatusModelLiveData;
    }

    public LiveData<TripIdModel> getLastTripStatus() {
        tripStatusModelLiveData = iftaRepository.getLastTripStatus();
        return tripStatusModelLiveData;
    }

    public LiveData<ErrorModel> sendTripData(JsonArray locations, String tripId, String status) {
        tripUpdateLiveData = iftaRepository.sendTripData(locations, tripId, status);
        return tripUpdateLiveData;
    }
}
