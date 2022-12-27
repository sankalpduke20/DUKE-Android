package com.dukeai.android.interfaces;

import com.dukeai.android.models.DocumentDetailsModel;

public interface DocumentDetailsUpdateObserver {
    void onChanged(String status, DocumentDetailsModel documentDetailsModel);
}
