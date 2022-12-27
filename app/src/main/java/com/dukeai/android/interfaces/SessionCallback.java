package com.dukeai.android.interfaces;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;

public interface SessionCallback {
    void processDone(CognitoUserSession session);
}
