package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class BalanceSheetModel {
    @SerializedName("data")
    DataListModel[] dataListModel;

    public DataListModel[] getDataListModel() {
        return dataListModel;
    }

    public void setDataListModel(DataListModel[] dataListModel) {
        this.dataListModel = dataListModel;
    }
//    ArrayList<DataListModel> dataListModel;

    //    public ArrayList<DataListModel> getDataListModel() {
//        return dataListModel;
//    }
//
//    public void setDataListModel(ArrayList<DataListModel> dataListModel) {
//        this.dataListModel = dataListModel;
//    }
    public static class DataListModel {
        @SerializedName("description")
        String description;
        @SerializedName("account_type")
        String accountType;
        @SerializedName("value")
        double cashValue;
        @SerializedName("delete")
        Boolean delete;
        @SerializedName("internal_id")
        String internalId;

        public DataListModel(String description, String accountType, double cashValue, boolean delete, String internalId) {
            this.description = description;
            this.accountType = accountType;
            this.cashValue = cashValue;
            this.delete = delete;
            this.internalId = internalId;
        }

        public String getInternalId() {
            return internalId;
        }

        public void setInternalId(String internalId) {
            this.internalId = internalId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public double getCashValue() {
            return cashValue;
        }

        public void setCashValue(double cashValue) {
            this.cashValue = cashValue;
        }

        public Boolean getDelete() {
            return delete;
        }

        public void setDelete(Boolean delete) {
            this.delete = delete;
        }
    }
}
