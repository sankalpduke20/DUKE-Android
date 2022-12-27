package com.dukeai.android.ui.fragments;

import android.Manifest;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;

import com.dukeai.android.ui.activities.PaymentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.ReportSpinnerAdapter;
import com.dukeai.android.adapters.ReportsAdapter;
import com.dukeai.android.apiUtils.InputParams;
import com.dukeai.android.bottomSheetDialogs.CustomFilterSheet;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.OnPeriodSubmitListener;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.DownloadImageModel;
import com.dukeai.android.models.FinancialSummaryModel;
import com.dukeai.android.models.GenerateReportModel;
import com.dukeai.android.models.ReportSpinnerDataModel;
import com.dukeai.android.ui.activities.DocumentViewerActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.DownloadProgressDialog;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PeriodSelectorAlert;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.ReportsViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Firebase: Setup

public class ReportsFragment extends Fragment implements ReportsAdapter.OnListItemClickListener, HeaderActions, PopupActions, Handler.Callback {
    String TAG = ReportsFragment.class.getSimpleName();

    View reportView;
    ReportsAdapter reportsAdapter;
    Button downloadBtn;
    Context context;
    ReportsViewModel reportsViewModel;
    ArrayList<FinancialSummaryModel.DataModel.ReportsModel> reportsModels = new ArrayList<>();
    ArrayList<ReportSpinnerDataModel> reportSpinnerDataModel = new ArrayList<>();
    ReportSpinnerAdapter reportSpinnerAdapter;
    CustomFilterSheet customFilterSheet;
    PeriodSelectorAlert periodSelectorAlert;
    @BindView(R.id.datesSelected)
    TextView datesSelected;
    @BindView(R.id.reports_header)
    CustomHeader customHeader;
    @BindView(R.id.reports_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.report_spinner)
    Spinner spinner;
    @BindView(R.id.empty_repots_layout)
    RelativeLayout emptyCardLayout;
    @BindView(R.id.upload_button)
    FloatingActionButton uploadButton;
    CustomProgressLoader customProgressLoader;
    GenerateReportModel generateReportModel;
    Boolean hasWritePermissions = false;
    FileStatusViewModel fileStatusViewModel;
    UploadDocumentInterface uploadDocumentInterface;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    int position = 0;
    long size = 0;
    int count = 0;
    float progressPercent = 0;
    String folderName;
    String sub_folder;
    boolean isPOD = false;
    Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalender = Calendar.getInstance();
    int fromDateFlag = 0, toDateFlag = 0;
    int tomonthVal, todateVal, toyearVal;
    int frommonthVal, fromdateVal, fromyearVal;
    long toTimeInMillis = 0;
    long fromTimeInMillis = 0;
    int pos = 3;
    int customIndex = 0;
    boolean ntgSelected = false;
    String tagName = "";
    int currentSpinnerValue = 3;
    ThreadPoolExecutor executor;
    int NUMBER_OF_CORES = 1;
    DownloadProgressDialog downloadProgressDialog;
    ReportsFragment prev, curent;
    private OnFragmentInteractionListener mListener;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    Date customFromDate = null;
    Date customToDate = null;

    public ReportsFragment() {
        // Required empty public constructor
    }

    public static ReportsFragment newInstance() {
        ReportsFragment fragment = new ReportsFragment();
        fragment.setMyTag("reportsfragment");
        return fragment;
    }

    public void setMyTag(String tagname) {
        if (tagname.equals("")) {
            return;
        }
        tagName = tagname;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorTransparent));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_document_viewer, container, false);

        // Inflate the layout for this fragment
        reportView = inflater.inflate(R.layout.fragment_reports, container, false);
        ButterKnife.bind(this, reportView);
        popupActions = this;

        setCurrentTheme();
//        TextView tv = reportView.findViewById(R.id.datesSelected);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(ntgSelected){}else{
//
//                }
//            }
//        });
        return reportView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInitials();
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        setSpinner();

    }


    private void setSpinner() {
        reportSpinnerDataModel.clear();
        reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_week)));
        reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_month)));
        reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_quarter)));
        reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.year_to_date)));
        reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.custom)));

        reportSpinnerAdapter = new ReportSpinnerAdapter(context, R.layout.report_spinner_item, reportSpinnerDataModel);
        spinner.setAdapter(reportSpinnerAdapter);
        spinner.setSelection(3); //YTD


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                Log.i("selected", "i" + i);

                if (i == 5) {

                    currentSpinnerValue = 5;
                    reportSpinnerDataModel.remove(5);
                    /**Show dialog**/
                    periodSelectorAlert = new PeriodSelectorAlert(getContext(), new OnPeriodSubmitListener() {
                        @Override
                        public void onSubmitClick(Calendar fromCalendarResponse, Calendar toCalendarResponse) {
                            periodSelectorAlert.dismiss();
                            fromCalendar = fromCalendarResponse;
                            toCalender = toCalendarResponse;
                            getReportsData(4);
                            datesSelected.setText(Utilities.getFormattedDate(fromCalendar) + " - " + Utilities.getFormattedDate(toCalender));
                            datesSelected.setVisibility(View.VISIBLE);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            periodSelectorAlert.dismiss();
                            spinner.setSelection(pos);
                        }
                    });

                } else if (i == 4) {
                    currentSpinnerValue = 4;
//                    showCustomFilterDialog();

                    reportSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.custom)));
                    reportSpinnerAdapter = new ReportSpinnerAdapter(context, R.layout.report_spinner_item, reportSpinnerDataModel);
                    spinner.setAdapter(reportSpinnerAdapter);
                    spinner.setSelection(5);

                } else {
                    currentSpinnerValue = i;
                    pos = i;
                    customIndex = i;
                    getReportsData(i);
                    datesSelected.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                ntgSelected = true;
            }


        });
    }

    private void setInitials() {
        setToolbarColor();
        reportsModels.clear();
        position = 0;
        fileStatusViewModel = ViewModelProviders.of(getActivity()).get(FileStatusViewModel.class);
        reportsViewModel = ViewModelProviders.of(getActivity()).get(ReportsViewModel.class);
        customProgressLoader = new CustomProgressLoader(getContext());
        setCustomHeader();
        setRecyclerAdapter();
    }

    private void setRecyclerAdapter() {
        reportsAdapter = new ReportsAdapter(context, R.layout.view_report_list_item, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(reportsAdapter);
        recyclerView.setFocusable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void getReportsData(int i) {
        position = i;
        String from = Utilities.getStartDate(i);
        String to = Utilities.getEndDateReport(i);
        // Firebase: Send choose time period event
        Bundle params = new Bundle();
        if (i == 0) {
            params.putString("Period", "Last Week");
        } else if (i == 1) {
            params.putString("Period", "Last Month");
        } else if (i == 2) {
            params.putString("Period", "Last Quarter");
        } else if (i == 3) {
            params.putString("Period", "Year To Date");
        } else if (i == 4) {
            params.putString("Period", "Customize");
            from = Utilities.getFormattedDate(fromCalendar); //"" + fromyearVal + "-" + frommonthVal + "-" + fromdateVal;
            to = Utilities.getFormattedDate(toCalender); //"" + toyearVal + "-" + tomonthVal + "-" + todateVal;
            customFromDate = fromCalendar.getTime();
            customToDate = toCalender.getTime();
        }

        mFirebaseAnalytics.logEvent("SelectTimePeriod", params);
        customProgressLoader.showDialog();
        reportsModels.clear();

        JsonObject jsonObject = InputParams.getFinancialSummary(from, to);
        jsonObject.addProperty("version", "1.2.5");
        reportsViewModel.getFinancialSummaryModelLiveData(jsonObject).observe(this, new Observer<FinancialSummaryModel>() {
            @Override
            public void onChanged(@Nullable FinancialSummaryModel financialSummaryModel) {
                customProgressLoader.hideDialog();
                if (financialSummaryModel != null && financialSummaryModel.getDataModel() != null && financialSummaryModel.getDataModel().getReportsModels().size() > 0) {
                    reportsModels.clear();
//                    reportsModels.addAll(financialSummaryModel.getDataModel().getReportsModels());
                    for(FinancialSummaryModel.DataModel.ReportsModel pm : financialSummaryModel.getDataModel().getReportsModels()){
                        if(pm.getType().equals("BalanceSheet")
                                || pm.getType().equals("Expenses")
                                ||pm.getType().equals("fed_tax")
                                ||pm.getType().equals("self_tax")
                                ||pm.getType().equals("PL")
                                ||pm.getType().equals("IFTA")
                                ||pm.getType().equals("YTD Tax Liability")){
                           reportsModels.add(pm);
                        }
                    }
                    reportsAdapter.updateDataList(reportsModels);
                    emptyCardLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    if (financialSummaryModel != null && financialSummaryModel.getMessage() != null) {
                        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), financialSummaryModel.getMessage(), false, getString(R.string.ok), popupActions, 1);
                    }
                    recyclerView.setVisibility(View.GONE);
                    emptyCardLayout.setVisibility(View.VISIBLE);
                    emptyCardLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent));
                    ImageView imageView = emptyCardLayout.findViewById(R.id.image);
                    imageView.setImageResource(R.drawable.ic_view_report);
                    TextView textView = emptyCardLayout.findViewById(R.id.error_message);
                    textView.setText(R.string.no_reports_to_list);
                }
            }
        });
    }

    public void generateReport(final int pos, FinancialSummaryModel.DataModel.ReportsModel reportsModel) {
        if (reportsModel == null || reportsModel.getType() == null) {
            return;
        }
        String reportType = "";
        String from = "";
        String to = "";
        String period = "";
        if (currentSpinnerValue != 4 && currentSpinnerValue != 5) {
            reportType = Utilities.getReportType(reportsModel.getType());
            from = Utilities.getStartDate(position);
            to = Utilities.getEndDateReport(currentSpinnerValue);
            period = Utilities.getPeriod(position);
        } else {
            reportType = Utilities.getReportType(reportsModel.getType());
            from = Utilities.getFormattedDate(fromCalendar);
            to = Utilities.getFormattedDate(toCalender);
            period = Utilities.getPeriod(position);
        }
        // Firebase: Send choose report type event
        Bundle params = new Bundle();
        params.putString("Type", reportType);
        mFirebaseAnalytics.logEvent("DownloadReport", params);

        customProgressLoader.showDialog();
        JsonObject jsonObject = InputParams.generateReport(from, to, period, reportType);
        Log.d(Duke.TAG,"Report Request ===> "+new Gson().toJson(jsonObject));
        reportsViewModel.getGenerateReportModelLiveData(jsonObject).observe(this, new Observer<GenerateReportModel>() {
            @Override
            public void onChanged(@Nullable GenerateReportModel model) {
                customProgressLoader.hideDialog();

                Log.d(Duke.TAG,"Report Response ===> "+new Gson().toJson(model));
                if (model != null) {
                    generateReportModel = model;
                    if(model.getResult()!= null) {
                        getReportDetails(pos);
                    } else {
                        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, 1);
                    }
                } else {
                    confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.error), getString(R.string.something_unexpected_happened_we_re_sorry_please_try_again_later), false, getString(R.string.ok), popupActions, 1);
                }
            }
        });
    }

    private void getReportDetails(int pos) {
        String data = generateReportModel.getResult();
        String fileName = data.split("\\/")[1].trim();
        String type = generateReportModel.getRequestModel().getReportType();
        if (type.equals("POD")) {
            isPOD = true;
        }
        if (hasWritePermissions) {
            handleFileData(fileName, pos);
        } else {
            checkWritePermissions(fileName, pos);
        }
    }


    private void handleFileData(String fileName, final int pos) {
        if (isPOD) {
            downloadDoc(fileName, pos);
            isPOD = false;
        } else {
            downloadDoc(fileName, pos);
        }
    }

    private void downloadDoc(String fileName, final int pos) {
        fileStatusViewModel.downloadReportFile(getActivity(), fileName).observe(this, new Observer<DownloadImageModel>() {
            @Override
            public void onChanged(@Nullable DownloadImageModel downloadImageModel) {
                if (downloadImageModel != null) {
                    openUrl(pos);
                } else {
                }
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        count++;
        progressPercent = (((float) count) / size) * 100;
        downloadProgressDialog.setProgress((int) progressPercent);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (count == size) {
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadProgressDialog.dismiss();
                            confirmationComponent = new ConfirmationComponent(getContext(), getActivity().getString(R.string.download_successful), getContext().getString(R.string.documents_downloaded_successfully_please_find_them_in_folder) + folderName + "/" + sub_folder, true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                                @Override
                                public void onPopupActions(String id, int dialogId) {
                                    confirmationComponent.dismiss();
                                }
                            }, 1);
                        }
                    });
                }
            }
        });

        return true;
    }

    private void openUrl(int pos) {

        File pdfFile = new File(Duke.FileStoragePath);//File path
        final Uri path = Duke.FileStoragePathURI;
//        if (pdfFile.exists())
//        {
//            final Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(getActivity(), DocumentViewerActivity.class);
            intent.putExtra("URL", path);
            intent.putExtra("pdfFile", pdfFile);
            intent.putExtra("type", reportsModels.get(pos).type);
            intent.putExtra("selectedPeriod", position);
            if(customFromDate != null && customToDate != null) {
                intent.putExtra("fromDate", customFromDate);
                intent.putExtra("toDate", customToDate);
            }
            Bundle b = new Bundle();
            b.putDouble("dataAvailable", reportsModels.get(0).getAmount());
            intent.putExtras(b);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

//        }
    }

    private void checkWritePermissions(final String fileName, final int pos) {
        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            hasWritePermissions = true;
                            handleFileData(fileName, pos);
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            hasWritePermissions = false;
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_LONG).show();
            }
        }).onSameThread()
                .check();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UploadDocumentInterface");
        }
    }

    private void setCustomHeader() {
        customHeader.setToolbarTitle(getString(R.string.reports));
        customHeader.showHideProfileImage(View.VISIBLE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
        if (Duke.ProfileImage != null) {
            customHeader.setHeaderImage(Duke.ProfileImage);
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        popupActions = null;
        ButterKnife.bind(this, reportView).unbind();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.bind(this, reportView).unbind();
    }

    @Override
    public void onListItemClick(int pos, FinancialSummaryModel.DataModel.ReportsModel dataModel) {
        generateReport(pos, dataModel);
    }

    @Override
    public void onClickProfile() {
        NavigationFlowManager.openFragments(ProfileFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.PROFILE);
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

    @Override
    public void onPopupActions(String id, int dialogId) {
        switch (id) {
            case AppConstants.PopupConstants.NEUTRAL:
                confirmationComponent.dismiss();
                break;
        }
    }

    private void loadPaywall() {
        Intent intent = new Intent(requireActivity(), PaymentActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    @OnClick(R.id.upload_button)
    void onClickUploadButton() {
        // Firebase: Send click upload button event
        if (((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD")) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() == 0.0) ||
                ((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD")) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() < 0.0)) {
            loadPaywall();
        } else {
            Bundle params = new Bundle();
            params.putString("Page", "Report");
            mFirebaseAnalytics.logEvent("AddDocument", params);

            if (uploadDocumentInterface != null) {
                Utilities.resetFileData();
                uploadDocumentInterface.uploadDocumentListener(false);
            }
        }
    }

    private void showCustomFilterDialog() {
        customFilterSheet = new CustomFilterSheet(getContext(), this.getActivity(), this);
        if (!customFilterSheet.isShowing()) {
//            customFilterSheet.showDialog();
        }
    }

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        uploadButton.setColorFilter(Color.parseColor(changeThemeModel.getFloatingButtonColor()));
        uploadButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(changeThemeModel.getFloatingButtonBackgroundColor())));
        datesSelected.setBackgroundColor(Color.parseColor(changeThemeModel.getBackgroundColor()));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
