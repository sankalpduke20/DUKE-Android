package com.dukeai.android.ui.fragments;

import android.Manifest;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.UploadImagesRecyclerViewAdapter;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.bottomSheetDialogs.UploadSelectionBottomSheet;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.interfaces.UploadImagePreviewClickListener;
import com.dukeai.android.interfaces.UploadSelectionListener;
import com.dukeai.android.interfaces.UploadStatusClickActions;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.CreateLoadModel;
import com.dukeai.android.models.FileUploadSuccessModel;
import com.dukeai.android.models.GenericResponseWithJSONModel;
import com.dukeai.android.models.SingleLoadModel;
import com.dukeai.android.models.UploadFileResponseModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.UploadStatusDialog;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.LoadsViewModel;
import com.dukeai.android.viewModel.UploadFileViewModel;
import com.dukeai.android.views.CustomHeader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.Multipart;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.dukeai.android.Duke.DocsOfALoad;
import static com.dukeai.android.Duke.isLoadDocument;

// Firebase: Setup

public class UploadPreviewFragment extends Fragment implements UploadImagePreviewClickListener, PopupActions, HeaderActions, UploadSelectionListener {

    View uploadPreview;
    @BindView(R.id.header)
    CustomHeader customHeader;
    @BindView(R.id.images_view)
    RecyclerView imagesRecyclerView;
    @BindView(R.id.list_view)
    HorizontalScrollView scrollView;
    @BindView(R.id.preview_image)
    ImageView previewImage;
    @BindView(R.id.upload_button)
    Button uploadButton;
    @BindView(R.id.add_picture)
    RelativeLayout addImageView;
    @BindView(R.id.cancel_action)
    RelativeLayout cacelLayout;
    @BindView(R.id.close_mark)
    ImageView closeMark;
    @BindView(R.id.cam_icon)
    ImageView camIcon;

    LoadsViewModel loadsViewModel;
    UploadDocumentInterface uploadDocumentInterface;
    UploadImagesRecyclerViewAdapter adapter;
    UploadFileViewModel uploadFileViewModel;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int screenWidth, screenHeight;
    String uploadAs;
    Bitmap imageBitmap;
    UploadSelectionBottomSheet uploadSelectionBottomSheet;
    ArrayList<Integer> bitmapsCount = new ArrayList<>();
    Boolean isPermissionGranted = false;
    UploadStatusDialog uploadStatusDialog;
    MultipartBody.Part[] list2 = new MultipartBody.Part[Duke.uploadingImageStoragePaths.size()];
    MultipartBody.Part count2;
    String currentLocation = "none";
    String latitude = "none";
    String longitude = "none";
    MultipartBody.Part coordinates;
    boolean isDocumentAPDF = false;

    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<String> cordinatesArray;

    public UploadPreviewFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static UploadPreviewFragment newInstance() {
        UploadPreviewFragment fragment = new UploadPreviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        uploadPreview = inflater.inflate(R.layout.fragment_upload_preview, container, false);
        ButterKnife.bind(this, uploadPreview);
        popupActions = this;
        setCurrentTheme();
        return uploadPreview;
    }

    private void getWindowWidthAndHeight() {
        screenWidth = Utilities.getScreenWidthInPixels(getActivity());
        screenHeight = Utilities.getScreenHeightInPixels(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInitials();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    private void setInitials() {
        customProgressLoader = new CustomProgressLoader(getContext());
        loadsViewModel = ViewModelProviders.of(this).get(LoadsViewModel.class);
        uploadFileViewModel = ViewModelProviders.of(getActivity()).get(UploadFileViewModel.class);
        cordinatesArray = new ArrayList<>();
        getArgumentsData();
        getWindowWidthAndHeight();
        setCustomHeader();
        setRecyclerView();
        setImageView(null);
    }

    private void getArgumentsData() {
        Bundle args = getArguments();
        if (args != null) {
            String path = args.getString(AppConstants.UploadDocumentsConstants.BITMAP_IMAGE);
            currentLocation = args.getString("address");
            latitude =args.getString("lat");
            longitude  = args.getString("longi");
            if(path.substring(path.length()-3, path.length()).toLowerCase().equals("pdf")) {
                try {
                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
                    PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                    PdfRenderer.Page page = pdfRenderer.openPage(0);
                    Bitmap bitmapfromPDF = Bitmap.createBitmap(1440, 2160, Bitmap.Config.ARGB_8888);
                    // Paint bitmap before rendering
                    Canvas canvas = new Canvas(bitmapfromPDF);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(bitmapfromPDF, 0, 0, null);
                    page.render(bitmapfromPDF, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    imageBitmap = bitmapfromPDF;
                    isDocumentAPDF = true;
                    page.close();
                    pdfRenderer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                /**Resizing the Image before Displaying**/
                imageBitmap = Utilities.getPortraitResizedBitmap(BitmapFactory.decodeFile(path), 2160, 1440); /**Temp. Solution**/
            }

            previewImage.setImageBitmap(imageBitmap);
            if (isPermissionGranted) {
                setImageFilePath(path);
            } else {
                checkWritePermissions(path);
            }
        }
    }

    private void setImageFilePath(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Duke.imageStoragePath = path;
        } else {
            Duke.imageStoragePath = saveImagePath(imageBitmap);
        }
        if(!isDocumentAPDF) {
            Duke.uploadingImagesList.add(imageBitmap);
            Duke.uploadingImageStoragePaths.add(Duke.imageStoragePath);
        } else {
            Duke.uploadingPDFStoragePaths.add(Duke.imageStoragePath);
        }
    }

    void checkWritePermissions(String path) {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermissionGranted = true;
                        setImageFilePath(path);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        isPermissionGranted = false;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_LONG).show();
            }
        }).onSameThread()
                .check();
    }

//    String saveImagePath(Bitmap finalBitmap) {
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SavedImages");
//        if (!myDir.exists()) {
//            myDir.mkdirs();
//        }
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String fname = "DukeFiles_" + timeStamp + ".jpg";
//        File file = new File(myDir, fname);
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileOutputStream out = new FileOutputStream(file);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return root + "/SavedImages/" + fname;
//    }

    String saveImagePath(Bitmap finalBitmap) {
        String root = "";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "DukeFiles_" + timeStamp + ".jpg";

        OutputStream out;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getActivity().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fname);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                out = resolver.openOutputStream(imageUri);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                try {
                    out.flush();
                    return Environment.DIRECTORY_PICTURES + "/" + fname;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SavedImages");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            File file = new File(myDir, fname);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return root + "/SavedImages/" + fname;
    }

    private void setImageView(Bitmap bm) {
        if (bm != null) {
            previewImage.setImageBitmap(bm);
        } else if (Duke.uploadingImagesList != null && Duke.uploadingImagesList.size() > 0) {
            previewImage.setImageBitmap(Duke.uploadingImagesList.get(Duke.uploadingImagesList.size() - 1));
        }
    }

    private void setRecyclerView() {
        adapter = new UploadImagesRecyclerViewAdapter(Duke.uploadingImagesList, R.layout.upload_preview_list_item, getContext(), this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        imagesRecyclerView.setLayoutManager(layoutManager);
        imagesRecyclerView.setHasFixedSize(true);
        imagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        imagesRecyclerView.setAdapter(adapter);
        if (Duke.uploadingImagesList.size() >= 2) {
            scrollView.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.INVISIBLE);
        }
        /**Restrict user to max 5 images**/
        /*if (Duke.uploadingImagesList.size() < 5 && !isLoadDocument && !Duke.isDocumentBeingScanned) {
            addImageView.setVisibility(View.VISIBLE);
        }*/ //for single Image scan
        if (Duke.uploadingImagesList.size() < 5 && !isLoadDocument) {
            addImageView.setVisibility(View.VISIBLE);
        }//for Multi Image scan

    }

    private void setCustomHeader() {
        if(Duke.isDocumentBeingScanned) {
            customHeader.setToolbarTitle("Scan");
        } else {
            customHeader.setToolbarTitle(getString(R.string.upload));
        }
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadDocumentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uploadDocumentInterface = null;
        popupActions = null;
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customProgressLoader != null && customProgressLoader.isShowing()) {
            customProgressLoader.dismiss();
        }
    }

    @OnClick(R.id.add_picture)
    void onClickAddPicture() {
        // Firebase: Send click upload button event
        Bundle params = new Bundle();
//        params.putString("Page", "Scan_Document");
        params.putString("Page", "Upload_Document");
        mFirebaseAnalytics.logEvent("AddDocument", params);
        if (uploadDocumentInterface != null) {
            uploadDocumentInterface.uploadDocumentListener(false);
        }
    }

    @OnClick(R.id.cancel_action)
    void onClickCancelAction() {
        // Firebase: Send click cancel button event
        Bundle params = new Bundle();
        params.putString("Button", "Cancel");
        mFirebaseAnalytics.logEvent("UploadProcess", params);
        uploadAs = null;
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.confirmation),
                getString(R.string.cancel_upload), false,
                getString(R.string.yes), getString(R.string.no),
                popupActions, 1);
    }

    public void uploadFiles(String type) {
        type = "AllAsOne";
        uploadButton.setEnabled(false);
        customProgressLoader.showDialog();

        bitmapsCount.clear();
        switch (type) {
            case AppConstants.UploadSelectionConstants.UPLOAD:
                bitmapsCount.add(1);
                break;
            case AppConstants.UploadSelectionConstants.ALL_AS_ONE:
                bitmapsCount.add(Duke.uploadingImagesList.size());
                break;
            case AppConstants.UploadSelectionConstants.ONE_AS_ONE:
                for (int i = 0; i < Duke.uploadingImagesList.size(); i++) {
                    bitmapsCount.add(1);
                }
                break;
        }

        if (Duke.uploadingImageStoragePaths.size() <= 0 && Duke.uploadingPDFStoragePaths.size() <= 0) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customProgressLoader.hideDialog();
                }
            });

            return;
        }
        MultipartBody.Part[] list = new MultipartBody.Part[Duke.uploadingImageStoragePaths.size()];
        MultipartBody.Part[] pdfList;
        list = Utilities.getMultipartBody(Duke.uploadingImageStoragePaths, false, screenWidth, screenHeight);
        Log.d("as", Duke.PDFDocURIs.toString());
        pdfList = Utilities.getPDFMultipartBody(getActivity(), null, Duke.PDFDocURIs, screenWidth, screenHeight);

        MultipartBody.Part fileCount = Utilities.getFileCountArray(bitmapsCount);
        MultipartBody.Part address = MultipartBody.Part.createFormData("address", currentLocation);
        String data = latitude+","+longitude;
        coordinates = MultipartBody.Part.createFormData("coordinates",  data);
//        coordinates = latitude+","+longitude;
//        cordinatesArray.add(latitude);
//        cordinatesArray.add(longitude);

        list2 = list;
        count2 = fileCount;

        if (list != null && pdfList != null && list.length <= 0 && pdfList.length<=0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customProgressLoader.hideDialog();
                }
            });
            return;
        }

        String[] filenames = new String[Duke.uploadingImageStoragePaths.size()];
        ArrayList<String> filenamesArr = new ArrayList<>();

        for(int i = 0; i<filenames.length; i++) {
            filenames[i] = '"' + Utilities.getFileName(i) + '"';
        }

        for(int i=0; i<Duke.uploadingImageStoragePaths.size(); i++) {
            filenamesArr.add(Utilities.getFileName(i));
        }


        if(Duke.isDocumentAddingToLoad && Duke.selectedLoadUUID.length()>0) {
            MultipartBody.Part signature = MultipartBody.Part.createFormData("signature", "");

            MultipartBody.Part is_scan = MultipartBody.Part.createFormData("is_scan", "false");
            MultipartBody.Part manifest;

            JSONObject manifestObj = new JSONObject();
            JSONObject cordinatesObj = new JSONObject();

            for(int i =0;i<cordinatesArray.size();i++){
                if(i==0){
                    try {
                        cordinatesObj.put("lat",cordinatesArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(i==1){
                    try {
                        cordinatesObj.put("long",cordinatesArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


            //Generate load object API
            uploadFileViewModel.getManifestLiveData().observe(getActivity(), new Observer<GenericResponseWithJSONModel>() {
                @Override
                public void onChanged(GenericResponseWithJSONModel jsonObject) {
                    try {


                        manifestObj.put("sha1", jsonObject.getData().getAsJsonObject("manifest").get("sha1").getAsString());
                        ArrayList<String> combinedDocs = new ArrayList<>();
                        combinedDocs.addAll(Duke.PDFDocFilenames);
                        combinedDocs.addAll(filenamesArr);
                        manifestObj.put("filenames", new JSONArray(combinedDocs));
//                        manifestObj.put("filenames", new JSONArray(filenamesArr));
                        MultipartBody.Part load_flag = MultipartBody.Part.createFormData("load_flag", Duke.selectedLoadUUID);
                        MultipartBody.Part manifest = MultipartBody.Part.createFormData("manifest", manifestObj.toString());
//                        MultipartBody.Part coordinates = MultipartBody.Part.createFormData("coordinates",cordinatesObj.toString());
                        coordinates = MultipartBody.Part.createFormData("coordinates", data);

                        uploadFileViewModel.getUploadFileResponseModelLiveData(address, load_flag, signature, is_scan,coordinates, manifest, list2, pdfList).observe(getActivity(), new Observer<UploadFileResponseModel>() {
                            @Override
                            public void onChanged(UploadFileResponseModel uploadFileResponseModel) {
                                customProgressLoader.hideDialog();
                                Bundle bundle = new Bundle();

                                if (uploadFileResponseModel != null && uploadFileResponseModel.getStatus() != null && uploadFileResponseModel.getStatus().toLowerCase().equals(ApiConstants.ERRORS.SUCCESS.toLowerCase())) {
                                    bundle.putString("upload_status", "Document upload successful!");
                                    bundle.putString(AppConstants.StringConstants.ADD_DOC_TO_LOAD, "Document added successfully!");
                                } else {
                                    bundle.putString("upload_status", "Document upload failed!");
                                    bundle.putString(AppConstants.StringConstants.ADD_DOC_TO_LOAD, "Document upload failed!");
                                }

                                bundle.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.PROCESS);
                                refreshLoadDocs(bundle);

//                                if(uploadFileResponseModel.getMessage() != null) {
//                                    if(uploadFileResponseModel.getStatus().toLowerCase().equals("success")) {
//                                        bundle.putString("upload_status", "Document upload successful!");
//                                    } else {
//                                        bundle.putString("upload_status", "Document upload failed!");
//                                    }
//                                }
//                                NavigationFlowManager.openFragments(new LoadsFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if(Duke.isDocumentBeingScanned) {

            MultipartBody.Part signature = MultipartBody.Part.createFormData("signature", "");

            MultipartBody.Part is_scan = MultipartBody.Part.createFormData("is_scan", "true");

            MultipartBody.Part manifest;

            JSONObject manifestObj = new JSONObject();
            JSONObject cordinatesObj = new JSONObject();
            for(int i =0;i<cordinatesArray.size();i++){
                if(i==0){
                    try {
                        cordinatesObj.put("lat",cordinatesArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(i==1){
                    try {
                        cordinatesObj.put("long",cordinatesArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            uploadFileViewModel.getManifestLiveData().observe(getActivity(), new Observer<GenericResponseWithJSONModel>() {
                @Override
                public void onChanged(GenericResponseWithJSONModel jsonObject) {
                    try {
                        //                        cordinatesObj.put("lat",new JSONArray(latitude));
//                        cordinatesObj.put("long",new JSONArray(longitude));
                        ObjectMapper objectMapper = new ObjectMapper();
                        manifestObj.put("sha1", jsonObject.getData().getAsJsonObject("manifest").get("sha1").getAsString());
                        ArrayList<String> combinedDocs = new ArrayList<>();
                        combinedDocs.addAll(Duke.PDFDocFilenames);
                        combinedDocs.addAll(filenamesArr);
                        manifestObj.put("filenames", new JSONArray(combinedDocs));

                        MultipartBody.Part load_flag = MultipartBody.Part.createFormData("load_flag", "");
                        MultipartBody.Part manifest = MultipartBody.Part.createFormData("manifest", manifestObj.toString());
//                        MultipartBody.Part coordinates = MultipartBody.Part.createFormData("coordinates",cordinatesObj.toString());

                        uploadFileViewModel.getUploadFileResponseModelLiveData(address, load_flag, signature, is_scan,coordinates, manifest, list2, pdfList).observe(getActivity(), new Observer<UploadFileResponseModel>() {
                            @Override
                            public void onChanged(UploadFileResponseModel uploadFileResponseModel) {
                                customProgressLoader.hideDialog();
                                if(uploadFileResponseModel.getMessage() != null) {
//                                    if(uploadFileResponseModel.getStatus().toLowerCase().equals("success")) {
//                                        bundle.putString("upload_status", "Document scanned successfully!");
//                                    } else {
//                                        bundle.putString("upload_status", "Document scanning failed!");
//                                    }
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean(HomeFragment.IS_DOC_SCAN_SUCCESSFUL, true);
                                    NavigationFlowManager.openFragments(new HomeFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if(Duke.isNewLoadBeingCreated) {

            MultipartBody.Part signature = MultipartBody.Part.createFormData("signature", "");

            MultipartBody.Part is_scan = MultipartBody.Part.createFormData("is_scan", "false");
            MultipartBody.Part manifest;

            JSONObject manifestObj = new JSONObject();
            JSONObject cordinatesObj = new JSONObject();

            loadsViewModel.createLoad().observe(this, new Observer<CreateLoadModel>() {
                @Override
                public void onChanged(CreateLoadModel createLoadModel) {
                    //Generate load object API
                    JsonObject payload = new JsonObject();
                    payload.addProperty("load_flag", createLoadModel.getData());

                    uploadFileViewModel.getManifestLiveData().observe(getActivity(), new Observer<GenericResponseWithJSONModel>() {
                        @Override
                        public void onChanged(GenericResponseWithJSONModel jsonObject) {
                            try {

                                ObjectMapper objectMapper = new ObjectMapper();
                                manifestObj.put("sha1", jsonObject.getData().getAsJsonObject("manifest").get("sha1").getAsString());
//                                manifestObj.put("filenames", new JSONArray(filenamesArr));
                                ArrayList<String> combinedDocs = new ArrayList<>();
                                combinedDocs.addAll(Duke.PDFDocFilenames);
                                combinedDocs.addAll(filenamesArr);
                                manifestObj.put("filenames", new JSONArray(combinedDocs));
//                                manifestObj.accumulate("filenames", filenamesArr);

                                MultipartBody.Part load_flag = MultipartBody.Part.createFormData("load_flag", createLoadModel.getData());
                                MultipartBody.Part manifest = MultipartBody.Part.createFormData("manifest", manifestObj.toString());

                                uploadFileViewModel.getUploadFileResponseModelLiveData(address, load_flag, signature, is_scan,coordinates, manifest, list2, pdfList).observe(getActivity(), new Observer<UploadFileResponseModel>() {
                                    @Override
                                    public void onChanged(UploadFileResponseModel uploadFileResponseModel) {
                                        customProgressLoader.hideDialog();
//                                        Log.d("sdf", uploadFileResponseModel.getStatus());
//                                        Log.d("sdf", uploadFileResponseModel.getStatus());
                                        Bundle bundle = new Bundle();
                                        if(uploadFileResponseModel.getMessage() != null) {
                                            if(uploadFileResponseModel.getStatus().toLowerCase().equals("success")) {
                                                bundle.putString("upload_status", "Document upload successful!");
                                            } else {
                                                bundle.putString("upload_status", "Document upload failed!");
                                            }
//                                            if(uploadFileResponseModel.getMessage().contains("Upload queued")) {
//                                                bundle.putString("upload_status", "Document upload successful!");
//                                            } else {
//                                                bundle.putString("upload_status", "Document upload failed!");
//                                            }
                                        }
                                        NavigationFlowManager.openFragments(Duke.loadsFragment, bundle, getActivity(), R.id.dashboard_wrapper);
                                    }
                                });
                            } catch (Exception e) {
                                customProgressLoader.hideDialog();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } else {
            uploadFileViewModel.uploadFile(list2, count2, address).observe(getActivity(), new Observer<FileUploadSuccessModel>() {
                @Override
                public void onChanged(@Nullable FileUploadSuccessModel jsonElement) {
                    uploadButton.setEnabled(true);
                    uploadAs = null;
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        customProgressLoader.hideDialog();
//                    }
//                });
                    if (jsonElement != null && jsonElement.getCode() != null && jsonElement.getCode().equals(ApiConstants.ERRORS.SUCCESS)) {
                        resetFileUploads();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, true);
                        NavigationFlowManager.openFragments(new HomeFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
                    } else {
                        if (jsonElement != null && jsonElement.getMessage() != null) {
                            /**Dismiss Dialogs(If any)**/
                            if (uploadStatusDialog != null)
                                uploadStatusDialog.dismiss();
                            /**Show dialog**/
                            uploadStatusDialog = new UploadStatusDialog(getContext(), 10, UploadStatusDialog.DialogType.UPLOAD_ERROR, jsonElement.getMessage(), new UploadStatusClickActions() {
                                @Override
                                public void onButtonCick(int dialogId, int type) {
                                    uploadStatusDialog.dismiss();
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean(HomeFragment.IS_DOCUMENT_UPLOAD_SUCCESSFUL, false);
                                    NavigationFlowManager.openFragments(new HomeFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
                                }
                            });
                            uploadStatusDialog.setCancelable(true);
                            uploadStatusDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    customProgressLoader.hideDialog();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void refreshLoadDocs(Bundle bundle) {
        DocsOfALoad.clear();
        if(Duke.selectedLoadUUID.length() > 1) {
            loadsViewModel.getGetLoadDetailLiveData(Duke.selectedLoadUUID).observe(this, new Observer<SingleLoadModel>() {
                @Override
                public void onChanged(SingleLoadModel singleLoadModel) {
                    if(singleLoadModel.getUserLoadsModels().getDocuments().size()>0) {
                        DocsOfALoad.addAll(singleLoadModel.getUserLoadsModels().getDocuments());
                    }
                    isLoadDocument = true;
                    customProgressLoader.hideDialog();
                    NavigationFlowManager.openFragments(Duke.loadsFragment, bundle, getActivity(), R.id.dashboard_wrapper);
//                    NavigationFlowManager.openFragments(Documents.newInstance(), bundle, getActivity(), R.id.dashboard_wrapper, AppConstants.StringConstants.PROCESS);
                }
            });
        }
    }

    private String getManifestObject(String[] filenames) {
        JSONObject manifestObj = new JSONObject();

//        loadsViewModel.createLoad().observe(this, new Observer<CreateLoadModel>() {
//            @Override
//            public void onChanged(CreateLoadModel createLoadModel) {
//                //Generate load object API
//                JsonObject payload = new JsonObject();
//                payload.addProperty("load_flag", createLoadModel.getData());
//                uploadFileViewModel.getManifestLiveData().observe(getActivity(), new Observer<JsonObject>() {
//                    @Override
//                    public void onChanged(JsonObject jsonObject) {
//                        try {
//                            manifestObj.put("sha1", jsonObject.getAsJsonObject("manifest").get("sha1"));
//                            manifestObj.put("filenames", filenames);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });

        return manifestObj.toString();
    }

    @OnClick(R.id.upload_button)
    void onClickUploadButton() {
       /* if (Duke.uploadingImagesList != null && Duke.uploadingImagesList.size() > 1 && (!Duke.isLoadDocument && !Duke.isNewLoadBeingCreated && !Duke.isDocumentAddingToLoad)) {
            uploadSelectionBottomSheet = new UploadSelectionBottomSheet(getContext(), this);
        } else {*/
//            uploadAs = AppConstants.UploadSelectionConstants.UPLOAD;
            uploadAs = "AllAsOne";
//            uploadFiles(AppConstants.UploadSelectionConstants.UPLOAD);
            uploadFiles("AllAsOne");
            Bundle params = new Bundle();
            params.putString("Button", "Directly upload");
            mFirebaseAnalytics.logEvent("UploadProcess", params);

            // Firebase: Send UPLOAD event
            UserConfig userConfig = UserConfig.getInstance();
            UserDataModel userDataModel;
            userDataModel = userConfig.getUserDataModel();
            Bundle params1 = new Bundle();
            params1.putString("UserEmail", userDataModel.getUserEmail());
            params1.putString("Type", "Directly upload");
            mFirebaseAnalytics.logEvent("UploadDocument", params1);
//            confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.confirmation), getString(R.string.upload_this_image), false, getString(R.string.yes), getString(R.string.no), popupActions,1);
//        }
    }

    public void resetFileUploads() {
        Duke.imageStoragePath = "";
        Duke.uploadingImageStoragePaths = new ArrayList<>();
        Duke.uploadingImagesList = new ArrayList<>();
        Duke.sortedUploadingImageStoragePaths = new ArrayList<>();
        Duke.sortedImagesList = new ArrayList<>();
        Duke.uploadingImageCount = new ArrayList<>();
    }

    @Override
    public void onUploadImagePreviewClickListener(Bitmap bm) {
        setImageView(bm);
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
        if (uploadAs != null && uploadAs.equals(AppConstants.UploadSelectionConstants.ONE_AS_ONE)) {
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    confirmationComponent.dismiss();
                    uploadFiles(AppConstants.UploadSelectionConstants.ONE_AS_ONE);
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    confirmationComponent.dismiss();
                    break;
            }
        } else if (uploadAs != null && uploadAs.equals(AppConstants.UploadSelectionConstants.ALL_AS_ONE)) {
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    confirmationComponent.dismiss();
                    uploadFiles(AppConstants.UploadSelectionConstants.ALL_AS_ONE);
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    confirmationComponent.dismiss();
                    break;
            }
        } else if (uploadAs != null && uploadAs.equals(AppConstants.UploadSelectionConstants.UPLOAD)) {
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    confirmationComponent.dismiss();
                    uploadFiles(AppConstants.UploadSelectionConstants.UPLOAD);
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    confirmationComponent.dismiss();
                    break;
            }
        } else {
            switch (id) {
                case AppConstants.PopupConstants.POSITIVE:
                    confirmationComponent.dismiss();
                    resetFileUploads();
                    NavigationFlowManager.openFragments(new HomeFragment(), null, getActivity(), R.id.dashboard_wrapper);
                    break;
                case AppConstants.PopupConstants.NEGATIVE:
                    confirmationComponent.dismiss();
                    break;
                case AppConstants.PopupConstants.NEUTRAL:
                    confirmationComponent.dismiss();
                    NavigationFlowManager.openFragments(new UploadFailureFragment(), null, getActivity(), R.id.dashboard_wrapper);
                    break;
            }
        }
    }

    @Override
    public void onUploadSelection(String string) {

        uploadSelectionBottomSheet.hideDialog();
        UserConfig userConfig = UserConfig.getInstance();
        UserDataModel userDataModel;
        userDataModel = userConfig.getUserDataModel();
        Bundle params = new Bundle();
        Bundle params1 = new Bundle();
        switch (string) {
            case AppConstants.UploadSelectionConstants.ONE_AS_ONE:
                uploadAs = AppConstants.UploadSelectionConstants.ONE_AS_ONE;
                params.putString("Button", "One As One");
                mFirebaseAnalytics.logEvent("UploadProcess", params);

                params1.putString("Type", "One As One");
                params1.putString("UserEmail", userDataModel.getUserEmail());
                mFirebaseAnalytics.logEvent("UploadDocument", params1);
                uploadFiles(AppConstants.UploadSelectionConstants.ONE_AS_ONE);
//                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.confirmation), getString(R.string.upload_each_as_one_file), false, getString(R.string.yes), getString(R.string.no), popupActions,1);
                break;
            case AppConstants.UploadSelectionConstants.ALL_AS_ONE:
                uploadAs = AppConstants.UploadSelectionConstants.ALL_AS_ONE;
                params.putString("Button", "All As One");
                mFirebaseAnalytics.logEvent("UploadProcess", params);

                params1.putString("Type", "All As One");
                params1.putString("UserEmail", userDataModel.getUserEmail());
                mFirebaseAnalytics.logEvent("UploadDocument", params1);
                uploadFiles(AppConstants.UploadSelectionConstants.ALL_AS_ONE);
//                confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.confirmation), getString(R.string.upload_all_images_as_one_file), false, getString(R.string.yes), getString(R.string.no), popupActions,1);
                break;
            case AppConstants.UploadSelectionConstants.MANUAL:
                params.putString("Button", "Manual configuration");
                mFirebaseAnalytics.logEvent("UploadProcess", params);
                NavigationFlowManager.openFragments(new SelectPhotoFragment(), null, getActivity(), R.id.dashboard_wrapper);
                break;
        }
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int backStackLength = fm.getBackStackEntryCount();
        if (backStackLength > 1) {
            fm.popBackStack();
        }
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    private void setCurrentTheme() {

        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        closeMark.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        cacelLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        camIcon.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        addImageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));

    }
}
