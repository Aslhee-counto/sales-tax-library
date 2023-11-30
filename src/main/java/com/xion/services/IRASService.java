package com.xion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import com.xion.config.IRASProperties;
import com.xion.data.F5Form;
import com.xion.data.Transaction;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class IRASService {

    private static Logger logger = Logger.getLogger(IRASService.class.getName());

    @Autowired
    private IRASProperties properties;
    @Autowired
    private StoreService storeService;
    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${iras.submitConfig.f5.run}")
    private boolean f5Run;
    @Value("${iras.submitConfig.f5.tries}")
    private int f5Tries;
    @Value("${iras.submitConfig.f5.continueOnError}")
    private boolean continueOnError;
    @Value("${iras.submitConfig.transactions.run}")
    private boolean transactionRun;
    @Value("${iras.submitConfig.transactions.tries}")
    private int transactionTries;
    @Value("${iras.submitConfig.auth.tries}")
    private int authTries;

    public void updateF5(F5Form form, String rawUpdate, String legalName, String uuid) throws Exception {
        logger.info("updateF5 called");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(rawUpdate, Map.class);
            logger.info(parsed.toString());
//            if (System.getProperty("mode").equals("dev")) {
//                form.setTaxRefNo("198300477M");
//                form.setGstRefNo("198300477M");
//            }

            Map<String, String> supplies = (Map<String, String>) parsed.get("supplies");
            form.setBox1(Double.valueOf(supplies.get("totStdSupply")));
            form.setBox2(Double.valueOf(supplies.get("totZeroSupply")));
            form.setBox3(Double.valueOf(supplies.get("totExemptSupply")));

            Map<String, String> purchases = (Map<String, String>) parsed.get("purchases");
            form.setBox5(Double.valueOf(purchases.get("totTaxPurchase")));

            Map<String, String> taxes = (Map<String, String>) parsed.get("taxes");
            form.setBox6(Double.valueOf(taxes.get("outputTaxDue")));
            form.setBox7(Double.valueOf(taxes.get("inputTaxRefund")));

            Map<String, String> schemes = (Map<String, String>) parsed.get("schemes");
            form.setBox9(Double.valueOf(schemes.get("totValueScheme")));
            form.setBox10(Boolean.valueOf(schemes.get("touristRefundChk")));
            form.setBox11(Boolean.valueOf(schemes.get("badDebtChk")));
            form.setBox11Amt(Double.valueOf(schemes.get("badDebtReliefClaimAmt")));
            form.setBox12(Boolean.valueOf(schemes.get("preRegistrationChk")));

            Map<String, String> revenue = (Map<String, String>) parsed.get("revenue");
            form.setBox13(Double.valueOf(revenue.get("revenue")));

            Map<String, String> igdScheme = (Map<String, String>) parsed.get("igdScheme");
            form.setBox15(Double.valueOf(igdScheme.get("defImpPayableAmt")));
            form.setBox17(Double.valueOf(igdScheme.get("defTotalGoodsImp")));

            Map<String, String> reasons = (Map<String, String>) parsed.get("reasons");
            form.setGrp1BadDebtRecoveryChk(Boolean.valueOf(reasons.get("grp1BadDebtRecoveryChk")));
            form.setGrp1PriorToRegChk(Boolean.valueOf(reasons.get("grp1PriorToRegChk")));
            form.setGrp1OtherReasonChk(Boolean.valueOf(reasons.get("grp1OtherReasonChk")));
            form.setGrp1OtherReasons(removeInvalidChars(reasons.get("grp1OtherReasons")));
            form.setGrp2TouristRefundChk(Boolean.valueOf(reasons.get("grp2TouristRefundChk")));
            form.setGrp2AppvBadDebtReliefChk(Boolean.valueOf(reasons.get("grp2AppvBadDebtReliefChk")));
            form.setGrp2CreditNotesChk(Boolean.valueOf(reasons.get("grp2CreditNotesChk")));
            form.setGrp2OtherReasonsChk(Boolean.valueOf(reasons.get("grp2OtherReasonsChk")));
            form.setGrp2OtherReasons(removeInvalidChars(reasons.get("grp2OtherReasons")));
            form.setGrp3CreditNotesChk(Boolean.valueOf(reasons.get("grp3CreditNotesChk")));
            form.setGrp3OtherReasonsChk(Boolean.valueOf(reasons.get("grp3OtherReasonsChk")));
            form.setGrp3OtherReasons(removeInvalidChars(reasons.get("grp3OtherReasons")));

        } catch (NullPointerException e) {
            logger.severe("NPE in updateF5");
            logger.severe("RAW JSON -->");
            logger.severe(rawUpdate);
            logger.severe("<-- RAW JSON");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.severe("Error in updateF5");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    public boolean submit(F5Form f5, String legalName, String code, String formType) throws Exception {
        logger.info("submit called for legalName: " + legalName);
        try {

            String token = properties.isCorpPass() ?
                    retrieveTokenCorpPass(legalName, code) :
                    retrieveToken(legalName, code);

            if(f5Run) {
                for (int i = 0; i < f5Tries; i++) {
                    try {
                        logger.info("in f5 loop");
                        submitF5Authenticated(f5, legalName, token, formType);
                        break;
                    } catch (Exception e) {
                        logger.warning(e.getMessage());
                        logger.warning("error thrown in f5 loop");
                        logger.warning("JSON: " + f5);
                        if (i == (f5Tries-1)){
                            if (continueOnError)
                                break;
                            throw e;
                        }
                    }
                }
            }

            if (transactionRun && formType.toLowerCase().equals("f5")) {
                for (int i = 0; i < transactionTries; i++) {
                    try {
                        logger.info("in transaction loop");
                        submitTransactionsAuthenticated(f5, legalName, token);
                        break;
                    } catch (Exception e) {
                        if (i == (transactionTries-1)) return false;
                    }
                }
            }

            return true;

        } catch (NullPointerException e) {
            logger.severe("NPE in submit of legalName: " + legalName);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.severe("Error in submit of legalName: " + legalName);
            logger.severe(e.getMessage());
            throw e;
        }
    }

    public void submitF5Authenticated(F5Form f5, String legalName, String token, String formType) throws Exception {
        logger.info("Attempting to submit F5 for " + legalName);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        String f5form = f5.buildF5Form(formType);
        logger.info("==ENDDATE: " + f5.getEndDate());

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), f5form
        );

        logger.info("f5 submission body: ");
        logger.info(f5form.replace("\n", ""));

        Request request = new Request.Builder()
                .url(properties.isCorpPass() ? properties.getAuthF5Url() : properties.getAuthF5UrlSingPass())
                .post(body)
                .addHeader("x-ibm-client-id", properties.isCorpPass() ? properties.getId() : properties.getIdSingPass())
                .addHeader("x-ibm-client-secret", properties.isCorpPass() ? properties.getSecret() : properties.getSecretSingPass())
                .addHeader("access_token", token)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        if (response.code() != 200) {
            logger.severe(response.code() + ": " + response.message());
            logger.severe(responseBody);
            logger.severe("Body: " + f5.buildF5Form(formType));
            throw new Exception(responseBody);
        }
        logger.info("RESPONSE: ");
        logger.info(response.code() + ": " + response.message());
        logger.info(responseBody);
        checkForIRAS30Code(responseBody);
    }


    //Submission of GST Transaction Listings (CorpPass)-apigw
    public void submitTransactionsAuthenticated(F5Form f5, String legalName, String token) throws Exception {
        logger.info("Attempting to submit transactions for " + legalName);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        for (String transaction : f5.buildTransactionsForm()) {

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), transaction);

            logger.info("transaction submission body: ");
            logger.info(transaction);

            int supplyNum = 0;
            int purchaseNum = 0;

            for(Transaction t : f5.getTransactions()){
                if (t.isSupply())
                    supplyNum = supplyNum +1;
                else
                    purchaseNum = purchaseNum +1;
            }

            logger.info("supply: " + supplyNum + ", purchase: " + purchaseNum);


            Request request = new Request.Builder()
                    .url(properties.isCorpPass() ? properties.getAuthTransactionsUrl() : properties.getAuthTransactionsUrlSingPass())
                    .post(body)
                    .addHeader("x-ibm-client-id", properties.isCorpPass() ? properties.getId() : properties.getIdSingPass())
                    .addHeader("x-ibm-client-secret", properties.isCorpPass() ? properties.getSecret() : properties.getSecretSingPass())
                    .addHeader("access_token", token)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.code() != 200) {
                logger.severe(response.code() + ": " + response.message());
                logger.severe(responseBody);
                logger.severe("Body: " + f5.buildTransactionsForm());
            }
            logger.info("RESPONSE: ");
            logger.info(response.code() + ": " + response.message());
            logger.info(responseBody);
            checkForIRAS30Code(responseBody);
        }

    }

    private String retrieveToken(String legalName, String code) throws Exception {
        logger.info("Attempting to retrieve token for " + legalName);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(
                        "https://apisandbox.iras.gov.sg/iras/sb/Authentication/SingPassToken" +
                                "?scope=" + "GSTReturnsSub+GSTTransListSub" +
                                "&callback_url=" + properties.getCallbackSingPass() +
                                "&code=" + code
                )
                .get()
                .addHeader("content-type", "application/json")
                .addHeader("x-ibm-client-id", properties.getIdSingPass())
                .addHeader("x-ibm-client-secret", properties.getSecretSingPass())
                .addHeader("accept", "application/json")
                .build();

        String token = "";
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            logger.severe("Call errored with code " + response.code());
            throw new IOException("Call errored with code " + response.code());
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> json = objectMapper.readValue(response.body().string().trim(), Map.class);
            Map<String, String> data = (Map) json.get("data");
            token = data.get("token");
            logger.info("token successfully loaded");
        } catch (Exception e) {
            logger.severe("error parsing return body");
            throw new IOException(e);
        }

        return token;

    }

    public String callSingPathAuth(String legalName) throws Exception {
        logger.info("Attempting to call SingPassAuth");
        try {

            OkHttpClient client = new OkHttpClient();

            String url = "";
            Request request = new Request.Builder()
                    .url(
                            "https://apisandbox.iras.gov.sg/iras/sb/Authentication/SingPassAuth" +
                                    "?scope=" + "GSTReturnsSub+GSTTransListSub" +
                                    "&callback_url=" + properties.getCallbackSingPass() +
                                    "&state=" + defineProfile() + legalName
                    )
                    .get()
                    .addHeader("x-ibm-client-id", properties.getIdSingPass())
                    .addHeader("x-ibm-client-secret", properties.getSecretSingPass())
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.severe("Call errored with code " + response.code());
                throw new IOException("Call errored with code " + response.code());
            }
            try {
                String body = response.body().string();
                logger.info("RESPONSE BODY: \n" + body);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> json = objectMapper.readValue(body, Map.class);
                Map<String, String> data = (Map) json.get("data");
                url = data.get("url");

            } catch (Exception e) {
                logger.severe("error parsing return body");
                throw new IOException(e);
            }

            return url;
        } catch (NullPointerException e) {
            logger.severe("NPE while connecting to IRAS");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.severe("Error in IRAS login");
            logger.severe(e.getMessage());
            if (e.getMessage().equals("timeout"))
                throw new Exception("Timeout Error connecting to SingPass, please try again.");
            else throw e;
        }
    }

    //CorpPass Authentication-apigw
    private String retrieveTokenCorpPass(String legalName, String code) throws Exception {
        logger.info("Attempting to call CorpPassToken for " + legalName);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        JsonObject request = new JsonObject();
//        request.addProperty("scope", "GSTF5F8SubCP+GSTTxnLstgSubCP");
        request.addProperty("scope", "GSTF5F8SubCP");
        request.addProperty("callback_url", properties.getCallback());
        request.addProperty("code", code);
        request.addProperty("state",  defineProfile() + legalName);

        logger.info("Access Token Request URL = " + properties.getCorpPassTokenUrl());
        logger.info("Access Token Request Body = " + request.toString());
        logger.info("Access Token Request Headers = " + "X-IBM-Client-Id: " + properties.getId() + ", X-IBM-Client-Secret: NOT PRINTED, Content-Type: application/json");

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType, request.toString());

        Request okRequest = new Request.Builder()
                .url(properties.getCorpPassTokenUrl())
                .method("POST", body)
                .addHeader("X-IBM-Client-Id", properties.getId())
                .addHeader("X-IBM-Client-Secret", properties.getSecret())
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(okRequest).execute();
        String token = "";
        if (!response.isSuccessful()) {
            logger.severe("Call errored with code " + response.code());
            logger.severe(new ObjectMapper().writeValueAsString(response));
            throw new IOException("Call errored with code " + response.code());
        } else {
            try{
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> json = objectMapper.readValue(response.body().string().trim(), Map.class);
                if(Integer.parseInt(json.get("returnCode").toString()) == 10) {
                    Map<String, String> data = (Map) json.get("data");
                    token = data.get("token");
                    logger.info("token successfully loaded");
                } else {
                    String error = new ObjectMapper().writeValueAsString(json);
                    logger.severe(error);
                    throw new IOException(error);
                }

            } catch (Exception e) {
                logger.severe("error parsing return body");
                throw new IOException(e);
            }
        }
        return token;
    }

    public String callCorpPathAuth(String legalName, String form) throws Exception {
        logger.info("Attempting to call CorpPassAuth for " + legalName);
        try {

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            String url = properties.getCorpPassAuthUrl() +
                    (form.equalsIgnoreCase("f5") ? ("?scope=" + "GSTF5F8SubCP+GSTTxnLstgSubCP") :
                            ("?scope=" + "GSTF5F8SubCP")) +
                    "&callback_url=" + properties.getCallback() +
                    "&state=" + defineProfile() + legalName +
                    "&tax_agent=" + (form.equalsIgnoreCase("f5") ? "false" : "true");
            logger.info(url);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-ibm-client-id", properties.getId())
                    .addHeader("x-ibm-client-secret", properties.getSecret())
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.severe("Call errored with code " + response.code());
                logger.severe(new ObjectMapper().writeValueAsString(response));
                throw new IOException("Call errored with code " + response.code());
            }
            try {
                String body = response.body().string();
                logger.info("RESPONSE BODY: \n" + body);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> json = objectMapper.readValue(body, Map.class);
                Map<String, String> data = (Map) json.get("data");
                url = data.get("url");

            } catch (Exception e) {
                logger.severe("error parsing return body");
                throw new IOException(e);
            }

            return url;
        } catch (NullPointerException e) {
            logger.severe("NPE while connecting to IRAS");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            logger.severe("Error in IRAS login");
            logger.severe(e.getMessage());
            if (e.getMessage().equals("timeout"))
                throw new Exception("Timeout Error connecting to CorpPass, please try again.");
            else throw e;
        }
    }

    private String removeInvalidChars(String initial){
        return initial
                .replace(":", "")
                .replace("\"", "")
                .replace("{", "")
                .replace("}", "")
                .replace("|", "")
                .replace("\\", "");
    }

    private void checkForIRAS30Code(String body) throws Exception{
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(body, Map.class);
            String returnCode = (String) map.get("returnCode");
            logger.info("return code debug --> " + returnCode);
            if (!returnCode.equals("10")){
                String msg = "submission failure, code: " + returnCode;
                logger.severe(msg);
                throw new Exception(msg);
            }
            return;
        }catch (IOException e){
            logger.severe(e.getMessage());
            throw new Exception("Could not parse response body from IRAS");
        }catch (Exception e){
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private String defineProfile(){
        return activeProfile.equals("prod") ?
                "" :
                activeProfile + "_";
    }
}
