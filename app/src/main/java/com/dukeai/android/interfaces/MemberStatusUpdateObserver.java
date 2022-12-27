package com.dukeai.android.interfaces;

import com.dukeai.android.models.UpdatePaymentModel;

public interface MemberStatusUpdateObserver {
    void onChanged(String status, UpdatePaymentModel updatePaymentModel);
}
