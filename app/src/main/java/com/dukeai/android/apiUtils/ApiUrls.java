package com.dukeai.android.apiUtils;

import com.dukeai.android.BuildConfig;

public class ApiUrls {
    // URL syntax: "{BASE_ENDPOINT}{API_STAGE}{suffix endpoint(s)...}"

    public static final String BASE_URL = BuildConfig.URL;
//    public static final String BASE_URL = BuildConfig.URL + "api/";

    // PRODUCTION#2 ROUTES
//    public static final String DUKE_API_BASE = "https://cjuwzz07ri.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String DOCUMENT_API_BASE = "https://7vp2cc1gxd.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String LOAD_API_BASE = "https://wrrflng4q0.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String REPORT_API_BASE = "https://m4rvl2hkdh.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String DUKE_PLAID_BASE = "https://u967b4j2o6.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String PAY_API_BASE = "https://ztxk4o75s1.execute-api.us-east-1.amazonaws.com/api/";
//    public static final String TRANSMIT_DOCS_API = "https://lwrh9g6xo1.execute-api.us-east-1.amazonaws.com/api/";

    // DEVELOPMENT ROUTES (New production routes for BEMO-4)
//    public static final String DUKE_API_BASE = "https://8kp4o32tvh.execute-api.us-east-1.amazonaws.com/dev/"; // Oh boy...
    public static final String DUKE_API_BASE = "https://25g071tuih.execute-api.us-east-1.amazonaws.com/api/"; // Oh boy...
    public static final String DUKE_API_BASE_DOWNLOAD_REPORT = "https://bqlr44p503.execute-api.us-east-1.amazonaws.com/dev/"; // Oh boy...
    public static final String DOCUMENT_API_BASE = "https://vnv960yw3a.execute-api.us-east-1.amazonaws.com/dev/"; // Oh boy
    public static final String LOAD_API_BASE = "https://onga2hgadl.execute-api.us-east-1.amazonaws.com/dev/"; // da, is good
    //For Testing
//    public static final String REPORT_API_BASE = "https://eu8973rww2.execute-api.us-east-1.amazonaws.com/dev/";
    public static final String REPORT_API_BASE = "https://v1vyuybr3c.execute-api.us-east-1.amazonaws.com/dev/"; // da, is good
    public static final String DUKE_PLAID_BASE = "https://9w8rimjg1i.execute-api.us-east-1.amazonaws.com/dev/"; // da, is good
    public static final String PAY_API_BASE = "https://haxd6a1hca.execute-api.us-east-1.amazonaws.com/dev/"; // yeesh...
    public static final String TRANSMIT_DOCS_API = "https://bqlr44p503.execute-api.us-east-1.amazonaws.com/dev/"; // da, is good
//    public static final String MANAGE_BANK_CONNECTIONS = "https://duke.ai/accountant-portal/dev/login.html";
    public static final String MANAGE_BANK_CONNECTIONS = "https://j94p6hd717.execute-api.us-east-1.amazonaws.com/api/";

//     DEPRECATED -ALEX 0.
//     public static final String ALPHA_STAGING_ENDPOINT = "https://ppmzk22avh.execute-api.us-east-2.amazonaws.com/dev/";
//     public static final String PRODUCTION_STAGING_ENDPOINT = "https://cjuwzz07ri.execute-api.us-east-1.amazonaws.com/api/";
//     public static final String DEV_STAGING_ENDPOINT = "https://temsqvr95c.execute-api.us-east-2.amazonaws.com/dev/";
//     public static final String LOADS_TEST_ENDPOINT = "https://temsqvr95c.execute-api.us-east-2.amazonaws.com/dev/";
//     public static final String SLIM_API_ENDPOINT = "https://tn2pq3jwlk.execute-api.us-east-2.amazonaws.com/dev/";




    // [ALEX]: Mobile device only?
    public class DeviceToken {
        public static final String UPDATE_DEVICE_TOKEN = BASE_URL + "{cust_id}";
        public static final String DELETE_DEVICE_TOKEN = BASE_URL + "{cust_id}";
    }

    // [ALEX]: Mobile device only?
    public class MigrateUser {
        public static final String MIGRATE_USER_TO_DUKE = BASE_URL + "{cust_id}" + "/cognito";
    }

    // Upload uses DUKE-API
    public class MultiFileUpload {
           public static final String MULTI_FILE_UPLOAD = DUKE_API_BASE + "{cust_id}" + "/multiupload";
           public static final String NEW_MULTI_UPLOAD = DUKE_API_BASE + "{cust_id}" + "/multiupload_prime";
           public static final String GENERATE_MANIFEST = DUKE_API_BASE + "{cust_id}" + "/multiupload/gen_manifest";
//         public static final String MULTI_FILE_UPLOAD = ALPHA_STAGING_ENDPOINT + "{cust_id}" + "/multiupload";
//         public static final String NEW_MULTI_UPLOAD = ALPHA_STAGING_ENDPOINT + "{cust_id}" + "/multiupload_prime";
//         public static final String GENERATE_MANIFEST = ALPHA_STAGING_ENDPOINT + "{cust_id}" + "/multiupload/gen_manifest";
    }

    public class FileStatus {
        // status uses DUKE-API
        public static final String FILE_STATUS = DUKE_API_BASE + "{cust_id}" + "/status";
//      public static final String FILE_STATUS = ALPHA_STAGING_ENDPOINT + "{cust_id}" + "/status";

        public static final String FILE_DOWNLOAD = DUKE_API_BASE + "{cust_id}" + "/" + "{filename}";
        public static final String DOWNLOAD_REPORT = DUKE_API_BASE_DOWNLOAD_REPORT + "{cust_id}" + "/" + "download_report";
        public static final String DELETE_FILE = BASE_URL + "{cust_id}" + "/" + "{sha1}" + "/fromuser";
//      public static final String FILE_DOWNLOAD = BASE_URL + "{cust_id}" + "/" + "{filename}";
//      public static final String DELETE_FILE = BASE_URL + "{cust_id}" + "/" + "{sha1}" + "/fromuser";
    }

    public class PromoCode {
        public static final String PROMO_CODE_CHECK = DUKE_API_BASE + "promocodecheck";
//         public static final String PROMO_CODE_CHECK = BASE_URL + "promocodecheck";
    }

    public class Loads {
        public static final String CREATE_LOAD = LOAD_API_BASE + "{cust_id}" + "/create_load";
        public static final String RECIPIENTS_LIST = LOAD_API_BASE + "{cust_id}" + "/get_load_recipients";
        public static final String ADD_RECIPIENT = LOAD_API_BASE + "{cust_id}" + "/update_load_recipient";
        public static final String UPDATE_RECIPIENT = LOAD_API_BASE + "{cust_id}" + "/update_load_recipient";
        public static final String USER_LOADS = LOAD_API_BASE + "{cust_id}" + "/get_loads";
        public static final String DELETE_LOAD_OBJECT = LOAD_API_BASE + "{cust_id}" + "/" + "{loadUUID}" + "/" + "delete_load_object";
        public static final String DELETE_FROM_LOAD = LOAD_API_BASE + "{cust_id}" + "/" + "{loadUUID}" + "/" + "delete_from_load";
        public static final String ADD_TO_LOAD = LOAD_API_BASE + "{cust_id}" + "/" + "{loadUUID}" + "/" + "add_to_load";
        public static final String GET_LOAD_DETAIL = LOAD_API_BASE + "{cust_id}" + "/" + "{loadUUID}" + "/" + "get_load_detail";
        public static final String TRANSMIT_LOADS = LOAD_API_BASE + "{cust_id}" + "/transmit_load";
        public static final String TRANSMIT_PROCESSED_DOCS = TRANSMIT_DOCS_API + "{cust_id}" + "/download";
//         public static final String CREATE_LOAD = LOADS_TEST_ENDPOINT + "{cust_id}" + "/create_load";
//         public static final String RECIPIENTS_LIST = LOADS_TEST_ENDPOINT + "{cust_id}" + "/get_load_recipients";
//         public static final String ADD_RECIPIENT = LOADS_TEST_ENDPOINT + "{cust_id}" + "/update_load_recipient";
//         public static final String UPDATE_RECIPIENT = LOADS_TEST_ENDPOINT + "{cust_id}" + "/update_load_recipient";
//         public static final String USER_LOADS = LOADS_TEST_ENDPOINT + "{cust_id}" + "/get_loads";
//         public static final String DELETE_LOAD_OBJECT = LOADS_TEST_ENDPOINT + "{cust_id}" + "/" + "{loadUUID}" + "/" + "delete_load_object";
//         public static final String DELETE_FROM_LOAD = LOADS_TEST_ENDPOINT + "{cust_id}" + "/" + "{loadUUID}" + "/" + "delete_from_load";
//         public static final String ADD_TO_LOAD = LOADS_TEST_ENDPOINT + "{cust_id}" + "/" + "{loadUUID}" + "/" + "add_to_load";
//         public static final String GET_LOAD_DETAIL = LOADS_TEST_ENDPOINT + "{cust_id}" + "/" + "{loadUUID}" + "/" + "get_load_detail";
//         public static final String TRANSMIT_LOADS = LOADS_TEST_ENDPOINT + "{cust_id}" + "/transmit_load";
    }

    public class GetFinancialSummary {
        //FOR TESTING
//        public static final String FINANCIAL_SUMMARY = REPORT_API_BASE + "{cust_id}" + "/summary";
        public static final String FINANCIAL_SUMMARY = REPORT_API_BASE + "{cust_id}" + "/summary";
        public static final String GENERATE_REPORT = REPORT_API_BASE + "{cust_id}" + "/gen_report";
//         public static final String FINANCIAL_SUMMARY = BASE_URL + "{cust_id}" + "/summary";
//         public static final String GENERATE_REPORT = SLIM_API_ENDPOINT + "{cust_id}" + "/gen_report";
    }

    public class IFTA {
        public static final String URL = DUKE_API_BASE + "{cust_id}" + "/ifta";
        public static final String IFTA_CHECK = DUKE_API_BASE + "{cust_id}" + "/iftacheck";
//         public static final String URL = BASE_URL + "{cust_id}" + "/ifta";
//         public static final String IFTA_CHECK = BASE_URL + "{cust_id}" + "/iftacheck";
    }

    public class UserRegistration {
        public static final String DELETE_UNCONFIRMED = DUKE_API_BASE + "{cust_id}/unconfirmed";
        // [ALEX-WARN]: Below route does not match to any API
        public static final String UPDATE_USER_GROUP = DUKE_API_BASE + "{cust_id}";
        public static final String FORGOT_PASSWORD = DUKE_API_BASE + "{cust_id}/forgetPassword";
//         public static final String DELETE_UNCONFIRMED = BASE_URL + "{cust_id}/unconfirmed";
//         public static final String UPDATE_USER_GROUP = BASE_URL + "{cust_id}";
//         public static final String FORGOT_PASSWORD = BASE_URL + "{cust_id}/forgetPassword";
    }

    public class AppUpdate {
        public static final String URL = DUKE_API_BASE + "android/app_update";
//         public static final String URL = BASE_URL + "android/app_update";
    }

    public class FederalDeduction {
        public static final String URL = REPORT_API_BASE + "{cust_id}" + "/tax_info/tax_info";
//         public static final String URL = BASE_URL + "{cust_id}" + "/tax_info/tax_info";
    }

    public class AssetDeduction {
        public static final String URL = REPORT_API_BASE + "{cust_id}" + "/tax_info/assets_info";
//         public static final String URL = BASE_URL + "{cust_id}" + "/tax_info/assets_info";
    }

    public class BalanceSheet {
        public static final String URL = REPORT_API_BASE + "{cust_id}" + "/tax_info/cash_info";
//         public static final String URL = BASE_URL + "{cust_id}" + "/tax_info/cash_info";
    }

    public class MemberStatusUpdate {
        public static final String URL = PAY_API_BASE + "{cust_id}" + "/app_payment";
//         public static final String URL = "https://ztxk4o75s1.execute-api.us-east-1.amazonaws.com/api/" + "{cust_id}" + "/app_payment";
    }

    public class DocumentSignatureUpdate {
        public static final String URL = DUKE_API_BASE + "{cust_id}" + "/signature";
//         public static final String URL = "https://8kp4o32tvh.execute-api.us-east-1.amazonaws.com/dev/" + "{cust_id}" + "/signature";
    }

    public class DocumentDetails {
       public static final String URL = DOCUMENT_API_BASE + "{cust_id}" + "/" + "{sha1}";
//         public static final String URL = " https://aluzzhbxa4.execute-api.us-east-2.amazonaws.com/dev/" + "{cust_id}" + "/" + "{sha1}";
    }

    public class ManageBankConnections{
        public static final String URL = MANAGE_BANK_CONNECTIONS+"web_auth"+"/"+"{cust_id}"+"/"+"generate_access_token";
    }
}