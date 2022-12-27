package com.dukeai.android.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.dukeai.android.BuildConfig;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.MemberStatusUpdateObserver;
import com.dukeai.android.interfaces.TripObserver;
import com.dukeai.android.models.SubscriptionPlan;
import com.dukeai.android.models.TripIdModel;
import com.dukeai.android.models.UpdatePaymentModel;
import com.dukeai.android.utils.CustomProgressLoader;
import com.dukeai.android.viewModel.FileStatusViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.revenuecat.purchases.Offering;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PurchaserInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.MakePurchaseListener;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsListener;
import com.revenuecat.purchases.interfaces.ReceivePurchaserInfoListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.revenuecat.purchases.Purchases.getSharedInstance;

public class PaymentActivity extends BaseActivity implements View.OnClickListener {

    MaterialCardView subscriptionOption1;
    MaterialCardView subscriptionOption2;
    ImageButton closeBtn;
    ImageView applogo;
    TextView subscriptionPeriod1;
    TextView subscriptionPeriod2;
    TextView subscriptionAmount1;
    TextView subscriptionAmount2;
    TextView subscriptionDescription;
    TextView restorePurchase;
    View separator1;
    View separator2;
    Button continueBtn;
    Package selectedPackage;
    String selectedPlan = "monthly";
    List<Package> availablePackages;
    FileStatusViewModel fileStatusViewModel;
    JsonObject plan;
    Offering currentOffering;
    CustomProgressLoader customProgressLoader;
    int selectedColor;
    int defaultColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        customProgressLoader = new CustomProgressLoader(this);
        View view = this.getWindow().getDecorView();
        if(BuildConfig.theme.equals("duke")) {
            window.setStatusBarColor(getResources().getColor(R.color.colorBlack, null));
            view.setBackgroundColor(getResources().getColor(R.color.colorBlack, null));
        } else {
            window.setStatusBarColor(getResources().getColor(R.color.colorTTT, null));
            view.setBackgroundColor(getResources().getColor(R.color.colorTTT, null));
        }
        setContentView(R.layout.activity_payment);
        setInitials();
    }

    private void setInitials() {
        applogo = findViewById(R.id.duke_logo);
        subscriptionOption1 = findViewById(R.id.subscription_option1);
        subscriptionOption2 = findViewById(R.id.subscription_option2);
        closeBtn = findViewById(R.id.close_btn);
        subscriptionPeriod1 = findViewById(R.id.subscription_period1);
        subscriptionPeriod2 = findViewById(R.id.subscription_period2);
        subscriptionAmount1 = findViewById(R.id.subscription_amount1);
        subscriptionAmount2 = findViewById(R.id.subscription_amount2);
        subscriptionDescription = findViewById(R.id.subscription_description);
        separator1 = findViewById(R.id.separator1);
        separator2 = findViewById(R.id.separator2);
        continueBtn = findViewById(R.id.continue_btn);
        restorePurchase = findViewById(R.id.restore_purchase);

        subscriptionOption1.setOnClickListener(this);
        subscriptionOption2.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        continueBtn.setOnClickListener(this);
        restorePurchase.setOnClickListener(this);
        fileStatusViewModel = ViewModelProviders.of(this).get(FileStatusViewModel.class);
        identifyUser();
        setUIInitials();
        getOfferings();
    }

    private void setUIInitials() {
        if(BuildConfig.theme.equals("truck")) {
            applogo.setImageResource(R.drawable.truck);
//            applogo.getLayoutParams().width = ConstraintLayout.LayoutParams.MATCH_PARENT-100;
//            applogo.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            selectedColor = getResources().getColor(R.color.colorTTT2, null);
            defaultColor = getResources().getColor(R.color.colorGray, null);
            setSubscriptionOptionColorScheme(selectedColor, defaultColor);
            applogo.requestLayout();
        } else {
            selectedColor = getResources().getColor(R.color.yellow_BBAE27, null);
            defaultColor = getResources().getColor(R.color.colorGray, null);
        }
    }

    private void identifyUser() {
        if(Duke.uniqueUserId.length()>0) {
            String appUserId = Duke.uniqueUserId + "#" + Duke.fileStatusModel.getRequest().getCustomerId();
            Purchases.getSharedInstance().identify(appUserId, new ReceivePurchaserInfoListener() {
                @Override
                public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
                    Log.d("User uniquely identify", purchaserInfo.toString());
                }

                @Override
                public void onError(@NonNull PurchasesError error) {
                    Log.d("User could not be found", error.toString());
                }
            });
        }
    }

    private void getOfferings() {
        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsListener() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                if (offerings.getCurrent() != null) {
                    availablePackages = offerings.getCurrent().getAvailablePackages();
                    currentOffering = offerings.getCurrent();
                    // Display packages for sale
                    if (currentOffering.getMonthly() != null) {
                        SkuDetails monthlyProduct = currentOffering.getMonthly().getProduct();
                        subscriptionAmount1.setText(monthlyProduct.getPrice());
                        String freeTrialEndDate = getFreeTrialEndDate(monthlyProduct.getFreeTrialPeriod());
                        subscriptionDescription.setText("Include 14-day free trial. Cancel before " + freeTrialEndDate + " and nothing will be billed.");
                        Log.d("product 1", monthlyProduct.toString());
                        // Get the price and introductory period from the SkuDetails
                    }

                    if (currentOffering.getAnnual() != null) {
                        SkuDetails annualProduct = currentOffering.getAnnual().getProduct();
                        subscriptionAmount2.setText(annualProduct.getPrice());
                        Log.d("product 2", annualProduct.toString());
                        // Get the price and introductory period from the SkuDetails
                    }
                }
            }

            private String getFreeTrialEndDate(String freeTrialPeriod) {
                int noOfWeek = Integer.parseInt(String.valueOf(freeTrialPeriod.charAt(1)));
                Date date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.WEEK_OF_MONTH, noOfWeek);
                date = c.getTime();
                SimpleDateFormat DateFor = new SimpleDateFormat("MMMM d");
                return DateFor.format(date);
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                // An error occurred
            }
        });
    }

    @Override
    public void onClick(View v) {
//        int selectedColor = getResources().getColor(R.color.yellow_BBAE27, null);
//        int defaultColor = getResources().getColor(R.color.colorGray, null);
//        int temp = selectedColor;
//        selectedColor = defaultColor;
//        defaultColor = temp;
        String theme = BuildConfig.theme;

//        v.setBackgroundColor(getResources().getColor(R.color.colorBlack, null));

        switch (v.getId()) {
            case R.id.subscription_option1:
                subscriptionOption1.setStrokeColor(selectedColor);
                subscriptionOption2.setStrokeColor(defaultColor);
                subscriptionPeriod1.setTextColor(selectedColor);
                subscriptionPeriod2.setTextColor(defaultColor);
                subscriptionAmount1.setTextColor(selectedColor);
                subscriptionAmount2.setTextColor(defaultColor);
                separator1.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                separator2.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
                selectedPlan = "monthly";
                break;
            case R.id.subscription_option2:
                subscriptionOption2.setStrokeColor(selectedColor);
                subscriptionOption1.setStrokeColor(defaultColor);
                subscriptionPeriod1.setTextColor(defaultColor);
                subscriptionPeriod2.setTextColor(selectedColor);
                subscriptionAmount1.setTextColor(defaultColor);
                subscriptionAmount2.setTextColor(selectedColor);
                separator2.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                separator1.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
                selectedPlan = "annual";
                break;
            case R.id.close_btn:
                returnToHome();
                break;
            case R.id.continue_btn:
                makePurchase();
                break;
            case R.id.restore_purchase:
                restorePurchase();
                break;
            default:
                break;
        }
    }

    private void returnToHome() {
        customProgressLoader.showDialog();
        Intent intent = new Intent(PaymentActivity.this, DashboardActivity.class);
        intent.putExtra("isClosed", true);
        startActivity(intent);
        finish();
        customProgressLoader.hideDialog();
    }

    private void restorePurchase() {
        Purchases.getSharedInstance().restorePurchases(new ReceivePurchaserInfoListener() {
            @Override
            public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
                if(purchaserInfo.getEntitlements().get("DocumentUpload").isActive()) {
                    if (purchaserInfo.getEntitlements().get("productIdenfier").equals("duke_monthly") || purchaserInfo.getEntitlements().get("productIdenfier").equals("ttt_monthly")) {
                        // Unlock that great "pro" content
                        Duke.subscriptionPlan.setMemberStatus(SubscriptionPlan.MemberStatus.MONTHLY_POD);
                        returnToHome();
                    } else {
                        Duke.subscriptionPlan.setMemberStatus(SubscriptionPlan.MemberStatus.ANNUALLY_POD);
                        returnToHome();
                    }
                }
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                Log.d("Purchase restored", error.getMessage());
            }
        });
    }

    public void makePurchase() {
        if(selectedPlan.equals("monthly")) {
            selectedPackage = availablePackages.get(0);
        } else {
            selectedPackage = currentOffering.getAnnual();
        }
        if (selectedPackage != null) {
            customProgressLoader.showDialog();
            Purchases.getSharedInstance().purchasePackage(
                    this,
                    selectedPackage,
                    new MakePurchaseListener() {
                        @Override
                        public void onError(@NonNull PurchasesError error, boolean userCancelled) {
                            customProgressLoader.hideDialog();
                            Log.d("Payment failed", error.toString());

                        }

                        @Override
                        public void onCompleted(@NonNull Purchase purchase, @NonNull PurchaserInfo purchaserInfo) {
                            if (purchaserInfo.getEntitlements().get("DocumentUpload").isActive()) {
                                // Unlock that great "pro" content
                                Log.d("Purchase successfull", purchaserInfo.toString());
                                if(purchaserInfo.getEntitlements().get("DocumentUpload").getProductIdentifier().equals("duke_monthly") || purchaserInfo.getEntitlements().get("DocumentUpload").getProductIdentifier().equals("ttt_monthly")){
                                    updatePayment("Premium_Monthly");
                                } else {
                                    updatePayment("Premium_Annually");
                                }
                                customProgressLoader.hideDialog();
                            }
                            returnToHome();
                        }
                    }
            );
        }
    }

    private void updatePayment(String subscriptionType) {
        plan = new JsonObject();
        plan.addProperty("plan" ,subscriptionType);

        updatePaymentStatus(new MemberStatusUpdateObserver() {
            @Override
            public void onChanged(String status, UpdatePaymentModel updatePaymentModel) {
                if(status.toLowerCase().equals("success")) {
                    Log.d("Purchase successfull", updatePaymentModel.getMsg());
                } else {
                    Log.d("Purchase failed", updatePaymentModel.getMsg());
                }
            }
        });
    }

    public void setSubscriptionOptionColorScheme(int selectedColor, int defaultColor) {
        subscriptionOption1.setStrokeColor(selectedColor);
        subscriptionOption2.setStrokeColor(defaultColor);
        subscriptionPeriod1.setTextColor(selectedColor);
        subscriptionPeriod2.setTextColor(defaultColor);
        subscriptionAmount1.setTextColor(selectedColor);
        subscriptionAmount2.setTextColor(defaultColor);
        separator1.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
        separator2.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
    }

    private void updatePaymentStatus(MemberStatusUpdateObserver memberStatusUpdateObserver) {

        fileStatusViewModel.memberStatusUpdate(plan).observe(this, new Observer<UpdatePaymentModel>() {
            @Override
            public void onChanged(UpdatePaymentModel updatePaymentModel) {
                try {
                    String msg = "";
                    if (updatePaymentModel != null && updatePaymentModel.getMsg() == null) {
                        if(updatePaymentModel.getMsg().contentEquals("Subscription updated")) {
                            msg = "SUCCESS";
                        }
                    } else {
                        //Error
                        msg = "ERROR";
                    }
                    memberStatusUpdateObserver.onChanged(msg, updatePaymentModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    /**Error occurred**/
                    memberStatusUpdateObserver.onChanged("ERROR", updatePaymentModel);
                }
            }
        });
    }
}