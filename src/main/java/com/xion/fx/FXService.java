package com.xion.fx;


import com.xion.exceptions.GstException;
import com.xion.repositories.GstRepository;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Service
public class FXService {

    private static Logger logger = Logger.getLogger(FXService.class.getName());

    private static final String FIXER_BASE_URL = "https://data.fixer.io/api/";
    private static final String LATESTENDPOINT = "latest?";
    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    @Value("${fx.key}")
    private String fxKey;

    @Autowired
    private GstRepository gstRepository;

    public double getRate(Date date, String currency, String targetCurrency){
        logger.info("Get rate called for " + currency + " -> " + targetCurrency + " on " + date);

//       checks if the source currency and target currency are the same
        if (currency.equals(targetCurrency)) {
            return 1;
        }

        List<String> tc = new ArrayList<>();
        tc.add(targetCurrency);
        try {
            FXRecord record = null;
            try{
                record = gstRepository.loadFXRecord(currency, date); //retrieve record using gstRepo loadFxRecord method
                //if not available,
            }catch(GstException e){
                logger.warning("FX rate data not currently available, pulling from provider");
                if (new Date().equals(date)) { //new date and pullRateFromProviderToday method
                    logger.warning("Running: pullRateFromProviderToday");
                    record = pullRateFromProviderToday(currency);
                    logger.info("record: " + record);
                    //or
                }else {
                    logger.warning("Running: pullRateFromProviderHistorical");
                    record = pullRateFromProviderHistorical(date, currency);
                }
                //when the record is found, store the FXRecord
                gstRepository.storeFXRecord(currency, record);
            }

            return record.getQuotes().get((currency+targetCurrency).toUpperCase());

        }catch (Exception e){
            e.printStackTrace();
            logger.severe("Error processing FX rate");
            logger.severe(e.getMessage());
            return 0;
        }
    }


    private FXRecord pullRateFromProviderToday(String currency) throws Exception{
        logger.info("pullRate called for today: " + currency);

        StringBuilder uri = new StringBuilder()
                .append(FIXER_BASE_URL)
                .append(LATESTENDPOINT)
                .append("access_key=").append(fxKey)
                .append("&base=").append(currency);

        logger.info("url: " + uri.toString());
        HttpGet get = new HttpGet(uri.toString());

        try {
            CloseableHttpResponse response =  httpClient.execute(get);
            HttpEntity entity = response.getEntity();

            JSONObject exchangeRates = new JSONObject(EntityUtils.toString(entity));
            logger.info(exchangeRates.toString(2));

            JSONObject quotes = exchangeRates.getJSONObject("rates");
            String base = exchangeRates.getString("base");

            Map<String, Double> quoteMap = new HashMap<>();
            for(String target : quotes.keySet()){
                quoteMap.put(base + target, quotes.getDouble(target));
            }

            response.close();

            FXRecord record = new FXRecord();
            record.setQuotes(quoteMap);
            record.setDateOf(new Date());

            return record;
        } catch (ClientProtocolException e) {
            logger.severe("ClientProtocolException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            logger.severe("IOException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (ParseException e) {
            logger.severe("ParseException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (JSONException e) {
            logger.severe("JSONException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    private FXRecord pullRateFromProviderHistorical(Date date, String currency) throws Exception{
        logger.info("pullRate called for " + date + " : " + currency);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String dateString = format.format(date);

        StringBuilder uri = new StringBuilder()
                .append(FIXER_BASE_URL)
                .append(dateString).append("?")
                .append("base=").append(currency)
                .append("&access_key=").append(fxKey);

        logger.info("url: " + uri.toString());
        HttpGet get = new HttpGet(uri.toString());

        try {
            CloseableHttpResponse response =  httpClient.execute(get);

            HttpEntity entity = response.getEntity();


            JSONObject exchangeRates = new JSONObject(EntityUtils.toString(entity));
            logger.info(exchangeRates.toString(2));

            JSONObject rates = exchangeRates.getJSONObject("rates");
            String base = exchangeRates.getString("base");


            Map<String, Double> quoteMap = new HashMap<>();
            for(String target : rates.keySet()){
                quoteMap.put(base + target, rates.getDouble(target));
            }

            response.close();

            FXRecord record = new FXRecord();
            record.setQuotes(quoteMap);
            record.setDateOf(date);

            return record;
        } catch (ClientProtocolException e) {
            logger.severe("ClientProtocolException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            logger.severe("IOException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (ParseException e) {
            logger.severe("ParseException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (JSONException e) {
            logger.severe("JSONException in getRate");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

}
