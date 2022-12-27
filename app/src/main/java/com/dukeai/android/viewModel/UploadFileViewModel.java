package com.dukeai.android.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.UploadFileRepository;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.GenericResponseWithJSONModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.MultipartBody;

public class UploadFileViewModel extends ViewModel {
    UploadFileRepository uploadFileRepository;
    LiveData<FileUploadSuccessModel> jsonElementLiveData;
    LiveData<GenericResponseWithJSONModel> manifestLiveData;
    LiveData<UploadFileResponseModel> uploadFileResponseModelLiveData;

    public UploadFileViewModel() {
        uploadFileRepository = new UploadFileRepository();
    }

    public LiveData<FileUploadSuccessModel> uploadFile(MultipartBody.Part[] file) {
        jsonElementLiveData = uploadFileRepository.uploadFile(file);
        return jsonElementLiveData;
    }

    public LiveData<FileUploadSuccessModel> uploadFile(MultipartBody.Part[] file, MultipartBody.Part fileCount, MultipartBody.Part address) {
        jsonElementLiveData = uploadFileRepository.uploadFile(file, fileCount, address);
        return jsonElementLiveData;
    }

    public LiveData<GenericResponseWithJSONModel> getManifestLiveData() {
        manifestLiveData = uploadFileRepository.getManifest();
        return manifestLiveData;
    }


    public LiveData<UploadFileResponseModel> getUploadFileResponseModelLiveData(MultipartBody.Part address, MultipartBody.Part load_flag, MultipartBody.Part signature, MultipartBody.Part is_scan,MultipartBody.Part coordinates,MultipartBody.Part manifest, MultipartBody.Part[] file, MultipartBody.Part[] pdfFiles) {
        uploadFileResponseModelLiveData = uploadFileRepository.upload(address, load_flag, signature, is_scan, manifest, file, pdfFiles,coordinates);
        return uploadFileResponseModelLiveData;
    }

}
