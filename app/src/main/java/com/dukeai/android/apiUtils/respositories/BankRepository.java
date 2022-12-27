package com.dukeai.android.apiUtils.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiClient;
import com.dukeai.android.apiUtils.DukeApi;
import com.dukeai.android.interfaces.OnSuccessListener;
import com.dukeai.android.models.BankModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.viewModel.BankViewModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankRepository {
    DukeApi dukeApi;
    UserConfig userConfig = UserConfig.getInstance();
    UserDataModel userDataModel;

    public BankRepository() {
        dukeApi = ApiClient.getClient().create(DukeApi.class);
    }

    public LiveData<BankModel> manageBank(){
        userDataModel = userConfig.getUserDataModel();
        Map<String, Object> jsonPrams = new HashMap<>();
        jsonPrams.put("web_url","manage-bank/index.html");

        final MutableLiveData<BankModel> data = new MutableLiveData<>();
        userDataModel.getJWTToken(Duke.appContext, new OnSuccessListener() {
            @Override
            public void onSuccess(String jwtToken) {
                Call<BankModel> call = dukeApi.manageBankConnections(jwtToken,userDataModel.getUserEmail(),jsonPrams);
                call.enqueue(new Callback<BankModel>() {
                    @Override
                    public void onResponse(Call<BankModel> call, Response<BankModel> response) {
                        if (response.isSuccessful()) {
                            data.setValue(response.body());
                        } else {
                            BankModel model = new BankModel();
                            try {
//                        model.setMessage(response.errorBody().string());
                                data.setValue(model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BankModel> call, Throwable t) {
                        data.setValue(null);
                    }
                });
            }
        });
        return data;
    }
}
