package com.dukeai.android.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.LoadsListAdapter;
import com.dukeai.android.adapters.ProcessedDocumentsAdapter;
import com.dukeai.android.adapters.ReportSpinnerAdapter;
import com.dukeai.android.interfaces.OnPeriodSubmitListener;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UpdateDocumentNavigationIcons;
import com.dukeai.android.models.ChangeThemeModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.ProcessedDocumentsModel;
import com.dukeai.android.models.ReportSpinnerDataModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.DasSpinner;
import com.dukeai.android.utils.DownloadProgressDialog;
import com.dukeai.android.utils.ImageDownloaderThread;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PeriodSelectorAlert;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.LoadsViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.dukeai.android.apiUtils.ApiUrls.BASE_URL;

// Firebase: Setup

public class SearchProcessedDocumentsFragment extends Fragment implements ProcessedDocumentsAdapter.OnListItemClickListener, Handler.Callback, UpdateDocumentNavigationIcons {

    public ProcessedDocumentsAdapter adapter;
    View mainView;
    @BindView(R.id.search_bar)
    SearchView searchView;
    @BindView(R.id.period_selector_spinner)
    DasSpinner periodSpinner;
    @BindView(R.id.search_result_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.no_results_view)
    RelativeLayout noResultsView;
    @BindView(R.id.document_download_button)
    Button downloadButton;
    @BindView(R.id.datesSelected)
    TextView datesSelected;
    PeriodSelectorAlert periodSelectorAlert;
    Context context;
    Context mainContext;
    ArrayList<ProcessedDocumentsModel> processedDocumentsList = new ArrayList<>();
    ArrayList<ProcessedDocumentsModel> filteredDocumentsList = new ArrayList<>();
    ArrayList<ProcessedDocumentsModel> searchedFilteredDocumentsList = new ArrayList<>();
    ArrayList<ProcessedDocumentsModel> podProccessedDocuments = new ArrayList<>();
    CustomProgressLoader customProgressLoader;
    File pathOfFile;
    Activity activity;
    ConfirmationComponent confirmationComponent;
    boolean downloadAction = false;
    Map<String, URL> imageNameURLMap;
    String folderName = "";
    Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalender = Calendar.getInstance();
    DownloadProgressDialog downloadProgressDialog;
    boolean podProcessedDocAvailable = false;
    int pos = 3;
    /**
     * To Keep track of The progress
     **/
    float curCount = 0;
    float totalCount = 0;
    ThreadPoolExecutor executor;
    int NUMBER_OF_CORES = 1;
    /**
     * This is the DEFAULT selected Period
     **/
    int selectedPeriod = 3; //Year To Date = 3
    /**
     * This Holds the Latest Search String
     **/
    String inputText = "";
    ArrayList<ReportSpinnerDataModel> periodSpinnerDataModel = new ArrayList<>();
    ReportSpinnerAdapter periodSpinnerAdapter;
    private NotificationManagerCompat notificationManager;
    private SearchProcessedDocumentsFragmentListener listener = null;
    LoadsViewModel loadsViewModel;
    // Firebase: Setup
    private FirebaseAnalytics mFirebaseAnalytics;
    int loadAdapterCurrentItem = -1;

    public SearchProcessedDocumentsFragment() {
        // Required empty public constructor
    }

    public static SearchProcessedDocumentsFragment newInstance() {
        SearchProcessedDocumentsFragment fragment = new SearchProcessedDocumentsFragment();
        return fragment;
    }

    @Override
    public void UpdateDocumentNavigation(int position) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_search_processed_documents, container, false);
        ButterKnife.bind(this, mainView);
        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });
        loadsViewModel = ViewModelProviders.of(getActivity()).get(LoadsViewModel.class);
        setRecyclerAdapter();
        setSearchViewTextChangeListener();
        activity = getActivity();
        activity = getActivity();
        setCurrentTheme();
        notificationManager = NotificationManagerCompat.from(getActivity());
        // Firebase: Setup
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        setSwipeActionCallback();
        return mainView;
    }

    private void setSearchViewTextChangeListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.trim();
                inputText = s;

//                applyFilters(pos);

                applyFilterForSearch(pos);
                return false;
            }
        });
    }

    private void applyFilterForSearch(int pos) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                /**Mark:- 1.Apply Filter based on search input**/

                if (!inputText.equals("")) {
                    String[] searchWords = inputText.split(" |\n");

                    StringBuilder regexp = new StringBuilder();

                    for (int i = 0; i < searchWords.length; i++) {
                        Log.d("Search Word =>", new Gson().toJson(searchWords));
                        if (i != (searchWords.length - 1)) {
                            regexp.append("(?=.*").append(searchWords[i]).append(")|");
                        } else {
                            regexp.append("(?=.*").append(searchWords[i]).append(")");
                        }
                    }

                    Pattern pattern = Pattern.compile(regexp.toString(), Pattern.CASE_INSENSITIVE);

                    /**Apply Search Logic Here**/
                    filteredDocumentsList.clear();

                    for (int i = 0; i < processedDocumentsList.size(); ++i) {

                        ProcessedDocumentsModel document = processedDocumentsList.get(i);
                        String title = document.getProcessedData().getTitle();
                        String docType = document.getProcessedData().getDocType();
                        String signature = document.getSignature();

                        /**Add Document To Filtered Array to display the results**/
                        /*if (title.contains(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                            filteredDocumentsList.add(document);
                        }*/

                        Log.d("Search Word =>", inputText);


                        try {
                            if (title.equals(inputText)||signature.toLowerCase().equals(inputText.toLowerCase()) || signature.equals(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                                filteredDocumentsList.add(document);
                                searchedFilteredDocumentsList.add(document);
                                Log.d("Search List Count ", filteredDocumentsList.size() + "");
                            } else {

                            }
                        } catch (Exception ex) {
                            Log.e("Exception: SearchProcessedDocumentsFragment-279 ", ex.toString());
                        }
                    }
                    Log.d("Search Filter List ", new Gson().toJson(filteredDocumentsList));

                } else {
                    filteredDocumentsList.clear();
                    filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) processedDocumentsList.clone();
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredDocumentsList.size() == 0) {
                            noResultsView.setVisibility(View.VISIBLE);
                            downloadButton.setEnabled(false);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.grey_bg_rounded_corners));
                        } else {
                            downloadButton.setEnabled(true);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.blue_bg_rounded_corners));
                            noResultsView.setVisibility(View.GONE);
                        }
                        /**Mark:- Update the Adapter with Result**/
                        /*if (searchedFilteredDocumentsList.size() > 0) {
                            adapter.updateDataList(searchedFilteredDocumentsList);
                            adapter.notifyDataSetChanged();
                            noResultsView.setVisibility(View.GONE);
                        } else {*/
                            adapter.updateDataList(filteredDocumentsList);
                            adapter.notifyDataSetChanged();
//                        }


                    }
                });

            }
        });

    }

    public void applyFilters(int pos) {
        this.pos = pos;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                /**Mark:- 1.Apply Filter based on search input**/

                if (!inputText.equals("")) {
                    String[] searchWords = inputText.split(" |\n");

                    StringBuilder regexp = new StringBuilder();

                    for (int i = 0; i < searchWords.length; i++) {
                        Log.d("Search Word =>", new Gson().toJson(searchWords));
                        if (i != (searchWords.length - 1)) {
                            regexp.append("(?=.*").append(searchWords[i]).append(")|");
                        } else {
                            regexp.append("(?=.*").append(searchWords[i]).append(")");
                        }
                    }

                    Pattern pattern = Pattern.compile(regexp.toString(), Pattern.CASE_INSENSITIVE);

                    /**Apply Search Logic Here**/
                    filteredDocumentsList.clear();

                    for (int i = 0; i < processedDocumentsList.size(); ++i) {

                        ProcessedDocumentsModel document = processedDocumentsList.get(i);
                        String title = document.getProcessedData().getTitle();
                        String docType = document.getProcessedData().getDocType();
                        String signature = document.getSignature();

                        /**Add Document To Filtered Array to display the results**/
                        /*if (title.contains(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                            filteredDocumentsList.add(document);
                        }*/

                        Log.d("Search Word =>", inputText);
                        try {
                            if (title.equals(inputText) || signature.equals(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                                filteredDocumentsList.add(document);
                                searchedFilteredDocumentsList.add(document);
                                Log.d("Search List Count ", filteredDocumentsList.size() + "");
                            } else {

                            }
                        } catch (Exception ex) {
                            Log.e("Exception: SearchProcessedDocumentsFragment-279 ", ex.toString());
                        }
                    }
                    Log.d("Search Filter List ", new Gson().toJson(filteredDocumentsList));

                } else {
                    filteredDocumentsList.clear();
                    filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) processedDocumentsList.clone();
                }

                /**Mark:- 2.Apply Date Filters**/
                Date fromDate = new Date();
                fromDate.setHours(0);
                fromDate.setMinutes(0);
                fromDate.setSeconds(0);
                Date toDate = new Date();
                toDate.setHours(0);
                toDate.setMinutes(0);
                toDate.setSeconds(0);
                Date docDate = new Date();
                docDate.setHours(0);
                docDate.setMinutes(0);
                docDate.setSeconds(0);
                LocalDate fromLocalDate = null;
                LocalDate toLocalDate = null;
                LocalDate docLocalDate = null;


                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                format.setTimeZone(TimeZone.getTimeZone("UTC"));

                if (selectedPeriod == 4) {
                    fromDate = fromCalendar.getTime();
                    toDate = toCalender.getTime();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            datesSelected.setText(Utilities.getFormattedDate(fromCalendar) + " - " + Utilities.getFormattedDate(toCalender));
                            datesSelected.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    try {
                        fromDate = format.parse(Utilities.getStartDate(selectedPeriod));
//                        toDate = format.parse(Utilities.getEndDate());
                        toDate = format.parse(Utilities.getEndDateReport(pos));
                    } catch (Exception e) {
                        Log.d("FILTER DATE EXCEPTION", "e: " + e.getStackTrace());
                    }
                }

                ArrayList<ProcessedDocumentsModel> periodFilteredDocumentList = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "fromLocalDate " + fromLocalDate);
                    toLocalDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "toLocalDate " + toLocalDate);

                }
                for (ProcessedDocumentsModel document : filteredDocumentsList) {
                    Log.d("Document ", new Gson().toJson(filteredDocumentsList));
                    DateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
//                    dF.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        docDate = dF.parse(document.getProcessedData().getDocDate());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            docLocalDate = docDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            Log.d("LocalDate ", "docLocalDate " + docLocalDate);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }


                    /*Calendar fromCal = Calendar.getInstance();
                    fromCal.setTime(fromDate);
//                    fromCal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    fromCal.set(Calendar.HOUR, 0);
                    fromCal.set(Calendar.MINUTE, 0);
                    fromCal.set(Calendar.SECOND, 0);
                    fromCal.set(Calendar.HOUR_OF_DAY, 0);
                    fromDate = fromCal.getTime();
                    Log.d("After From Date", fromDate + "");


                    Calendar toCal = Calendar.getInstance();
//                    toCal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    toCal.setTime(toDate);
                    toCal.set(Calendar.HOUR, 0);
                    toCal.set(Calendar.MINUTE, 0);
                    toCal.set(Calendar.SECOND, 0);
                    toCal.set(Calendar.HOUR_OF_DAY, 0);
                    toDate = toCal.getTime();
                    Log.d("After To Date", toDate + "");


                    Calendar docCal = Calendar.getInstance();
//                    docCal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    docCal.setTime(docDate);
                    docCal.set(Calendar.HOUR, 0);
                    docCal.set(Calendar.MINUTE, 0);
                    docCal.set(Calendar.SECOND, 0);
                    docCal.set(Calendar.HOUR_OF_DAY, 0);
                    docDate = docCal.getTime();
                    Log.d("After Doc Date", docDate + "");*/


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (fromLocalDate.compareTo(docLocalDate) <= 0 && toLocalDate.compareTo(docLocalDate) >= 0) {
                            System.out.println("Date List ======>" + fromLocalDate + " to date " + toLocalDate + " docDate " + docLocalDate);
                            periodFilteredDocumentList.add(document);
                        }
                    }
//                    fromDate = new Date(2022,8,12);


//                    Log.d("New From date ", "" +fromLocalDate);
//                    Log.d("New docDate date ", "" +docLocalDate);
//                    toDate = new Date(2022,8,11);

                    if (fromDate.after(docDate)) {
                        Log.d("New From date ", "is after to date");
                    }
                    if (fromDate.before(docDate)) {
                        Log.d("New From date ", "is before to date");
                    }
                    if (fromDate.equals(docDate)) {
                        Log.d("New From date ", "is equal to date");
                    }
                    if (toDate.after(docDate)) {
                        Log.d("New To date ", "is after to date");
                    }
                    if (toDate.before(docDate)) {
                        Log.d("New To date ", "is before to date");
                    }
                    if (toDate.equals(docDate)) {
                        Log.d("New To date ", "is equal to date");
                    }
                    if (fromDate.after(docDate) && toDate.before(docDate) && fromDate.equals(docDate)) {

                    }

                }
                System.out.println("Filter List ======>" + new Gson().toJson(periodFilteredDocumentList));
                Log.d("Hey ", "Size " + periodFilteredDocumentList.size());

                filteredDocumentsList.clear();
                filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) periodFilteredDocumentList.clone();


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredDocumentsList.size() == 0) {
                            noResultsView.setVisibility(View.VISIBLE);
                            downloadButton.setEnabled(false);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.grey_bg_rounded_corners));
                        } else {
                            downloadButton.setEnabled(true);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.blue_bg_rounded_corners));
                            noResultsView.setVisibility(View.GONE);
                        }
                        /**Mark:- Update the Adapter with Result**/
                        if (searchedFilteredDocumentsList.size() > 0) {
                            adapter.updateDataList(searchedFilteredDocumentsList);
                            adapter.notifyDataSetChanged();
                            noResultsView.setVisibility(View.GONE);
                        } else {
                            adapter.updateDataList(filteredDocumentsList);
                            adapter.notifyDataSetChanged();
                        }


                    }
                });

            }
        });


    }

    public static String compareTwoDates(LocalDate startDate, LocalDate endDate) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (startDate.isBefore(endDate)) {
                return "Start date [" + startDate + "] is before end date [" + endDate + "]";
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (startDate.isAfter(endDate)) {
                return "Start date [" + endDate + "] is after end date [" + endDate + "]";
            }
        }

        return "Start date [" + startDate + "] and end date [" + endDate + "] are equal";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customProgressLoader = new CustomProgressLoader(getContext());
        setPeriodSelectorSpinner();
    }

    @Override
    public void onAttach(Context context) {
        mainContext = context;
        super.onAttach(context);
        try {
            if (context instanceof SearchProcessedDocumentsFragmentListener) {
                listener = (SearchProcessedDocumentsFragmentListener) context;
                downloadAction = true;
            }
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
            throw new RuntimeException(context.toString() + "must implement SearchProcessedDocumentsFragmentListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setRecyclerAdapter() {
        processedDocumentsList.clear();
        adapter = new ProcessedDocumentsAdapter(context, R.layout.layout_processed_document_item, SearchProcessedDocumentsFragment.this);
//        processedDocumentsList.addAll(Duke.fileStatusModel.getAllProcessedDocuments());
        for (ProcessedDocumentsModel doc : Duke.fileStatusModel.getAllProcessedDocuments()) {
            if (!doc.getProcessedData().getTitle().equals("Load Document")) {
                processedDocumentsList.add(doc);
            }
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);
        if (processedDocumentsList.size() <= 0) {
            downloadButton.setEnabled(false);
            downloadButton.setAlpha((float) 0.3);
            return;
        }
        /**initialize FilteredDocumentsList with processedDocumentsList**/
        applyFilters(pos);
    }

    private void setPeriodSelectorSpinner() {
        periodSpinnerDataModel.clear();
        periodSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_week)));
        periodSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_month)));
        periodSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.last_quarter)));
        periodSpinnerDataModel.add(new ReportSpinnerDataModel(getResources().getString(R.string.year_to_date)));
        periodSpinnerDataModel.add(new ReportSpinnerDataModel("Custom"));

        periodSpinnerAdapter = new ReportSpinnerAdapter(context, R.layout.report_spinner_item, periodSpinnerDataModel);
        periodSpinner.setAdapter(periodSpinnerAdapter);
        periodSpinner.setSelection(selectedPeriod); //YTD

        final AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPeriod = i;

                if (downloadAction != false) {
                    curCount = 1;
                    onDownloadButtonClicked();
                } else {
                    if (selectedPeriod == 4) {
                        if (periodSelectorAlert == null || !periodSelectorAlert.isShowing()) {
                            if (processedDocumentsList.size() > 0) {
                                showCustomPeriodSelectorDialog();
                            }
                        }
                    } else {
                        pos = i;
                        if (pos == 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                applyFiltersLastMonth(pos);
                            }
                            datesSelected.setVisibility(View.INVISIBLE);
                        } else if (pos == 2) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                applyFiltersQuater(pos);
                            }
                            datesSelected.setVisibility(View.INVISIBLE);
                        } else {
                            applyFilters(pos);
                            datesSelected.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("&*&**&*&*&*&*&*&", "onNothingSelected: ");
            }
        };
//
//        periodSpinner.setOnItemSelectedListener(itemSelectedListener);
        periodSpinner.post(new Runnable() {
            public void run() {
                periodSpinner.setOnItemSelectedListener(itemSelectedListener);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void applyFiltersLastMonth(int pos) {


        SimpleDateFormat shortSdf = new SimpleDateFormat(
                "yyyy-MM-dd");
        SimpleDateFormat longSdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH);
        c.add(Calendar.MONTH, -1);

        Log.d("Current Month ", " " + c.getTime());

        Date fromDateQuater = null;

        try {
            c.set(Calendar.DATE, 1);
            fromDateQuater = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Log.d("Last Month Before ", currentMonth + "");

        Date toDateQuater = null;
//        currentMonth = currentMonth + 2;
        Log.d("Last Month After", currentMonth + "");
        try {
            c1.add(Calendar.MONTH, -1);
            c1.set(Calendar.DAY_OF_MONTH, c1.getActualMaximum(Calendar.DAY_OF_MONTH));
            toDateQuater = longSdf.parse(shortSdf.format(c1.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Current Quater ", fromDateQuater + "");
        Log.d("Current Quater Last", toDateQuater + "");


        Date finalFromDateQuater = fromDateQuater;
        Date finalToDateQuater = toDateQuater;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                /**Mark:- 1.Apply Filter based on search input**/

                if (!inputText.equals("")) {
                    String[] searchWords = inputText.split(" |\n");

                    StringBuilder regexp = new StringBuilder();

                    for (int i = 0; i < searchWords.length; i++) {
                        if (i != (searchWords.length - 1))
                            regexp.append("(?=.*").append(searchWords[i]).append(")|");
                        else
                            regexp.append("(?=.*").append(searchWords[i]).append(")");
                    }

                    Pattern pattern = Pattern.compile(regexp.toString(), Pattern.CASE_INSENSITIVE);

                    /**Apply Search Logic Here**/
                    filteredDocumentsList.clear();

                    for (int i = 0; i < processedDocumentsList.size(); ++i) {

                        ProcessedDocumentsModel document = processedDocumentsList.get(i);
                        String title = document.getProcessedData().getTitle();
                        String docType = document.getProcessedData().getDocType();
                        String signature = document.getSignature();

                        /**Add Document To Filtered Array to display the results**/
                       /* if (title.contains(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                            filteredDocumentsList.add(document);
                        }*/
                        try {
                            if (title.equals(inputText) || signature.equals(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                                filteredDocumentsList.add(document);
                            }
                        } catch (Exception ex) {
                            Log.e("Exception: SearchProcessedDocumentsFragment-667 ", ex.toString());
                        }
                    }
                } else {
                    filteredDocumentsList.clear();
                    filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) processedDocumentsList.clone();
                }

                /**Mark:- 2.Apply Date Filters**/
                Date fromDate = new Date();
                fromDate.setHours(0);
                fromDate.setMinutes(0);
                fromDate.setSeconds(0);
                Date toDate = new Date();
                toDate.setHours(0);
                toDate.setMinutes(0);
                toDate.setSeconds(0);
                Date docDate = new Date();
                docDate.setHours(0);
                docDate.setMinutes(0);
                docDate.setSeconds(0);
                LocalDate fromLocalDate = null;
                LocalDate toLocalDate = null;
                LocalDate docLocalDate = null;


                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                format.setTimeZone(TimeZone.getTimeZone("UTC"));

                if (selectedPeriod == 4) {
                    fromDate = fromCalendar.getTime();
                    toDate = toCalender.getTime();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            datesSelected.setText(Utilities.getFormattedDate(fromCalendar) + " - " + Utilities.getFormattedDate(toCalender));
                            datesSelected.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    try {
                        fromDate = format.parse(Utilities.getStartDate(selectedPeriod));
//                        toDate = format.parse(Utilities.getEndDate());
                        toDate = format.parse(Utilities.getEndDateReport(pos));
                    } catch (Exception e) {
                        Log.d("FILTER DATE EXCEPTION", "e: " + e.getStackTrace());
                    }
                }

                ArrayList<ProcessedDocumentsModel> periodFilteredDocumentList = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fromLocalDate = finalFromDateQuater.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "fromLocalDateQuater " + fromLocalDate);
                    toLocalDate = finalToDateQuater.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "toLocalDate " + toLocalDate);

                }
                for (ProcessedDocumentsModel document : filteredDocumentsList) {
                    Log.d("Document ", new Gson().toJson(filteredDocumentsList));
                    DateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
//                    dF.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        docDate = dF.parse(document.getProcessedData().getDocDate());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            docLocalDate = docDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            Log.d("LocalDate ", "docLocalDate " + docLocalDate);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (fromLocalDate.compareTo(docLocalDate) <= 0 && toLocalDate.compareTo(docLocalDate) >= 0) {
                            System.out.println("Date List ======>" + fromLocalDate + " to date " + toLocalDate + " docDate " + docLocalDate);
                            periodFilteredDocumentList.add(document);
                        }
                    }

                }
                System.out.println("Filter List ======>" + new Gson().toJson(periodFilteredDocumentList));
                Log.d("Hey ", "Size " + periodFilteredDocumentList.size());

                filteredDocumentsList.clear();
                filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) periodFilteredDocumentList.clone();


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredDocumentsList.size() == 0) {
                            noResultsView.setVisibility(View.VISIBLE);
                            downloadButton.setEnabled(false);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.grey_bg_rounded_corners));
                        } else {
                            downloadButton.setEnabled(true);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.blue_bg_rounded_corners));
                            noResultsView.setVisibility(View.GONE);
                        }
                        /**Mark:- Update the Adapter with Result**/
                        adapter.updateDataList(filteredDocumentsList);
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void applyFiltersQuater(int pos) {
        SimpleDateFormat shortSdf = new SimpleDateFormat(
                "yyyy-MM-dd");
        SimpleDateFormat longSdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH);

        if (currentMonth == 1 || currentMonth == 2 || currentMonth == 3) {
            currentMonth = 1;
        } else if (currentMonth == 4 || currentMonth == 5 || currentMonth == 6) {
            currentMonth = 4;
        } else if (currentMonth == 7 || currentMonth == 8 || currentMonth == 9) {
            currentMonth = 7;
        } else if (currentMonth == 10 || currentMonth == 11 || currentMonth == 12) {
            currentMonth = 10;
        }

        currentMonth = currentMonth - 3;
        Log.d("Current Month ", " " + currentMonth);
        Date fromDateQuater = null;/*w  w  w  .  j a va 2 s . c  om*/
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                c.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                c.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9)
                c.set(Calendar.MONTH, 4);
            else if (currentMonth >= 10 && currentMonth <= 12)
                c.set(Calendar.MONTH, 9);
            c.set(Calendar.DATE, 1);
            fromDateQuater = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Last Month Before ", currentMonth + "");

        Date toDateQuater = null;/*w  w  w  .  j a va 2 s . c  om*/
        currentMonth = currentMonth + 2;
        Log.d("Last Month After", currentMonth + "");
        try {
            c1.set(Calendar.MONTH, currentMonth - 1);
            c1.set(Calendar.DAY_OF_MONTH, c1.getActualMaximum(Calendar.DAY_OF_MONTH));
            toDateQuater = longSdf.parse(shortSdf.format(c1.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Current Quater ", fromDateQuater + "");
        Log.d("Current Quater Last", toDateQuater + "");


        Date finalFromDateQuater = fromDateQuater;
        Date finalToDateQuater = toDateQuater;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                /**Mark:- 1.Apply Filter based on search input**/

                if (!inputText.equals("")) {
                    String[] searchWords = inputText.split(" |\n");

                    StringBuilder regexp = new StringBuilder();

                    for (int i = 0; i < searchWords.length; i++) {
                        if (i != (searchWords.length - 1))
                            regexp.append("(?=.*").append(searchWords[i]).append(")|");
                        else
                            regexp.append("(?=.*").append(searchWords[i]).append(")");
                    }

                    Pattern pattern = Pattern.compile(regexp.toString(), Pattern.CASE_INSENSITIVE);

                    /**Apply Search Logic Here**/
                    filteredDocumentsList.clear();

                    for (int i = 0; i < processedDocumentsList.size(); ++i) {

                        ProcessedDocumentsModel document = processedDocumentsList.get(i);
                        String title = document.getProcessedData().getTitle();
                        String docType = document.getProcessedData().getDocType();
                        String signature = document.getSignature();

                        /**Add Document To Filtered Array to display the results**/
                       /* if (title.contains(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                            filteredDocumentsList.add(document);
                        }*/
                        try {
                            if (title.equals(inputText) || signature.equals(inputText) || docType.contains(inputText) || pattern.matcher(title).find() || pattern.matcher(docType).find()) {
                                filteredDocumentsList.add(document);
                            }
                        } catch (Exception ex) {
                            Log.e("Exception: SearchProcessedDocumentsFragment-873 ", ex.toString());
                        }
                    }
                } else {
                    filteredDocumentsList.clear();
                    filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) processedDocumentsList.clone();
                }

                /**Mark:- 2.Apply Date Filters**/
                Date fromDate = new Date();
                fromDate.setHours(0);
                fromDate.setMinutes(0);
                fromDate.setSeconds(0);
                Date toDate = new Date();
                toDate.setHours(0);
                toDate.setMinutes(0);
                toDate.setSeconds(0);
                Date docDate = new Date();
                docDate.setHours(0);
                docDate.setMinutes(0);
                docDate.setSeconds(0);
                LocalDate fromLocalDate = null;
                LocalDate toLocalDate = null;
                LocalDate docLocalDate = null;


                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                format.setTimeZone(TimeZone.getTimeZone("UTC"));

                if (selectedPeriod == 4) {
                    fromDate = fromCalendar.getTime();
                    toDate = toCalender.getTime();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            datesSelected.setText(Utilities.getFormattedDate(fromCalendar) + " - " + Utilities.getFormattedDate(toCalender));
                            datesSelected.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    try {
                        fromDate = format.parse(Utilities.getStartDate(selectedPeriod));
//                        toDate = format.parse(Utilities.getEndDate());
                        toDate = format.parse(Utilities.getEndDateReport(pos));
                    } catch (Exception e) {
                        Log.d("FILTER DATE EXCEPTION", "e: " + e.getStackTrace());
                    }
                }

                ArrayList<ProcessedDocumentsModel> periodFilteredDocumentList = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fromLocalDate = finalFromDateQuater.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "fromLocalDateQuater " + fromLocalDate);
                    toLocalDate = finalToDateQuater.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Log.d("LocalDate ", "toLocalDate " + toLocalDate);

                }
                for (ProcessedDocumentsModel document : filteredDocumentsList) {
                    Log.d("Document ", new Gson().toJson(filteredDocumentsList));
                    DateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
//                    dF.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        docDate = dF.parse(document.getProcessedData().getDocDate());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            docLocalDate = docDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            Log.d("LocalDate ", "docLocalDate " + docLocalDate);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (fromLocalDate.compareTo(docLocalDate) <= 0 && toLocalDate.compareTo(docLocalDate) >= 0) {
                            System.out.println("Date List ======>" + fromLocalDate + " to date " + toLocalDate + " docDate " + docLocalDate);
                            periodFilteredDocumentList.add(document);
                        }
                    }

                }
                System.out.println("Filter List ======>" + new Gson().toJson(periodFilteredDocumentList));
                Log.d("Hey ", "Size " + periodFilteredDocumentList.size());

                filteredDocumentsList.clear();
                filteredDocumentsList = (ArrayList<ProcessedDocumentsModel>) periodFilteredDocumentList.clone();


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (filteredDocumentsList.size() == 0) {
                            noResultsView.setVisibility(View.VISIBLE);
                            downloadButton.setEnabled(false);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.grey_bg_rounded_corners));
                        } else {
                            downloadButton.setEnabled(true);
                            downloadButton.setBackground(getResources().getDrawable(R.drawable.blue_bg_rounded_corners));
                            noResultsView.setVisibility(View.GONE);
                        }
                        /**Mark:- Update the Adapter with Result**/
                        adapter.updateDataList(filteredDocumentsList);
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });


    }


    @OnClick(R.id.nav_back)
    public void onBackButtonPressed() {
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.PROCESS);
        NavigationFlowManager.openFragments(new Documents(), args, getActivity(), R.id.dashboard_wrapper);
    }

    @OnClick(R.id.document_download_button)
    public void onDownloadButtonClicked() {

//        if ((getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
//            Bundle params = new Bundle();
//            params.putString("Type", "download");
//            mFirebaseAnalytics.logEvent("DocumentViewSelection", params);
//            downloadImages();
//        } else {
//            Dexter.withActivity(getActivity())
//                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    .withListener(new MultiplePermissionsListener() {
//                        @Override
//                        public void onPermissionsChecked(MultiplePermissionsReport report) {
//                            downloadImages();
//                        }
//
//                        @Override
//                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                            token.continuePermissionRequest();
//                        }
//                    }).withErrorListener(new PermissionRequestErrorListener() {
//                @Override
//                public void onError(DexterError error) {
//                    Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_LONG).show();
//                }
//            }).onSameThread()
//                    .check();
//        }

        ArrayList<String> docSHA1s = new ArrayList<>();
        for (final ProcessedDocumentsModel document : filteredDocumentsList) {
            docSHA1s.add(document.getSha1());
        }

        Log.d("sddf", docSHA1s.toString());

        //Transmit Processed Docs API
        customProgressLoader.showDialog();
        loadsViewModel.getTransmitProcessedDocsLiveData(docSHA1s).observe(this, new Observer<LoadsTransmitModel>() {
            @Override
            public void onChanged(LoadsTransmitModel loadsTransmitModel) {
                customProgressLoader.hideDialog();
                Toast.makeText(getContext(), loadsTransmitModel.message.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void downloadImages() {
        final List<URL> pageList = new ArrayList<>();
        UserDataModel userDataModel = UserConfig.getInstance().getUserDataModel();
        imageNameURLMap = new HashMap<>();

        /**Here we are getting the number of cores of the processor.**/
        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES + 1,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

        folderName = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss").format(new Date());

        if (listener != null) {
            podProccessedDocuments.addAll(Duke.fileStatusModel.getPodProcessedDocuments());
            if (podProccessedDocuments.size() > 0) {
                for (final ProcessedDocumentsModel document : podProccessedDocuments) {
                    for (String page : document.getPages()) {
                        pageList.add(stringToURL(BASE_URL + userDataModel.getUserEmail() + "/" + page));
                        imageNameURLMap.put(page, stringToURL(BASE_URL + userDataModel.getUserEmail() + "/" + page));
                    }
                }
                podProcessedDocAvailable = true;
                listener.onSearchProcessedDocument(folderName, (long) totalCount, false, podProcessedDocAvailable);

                downloadProgressDialog = new DownloadProgressDialog(getContext(), getString(R.string.downloading), "Please wait while POD documents is downloaded", getString(R.string.connecting));
                downloadProgressDialog.showDialog();

                int i = 0;
                for (Map.Entry<String, URL> entry : imageNameURLMap.entrySet()) {
                    executor.execute(new ImageDownloaderThread(i++, new Handler(this), entry.getValue(), entry.getKey(), folderName));
                }

            } else {
                podProcessedDocAvailable = false;
                listener.onSearchProcessedDocument(folderName, (long) totalCount, false, podProcessedDocAvailable);
            }
        } else {
            for (final ProcessedDocumentsModel document : filteredDocumentsList) {
                for (String page : document.getPages()) {
                    pageList.add(stringToURL(BASE_URL + userDataModel.getUserEmail() + "/" + page));
                    imageNameURLMap.put(page, stringToURL(BASE_URL + userDataModel.getUserEmail() + "/" + page));
                }
            }

            downloadProgressDialog = new DownloadProgressDialog(getContext(), getString(R.string.downloading), "Please wait while POD documents is downloaded", getString(R.string.connecting));
            downloadProgressDialog.showDialog();

            int i = 0;
            for (Map.Entry<String, URL> entry : imageNameURLMap.entrySet()) {
                executor.execute(new ImageDownloaderThread(i++, new Handler(this), entry.getValue(), entry.getKey(), folderName));
            }
        }

        totalCount = imageNameURLMap.size();
        curCount = 0;


        pathOfFile = new File(Environment.getExternalStorageDirectory() + "/" + "Duke.AI" + "/" + "POD Documents", folderName);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onListItemClick(int pos, ProcessedDocumentsModel dataModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.StringConstants.PROCESSED_DATA_MODEL, dataModel);
        bundle.putBoolean("IS_FROM_SEARCH", true);
        NavigationFlowManager.openFragments(new ProcessedDocumentsDetailsFragment(), bundle, getActivity(), R.id.dashboard_wrapper, ProcessedFragment.TAG);
    }

    @Override
    public boolean handleMessage(Message msg) {
        curCount++;
        float per = (curCount / (totalCount)) * 100;
        downloadProgressDialog.setProgress((int) per);
        AsyncTask.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (curCount == (totalCount)) {
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (getActivity() != null) {

                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                downloadProgressDialog.dismiss();
                                if (Duke.PODDocumentsPath != "") {
                                    confirmationComponent = new ConfirmationComponent(getContext(), getActivity().getString(R.string.download_successful), getContext().getString(R.string.documents_downloaded_successfully_please_find_them_in_folder) + Duke.PODDocumentsPath + getContext().getString(R.string.folder), true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                                        @Override
                                        public void onPopupActions(String id, int dialogId) {
                                            confirmationComponent.dismiss();
                                        }
                                    }, 1, pathOfFile);
                                } else {
                                    confirmationComponent = new ConfirmationComponent(getContext(), getActivity().getString(R.string.download_successful), getContext().getString(R.string.documents_downloaded_successfully_please_find_them_in_folder) + folderName + getContext().getString(R.string.folder), true, Utilities.getStrings(Duke.getInstance(), R.string.ok), new PopupActions() {
                                        @Override
                                        public void onPopupActions(String id, int dialogId) {
                                            confirmationComponent.dismiss();
                                        }
                                    }, 1, pathOfFile);
                                }
                            }
                        });

                    }
                    if (listener != null) {
                        listener.onSearchProcessedDocument(null, 1, true, true);
                    }
                }
            }
        });
        downloadAction = false;
        return true;
    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showCustomPeriodSelectorDialog() {

        /**Show dialog**/

        periodSelectorAlert = new PeriodSelectorAlert(getContext(), new OnPeriodSubmitListener() {
            @Override
            public void onSubmitClick(Calendar fromCalendarResponse, Calendar toCalendarResponse) {
                periodSelectorAlert.dismiss();
                fromCalendar = fromCalendarResponse;
                toCalender = toCalendarResponse;
                //Apply Filters Now based on the Period selected
                applyFilters(4);

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                periodSelectorAlert.dismiss();
                selectedPeriod = pos;
                if (selectedPeriod != 4)
                    periodSpinner.setSelection(selectedPeriod); //YTD
            }
        });
    }

    private void setCurrentTheme() {
        ChangeThemeModel changeThemeModel = new ChangeThemeModel();
        if (!AppConstants.currentTheme.equals("duke")) {
            datesSelected.setBackgroundColor(getResources().getColor(R.color.TTTSemiWhite));
        } else {
            datesSelected.setBackgroundColor(getResources().getColor(R.color.dukeSemiWhite));
        }
    }

    public interface SearchProcessedDocumentsFragmentListener {
        void onSearchProcessedDocument(String folderName, long size, boolean downloaded, boolean podProcessedDocAvailable);
    }

    private void setSwipeActionCallback() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Take action for the swiped item
                int position = viewHolder.getAdapterPosition();

                loadAdapterCurrentItem = position;
                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        Log.d(Duke.TAG, " swiped");
                        ArrayList<String> docSHA1s = new ArrayList<>();

                        docSHA1s.add(filteredDocumentsList.get(loadAdapterCurrentItem).getSha1());

                        Log.d("sddf", docSHA1s.toString());

                        //Transmit Processed Docs API
                        customProgressLoader.showDialog();
                        loadsViewModel.getTransmitProcessedDocsLiveData(docSHA1s).observe(getActivity(), new Observer<LoadsTransmitModel>() {
                            @Override
                            public void onChanged(LoadsTransmitModel loadsTransmitModel) {
                                customProgressLoader.hideDialog();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), loadsTransmitModel.message.toString(), Toast.LENGTH_LONG).show();

                            }
                        });
                        break;
                    case ItemTouchHelper.RIGHT:
                        break;
                }
            }

            //Add icons for swipe on delete action

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorBlue))
                        .addSwipeLeftActionIcon(R.drawable.ic_mailshare)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
