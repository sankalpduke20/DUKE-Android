package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GenerateReportModel extends ResponseModel implements Serializable {
    @SerializedName("request")
    RequestModel requestModel;

    @SerializedName("result")
    String result;

    public RequestModel getRequestModel() {
        return requestModel;
    }

    public void setRequestModel(RequestModel requestModel) {
        this.requestModel = requestModel;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public class RequestModel implements Serializable {
        @SerializedName("cust_id")
        String customerId;
        @SerializedName("caller")
        String caller;
        @SerializedName("from")
        String from;
        @SerializedName("to")
        String to;
        @SerializedName("report_type")
        String reportType;

        public String getReportType() {
            return reportType;
        }

        public void setReportType(String reportType) {
            this.reportType = reportType;
        }

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
}
