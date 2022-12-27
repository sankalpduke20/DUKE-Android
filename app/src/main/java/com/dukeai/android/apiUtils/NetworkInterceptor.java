package com.dukeai.android.apiUtils;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.auth0.android.jwt.JWT;
import com.dukeai.android.Duke;
import com.dukeai.android.R;
import com.dukeai.android.interfaces.PopupActions;
import com.dukeai.android.interfaces.SessionCallback;
import com.dukeai.android.utils.AppConstants;
import com.dukeai.android.utils.ConfirmationComponent;
import com.dukeai.android.utils.UserConfig;
import com.dukeai.android.utils.Utilities;

import java.io.IOException;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkInterceptor implements Interceptor {

    String userName, password;
    ConfirmationComponent confirmationComponent;
    PopupActions popupActions;
    Response response;
    private UserConfig userConfig = UserConfig.getInstance();

    @Override
    public Response intercept(final Chain chain) throws IOException {
        if (!new ServiceManager(Duke.getInstance()).isNetworkAvailable()) {
            throw (new IOException(Duke.getInstance().getString(R.string.no_internet)));
        } else {
            final Request originalRequest = chain.request();
            final Headers headers = originalRequest.headers();
            if (headers != null && headers.get(AppConstants.StringConstants.AUTHORIZATION) != null && headers.get(AppConstants.StringConstants.AUTHORIZATION).length() > 0 && !Duke.isWithOutToken) {
                String token = headers.get(AppConstants.StringConstants.AUTHORIZATION);
                JWT jwt = new JWT(token);
                if (jwt.getExpiresAt() != null && jwt.getExpiresAt().getTime() < new Date().getTime()) {
                    NewUserSession.getNewSession(new SessionCallback() {
                        @Override
                        public void processDone(CognitoUserSession session) {
                            if (session != null) {
                                Request.Builder requestBuilder = originalRequest.newBuilder()
                                        .header(AppConstants.StringConstants.AUTHORIZATION, session.getIdToken().getJWTToken()).method(originalRequest.method(), originalRequest.body());
                                Request request = requestBuilder.build();
                                try {
                                    response = chain.proceed(request);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Utilities.sendLogoutBroadcast();
                            }
                        }
                    });
                } else {
                    response = chain.proceed(chain.request());
                }
            } else {
                response = chain.proceed(chain.request());
            }
        }
        return response;
    }

}
