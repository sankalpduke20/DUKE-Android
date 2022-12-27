package com.dukeai.android.utils;

import com.dukeai.android.Duke;
import com.dukeai.android.R;

public class UIMessages {

    public static String getMessage(String messageType, String key) {
        switch (messageType) {
            case AppConstants.StringConstants.ERROR:
                return getErrorMessages(key);
            case AppConstants.StringConstants.HINT:
                return getHintMessages(key);
        }
        return null;
    }

    private static String getErrorMessages(String key) {
        int resourceId = -1;

        switch (key.toLowerCase()) {
            case "email":
                resourceId = R.string.please_enter_valid_email_address;
                break;
            case "password":
                resourceId = R.string.please_enter_valid_password;
                break;
            case "username":
                resourceId = R.string.please_enter_valid_username;
                break;
            case "phone":
                resourceId = R.string.please_enter_valid_phone_number;
                break;
            case "code":
                resourceId = R.string.please_enter_valid_code;
                break;
            case "oldPassword":
                resourceId = R.string.invalid_password;
                break;
            case "newPassword":
                resourceId = R.string.invalid_password;
                break;
            case "decimalnum":
                resourceId = R.string.invalid_amount;
                break;
            case "promo":
                resourceId = R.string.please_enter_valid_promo_code;
                break;
            case "alphanumeric":
                resourceId = R.string.please_enter_alpha_numeric_value;
                break;
            case "nonempty":
                resourceId = R.string.please_enter_value;
                break;
            case "nonemptydate":
                resourceId = R.string.please_enter_date;
                break;
            case "descempty":
                resourceId = R.string.please_enter_value;
                break;
            case "companyname":
                resourceId = R.string.valid_company_name;
                break;
        }
        return getString(resourceId);
    }

    private static String getHintMessages(String key) {
        return null;
    }

    private static String getString(int res) {
        return Utilities.getStrings(Duke.getInstance(), res);
    }


}