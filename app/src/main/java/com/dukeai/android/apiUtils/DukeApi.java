package com.dukeai.android.apiUtils;

import com.dukeai.android.models.AppUpdateStatusModel;
import com.dukeai.android.models.ArrayResponseModel;
import com.dukeai.android.models.AssetDeductionModel;
import com.dukeai.android.models.AssetDeductionResponseModel;
import com.dukeai.android.models.BalanceSheetModel;
import com.dukeai.android.models.BalanceSheetResponseModel;
import com.dukeai.android.models.BankModel;
import com.dukeai.android.models.CreateLoadModel;
import com.dukeai.android.models.DeviceTokenModel;
import com.dukeai.android.models.DocumentDetailsModel;
import com.dukeai.android.models.DownloadReportModel;
import com.dukeai.android.models.ErrorModel;
import com.dukeai.android.models.FederalDeductionModel;
import com.dukeai.android.models.FederalDeductionResponseModel;
import com.dukeai.android.models.FileDeleteSuccessModel;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.FinancialSummaryModel;
import com.dukeai.android.models.GenerateReportModel;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.GenericResponseWithJSONModel;
import com.dukeai.android.models.LoadDocumentModel;
import com.dukeai.android.models.LoadsListModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.RecipientsListModel;
import com.dukeai.android.models.ResponseModel;
import com.dukeai.android.models.SingleLoadModel;
import com.dukeai.android.models.TripIdModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.dukeai.android.models.UserLoadsModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DukeApi {

    @PUT(ApiUrls.DeviceToken.UPDATE_DEVICE_TOKEN)
    Call<DeviceTokenModel> updateDeviceToken(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @HTTP(method = "DELETE", path = ApiUrls.DeviceToken.DELETE_DEVICE_TOKEN, hasBody = true)
    Call<DeviceTokenModel> deleteDeviceToken(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.MigrateUser.MIGRATE_USER_TO_DUKE)
    Call<DeviceTokenModel> migrateUserToDuke(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.PromoCode.PROMO_CODE_CHECK)
    Call<DeviceTokenModel> updatePromoCode(@Header("Authorization") String auth, @Body JsonObject jsonObject);

    @GET(ApiUrls.MultiFileUpload.GENERATE_MANIFEST)
    Call<GenericResponseWithJSONModel> generateManifest(@Header("Authorization") String auth, @Path("cust_id") String customerId);

    @Multipart
    @POST(ApiUrls.MultiFileUpload.MULTI_FILE_UPLOAD)
    Call<FileUploadSuccessModel> uploadMultipleFiles(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Part MultipartBody.Part[] files);

    @Multipart
    @POST(ApiUrls.MultiFileUpload.MULTI_FILE_UPLOAD)
    Call<FileUploadSuccessModel> uploadMultipleFilesWithCount(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Part MultipartBody.Part[] files, @Part MultipartBody.Part fileCount, @Part MultipartBody.Part address);

    @Multipart
    @POST(ApiUrls.MultiFileUpload.NEW_MULTI_UPLOAD)
    Call<UploadFileResponseModel> upload(@Header("Authorization") String auth, @Path("cust_id") String customerId,
                                         @Part MultipartBody.Part address, @Part MultipartBody.Part signature,
                                         @Part MultipartBody.Part is_scan, @Part MultipartBody.Part load_flag,
                                         @Part MultipartBody.Part coordinates,
                                         @Part MultipartBody.Part manifest, @Part MultipartBody.Part[] files,
                                         @Part MultipartBody.Part[] pdfFiles);

    @POST(ApiUrls.FileStatus.FILE_STATUS)
    Call<FileStatusModel> getFileStatus(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.GetFinancialSummary.FINANCIAL_SUMMARY)
    Call<FinancialSummaryModel> getFinancialSummary(
            @Header("Authorization") String auth,
            @Path("cust_id") String customerId,
            @Body JsonObject jsonObject);

    @POST(ApiUrls.GetFinancialSummary.GENERATE_REPORT)
    Call<GenerateReportModel> generateReport(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @GET(ApiUrls.FileStatus.FILE_DOWNLOAD)
    Call<ResponseBody> downloadFile(@Header("Authorization") String auth, @Header("Content-Type") String content_type, @Header("Accept") String accept, @Path("cust_id") String customerId, @Path("filename") String filename);

    @POST(ApiUrls.FileStatus.DOWNLOAD_REPORT)
    Call<DownloadReportModel> downloadReport(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);


    @DELETE(ApiUrls.FileStatus.DELETE_FILE)
    Call<FileDeleteSuccessModel> deleteFile(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("sha1") String sha1);

    @DELETE(ApiUrls.UserRegistration.DELETE_UNCONFIRMED)
    Call<ResponseModel> deleteUser(@Path("cust_id") String customerId);

    @POST(ApiUrls.UserRegistration.UPDATE_USER_GROUP)
    Call<ResponseModel> updateUserGroup(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @GET(ApiUrls.UserRegistration.FORGOT_PASSWORD)
    Call<ArrayResponseModel> forgotPassword(@Path("cust_id") String customerId);

    @GET(ApiUrls.UserRegistration.DELETE_UNCONFIRMED)
    Call<ResponseModel> verifyUser(@Path("cust_id") String customerId);

    @GET(ApiUrls.IFTA.URL)
    Call<TripIdModel> getTripId(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Header("Accept") String accept);

    @GET(ApiUrls.IFTA.IFTA_CHECK)
    Call<TripIdModel> getLastTripStatus(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Header("Accept") String accept);

    @POST(ApiUrls.IFTA.URL)
    Call<ErrorModel> sendTripData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.AppUpdate.URL)
    Call<AppUpdateStatusModel> getAppUpdateStatus(@Body JsonObject jsonObject);

    @GET(ApiUrls.FederalDeduction.URL)
    Call<FederalDeductionModel> getFederalDeductionData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Header("Accept") String accept);

    @POST(ApiUrls.FederalDeduction.URL)
    Call<FederalDeductionResponseModel> postFederalDeductionData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @GET(ApiUrls.AssetDeduction.URL)
    Call<AssetDeductionModel> getAssetDeductionData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Header("Accept") String accept);

    @POST(ApiUrls.AssetDeduction.URL)
    Call<AssetDeductionResponseModel> postAssetDeductionData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);


    @GET(ApiUrls.BalanceSheet.URL)
    Call<BalanceSheetModel> getBalanceSheetData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Header("Accept") String accept);

    @POST(ApiUrls.BalanceSheet.URL)
    Call<BalanceSheetResponseModel> postBalanceSheetData(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.MemberStatusUpdate.URL)
    Call<UpdatePaymentModel> memberStatusUpdate(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.DocumentSignatureUpdate.URL)
    Call<UpdatePaymentModel> updateDocumentSignature(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @GET(ApiUrls.DocumentDetails.URL)
    Call<DocumentDetailsModel> fetchDocumentDetails(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("sha1") String sha1);

    @POST(ApiUrls.Loads.CREATE_LOAD)
    Call<CreateLoadModel> createLoad(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.Loads.RECIPIENTS_LIST)
    Call<RecipientsListModel> getRecipientsList(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.Loads.ADD_RECIPIENT)
    Call<GenericResponseModel> addRecipient(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @PUT(ApiUrls.Loads.UPDATE_RECIPIENT)
    Call<GenericResponseModel> updateRecipientDetail(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @DELETE(ApiUrls.Loads.UPDATE_RECIPIENT)
    Call<GenericResponseModel> deleteRecipient(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @POST(ApiUrls.Loads.USER_LOADS)
    Call<LoadsListModel> getUserLoads(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body JsonObject jsonObject);

    @PUT(ApiUrls.Loads.DELETE_LOAD_OBJECT)
    Call<GenericResponseModel> deleteLoadObject(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("loadUUID") String loadUUID);

    @PUT(ApiUrls.Loads.DELETE_FROM_LOAD)
    Call<GenericResponseModel> deleteFromLoad(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("loadUUID") String loadUUID, @Body JsonObject jsonObject);

    @GET(ApiUrls.Loads.ADD_TO_LOAD)
    Call<GenericResponseModel> addToLoad(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("loadUUID") String loadUUID);

    @GET(ApiUrls.Loads.GET_LOAD_DETAIL)
    Call<SingleLoadModel> getLoadDetail(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Path("loadUUID") String loadUUID);

    @POST(ApiUrls.Loads.TRANSMIT_LOADS)
    Call<LoadsTransmitModel> transmitLoads(@Header("Authorization") String auth,
                                           @Path("cust_id") String customerId,
                                           @Body Map<String, Object> jsonObject);

    @POST(ApiUrls.Loads.TRANSMIT_PROCESSED_DOCS)
    Call<LoadsTransmitModel> transmitProcessedDocs(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body Map<String, Object> jsonObject);

    @POST(ApiUrls.ManageBankConnections.URL)
    Call<BankModel> manageBankConnections(@Header("Authorization") String auth, @Path("cust_id") String customerId, @Body Map<String, Object> jsonObject);
}
