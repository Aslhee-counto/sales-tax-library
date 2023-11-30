package com.xion.repositories;



import com.xion.data.F5Form;
import com.xion.exceptions.GstException;
import com.xion.fx.FXRecord;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Date;

public interface GstRepository {


    public boolean  storeF5PDF(             String legalName, String uuid, InputStream pdf  ) throws GstException;
    public boolean  storeTransactionsXlsx(  String legalName, String uuid, Workbook xlsx    ) throws GstException;
    public boolean  storeF5(                String legalName, String uuid, F5Form f5Form    ) throws GstException;
    public boolean  storeFXRecord(          String currency,  FXRecord fxRecord             ) throws GstException;
    public Resource loadF5PDF(String legalName, String uuid                   ) throws GstException;
    public Resource loadTransactionsXlsx(   String legalName, String uuid                   ) throws GstException;
    public F5Form loadF5(String legalName, String uuid                   ) throws GstException;
    public FXRecord loadFXRecord(           String currency,  Date date                     ) throws GstException;
    public boolean  checkF5Exists(          String legalName, String uuid, String fileType  ) throws GstException;

}
