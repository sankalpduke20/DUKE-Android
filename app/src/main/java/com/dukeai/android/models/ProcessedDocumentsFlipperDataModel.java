package com.dukeai.android.models;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ProcessedDocumentsFlipperDataModel extends ResponseModel implements Serializable {

    private Bitmap bm;

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }
}
