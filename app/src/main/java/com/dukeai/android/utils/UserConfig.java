package com.dukeai.android.utils;

import com.dukeai.android.models.UserDataModel;

public class UserConfig {
    private static UserConfig userConfig = new UserConfig();
    private UserDataModel userDataModel;

    public static UserConfig getInstance() {
        return userConfig;
    }

    public static UserConfig getUserConfig() {
        return userConfig;
    }

    public static void setUserConfig(UserConfig userConfig) {
        UserConfig.userConfig = userConfig;
    }

    public UserDataModel getUserDataModel() {
        return userDataModel;
    }

    public void setUserDataModel(UserDataModel userDataModel) {
        this.userDataModel = userDataModel;
    }
}
