package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FederalDeductionModel implements Serializable {

    @SerializedName("data")
    FederalDeductionDataModel federalDeductionDataModel;


//    public String getMessage() {
//        return message;
//    }

    public FederalDeductionModel() {

    }
//    public void setMessage(String message) {
//        this.message = message;
//    }

//    public FederalDeductionModel(String msg){
//        setErrorMessage(msg);
//    }


    public FederalDeductionDataModel getFederalDeductionDataModel() {
        return federalDeductionDataModel;
    }

    public void setFederalDeductionDataModel(FederalDeductionDataModel preTaxDeductionModel) {
        this.federalDeductionDataModel = federalDeductionDataModel;
    }

    public class FederalDeductionDataModel {

        @SerializedName("Filling_Status")
        String fillingStatus;
        @SerializedName("Other_Income")
        OtherIncomeModel otherIncomeModel;
        @SerializedName("Pre_tax_deduction")
        PreTaxDeductionModel preTaxDeductionModel;

        @SerializedName("Tax_credit")
        TaxCreditDeductionModel taxCreditDeductionModel;
        @SerializedName("Standard_Deduction")
        StandardDeductionModel standardDeductionModel;
        @SerializedName("Itemize_Deduction")
        ItemizeDeductionModel itemmizeDeductionModel;

        public String getFillingStatus() {
            return this.fillingStatus;
        }


        public void setFillingStatus(String fillingStatus) {
            this.fillingStatus = fillingStatus;
        }

        public PreTaxDeductionModel getPreTaxDeductionData() {
            return preTaxDeductionModel;
        }

        public void setPreTaxDeductionData(PreTaxDeductionModel preTaxDeductionModel) {
            this.preTaxDeductionModel = preTaxDeductionModel;
        }

        public TaxCreditDeductionModel getTaxCreditDeductionModelData() {
            return taxCreditDeductionModel;
        }

        public void setTaxCreditDeductionModelData(TaxCreditDeductionModel taxCreditDeductionModel) {
            this.taxCreditDeductionModel = taxCreditDeductionModel;
        }

        public StandardDeductionModel getStandardDeductionModelData() {
            return standardDeductionModel;
        }

        public void setStandardDeductionModelData(StandardDeductionModel standardDeductionModel) {
            this.standardDeductionModel = standardDeductionModel;
        }

        public ItemizeDeductionModel getItemizeDeductionModelData() {
            return itemmizeDeductionModel;
        }

        public void setItemizeDeductionModelData(ItemizeDeductionModel itemmizeDeductionModel) {
            this.itemmizeDeductionModel = itemmizeDeductionModel;
        }

        public OtherIncomeModel getOtherIncomeModel() {
            return otherIncomeModel;
        }

        public void setOtherIncomeModel(OtherIncomeModel otherIncomeModel) {
            this.otherIncomeModel = otherIncomeModel;
        }

        public class OtherIncomeModel {
            @SerializedName("amount")
            Double amount;

            public Double getAmount() {
                return amount;
            }

            public void setAmount(Double amount) {
                this.amount = amount;
            }
        }

        public class PreTaxDeductionModel {
            @SerializedName("amount")
            Double amount;
            @SerializedName("status")
            Boolean status;

            public Double getAmount() {
                return amount;
            }

            public void setAmount(Double amount) {
                this.amount = amount;
            }

            public Boolean getStatus() {
                return status;
            }

            public void setStatus(Boolean status) {
                this.status = status;
            }
        }

        public class TaxCreditDeductionModel {
            @SerializedName("amount")
            Double amount;
            @SerializedName("status")
            Boolean status;

            public Double getAmount() {
                return amount;
            }

            public void setAmount(Double amount) {
                this.amount = amount;
            }

            public Boolean getStatus() {
                return status;
            }

            public void setStatus(Boolean status) {
                this.status = status;
            }
        }

        public class StandardDeductionModel {
            @SerializedName("status")
            Boolean status;

            public Boolean getStatus() {
                return status;
            }

            public void setStatus(Boolean status) {
                this.status = status;
            }

        }

        public class ItemizeDeductionModel {
            @SerializedName("amount")
            Double amount;
            @SerializedName("status")
            Boolean status;

            public Double getAmount() {
                return amount;
            }

            public void setAmount(Double amount) {
                this.amount = amount;
            }

            public Boolean getStatus() {
                return status;
            }

            public void setStatus(Boolean status) {
                this.status = status;
            }
        }

    }

}
