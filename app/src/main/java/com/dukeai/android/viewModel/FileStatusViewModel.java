package com.dukeai.android.viewModel;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dukeai.android.apiUtils.respositories.FileStatusRepository;
import com.dukeai.android.models.ByteStreamResponseModel;
import com.dukeai.android.models.DocumentDetailsModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.DownloadReportModel;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.FileDeleteSuccessModel;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class FileStatusViewModel extends ViewModel {
    FileStatusRepository fileStatusRepository;
    LiveData<FileStatusModel> fileStatusModelLiveData;
    LiveData<DownloadImageModel> liveData;
    LiveData<FileDeleteSuccessModel> deleteSuccessModelLiveData;
    LiveData<UpdatePaymentModel> updatePaymentModelLiveData;
    LiveData<DocumentDetailsModel> getDocumentDetailsLiveData;
    LiveData<ByteStreamResponseModel> getPODDocByteResponse;
    LiveData<UpdatePaymentModel> updateDocumentSignature;
    LiveData<DownloadReportModel> downloadReportModelLiveData;

    public FileStatusViewModel() {
        fileStatusRepository = new FileStatusRepository();
    }

    public LiveData<FileStatusModel> getFileStatusModelLiveData(String numberOfDocs) {
        fileStatusModelLiveData = fileStatusRepository.getFileStatus(numberOfDocs);
        return fileStatusModelLiveData;
    }

    public LiveData<DownloadImageModel> downloadFile(String fileName, int screenWidth, int screenHeight, Boolean isInnerFile) {
        if (isInnerFile) {
            liveData = fileStatusRepository.downloadInnerFiles(fileName, screenWidth, screenHeight);
        } else {
            liveData = fileStatusRepository.downloadFile(fileName, screenWidth, screenHeight);
        }
        return liveData;
    }

    public LiveData<DownloadImageModel> downloadImage(String fileName) {
        liveData = fileStatusRepository.downloadImage(fileName);
        return liveData;
    }

    public LiveData<DownloadImageModel> downloadReportFile(Activity activity, String fileName) {
        liveData = fileStatusRepository.downloadPdfFile(activity, fileName);
        return liveData;
    }

    public LiveData<ByteStreamResponseModel> downloadPODDoc(String fileName) {
        getPODDocByteResponse = fileStatusRepository.downloadPodDocFile(fileName);
        return getPODDocByteResponse;
    }

    public LiveData<FileDeleteSuccessModel> deleteFile(String sha1) {
        deleteSuccessModelLiveData = fileStatusRepository.deleteFile(sha1);
        return deleteSuccessModelLiveData;
    }

    public LiveData<UpdatePaymentModel> memberStatusUpdate(JsonObject jsonObject) {
        updatePaymentModelLiveData = fileStatusRepository.memberStatusChanged(jsonObject);
        return updatePaymentModelLiveData;
    }

    public LiveData<UpdatePaymentModel> updateDocumentSignature(String sha1, String signature, String is_scan) {
        updateDocumentSignature = fileStatusRepository.updateDocumentSignature(sha1, signature, is_scan);
        return updateDocumentSignature;
    }

    public LiveData<DocumentDetailsModel> getDocumentDetails(String sha1) {
        getDocumentDetailsLiveData = fileStatusRepository.fetchDocumentDetails(sha1);
        return  getDocumentDetailsLiveData;
    }

    public LiveData<DownloadReportModel> downloadReports( JsonObject object) {
        downloadReportModelLiveData = fileStatusRepository.downloadReports(object);
        return downloadReportModelLiveData;
    }
}
