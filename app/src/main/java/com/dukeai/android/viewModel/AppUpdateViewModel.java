package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;

import com.dukeai.android.BuildConfig;
import com.dukeai.android.apiUtils.respositories.AppUpdateRepository;
import com.dukeai.android.models.AppUpdateStatusModel;
import com.google.gson.JsonObject;

public class AppUpdateViewModel {

    AppUpdateRepository appUpdateRepository;
    LiveData<AppUpdateStatusModel> appUpdateStatusLiveData;

    public AppUpdateViewModel() {
        appUpdateRepository = new AppUpdateRepository();
    }

    public LiveData<AppUpdateStatusModel> getAppUpdateStatus() {
        JsonObject jsonObject = new JsonObject();

        /**Getting App's version Name**/
        String version = BuildConfig.VERSION_NAME;
        /**Getting App's version Code**/
        int build = BuildConfig.VERSION_CODE;

        jsonObject.addProperty("app_version", version);
        jsonObject.addProperty("app_build", build);
        appUpdateStatusLiveData = appUpdateRepository.getAppUpdateStatus(jsonObject);
        return appUpdateStatusLiveData;
    }
}
