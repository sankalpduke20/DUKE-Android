package com.dukeai.android.apiUtils;

import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.utils.Utilities;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ApiUtils {
    public static String getApiError(String response) throws Exception {
        if (response == null || response.length() <= 1) {
            return Utilities.getStrings(Duke.getInstance(), R.string.invalid_error);
        }
        JsonObject errorObject = new JsonParser().parse(response).getAsJsonObject();
        // Throw the API error and return the message for futher use
        if (errorObject.has(ApiConstants.ERRORS.CODE)) {
            return errorObject.get(ApiConstants.ERRORS.MESSAGE).getAsString();
        } else if (errorObject.has(ApiConstants.ERRORS.MESSAGE)) {
            return errorObject.get(ApiConstants.ERRORS.MESSAGE).getAsString();
        } else if (errorObject.has(ApiConstants.ERRORS.MESSAGE_TOKEN_EXPIRY)) {
            return errorObject.get(ApiConstants.ERRORS.MESSAGE_TOKEN_EXPIRY).getAsString();
        }
        return Utilities.getStrings(Duke.getInstance(), R.string.invalid_error);
    }

    public static String getFailureErrorString(Throwable t) {
        if (!new ServiceManager(Duke.getInstance()).isNetworkAvailable()) {
            return Duke.getInstance().getString(R.string.no_internet);
        } else {
            return t.getMessage();
        }
    }
}
