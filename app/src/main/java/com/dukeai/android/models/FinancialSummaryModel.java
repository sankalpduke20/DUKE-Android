package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FinancialSummaryModel extends ResponseModel {

    @SerializedName("request")
    RequestModel requestModel;

    @SerializedName("data")
    DataModel dataModel;

    public RequestModel getRequestModel() {
        return requestModel;
    }

    public void setRequestModel(RequestModel requestModel) {
        this.requestModel = requestModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public class RequestModel {
        @SerializedName("cust_id")
        String customerId;
        @SerializedName("caller")
        String caller;
        @SerializedName("from")
        String from;
        @SerializedName("to")
        String to;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    public class DataModel {
        @SerializedName("reports")
        ArrayList<ReportsModel> reportsModels;

        public ArrayList<ReportsModel> getReportsModels() {
            return reportsModels;
        }

        public void setReportsModels(ArrayList<ReportsModel> reportsModels) {
            this.reportsModels = reportsModels;
        }

        public class ReportsModel {
            @SerializedName("amount")
            public double amount;
            @SerializedName("type")
            public String type;

            public ReportsModel(double amount, String type) {
                this.amount = amount;
                this.type = type;
            }

            public double getAmount() {
                return amount;
            }

            public void setAmount(double amount) {
                this.amount = amount;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }
}
