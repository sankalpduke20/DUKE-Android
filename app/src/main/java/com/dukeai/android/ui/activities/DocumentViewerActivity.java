package com.dukeai.android.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dukeai.android.PODEmailComposition;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.models.ByteStreamResponseModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.DownloadReportModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.models.ProcessedDocumentsModel;
import com.dukeai.android.ui.fragments.SearchProcessedDocumentsFragment;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.DocumentDownloaderThread;
import com.dukeai.android.utils.DownloadProgressDialog;
import com.dukeai.android.utils.Utilities;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
//import javax.mail.Message;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static java.lang.Double.isNaN;

// Firebase: Setup

public class DocumentViewerActivity extends BaseActivity implements Handler.Callback, SearchProcessedDocumentsFragment.SearchProcessedDocumentsFragmentListener {

    public static final int progress_bar_type = 0;
    public static final int progress_bar_for_email_transmission = 1;
    PDFView pdfView;
    FloatingActionButton downloadBtn;
    FloatingActionButton shareBtn;
    Uri path;
    File pdfFile;
    long size = 0;
    int count = 0;
    float progressPercent = 0;
    String folderName;
    String sub_folder;
    long imagePoolSize = 1;
    Context context;
    String timeStampFolder = "";
    float totalCompletion;
    boolean downloadCompleted = false;
    boolean downloadClicked = false;
    double balanceSheetAmount;
    String type;
    ArrayList<ProcessedDocumentsModel> podProccessedDocuments = new ArrayList<>();
    ArrayList<ProcessedDocumentsModel> filteredDocumentLsist = new ArrayList<>();
    ThreadPoolExecutor executor;
    File podDocPath;
    int NUMBER_OF_CORES = 1;
    DownloadProgressDialog downloadProgressDialog;
    ConfirmationComponent confirmationComponent;
    boolean podDocumentsAvailable = false;
    private ProgressDialog pDialog;
    private SearchProcessedDocumentsFragment searchProcessedDocumentsFragment;
    FileStatusViewModel fileStatusViewModel;
    ArrayList<ProcessedDocumentsModel> podDocumentsList = new ArrayList<>();
    Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalender = Calendar.getInstance();
    Date customFromDate = null;
    Date customToDate = null;
    int selectedPeriod = 3;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<String> filePaths = new ArrayList<>();
    ArrayList<byte[]> byteStreamArray = new ArrayList<>();
    String flag;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        context = this;
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(this));
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);


        searchProcessedDocumentsFragment = new SearchProcessedDocumentsFragment();

        pdfView = findViewById(R.id.pdf_viewer);
        downloadBtn = findViewById(R.id.downloadBtn);
        shareBtn = findViewById(R.id.shareBtn);
        changeTheme();

        podProccessedDocuments = Duke.fileStatusModel.getPodProcessedDocuments();

        if (podProccessedDocuments.size() > 0) {
            podDocumentsAvailable = true;
        }

        if (getIntent() != null) {

            path = getIntent().getParcelableExtra("URL");
            pdfFile = (File) getIntent().getSerializableExtra("pdfFile");
            balanceSheetAmount = getIntent().getExtras().getDouble("dataAvailable");
            String type1 = getIntent().getExtras().getString("type");
            Log.e("Types ",type1);

            if(type1.equals("BalanceSheet")){
                type = "Balance Sheet";
            }else if(type1.equals("Expenses")){
                type = "Expense";
            }else if(type1.equals("fed_tax")){
                type = "Federal Tax Liability";
            }else if(type1.equals("self_tax")){
                type = "Self Employment Tax";
            }else if(type1.equals("PL")){
                type = "Profit&Loss";
            }else if(type1.equals("IFTA")){
                type = "IFTA";
            }
            selectedPeriod = getIntent().getExtras().getInt("selectedPeriod");
            if(getIntent().getExtras().get("fromDate") != null && getIntent().getExtras().get("toDate") != null ) {
                customFromDate = (Date) getIntent().getExtras().get("fromDate");
                customToDate = (Date) getIntent().getExtras().get("toDate");
            }

            pdfView.fromUri(path)
                    .enableSwipe(true) // allows    to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onRender(new OnRenderListener() {

                        @Override
                        public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {
                            pdfView.fitToWidth();
                            pdfView.setGravity(RelativeLayout.CENTER_IN_PARENT);
                        }
                    }) // called after document is rendered for the first time
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .load();

        }

        if (balanceSheetAmount == 0.0 || isNaN(balanceSheetAmount)) {
//            downloadBtn.setEnabled(false);
            downloadBtn.setBackground(getResources().getDrawable(R.drawable.grey_bg_rounded_corners));
        }

//        if (!type.equals("POD")) {
//            downloadBtn.setVisibility(View.GONE);
//        }

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(filteredDocumentLsist.size()>0) {
                    *//**Here we are getting the number of cores of the processor.**//*
                    NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

                    executor = new ThreadPoolExecutor(
                            NUMBER_OF_CORES + 1,
                            NUMBER_OF_CORES * 2,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>()
                    );
                    confirmationComponent = null;
                    downloadProgressDialog = new DownloadProgressDialog(DocumentViewerActivity.this, getString(R.string.sending),
                            "Documents are being transmitted to your email. It can take up to 2-3 minutes depending upon the number of the documents.", getString(R.string.preparing_documents_for_upload));
                    downloadProgressDialog.showDialog();
                    String username = "dispatcher@duke.ai";
                    String password = "14TheDispatcher";

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Properties props = new Properties();
                            props.put("mail.smtp.auth", "true");
                            props.put("mail.smtp.starttls.enable", "true");
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.port", "587");

                            Session session = Session.getInstance(props,
                                    new javax.mail.Authenticator() {
                                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                                            return new javax.mail.PasswordAuthentication(
                                                    username, password);
                                        }
                                    });

                            for(int i = 0; i< filteredDocumentLsist.size() ; i++) {
                                String fileName = filteredDocumentLsist.get(i).getFileName();
                                if(String.valueOf(fileName.charAt(fileName.length()-7)).equals("_") && fileName.substring(0, 3).equals("PDF")) {
                                    fileName = fileName.substring(0, fileName.length()-7) + ".pdf";

                                }
                                int finalI = i;
                                fileStatusViewModel.downloadPODDoc(fileName).observe(DocumentViewerActivity.this, new Observer<ByteStreamResponseModel>() {
                                    @Override
                                    public void onChanged(@Nullable ByteStreamResponseModel byteStreamResponseModel) {
                                        if(byteStreamResponseModel.getBytes() != null) {
                                            if(byteStreamResponseModel.getFileName().substring(0, 3).equals("PDF")) {
                                                Duke.podDocumentByteData.add(byteStreamResponseModel.bytes);
                                            } else {
                                                Duke.podDocumentByteDataImg.add(byteStreamResponseModel.bytes);
                                            }
                                            if(Duke.podDocumentByteData.size() == filteredDocumentLsist.size() || ((int) Duke.podDocumentByteData.size() + (int) Duke.podDocumentByteDataImg.size()) == filteredDocumentLsist.size() ||
                                                    Duke.podDocumentByteDataImg.size() == filteredDocumentLsist.size()) {
                                                executor.execute(new PODEmailComposition(new Handler(DocumentViewerActivity.this), session, Duke.podDocumentByteData, Duke.podDocumentByteDataImg, pdfFile.getPath()));
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    confirmationComponent = null;
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            confirmationComponent = new ConfirmationComponent(DocumentViewerActivity.this,
                                    getString(R.string.no_pod_doc_available),
                                    getString(R.string.no_pod_available_desc),
                                    true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                                @Override
                                public void onPopupActions(String id, int dialogId) {
                                    confirmationComponent.dismiss();
                                }
                            }, 1);
                        }
                    }, 1);
                }*/



                JsonObject jsonObject = InputParams.downloadReport(type);
               fileStatusViewModel.downloadReports(jsonObject).observe(DocumentViewerActivity.this, new Observer<DownloadReportModel>() {
                   @Override
                   public void onChanged(DownloadReportModel downloadReportModel) {
                       Context context = DocumentViewerActivity.this;
                       LayoutInflater inflater = getLayoutInflater();
                       View toastRoot = inflater.inflate(R.layout.custom_toast, null);
                       TextView tv = toastRoot.findViewById(R.id.textView1);
                       tv.setText(downloadReportModel.getMsg());
                       Toast toast = new Toast(context);
                       toast.setView(toastRoot);
                       toast.show();
                       toast.setDuration(Toast.LENGTH_LONG);
                   }
               });

            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle params = new Bundle();
                params.putString("report", "Share");
                mFirebaseAnalytics.logEvent("DownloadReport", params);
//                Uri path = FileProvider.getUriForFile(DocumentViewerActivity.this, DocumentViewerActivity.this.getApplicationContext().getPackageName() + ".provider", pdfFile);
                Intent objIntent = new Intent(Intent.ACTION_VIEW);
                objIntent.setDataAndType(path, "application/pdf");
                objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                DocumentViewerActivity.this.startActivity(objIntent);
            }

        });
//        getPODDocs();
    }

    private void writeByteStreamToMemory() {
        for(int i = 0; i< podDocumentsList.size() ; i++) {
            String fileName = podDocumentsList.get(i).getFileName();
            fileName = fileName.substring(0, fileName.length()-7) + ".pdf";
            fileStatusViewModel.downloadPODDoc(fileName).observe(DocumentViewerActivity.this, new Observer<ByteStreamResponseModel>() {
                @Override
                public void onChanged(@Nullable ByteStreamResponseModel byteStreamResponseModel) {
                    if(byteStreamResponseModel.getBytes() != null) {
                        if(byteStreamResponseModel.getFileName().substring(byteStreamResponseModel.getFileName().length()-2
                        ).equals("pdf")) {
                            Duke.podDocumentByteData.add(byteStreamResponseModel.bytes);
                        } else {
                            Duke.podDocumentByteDataImg.add(byteStreamResponseModel.bytes);
                        }
                    }
                }
            });
        }

    }

    private String createFile(byte[] fileData, String filename) {
        String filePath = "";

        InputStream is = new ByteArrayInputStream(fileData);
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Create directory..
            String root = Environment.getExternalStorageDirectory() + "/Duke2/pod";
            File dir = new File(root + File.separator);
            if (!dir.exists()) dir.mkdirs();

            //Create file..
            File file = new File(root, filename.substring(0, filename.length()-4) + ".jpg");
            filePath = file.getPath();
            file.createNewFile();

            //Write image from bytes array
            FileOutputStream out = new FileOutputStream(file);
            out.write(fileData);
            out.close();

            //convert image to pdf
            filePath = convertToPDF(filePath);

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public byte[] handleDataForMultiplePages() {

        return null;
    }

    private String convertToPDF(String filePath) {

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(),bitmap.getHeight(),1).create();
        PdfDocument.Page page = pdfDocument.startPage(myPageInfo);

        page.getCanvas().drawBitmap(bitmap,0,0, null);
        pdfDocument.finishPage(page);

        filePath = filePath.substring(0, filePath.length()-4) + ".pdf";
        String pdfFile = filePath;
        File myPDFFile = new File(pdfFile);

        try {
            pdfDocument.writeTo(new FileOutputStream(myPDFFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();
        return filePath;
    }

    private void getPODDocs() {
        Date fromDate = new Date();
        Date toDate = new Date();
        Date docDate;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Log.d("dsdf", podDocumentsList.toString());

        //Filter POD based on filter provided on the previous screen
        if (selectedPeriod == 4) {
            if(customFromDate != null && customToDate != null) {
                fromDate = customFromDate;
                toDate = customToDate;
            }
        } else {
            try {
                fromDate = format.parse(Utilities.getStartDate(selectedPeriod));
//                toDate = format.parse(Utilities.getEndDate());
                toDate = format.parse(Utilities.getEndDateReport(0));
            } catch (Exception e) {
                Log.d("FILTER DATE EXCEPTION", "e: " + e.getStackTrace());
            }
        }

        //for filter with document processed date
        for (ProcessedDocumentsModel document : Duke.fileStatusModel.getAllProcessedDocuments()) {
            DateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
            try {
                docDate = dF.parse(document.getProcessedData().getDocDate());

            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
            Date d1 = getZeroTimeDate(fromDate);
            Date d2 = getZeroTimeDate(docDate);

            if (getZeroTimeDate(fromDate).compareTo(getZeroTimeDate(docDate)) <= 0 && toDate.compareTo(docDate) >= 0) {
                podDocumentsList.add(document);
            }
//            if (fromDate.compareTo(docDate) <= 0 && toDate.compareTo(docDate) >= 0) {
//                podDocumentsList.add(document);
//            }
        }

        //for filter with document type as "POD"
        Log.e("PODDocumentList ",new Gson().toJson(podDocumentsList));
        for(ProcessedDocumentsModel document : podDocumentsList) {

            Log.e("Document Type ",document.getProcessedData().getDocType());
            if(document.getProcessedData().getDocType().toLowerCase().equals("pod")) {
                filteredDocumentLsist.add(document);
            }
        }

        Log.d("dsdf", filteredDocumentLsist.toString());
        //writeByteStreamToMemory();

    }

    public static Date getZeroTimeDate(Date fecha) {
        Date res = fecha;
        Calendar calendar = Calendar.getInstance();

        calendar.setTime( fecha );
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        res = calendar.getTime();

        return res;
    }

    private void writeFile(File myFile) {

        folderName = "DUKE.AI";
        sub_folder = "POD Documents";

        File myDir0 = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), folderName);
        if (!myDir0.exists()) {
            myDir0.mkdirs();
        }

        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName, sub_folder);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        File myDir2 = new File(Environment.getExternalStorageDirectory() + "/" + folderName + "/" + sub_folder, timeStampFolder);

        if (!myDir2.exists()) {
            myDir2.mkdirs();
        }


        /**Here we are getting the number of cores of the processor.**/
        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES + 1,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
        if (podDocumentsAvailable) {
            downloadProgressDialog = new DownloadProgressDialog(this, getString(R.string.downloading), "Please wait while POD documents is downloaded", getString(R.string.connecting));
        }


        File extStore = Environment.getExternalStorageDirectory();
        String path = extStore + "/" + folderName + "/" + sub_folder;

        Log.i("ExternalStorageDemo", "Save to: " + path);


        File finalPath = new File(path, "POD.pdf");
        podDocPath = finalPath;

        try {
            InputStream is = new FileInputStream(myFile);
            OutputStream os = new FileOutputStream(finalPath);

            int byteRead;

            size = myFile.length();
            int i = 0;

            while ((byteRead = is.read()) != -1) {
                executor.execute(new DocumentDownloaderThread(i++, new Handler(this), os, byteRead));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            case progress_bar_for_email_transmission:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.obj.equals("SUCCESS_EMAIL")) {
            downloadProgressDialog.setProgress(100);
            final Handler handler = new Handler(Looper.getMainLooper());
            Duke.podDocumentByteData.clear();
            Duke.podDocumentByteDataImg.clear();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.dismiss();
                    confirmationComponent = new ConfirmationComponent(DocumentViewerActivity.this, getString(R.string.email_sent_successfully), getString(R.string.documents_sent_to_mail_address), true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                        @Override
                        public void onPopupActions(String id, int dialogId) {
                            confirmationComponent.dismiss();
                        }
                    }, 1);
                }
            }, 500);

        } else if (msg.obj.equals("FAILED_EMAIL")) {
            final Handler handler = new Handler(Looper.getMainLooper());
            Duke.podDocumentByteData.clear();
            Duke.podDocumentByteDataImg.clear();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.dismiss();
                    confirmationComponent = new ConfirmationComponent(DocumentViewerActivity.this, getString(R.string.email_sending_failed), getString(R.string.document_could_not_be_send), true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                        @Override
                        public void onPopupActions(String id, int dialogId) {
                            confirmationComponent.dismiss();
                        }
                    }, 1);
                }
            }, 500);
        } else {
            count++;
            progressPercent = (((float) count) / size) * 100;
            if (count == size) {
                totalCompletion = (1 / imagePoolSize) * 100;
                downloadProgressDialog.setProgress((int) totalCompletion);
            }
        }
        return true;
    }

    @Override
    public void onSearchProcessedDocument(String folderName, long size, boolean downloaded, boolean podProccessedDocAvailable) {
        timeStampFolder = folderName;
        imagePoolSize = size;
        downloadCompleted = downloaded;
        podDocumentsAvailable = podProccessedDocAvailable;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void removeUncessaryFiles() {
        if (!type.equals("POD") || !podDocumentsAvailable || !downloadClicked) {
            File file = new File(pdfFile.getPath());
            if (file.exists()) {
                file.delete();
            }

            ArrayList<String> dir = new ArrayList<>();
            dir.add(0, Environment.getExternalStorageDirectory() + "/" + "DUKE.AI" + "/" + "POD Documents" + "/" + Duke.PODDocumentsPath);
            dir.add(1, Environment.getExternalStorageDirectory() + "/" + "DUKE.AI" + "/" + "POD Documents");
            dir.add(2, Environment.getExternalStorageDirectory() + "/" + "DUKE.AI");

            for (int i = 0; i < dir.size(); i++) {
                File file1 = new File(dir.get(i));
                if (file1.exists()) {
                    String[] list = file1.list();
                    if (list.length == 0 && list != null) {
                        file1.delete();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onDestroy() {
//        removeUncessaryFiles();
        Duke.PODDocumentsPath = "";
        super.onDestroy();
    }

    private void changeTheme() {
//        downloadBtn.setBackgroundColor(Color.parseColor("#000000"));
    }

}
