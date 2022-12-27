package com.dukeai.android.ui.fragments;

import static com.dukeai.android.Duke.DocsOfALoad;
import static com.dukeai.android.Duke.isLoadDocument;
import static com.dukeai.android.ui.fragments.HomeFragment.REQUEST_GPS_FOR_UPLOAD_DOC;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.adapters.CustomRecipientListAdapter;
import com.dukeai.android.adapters.LoadsFilterSpinnerAdapter;
import com.dukeai.android.adapters.LoadsListAdapter;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.interfaces.OnPeriodSubmitListener;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.UploadDocumentInterface;
import com.dukeai.android.models.FileStatusModel;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.LoadsFilterDataModel;
import com.dukeai.android.models.LoadsListModel;
import com.dukeai.android.models.LoadsTransmitModel;
import com.dukeai.android.models.RecipientDataModel;
import com.dukeai.android.models.RecipientsListModel;
import com.dukeai.android.models.SingleLoadModel;
import com.dukeai.android.models.UserDataModel;
import com.dukeai.android.models.UserLoadsModel;
import com.dukeai.android.ui.activities.DashboardActivity;
import com.dukeai.android.ui.activities.PaymentActivity;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.PeriodSelectorAlert;
import com.dukeai.android.utils.UploadDocument;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.dukeai.android.viewModel.LoadsViewModel;
import com.dukeai.android.viewModel.UploadFileViewModel;
import com.dukeai.android.views.CustomHeader;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class LoadsFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback, LoadsListAdapter.OnListItemClickListener, CustomRecipientListAdapter.OnListItemClickListener, PopupActions, DialogInterface.OnDismissListener, HeaderActions, CustomRecipientListAdapter.OnlastItemLoadedListener {

    View loadsView;
    Context context;
    Bundle savedInstance;
    UploadDocument uploadDocument;
    @BindView(R.id.numberPicker)
    NumberPicker numberPicker;
    @BindView(R.id.edit_btn)
    ImageView editBtn;
    @BindView(R.id.recipient_selector)
    LinearLayout customRecipientsEditPanel;
    @BindView(R.id.loads_header)
    CustomHeader customHeader;
    @BindView(R.id.no_loads_for_selected_period)
    TextView noLoadsForThisPeriod;
    //    @BindView(R.id.transmit)
//    Button transmitLoad;
    LoadsFilterSpinnerAdapter loadsFilterSpinnerAdapter;
    FusedLocationProviderClient fusedLocationProviderClient;
    PeriodSelectorAlert periodSelectorAlert;
    CustomProgressLoader customProgressLoader;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalender = Calendar.getInstance();
    int pos = 3;
    int customIndex = 0;
    boolean ntgSelected = false;
    String tagName = "";
    int currentSpinnerValue = 3;
    TextView datesSelected;
    Spinner spinner;
    //    DasSpinner spinner;
    TextView addRecipient;
    TextView addRecipientText;
    String address = "";
    //Loads list related variable
    ArrayList<UserLoadsModel> loadsListModels = new ArrayList<>();
    ArrayList<UserLoadsModel> filteredDocumentsList = new ArrayList<>();
    ArrayList<RecipientDataModel> recipientDataModels = new ArrayList<>();
    ArrayList<String> transmitToTheseUsers = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView customRecipientsListRecyclerView;
    //    RecyclerView.Adapter adapter;
    LoadsListAdapter adapter;
    CustomRecipientListAdapter customRecipientListAdapter;
    RecyclerView.LayoutManager layoutManager;
    //document picker related variables
    ArrayList<String> pdfURIs = new ArrayList<>();
    ArrayList<Uri> uriList = new ArrayList<>();
    ArrayList<Uri> imageUriList = new ArrayList<>();
    ArrayList<String> imageURIs = new ArrayList<>();
    Uri uri2;
    //Add recipient dialog related variables
    boolean[] selectedRecipients;
    ArrayList<Integer> recipientList = new ArrayList<>();
    String[] availableRecipients2 = {
            "sankalpvk18@gmail.com",
            "marcus@duke.ai",
            "alex@duke.ai",
            "sankalp@divami.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com",
            "sankalpvk18@gmail.com"
    };
    String[] availableRecipients;
    //    String[] selectedCustomRecipients;
    ArrayList<RecipientDataModel> selectedCustomRecipients = new ArrayList<>();
    int selectedPeriod = 3;
    DashboardActivity dashboardActivity;
    UploadDocumentInterface uploadDocumentInterface;
    int loadAdapterCurrentItem = -1;
    ArrayList<LoadsFilterDataModel> loadsFilterDataModels = new ArrayList<>();
    private static final int DELETE_LOAD_DIALOG_ID = 18;
    LoadsViewModel loadsViewModel;
    UploadFileViewModel uploadFileViewModel;
    FileStatusViewModel fileStatusViewModel;
    UserConfig userConfig = UserConfig.getInstance();
    Date fromDate = null;
    Date toDate = null;
    String sortBy = "";
    View snapView;
    int snapPosition = 0;
    @BindView(R.id.custom_recipients_recycler_view_wrapper)
    ConstraintLayout editPanelWrapper;
    boolean isCustomRecipientAlreadySelected = false;
    TextView editPanelTitle;
    @BindView(R.id.edit_icon)
    ImageView editIcon;
    private String message;
//    private SelectRecipientsDialog.OnDismissListener dismissListener;

    public LoadsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadsView = inflater.inflate(R.layout.fragment_loads, container, false);
        ButterKnife.bind(this, loadsView);
        Duke.loadsFragment = new LoadsFragment();
        spinner = loadsView.findViewById(R.id.loads_filter_spinner);
        datesSelected = loadsView.findViewById(R.id.datesSelected);
        addRecipient = loadsView.findViewById(R.id.select_recipient);
        addRecipientText = loadsView.findViewById(R.id.add_recipient_text);
        recyclerView = loadsView.findViewById(R.id.loads_recycler_view);
        customRecipientsListRecyclerView = loadsView.findViewById(R.id.custom_recipients_recycler_view);
        editPanelTitle = loadsView.findViewById(R.id.edit_recipient_panel_title);

        loadsViewModel = ViewModelProviders.of(this).get(LoadsViewModel.class);
        uploadFileViewModel = ViewModelProviders.of(this).get(UploadFileViewModel.class);
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);

        loadsView.findViewById(R.id.create_load_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDocumentPanel();
            }
        });
        addRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openRecipientSelectionDialog();
                openSelectRecipientDialog();
            }
        });
        addRecipientText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(AppConstants.StringConstants.ACTION, AppConstants.StringConstants.ACTION_TYPE_ADD);
                NavigationFlowManager.openFragments(new AddRecipientFragment(), bundle, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.LOADS);
            }
        });
        setInitials();

        datesSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomPeriodSelectorDialog();
            }
        });
        return loadsView;
    }

    private void openSelectRecipientDialog() {
        SelectRecipientsDialog selectRecipientsDialog = new SelectRecipientsDialog();
//        selectRecipientsDialog.setOnDismissListener(dismissListener);
        selectRecipientsDialog.setCancelable(false);
        selectRecipientsDialog.show(getFragmentManager(), "Select Recipients Dialog");
        selectRecipientsDialog.setDismissListener(new SelectRecipientsDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isCanceled) {
                if (!isCanceled) {
                    customProgressLoader.showDialog();
                    afterDialogDismissed();
                }
            }
        });
    }

    private void openRecipientSelectionDialog() {
        selectedRecipients = new boolean[availableRecipients.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Recipients");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(availableRecipients, selectedRecipients, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    recipientList.add(which);
                    Collections.sort(recipientList);
                } else {
                    for (int i = 0; i < recipientList.size(); i++) {
                        if (recipientList.get(i) == which) {
                            recipientList.remove(i);
                            break;
                        }
                    }
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (recipientList.size() > 0) {
                    addRecipient.setText(recipientList.size() + " Recipients Selected");
                    dialog.dismiss();
                } else {
                    //Show toast
                    Toast.makeText(getContext(), "when you see this message, the dialog should stay open", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < selectedRecipients.length; i++) {
                    selectedRecipients[i] = false;
                    recipientList.clear();
                }
            }
        });

        builder.show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCustomHeader();
        setPeriodSelectorSpinner();
        Bundle args = getArguments();
        if (args != null && args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD) != null) {
//            Toast.makeText(getContext(), args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD), Toast.LENGTH_LONG).show();
        }
    }

    private void setCustomHeader() {
        customHeader.setToolbarTitle("Loads");
        customHeader.showHideProfileImage(View.VISIBLE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
        if (Duke.ProfileImage != null) {
            customHeader.setHeaderImage(Duke.ProfileImage);
        }
    }

    private void setInitials() {
        customProgressLoader = new CustomProgressLoader(getContext());
        customProgressLoader.showDialog();
        //Clears the list to prevent any unintentional operation on them
        Duke.customRecipientsList.clear();
        Duke.globalRecipientsList.clear();
        //Resets the flag to smoothen the flow of load or normal upload docs
        Duke.isDocumentAddingToLoad = false;
        Duke.isNewLoadBeingCreated = false;
        Duke.PDFDocFilenames.clear();
        //To avoid trail of un-uploaded doc
        Duke.sortedUploadingImageStoragePaths.clear();
        Duke.uploadingImagesList.clear();
        Duke.imageStoragePath = "";
        Duke.uploadingImageStoragePaths.clear();
        Duke.PDFDocFilenames.clear();
        Duke.PDFDocURIs.clear();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        setToolbarColor();
        setLoadsList("last_year");
        setSwipeActionCallback();
        getRecipientsData();
        fetchLatestStatus();

        Bundle args = getArguments();
        if (args != null) {
            if (args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD) != null) {
                Toast.makeText(getContext(), args.getString(AppConstants.StringConstants.ADD_DOC_TO_LOAD), Toast.LENGTH_LONG).show();
            } else if (args.get("upload_status") != null) {
                Duke.PDFDocURIs.clear();
                Toast.makeText(getContext(), getArguments().get("upload_status").toString(), Toast.LENGTH_LONG).show();
            }
        }

       /* if(getArguments() != null) {
            if(getArguments().get("upload_status") != null) {
                Duke.PDFDocURIs.clear();
                Toast.makeText(getContext(), getArguments().get("upload_status").toString(), Toast.LENGTH_LONG).show();
            }
        }*/
//        customProgressLoader.hideDialog();
//        setCustomRecipientList();
    }

    private void fetchLatestStatus() {
        customProgressLoader.showDialog();
        fileStatusViewModel.getFileStatusModelLiveData("").observe(this, new Observer<FileStatusModel>() {
            @Override
            public void onChanged(FileStatusModel fileStatusModel) {
                if (fileStatusModel != null && fileStatusModel.getMessage() == null) {
                    Duke.fileStatusModel = fileStatusModel;
                } else {
                    String message = "";
                    if (fileStatusModel != null) {
                        // Firebase: Send Error information
                        UserDataModel userDataModel;
                        userDataModel = userConfig.getUserDataModel();
                        Bundle params = new Bundle();
                        params.putString("Page", "Home");
                        params.putString("Description", "Not a member");
                        params.putString("UserEmail", userDataModel.getUserEmail());
                        if (fileStatusModel.getMessage().contains(AppConstants.HomePageConstants.NO_GROUPS)) {
                            message = getString(R.string.not_part_of_any_group);
                        } else {
                            message = fileStatusModel.getMessage();
                        }
                        confirmationComponent = new ConfirmationComponent(getContext(), getResources().getString(R.string.error), message, false, getResources().getString(R.string.ok), popupActions, 1);
                    }
                }
            }
        });
    }

    private void setUserAddedRecipients() {
        String[] months3char = availableRecipients;
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(availableRecipients.length - 1);// important
        numberPicker.setDisplayedValues(months3char);
        numberPicker.setWrapSelectorWheel(true);
//        customRecipientsEditPanel.setVisibility(View.VISIBLE);// custom values
//        Duke.selectedRecipients.clear();
        customProgressLoader.hideDialog();
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorTransparent));
    }

    private void getRecipientsData() {
        customProgressLoader.showDialog();
        loadsViewModel.getRecipientsList("all").observe(this, new Observer<RecipientsListModel>() {
            @Override
            public void onChanged(RecipientsListModel recipientsListModel) {
                recipientDataModels = recipientsListModel.recipientsList;
//                availableRecipients = new String[recipientsListModel.recipientsList.size()];
                for (int i = 0; i < recipientsListModel.recipientsList.size(); i++) {
//                    availableRecipients[i] = recipientsListModel.recipientsList.get(i).getEmail();
                    if (recipientsListModel.getRecipientsList().get(i).getIsCustomRecipient().toLowerCase().equals("true")) {
                        Duke.customRecipientsList.add(recipientsListModel.getRecipientsList().get(i));
                    } else {
                        Duke.globalRecipientsList.add(recipientsListModel.getRecipientsList().get(i));
                    }
                }
                if (Duke.selectedRecipients.size() > 0) {
                    afterDialogDismissed();
                } else {
                    customProgressLoader.hideDialog();
                }
//                customProgressLoader.hideDialog();
//                setCustomRecipientList();
                //setUserAddedRecipients();
            }
        });
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
//                        if(position==3) {
//                            showDeleteConfirmationAlert();
//                        } else {
//                            loadsListModels.remove(position);
//                            adapter.notifyItemRemoved(position);
//                        }
                        showDeleteConfirmationAlert(position);
                        break;
                    case ItemTouchHelper.RIGHT:
                        break;
                }
            }

            //Add icons for swipe on delete action

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorRed))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete_icon_white)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setLoadsList(String filterType) {
        customProgressLoader.showDialog();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sort_by", filterType);
        loadsViewModel.getLoadsListLiveData(jsonObject).observe(this, new Observer<LoadsListModel>() {
            @Override
            public void onChanged(LoadsListModel loadsListModel) {
                try {
                    if (loadsListModel.getRecipientsList() != null) {
                        Log.d("data", loadsListModel.status);
                        int count = 0;
//                    *************REMOVE THIS AFTER TEST***************
                        //Code to limit only 8 load docs
//                    for(UserLoadsModel userLoadsModel: loadsListModel.userLoadsModels) {
//                        if(count<8) {
//                            loadsListModels.add(userLoadsModel);
//                            count++;
//                        }
//                    }
//                    *************REMOVE THIS AFTER TEST***************
                        loadsListModels = loadsListModel.userLoadsModels;
                        Duke.loadsDocuments = loadsListModel.userLoadsModels;
                        layoutManager = new LinearLayoutManager(getContext());
                        recyclerView.setHasFixedSize(true);
                        adapter = new LoadsListAdapter(loadsListModels, LoadsFragment.this);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
//                    applyFilters(pos);

                        if (loadsListModels.size() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            noLoadsForThisPeriod.setVisibility(View.INVISIBLE);
                            customProgressLoader.hideDialog();
                        } else {
                            recyclerView.setVisibility(View.INVISIBLE);
                            noLoadsForThisPeriod.setVisibility(View.VISIBLE);
                            customProgressLoader.hideDialog();
                        }
                    }
                } catch (Exception exception) {
                    Log.e("LoadFragment: Exception-579 ", String.valueOf(exception));

                }
//                customProgressLoader.hideDialog();
            }
        });
    }

    private void setCustomRecipientList() {
        layoutManager = new LinearLayoutManager(getContext());
        customRecipientsListRecyclerView.setHasFixedSize(true);
        customRecipientListAdapter = new CustomRecipientListAdapter(selectedCustomRecipients, this, this);
        SnapHelper snapHelper = new PagerSnapHelper();
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        customRecipientsListRecyclerView.setLayoutManager(layoutManager);
        linearSnapHelper.attachToRecyclerView(customRecipientsListRecyclerView);
        customRecipientsListRecyclerView.setAdapter(customRecipientListAdapter);
//        customRecipientsListRecyclerView.scrollToPosition(Integer.MAX_VALUE / 2);
//        customRecipientsListRecyclerView.smoothScrollToPosition(Integer.MAX_VALUE / 2);
//        if(selectedCustomRecipients.size()>1) {
//            customRecipientsListRecyclerView.scrollToPosition(Integer.MAX_VALUE / 2);
////            customRecipientsListRecyclerView.smoothScrollToPosition(1);
//        } else {
////            customRecipientsListRecyclerView.hasFixedSize();
////            customRecipientsListRecyclerView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
////            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
////                    ViewGroup.LayoutParams.MATCH_PARENT,
////                    ViewGroup.LayoutParams.WRAP_CONTENT
////            );
////            params.setMargins(0, 100, 0, 0);
////            params.height = 200;
////            customRecipientsListRecyclerView.setLayoutParams(params);
//            RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
////            param.width = 200; param.leftMargin = 100;
////            param.topMargin = 100;
//            param.height = 160;
////            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
////            params.setMargins(0,0,0,40);
////            editPanelTitle.setLayoutParams(params);
//            editIcon.setVisibility(View.INVISIBLE);
//            customRecipientsListRecyclerView.smoothScrollToPosition(0);
//            customRecipientsListRecyclerView.setLayoutParams(param);
//        }

//        if(selectedCustomRecipients.size() == 1) {
//            RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
//            param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_40);  //working-1
//            editIcon.setVisibility(View.INVISIBLE);
//            customRecipientsListRecyclerView.smoothScrollToPosition(0);
//            customRecipientsListRecyclerView.setLayoutParams(param);    //working-1
//        } else if (selectedCustomRecipients.size() == 2) {
//            RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
//            param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_60);
//            editIcon.setVisibility(View.INVISIBLE);
//            customRecipientsListRecyclerView.smoothScrollToPosition(0);
//            customRecipientsListRecyclerView.setLayoutParams(param);
//        } else {
//            RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
//            param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_100);
//            customRecipientsListRecyclerView.scrollToPosition(Integer.MAX_VALUE / 2);
//        }
        customRecipientsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                snapView = snapHelper.findSnapView(layoutManager);
//                snapView.findViewById(R.id.edit_icon).setVisibility(View.VISIBLE);
                snapPosition = layoutManager.getPosition(snapView);
                Log.d("sdf", String.valueOf(snapPosition));
            }
        });
        editPanelWrapper.setVisibility(View.VISIBLE);
        customProgressLoader.hideDialog();
    }

    private void showDeleteConfirmationAlert(int pos) {
        confirmationComponent = new ConfirmationComponent(getContext(), getString(R.string.delete_load), getString(R.string.delete_load_msg), false, Utilities.getStrings(Duke.getInstance(), R.string.delete), "Cancel", new PopupActions() {
            @Override
            public void onPopupActions(String id, int dialogId) {
                if (id.equals(AppConstants.PopupConstants.POSITIVE)) {
//                    loadsListModels.remove(loadAdapterCurrentItem);
//                    confirmationComponent.dismiss();
//                    adapter.notifyItemRemoved(loadAdapterCurrentItem);

                    loadsViewModel.getDeleteLoadObjectLiveData(loadsListModels.get(pos).getLoadUuid()).observe(getActivity(), new Observer<GenericResponseModel>() {
                        @Override
                        public void onChanged(GenericResponseModel genericResponseModel) {
                            if (genericResponseModel != null && genericResponseModel.getStatus().toLowerCase().equals("success")) {
                                confirmationComponent.dismiss();
                                adapter.notifyItemRemoved(loadAdapterCurrentItem);
                                applyFilters(selectedPeriod);
                            }
                            Toast.makeText(getContext(), genericResponseModel.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    confirmationComponent.dismiss();
                    adapter.notifyDataSetChanged();
                }
            }
        }, DELETE_LOAD_DIALOG_ID);
    }

    private void openAddDocumentPanel() {
        if ((Duke.fileStatusModel.getMemberStatus().equals("NONE") || Duke.fileStatusModel.getMemberStatus().equals("FREE_POD")) && Duke.fileStatusModel.getUploadLimit() - Duke.fileStatusModel.getUploadCount() < 1.0) {
            Intent intent = new Intent(requireActivity(), PaymentActivity.class);
            requireActivity().startActivity(intent);
            requireActivity().finish();
        } else {
            Bundle params = new Bundle();
            params.putString("Page", "CreateLoad");
            isLoadDocument = true;
            Duke.isDocumentAddingToLoad = false;
            Duke.isNewLoadBeingCreated = true;
            uploadDocumentInterface.uploadDocumentListener(false);
//            uploadDocument = new UploadDocument(getContext(), getActivity(), savedInstance, false, LoadsFragment.this);
        }
    }

    private void setCustomPeriodFilter() {
        Date docDate;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<UserLoadsModel> dateFilteredLoads = new ArrayList<>();

        for (UserLoadsModel loadsModel : Duke.loadsDocuments) {
//            for(ProcessedDocumentsModel document : Duke.fileStatusModel.getAllProcessedDocuments()) {
//                if(loadsModel.getDocuments().size() > 0 && loadsModel.getDocuments().get(0).getSha1().equals(document.getSha1())) {
//
//                }
//            }
            try {
                docDate = format.parse(loadsModel.getDateCreated());
                if (fromDate.compareTo(docDate) <= 0 && toDate.compareTo(docDate) >= 0) {
                    dateFilteredLoads.add(loadsModel);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dateFilteredLoads.size() != 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    noLoadsForThisPeriod.setVisibility(View.INVISIBLE);
//                    adapter.updateDataList(dateFilteredLoads);
//                    adapter.notifyDataSetChanged();
                    adapter = new LoadsListAdapter(dateFilteredLoads, LoadsFragment.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    customProgressLoader.hideDialog();
                } else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    noLoadsForThisPeriod.setVisibility(View.VISIBLE);
                    customProgressLoader.hideDialog();
                }
            }
        });

//        if(dateFilteredLoads.size() != loadsListModels.size()) {
//            adapter = new LoadsListAdapter(dateFilteredLoads, LoadsFragment.this);
//            recyclerView.setAdapter(adapter);
//            customProgressLoader.hideDialog();
//        }
    }


    //    Allternate Implmentation of Period Filter
    private void setPeriodSelectorSpinner() {
        loadsFilterDataModels.clear();
        loadsFilterDataModels.add(new LoadsFilterDataModel(getResources().getString(R.string.last_week)));
        loadsFilterDataModels.add(new LoadsFilterDataModel(getResources().getString(R.string.last_month)));
        loadsFilterDataModels.add(new LoadsFilterDataModel(getResources().getString(R.string.last_quarter)));
        loadsFilterDataModels.add(new LoadsFilterDataModel(getResources().getString(R.string.year_to_date)));
        loadsFilterDataModels.add(new LoadsFilterDataModel(getResources().getString(R.string.custom)));

        loadsFilterSpinnerAdapter = new LoadsFilterSpinnerAdapter(getContext(), R.layout.report_spinner_item, loadsFilterDataModels);
        spinner.setAdapter(loadsFilterSpinnerAdapter);
        spinner.setSelection(3);


        final AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPeriod = i;
                if (selectedPeriod == 4) {
                    if (periodSelectorAlert == null || !periodSelectorAlert.isShowing()) {
                        if (loadsListModels.size() > 0) {
                            showCustomPeriodSelectorDialog();
                        } else {
                            showCustomPeriodSelectorDialog();
                        }
                    }

                } else {
                    pos = i;
                    applyFilters(pos);
                    datesSelected.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("&*&**&*&*&*&*&*&", "onNothingSelected: ");
            }
        };


        //
        //        periodSpinner.setOnItemSelectedListener(itemSelectedListener);
        spinner.post(new Runnable() {
            public void run() {
                spinner.setOnItemSelectedListener(itemSelectedListener);
            }
        });
    }

    public void applyFilters(int pos) {
        this.pos = pos;

        fromDate = new Date();
        toDate = new Date();
        Date docDate = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

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
//                toDate = format.parse(Utilities.getEndDate());
                toDate = format.parse(Utilities.getEndDateReport(pos));
            } catch (Exception e) {
                Log.d("FILTER DATE EXCEPTION", "e: " + e.getStackTrace());
            }
        }

        switch (selectedPeriod) {
            case 0:
                sortBy = "last_week";
                break;
            case 1:
                sortBy = "last_month";
                break;
            case 2:
                sortBy = "last_quarter";
                break;
            case 4:
                sortBy = "custom";
                break;
            default:
                sortBy = "last_year";
        }

        DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

        JsonObject jsonObject = new JsonObject();
        String from = "";
        String to = "";
        if (sortBy.length() > 0) {
            if (fromDate != null || toDate != null) {
                from = formatDate.format(fromCalendar.getTime());
                to = formatDate.format(toCalender.getTime());
            } else {
                Log.d("Date error from:", fromDate.toString());
                Log.d("Date error from:", toDate.toString());
                from = formatDate.format(fromDate);
                to = formatDate.format(toDate);
            }
            if (sortBy.equals("custom") && from.length() > 0 && to.length() > 0) {
                jsonObject.addProperty("start_date", from);
                jsonObject.addProperty("end_date", to);
            }
            jsonObject.addProperty("sort_by", sortBy);
        }

        Log.d(Duke.TAG, "Request ==> " + new Gson().toJson(jsonObject));
        loadsViewModel.getLoadsListLiveData(jsonObject).observe(getActivity(), new Observer<LoadsListModel>() {
            @Override
            public void onChanged(LoadsListModel loadsListModel) {
                if (loadsListModel.getRecipientsList() != null) {
                    Log.d("data", loadsListModel.status);
                    loadsListModels = loadsListModel.userLoadsModels;
                    Duke.loadsDocuments = loadsListModel.userLoadsModels;
                    layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setHasFixedSize(true);
                    Log.d(Duke.TAG, "====>  " + new Gson().toJson(loadsListModels));
                    adapter = new LoadsListAdapter(loadsListModels, LoadsFragment.this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(adapter);

                    if (loadsListModels.size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noLoadsForThisPeriod.setVisibility(View.INVISIBLE);
                    } else {
                        Duke.selectedLoadsForTransmission.clear();
                        recyclerView.setVisibility(View.INVISIBLE);
                        noLoadsForThisPeriod.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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
                    spinner.setSelection(selectedPeriod); //YTD
            }
        });
    }

//    ***************Alternate Implmentation Over***************

    @Override
    public void onListItemClick(int pos, UserLoadsModel dataModel) {
        // Firebase: Send click view document button event
        Duke.DocsOfALoad.clear();

        loadsViewModel.getGetLoadDetailLiveData(dataModel.getLoadUuid()).observe(this, new Observer<SingleLoadModel>() {
            @Override
            public void onChanged(SingleLoadModel singleLoadModel) {
                if (singleLoadModel.getUserLoadsModels().getDocuments().size() > 0) {
                    DocsOfALoad.addAll(singleLoadModel.getUserLoadsModels().getDocuments());
                }
                Log.d("load details", singleLoadModel.toString());
                Bundle args = new Bundle();
                args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.PROCESS);
                isLoadDocument = true;
                Duke.selectedLoadUUID = dataModel.getLoadUuid();
                NavigationFlowManager.openFragments(Documents.newInstance(), args, getActivity(), R.id.dashboard_wrapper, AppConstants.StringConstants.PROCESS);
            }
        });

    }

//    public void onListItemClick(int pos, RecipientDataModel dataModel) {
//        // Firebase: Send click view document button event
//        Bundle params = new Bundle();
//        params.putString("Page", "Home");
//        Bundle args = new Bundle();
//        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.PROCESS);
//        NavigationFlowManager.openFragments(Documents.newInstance(), args, getActivity(), R.id.dashboard_wrapper, AppConstants.StringConstants.PROCESS);
//    }

    @OnClick(R.id.transmit)
    public void onClickTrasmit(View view) {
        customProgressLoader.showDialog();

        //Transmit Load API
        UserConfig userConfig = UserConfig.getInstance();
        UserDataModel userDataModel;
        userDataModel = userConfig.getUserDataModel();

        Log.d("selected loads", Duke.selectedLoadsForTransmission.toArray().toString());

        if (Duke.selectedRecipients.size() > 0 && Duke.selectedLoadsForTransmission.size() > 0) {
            transmitToTheseUsers.addAll(Duke.selectedRecipients);

            loadsViewModel.getTransmitLoadsModelLiveData(Duke.selectedLoadsForTransmission, transmitToTheseUsers).observe(this, new Observer<LoadsTransmitModel>() {
                @Override
                public void onChanged(LoadsTransmitModel loadsTransmitModel) {
                    customProgressLoader.hideDialog();
                    transmitToTheseUsers.clear();
                    /*if(loadsTransmitModel.message != null && loadsTransmitModel.message.length()>0) {
                        Toast.makeText(getContext(), loadsTransmitModel.message, Toast.LENGTH_LONG).show();
                    }*/
                    try {
                        Toast.makeText(getContext(), loadsTransmitModel.message.toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {

                    }
                }
            });
        } else {
            customProgressLoader.hideDialog();
            if (Duke.selectedRecipients.size() == 0) {
                Toast.makeText(getContext(), "Please select recipient(s)!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Please select load(s) to transmit!", Toast.LENGTH_LONG).show();
            }

        }
    }

    @OnClick(R.id.edit_btn)
    public void onEditBtnClicked(View view) {
        Log.d("number picker value: ", " - data: " + availableRecipients[numberPicker.getValue()]);
        Log.d("all recipients", recipientDataModels.toString());

        RecipientDataModel recipientDataModelForEdit = null;
        for (RecipientDataModel recipientDataModel : recipientDataModels) {
            if (recipientDataModel.getEmail().equals(availableRecipients[numberPicker.getValue()])) {
                recipientDataModelForEdit = recipientDataModel;
                break;
            }
        }

        if (recipientDataModelForEdit != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(AppConstants.StringConstants.RECIPIENT_DATA_MODEL, recipientDataModelForEdit);
            bundle.putString(AppConstants.StringConstants.ACTION, AppConstants.StringConstants.ACTION_TYPE_UPDATE);
            NavigationFlowManager.openFragments(new AddRecipientFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
        }
    }

    @Override
    public void onPopupActions(String id, int dialogId) {
    }

    private void dismissExistingComponentIfAny() {
        if (confirmationComponent != null) {
            confirmationComponent.dismiss();
            confirmationComponent = null;
        }
    }

    @Override
    public void onListItemClick(int pos, RecipientDataModel recipientDataModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.StringConstants.RECIPIENT_DATA_MODEL, recipientDataModel);
        bundle.putString(AppConstants.StringConstants.ACTION, AppConstants.StringConstants.ACTION_TYPE_UPDATE);
        NavigationFlowManager.openFragments(new AddRecipientFragment(), bundle, getActivity(), R.id.dashboard_wrapper);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    public void afterDialogDismissed() {
        selectedCustomRecipients.clear();
        for (RecipientDataModel recipientDataModel : Duke.customRecipientsList) {
            if (Duke.selectedRecipients.contains(recipientDataModel.getEmail())) {
                selectedCustomRecipients.add(recipientDataModel);
            }
        }

        if (Duke.selectedRecipients.size() > 0) {
            addRecipient.setText(Duke.selectedRecipients.size() + " Recipients Selected");
        } else {
            addRecipient.setText("Select Recipients");
        }

        if (selectedCustomRecipients.size() > 0) {

            if (selectedCustomRecipients.size() == 1) {
                RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
                param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_40);  //working-1
                editIcon.setVisibility(View.INVISIBLE);
//                customRecipientsListRecyclerView.smoothScrollToPosition(0);
                customRecipientsListRecyclerView.setLayoutParams(param);    //working-1
            } else if (selectedCustomRecipients.size() == 2) {
                RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
                param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_80);
                editIcon.setVisibility(View.INVISIBLE);
//                customRecipientsListRecyclerView.smoothScrollToPosition(1);
                customRecipientsListRecyclerView.setLayoutParams(param);
            } else {
                editIcon.setVisibility(View.VISIBLE);
                RecyclerView.MarginLayoutParams param = (RecyclerView.MarginLayoutParams) customRecipientsListRecyclerView.getLayoutParams();
                param.height = getResources().getDimensionPixelSize(R.dimen.edit_panel_100);
                customRecipientsListRecyclerView.scrollToPosition(Integer.MAX_VALUE / 2);
            }

            if (isCustomRecipientAlreadySelected) {
                customRecipientListAdapter = new CustomRecipientListAdapter(selectedCustomRecipients, this, this);
                customRecipientsListRecyclerView.setAdapter(customRecipientListAdapter);
                try {
                    adapter.notifyDataSetChanged();
                } catch (Exception ex) {
                    System.out.println("LoadFragment load list adapter " + ex);
                }
                customProgressLoader.hideDialog();
            } else {
                isCustomRecipientAlreadySelected = true;
                setCustomRecipientList();
            }
            editPanelWrapper.setVisibility(View.VISIBLE);
        } else {
            editPanelWrapper.setVisibility(View.INVISIBLE);
            customProgressLoader.hideDialog();
        }
    }

    @Override
    public void onClickProfile() {
        Utilities.navigateToProfilePage(getActivity());
    }

    @Override
    public void onBackClicked() {
        NavigationFlowManager.openFragments(HomeFragment.newInstance(), null, getActivity(), R.id.dashboard_wrapper, AppConstants.Pages.HOME);
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utilities.resetLoadFlags();
    }

    @Override
    public void onLastItemLodedListener() {
        customProgressLoader.hideDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UploadDocumentInterface) {
            uploadDocumentInterface = (UploadDocumentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("PM-LoadFragment", "onActivityResult: " + requestCode);
        if (requestCode == Utilities.CONTACT_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
//                Log.d("MyTag", results.get(0).getDisplayName());
                ListIterator<ContactResult> iterator = results.listIterator();
                String allNumbers = "";
                while (iterator.hasNext()) {
                    allNumbers += iterator.next().getPhoneNumbers().get(0).getNumber() + ";";
                }
                String name = (Duke.userName != null ? Duke.userName : "");
                if (AppConstants.currentTheme.equals("duke")) {
                    message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download) + userConfig.getUserDataModel().getUserEmail() + this.getString(R.string.when_you_register_in_order_to) + AppConstants.iosLink + this.getString(R.string.for_ios_or) + AppConstants.androidLink + this.getString(R.string.for_android);
                } else {
                    message = this.getString(R.string.your_friend) + name + this.getString(R.string.has_sent_you_a_private_invitation_to_download_ttt) + "INFO@TAXATIONSOLUTIONS.NET" + this.getString(R.string.when_you_register_in_order_to_ttt) + "bit.ly/TruckerTTiOS" + this.getString(R.string.for_ios_or) + "http://bit.ly/TruckerTTAndroid" + this.getString(R.string.for_android);
                }
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("smsto:" + allNumbers)); // This ensures only SMS apps respond
                intent.putExtra("sms_body", message);
                startActivity(intent);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        } else if (requestCode == REQUEST_GPS_FOR_UPLOAD_DOC) {
            if (resultCode == Activity.RESULT_OK) {
                /**GPS turned ON, proceed with Check Active trip status**/
                Duke.isLocationPermissionProvided = true;
                openAddDocumentPanel();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Request failed Show popup
                /**GPS is OFF, Show user the Alert**/
                Duke.isLocationPermissionProvided = false;
                openAddDocumentPanel();
            }
        } else if (requestCode == 12) {

        }
    }
}