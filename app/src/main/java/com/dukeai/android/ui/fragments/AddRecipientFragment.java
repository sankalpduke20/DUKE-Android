package com.dukeai.android.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.HeaderActions;
import com.dukeai.android.models.GenericResponseModel;
import com.dukeai.android.models.InProcessDocumentsModel;
import com.dukeai.android.models.RecipientDataModel;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.InputValidators;
import com.dukeai.android.utils.NavigationFlowManager;
import com.dukeai.android.utils.UIMessages;
import com.dukeai.android.viewModel.LoadsViewModel;
import com.dukeai.android.views.CustomEditInputField;
import com.dukeai.android.views.CustomHeader;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.facebook.FacebookSdk.getApplicationContext;

public class AddRecipientFragment extends Fragment implements HeaderActions {

    @BindView(R.id.add_recipient_header)
    CustomHeader customHeader;
    View addRecipientsView;
    @BindView(R.id.company_name_value)
    TextInputEditText companyNameField;
    @BindView(R.id.email_id)
    TextInputEditText emailField;
    @BindView(R.id.address_value)
    TextInputEditText addressField;
    @BindView(R.id.phone_value)
    TextInputEditText phoneField;
    @BindView(R.id.contact_value)
    TextInputEditText contactField;
    @BindView(R.id.delete)
    TextView deleteRecipient;
    String originalEmail;

    @BindView(R.id.submit)
    Button recipientBtn;
    LoadsViewModel loadsViewModel;
    RecipientDataModel data;
    String updateMode = "ADD";

    public AddRecipientFragment() {
        // Required empty public constructor
    }

    public static AddRecipientFragment newInstance() {
        AddRecipientFragment fragment = new AddRecipientFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Bundle args = new Bundle();
                args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.LOADS);
                NavigationFlowManager.openFragments(Duke.loadsFragment, args, getActivity(), R.id.dashboard_wrapper);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        addRecipientsView = inflater.inflate(R.layout.fragment_add_recipient, container, false);
        loadsViewModel = ViewModelProviders.of(this).get(LoadsViewModel.class);
        ButterKnife.bind(this, addRecipientsView);
        setCustomHeader();
        return addRecipientsView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInitials();
    }

    private void setToolbarColor() {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(Duke.getInstance(), R.color.colorBlack));
    }

    private void setInitials() {
        prePopulateData(getArguments());
        setToolbarColor();
        setDeleteBtnVisibilty();
    }

    private void setDeleteBtnVisibilty() {
        if(updateMode.equals("update")) {
            deleteRecipient.setVisibility(View.VISIBLE);
        }
    }

    private void prePopulateData(Bundle bundle) {
        if (bundle != null && bundle.get(AppConstants.StringConstants.RECIPIENT_DATA_MODEL) != null) {
            data = (RecipientDataModel) bundle.getSerializable(AppConstants.StringConstants.RECIPIENT_DATA_MODEL);
            if(bundle.get(AppConstants.StringConstants.ACTION) != null && bundle.get(AppConstants.StringConstants.ACTION).equals(AppConstants.StringConstants.ACTION_TYPE_UPDATE)) {
                companyNameField.setText(data.getCompanyName());
                emailField.setText(data.getEmail());
                originalEmail = data.getEmail();
                addressField.setText(data.getAddress());
                phoneField.setText(data.getPhone());
                contactField.setText(data.getContact());
                customHeader.setToolbarTitle("Edit  Recipient");
                recipientBtn.setText("Update");
            }
            updateMode = "update";
        }

        if(bundle != null && bundle.get(AppConstants.StringConstants.ACTION).equals(AppConstants.StringConstants.ACTION_TYPE_ADD)) {
            customHeader.setToolbarTitle("Add  Recipient");
        }
    }

    public void setCustomHeader() {
        customHeader.showHideProfileImage(View.GONE);
        customHeader.showHideHeaderTitle(View.GONE);
        customHeader.setHeaderActions(this);
        customHeader.setImageTintColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        TextView title = customHeader.findViewById(R.id.toolbar_title);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
    }

    @OnClick(R.id.submit)
    void onSubmitClicked(View v) {
        boolean isCompanyNameValid = validateFieldName(companyNameField);
        boolean isEmailIdValid = validateFieldEmail(emailField);

        if(isCompanyNameValid && isEmailIdValid) {
            String flag = "";
            if(updateMode.equals("update")) {
                flag = "update";
            } else {
                flag = "add";
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("app_version", "2.3.7");
            payload.addProperty("flag", flag);
            if(flag.equals("update")) {
                payload.addProperty("original_email", originalEmail);
            }
            payload.addProperty("email", emailField.getText().toString());
            payload.addProperty("company_name", companyNameField.getText().toString());
            payload.addProperty("address", addressField.getText().toString().length()>0 ? addressField.getText().toString() : " ");
            payload.addProperty("phone", phoneField.getText().toString().length()>0 ? phoneField.getText().toString() : " ");
            payload.addProperty("contact", contactField.getText().toString().length()>0 ? contactField.getText().toString() : " ");

            loadsViewModel.getUpdateRecipientsLiveData(payload).observe(this, new Observer<GenericResponseModel>() {
                @Override
                public void onChanged(GenericResponseModel genericResponseModel) {
                    if(genericResponseModel != null && genericResponseModel.getStatus() != null && genericResponseModel.getStatus().toLowerCase().equals("success")) {
                        Toast.makeText(getActivity(), genericResponseModel.getMessage(), Toast.LENGTH_LONG).show();
                        if(Duke.selectedRecipients.contains(originalEmail)) {
                            Duke.selectedRecipients.remove(originalEmail);
                        }
                        Bundle args = new Bundle();
                        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.LOADS);
                        NavigationFlowManager.openFragments(Duke.loadsFragment, args, getActivity(), R.id.dashboard_wrapper);
                    } else {
                        Toast.makeText(getActivity(), genericResponseModel.getMessage(), Toast.LENGTH_LONG).show();
                        Bundle args = new Bundle();
                        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.LOADS);
                        NavigationFlowManager.openFragments(Duke.loadsFragment, args, getActivity(), R.id.dashboard_wrapper);
                    }
                }
            });
        }
    }

    public boolean validateFieldEmail(View v) {
        String viewTag = v.getTag().toString();
        Boolean isValid = InputValidators.validateInput(viewTag, ((TextInputEditText) v).getText().toString());
        String msg;
        if (isValid) {
            msg = null;
        } else {
            msg = "! " + UIMessages.getMessage(AppConstants.StringConstants.ERROR, viewTag);
        }

//        if(textInputLayout.getError() == null)
        ((TextInputEditText) v).setError(msg);

        return isValid;
    }


    public boolean validateFieldName(View v) {
        String viewTag = v.getTag().toString();
        Boolean isValid = InputValidators.validateInput(viewTag, ((TextInputEditText) v).getText().toString());
        String msg;
        if (isValid) {
            msg = null;
        } else {
            msg = "! " + UIMessages.getMessage(AppConstants.StringConstants.ERROR, viewTag);
        }

//        if(textInputLayout.getError() == null)
        ((TextInputEditText) v).setError(msg);

        return isValid;
    }

    @OnClick(R.id.delete)
    void onDeleteClicked(View v) {
        JsonObject payload = new JsonObject();
        payload.addProperty("app_version", "2.3.7");
        payload.addProperty("flag", "delete");
        payload.addProperty("email", emailField.getText().toString());
        loadsViewModel.getUpdateRecipientsLiveData(payload).observe(this, new Observer<GenericResponseModel>() {
            @Override
            public void onChanged(GenericResponseModel genericResponseModel) {
                if(genericResponseModel != null && genericResponseModel.getStatus() != null && genericResponseModel.getStatus().toLowerCase().equals("success")) {
                    Toast.makeText(getActivity(), genericResponseModel.getMessage(), Toast.LENGTH_LONG).show();
                    Bundle args = new Bundle();
                    if(Duke.selectedRecipients.contains(emailField.getText().toString())) {
                        Duke.selectedRecipients.remove(emailField.getText().toString());
                    }
                    args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.LOADS);
                    NavigationFlowManager.openFragments(Duke.loadsFragment, args, getActivity(), R.id.dashboard_wrapper);
                }
            }
        });
    }

    @Override
    public void onClickProfile() {

    }

    @Override
    public void onBackClicked() {
        Bundle args = new Bundle();
        args.putString(AppConstants.StringConstants.NAVIGATE_TO, AppConstants.StringConstants.LOADS);
        NavigationFlowManager.openFragments(Duke.loadsFragment, args, getActivity(), R.id.dashboard_wrapper);
    }

    @Override
    public void onClickToolbarTitle() {

    }

    @Override
    public void onClickHeaderTitle() {

    }

}