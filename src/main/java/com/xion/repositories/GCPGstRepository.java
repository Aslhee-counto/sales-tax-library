package com.xion.repositories;

import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.xion.data.F5Form;
import com.xion.exceptions.GstException;
import com.xion.fx.FXRecord;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;

@Repository
public class GCPGstRepository implements GstRepository {


    @Value("${spring.profiles.active}")
    private String activeProfile;

    private static Logger logger = Logger.getLogger(GCPGstRepository.class.getName());
    private static final String PRE = "gst_";
    private static final String FXPREFIX = "fx_";
    private static final String JSON = ".json";
    private static final String PDF = ".pdf";
    private static final String XLSX = ".xlsx";
    private static final String REGION = "asia-east2";
//    private static final String REGION = "asia-southeast1";

    private Storage storage;

    public GCPGstRepository(){
        try {
            if (System.getProperty("hostingEnv").equals("GCP")) {
                GoogleCredentials credentials = ComputeEngineCredentials.create();
                storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                logger.info("STORAGE: " + storage.toString());
                logger.info("CREDENTIALS: " + credentials.toString());

            }else
                storage = null;
        }catch (RuntimeException e){
            logger.severe("Error initializing vault client");
        }
    }

    @Override
    public boolean storeF5(String legalName, String uuid, F5Form f5Form) throws GstException {
        logger.info("Attempting to store payload for " + legalName + " and " + uuid);

        try {
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
            if(bucket!=null){
                InputStream is = new ByteArrayInputStream(f5Form.toString().getBytes( "UTF-8"));
                bucket.create(uuid + JSON, is);
                logger.info("created f5 with " + f5Form.getTransactions() + " transactions");
                return true;
            }else{
                logger.info("Company " + legalName + " does not exist yet, generating bucket");
                bucket = storage.create(BucketInfo.newBuilder(genPrefix()+legalName)
                        .setStorageClass(StorageClass.STANDARD)
                        .setLocation(REGION)
                        .build());
                InputStream is = new ByteArrayInputStream(f5Form.toString().getBytes( "UTF-8"));
                bucket.create(uuid + JSON, is);
                logger.info("created f5 with " + f5Form.getTransactions() + " transactions");
                return true;
            }
        }catch (Exception e) {
            logger.severe(e.getMessage());
            logger.severe("Error storing F5 for " + legalName + " and " + uuid);
            return false;
        }
    }

    @Override
    public boolean storeFXRecord(String currency, FXRecord fxRecord) throws GstException {
        logger.info("storeFXRecord called for " + fxRecord.getDateOfName());
        try{
            Bucket bucket = storage.get(FXPREFIX + getEnv() + currency.toLowerCase(), Storage.BucketGetOption.fields());
            if(bucket!=null){
                InputStream is = new ByteArrayInputStream(fxRecord.toString().getBytes( "UTF-8"));
                bucket.create(fxRecord.getDateOfName()+JSON, is);
                logger.info("created fxRecord with " + fxRecord.getDateOfName());
                return true;
            }else{
                logger.info("Bucket " + FXPREFIX + getEnv() + currency.toLowerCase() + " does not exist yet, generating bucket");
                bucket = storage.create(BucketInfo.newBuilder(FXPREFIX + getEnv() + currency.toLowerCase())
                        .setStorageClass(StorageClass.STANDARD)
                        .setLocation(REGION)
                        .build());
                InputStream is = new ByteArrayInputStream(fxRecord.toString().getBytes( "UTF-8"));
                bucket.create(fxRecord.getDateOfName()+JSON, is);
                logger.info("created fxRecord with " + fxRecord.getDateOfName());
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.severe(e.getMessage());
            return false;
        }
    }

    @Override
    public F5Form loadF5(String legalName, String uuid) throws GstException {
        logger.info("Attempting to load payload for " + legalName + " and " + uuid);
        try {
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
            if(bucket!=null){
                logger.info("loadF5 -> bucket exists");
                String rawForm = new String(bucket.get(uuid+JSON).getContent());
                logger.info("loadF5 -> pulled raw form");
                return F5Form.parseF5(rawForm);
            }throw new GstException("No company " + legalName);
        }catch (GstException e){
            logger.severe("No company " + legalName);
            throw e;
        } catch (Exception e) {
            logger.severe("Error pulling F5 for " + legalName + " and " + uuid);
            throw new GstException(e);
        }

    }

    @Override
    public FXRecord loadFXRecord(String currency, Date date) throws GstException {
        logger.info("loadFXRecord called for " + date);
        try {
            Bucket bucket = storage.get(FXPREFIX + getEnv() + currency.toLowerCase(), Storage.BucketGetOption.fields());
            if(bucket!=null){
                logger.info("loadFXRecord -> bucket exists, Date: " + date);
                String rawRecord = new String(bucket.get(FXRecord.createDateOfName(date)+JSON).getContent());
                logger.info("loadFXRecord -> pulled raw form");
                return FXRecord.parseFXRecord(date, rawRecord);
            }throw new GstException("No bucket " + FXPREFIX + getEnv() + currency.toLowerCase());
        }catch (GstException e){
            logger.severe(e.getMessage());
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            logger.severe(e.getMessage());
            throw new GstException(e);
        }
    }

    @Override
    public boolean checkF5Exists(String legalName, String uuid, String fileType) throws GstException {
        logger.info("Checking if file exists for " + legalName + " and " + uuid);
        try{
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
            if(bucket!=null){
                if(bucket.get(uuid + fileType, Storage.BlobGetOption.fields())==null)
                    return false;
                return true;
            }else return false;

        }catch (Exception e){
            logger.severe("Error checking F5 for " + legalName + " and " + uuid);
            logger.severe(e.getMessage());
            throw new GstException( e );
        }
    }

    @Override
    public boolean storeF5PDF(String legalName, String uuid, InputStream pdf) throws GstException {
        logger.info("Attempting to store pdf for " + legalName + " and " + uuid);

//        if (checkF5Exists(legalName,uuid, PDF)){
//            try{
//                logger.info("removing old F5 pdf for " +legalName + " and " + uuid);
//                Blob blob = storage.get(PREFIX+legalName).get(uuid+PDF);
//                if(!blob.delete()){
//                    logger.info("did not delete blob");
//                    return false;
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//                logger.warn("could not delete old F5 pdf for " + legalName + " and " + uuid);
//                return false;
//            }
//        }

        try {
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
            if(bucket!=null){
                bucket.create(uuid + PDF, pdf);
                return true;
            }else{
                logger.info("Company " + legalName + " does not exist yet, generating bucket");
                bucket = storage.create(BucketInfo.newBuilder(legalName)
                        .setStorageClass(StorageClass.STANDARD)
                        .setLocation(REGION)
                        .build());
                bucket.create(uuid + PDF, pdf);
                return true;
            }
        }catch (Exception e) {
            logger.severe(e.getMessage());
            logger.severe("Error storing F5 pdf for " + legalName + " and " + uuid);
            return false;
        }
    }

    @Override
    public boolean storeTransactionsXlsx(String legalName, String uuid, Workbook xlsx) throws GstException {
        logger.info("Attempting to store xlsx for " + legalName + " and " + uuid);

//        if (checkF5Exists(legalName,uuid, XLSX)){
//            try{
//                logger.info("removing old xlsx for " +legalName + " and " + uuid);
//                Blob blob = storage.get(PREFIX+legalName).get(uuid+XLSX);
//                if(!blob.delete()){
//                    logger.info("did not delete blob");
//                    return false;
//                }
//            }catch (Exception e){
//                logger.warn("could not delete old xlsx file for " + legalName + " and " + uuid);
//                return false;
//            }
//        }

        try {
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            xlsx.write(bos);
            byte[] barray = bos.toByteArray();
            InputStream is = new ByteArrayInputStream(barray);

            if(bucket!=null){
                bucket.create(uuid + XLSX, is);
                return true;
            }else{
                logger.info("Company " + legalName + " does not exist yet, generating bucket");
                bucket = storage.create(BucketInfo.newBuilder(genPrefix()+legalName)
                        .setStorageClass(StorageClass.STANDARD)
                        .setLocation(REGION)
                        .build());
                bucket.create(uuid + XLSX, is);
                return true;
            }
        }catch (Exception e) {
            logger.severe(e.getMessage());
            logger.severe("Error storing xlsx for " + legalName + " and " + uuid);
            return false;
        }
    }
//    @Override
//    public Resource loadF5PDF(String legalName, String uuid) throws GstException {
//        logger.info("Attempting to load pdf for " + legalName + " and " + uuid);
//        try {
//            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
//            if(bucket!=null){
//
//                logger.info("==bucket: " + bucket.toString());
//
//                InputStream is = new ByteArrayInputStream(bucket.get(uuid+PDF).getContent());
//                logger.info("==inputStream bucket: " + bucket.get(uuid+PDF).getContent());
////
//                return new InputStreamResource(is);
//            }throw new GstException("No company " + legalName);
//        }catch (GstException e){
//            logger.severe("No company " + legalName);
//            throw e;
//        }catch (Exception e) {
//            logger.severe("Error pulling F5 pdf for " + legalName + " and " + uuid);
//            throw new GstException(e);
//        }
//    }
@Override
public Resource loadF5PDF(String legalName, String uuid) throws GstException {
    logger.info("Attempting to load pdf for " + legalName + " and " + uuid);
    try {
        Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
        if(bucket!=null){
            InputStream is = new ByteArrayInputStream(bucket.get(uuid+PDF).getContent());
            return new InputStreamResource(is);
        }throw new GstException("No company " + legalName);
    }catch (GstException e){
        logger.severe("No company " + legalName);
        throw e;
    }catch (Exception e) {
        logger.severe("Error pulling F5 pdf for " + legalName + " and " + uuid);
        throw new GstException(e);
    }
}

//    @Override
//    public Resource loadF5PDF(String legalName, String uuid) throws GstException {
//        logger.info("Attempting to load pdf for " + legalName + " and " + uuid);
//        try {
//            logger.info("TRY==22");
//            String bucketName = genPrefix() + legalName;
//            logger.info("Generated bucket name: " + bucketName);
//
////            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
////            logger.info("==bucket: " + bucket.toString());
//            Bucket bucket = storage.get(bucketName, Storage.BucketGetOption.fields());
//            logger.info("==bucket: " + bucket.toString());
//
//            String actualLegalName = bucketName.replace("gst_dev_", "");
//            logger.info("Actual legal name: " + actualLegalName);
//
//
//            logger.info("==bucket storage: " + storage.get(genPrefix()));
//            logger.info("==bucket bucketGet: " + Storage.BucketGetOption.fields());
//
//
//            if(bucket!=null){
//                logger.info("==bucket is not null");
//
//                InputStream is = new ByteArrayInputStream(bucket.get(uuid+PDF).getContent());
//                logger.info("==is: " + is);
//                logger.info("==inputStream bucket: " + bucket.get(uuid+PDF).getContent());
//
//
//                return new InputStreamResource(is);
//            }throw new GstException("No company " + legalName);
//        }catch (GstException e){
//            logger.severe("No company " + legalName);
//            throw e;
//        }catch (Exception e) {
//            logger.severe("Error pulling F5 pdf for " + legalName + " and " + uuid);
//            throw new GstException(e);
//        }
//    }
//@Override
//public Resource loadF5PDF(String legalName, String uuid) throws GstException {
//    logger.info("Attempting to load pdf for " + legalName + " and " + uuid);
//    try {
//        logger.info("TRY== FROM LOADF5PDF");
//        Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
//
//        // I-check kung null ang bucket
//        if (bucket == null) {
//            logger.severe("No bucket found for " + legalName);
//            throw new GstException("No company " + legalName);
//        }
//
//        logger.info("==bucket: " + bucket.toString());
//
//        logger.info("Retrieving content for " + uuid + PDF);
//        Blob blob = bucket.get(uuid+PDF);
//        byte[] content = bucket.get(uuid+PDF).getContent();
//        logger.info("==content " + content.toString());
//        logger.info("==bucket content " +bucket.get(uuid+PDF).getContent());
//        byte[] content2 = blob.getContent();
//
//        logger.info("==bucket content " +content2);
//
//
//        if (content == null) {
//            logger.severe("Content is null for " + uuid + PDF);
//            throw new GstException("Content not found for " + uuid + PDF);
//        }
//
//        String contentString = new String(content, StandardCharsets.UTF_8);
//        logger.info("PDF Content: " + contentString); // Note: This might not be readable for binary data like PDF
//
//        // Lumikha ng InputStream mula sa content
//        logger.info("Creating InputStream");
//        InputStream is = new ByteArrayInputStream(content);
//
//        logger.info("Creating InputStreamResource");
//        return new InputStreamResource(is);
//
//
//        // I-assume na hindi null ang bucket dito
////        InputStream is = new ByteArrayInputStream(bucket.get(uuid+PDF).getContent());
////        logger.info("==is: " + is);
////
////        return new InputStreamResource(is);
//
//    } catch (GstException e) {
//        logger.severe("No company " + legalName);
//        throw e;
//    } catch (Exception e) {
//        logger.severe("Error pulling F5 pdf for " + legalName + " and " + uuid);
//        throw new GstException(e);
//    }
//}


    @Override
    public Resource loadTransactionsXlsx(String legalName, String uuid) throws GstException {
        logger.info("Attempting to load xlsx for " + legalName + " and " + uuid);
        try {
            Bucket bucket = storage.get(genPrefix()+legalName, Storage.BucketGetOption.fields());
            if(bucket!=null){
                InputStream is = new ByteArrayInputStream(bucket.get(uuid+XLSX).getContent());
                return new InputStreamResource(is);
            }throw new GstException("No company " + legalName);
        }catch (GstException e){
            logger.severe("No company " + legalName);
            throw e;
        } catch (Exception e) {
            logger.severe("Error pulling xlsx for " + legalName + " and " + uuid);
            throw new GstException(e);
        }
    }

    private String genPrefix(){
        return PRE + activeProfile + "_";
    }


    private String getEnv(){
        return activeProfile.equals("prod") ?
                "" :
                activeProfile + "_";
    }

}
