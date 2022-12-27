package com.dukeai.android.utils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.apiUtils.ApiUrls;

public class URLBuilder {

    public static GlideUrl getGlideUrl(String path) {

        UserConfig userConfig = UserConfig.getInstance();
        String userName = userConfig.getUserDataModel().getUserEmail();

        GlideUrl imageURL = new GlideUrl(ApiUrls.BASE_URL + userName + "/" + path,
                new LazyHeaders.Builder()
                        .addHeader("Authorization", userConfig.getUserDataModel().getStoredJWTToken())
                        .addHeader("Content-Type", ApiConstants.DownloadFile.CONTENT_TYPE)
                        .addHeader("Accept", ApiConstants.DownloadFile.ACCEPT)
                        .build());
        return imageURL;
    }
}
