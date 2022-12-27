package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AssetDeductionModel implements Serializable {

    @SerializedName("data")
    DataListModel[] dataListModel;

    public DataListModel[] getDataListModel() {
        return dataListModel;
    }

    public void setDataListModel(DataListModel[] dataListModel) {
        this.dataListModel = dataListModel;
    }
//    ArrayList<DataListModel> dataListModel;
//
//    public ArrayList<DataListModel> getDataListModel() {
//        return dataListModel;
//    }
//
//    public void setDataListModel(ArrayList<DataListModel> dataListModel) {
//        this.dataListModel = dataListModel;
//    }

    public static class DataListModel {
        @SerializedName("purchase_date")
        String purchaseDate;
        @SerializedName("description")
        String description;
        @SerializedName("model")
        String model;
        @SerializedName("internal_id")
        String internalId;
        @SerializedName("id")
        String id;
        @SerializedName("value")
        Double valueVal;
        @SerializedName("delete")
        Boolean delete;
        @SerializedName("assets_type")
        String assetType;

        public DataListModel(String purchaseDate, String description, String model, String internalId, String id, Double valueVal, Boolean deleteType, String assetType) {
            this.purchaseDate = purchaseDate;
            this.description = description;
            this.model = model;
            this.internalId = internalId;
            this.id = id;
            this.valueVal = valueVal;
            this.delete = deleteType;
            this.assetType = assetType;
        }

        public String getPurchaseDate() {
            return purchaseDate;
        }

        public void setPurchaseDate(String purchaseDate) {
            this.purchaseDate = purchaseDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getInternalId() {
            return internalId;
        }

        public void setInternalId(String internalId) {
            this.internalId = internalId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getValueVal() {
            return valueVal;
        }

        public void setValueVal(Double value) {
            this.valueVal = value;
        }

        public Boolean getDelete() {
            return delete;
        }

        public void setDelete(Boolean delete) {
            this.delete = delete;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }
    }
}

