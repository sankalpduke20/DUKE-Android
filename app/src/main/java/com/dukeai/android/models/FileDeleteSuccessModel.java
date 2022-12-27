package com.dukeai.android.models;

import com.google.gson.annotations.SerializedName;

public class FileDeleteSuccessModel extends ResponseModel {
    @SerializedName("ResponseMetadata")
    ResponseMetadataModel responseMetadataModel;

    public ResponseMetadataModel getResponseMetadataModel() {
        return responseMetadataModel;
    }

    public void setResponseMetadataModel(ResponseMetadataModel responseMetadataModel) {
        this.responseMetadataModel = responseMetadataModel;
    }
}
