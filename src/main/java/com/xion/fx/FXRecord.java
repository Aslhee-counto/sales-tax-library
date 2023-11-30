package com.xion.fx;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class FXRecord {

    private static Logger logger = Logger.getLogger(FXRecord.class.getName());

    private Date dateOf;
    private Map<String, Double> quotes;

    public static FXRecord parseFXRecord(Date date, String rawRecord) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        FXRecord record = new FXRecord();
        record.setDateOf(date);

        record.setQuotes(mapper.readValue(rawRecord, Map.class));

        return record;
    }

    public static String createDateOfName(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public Date getDateOf() {
        return dateOf;
    }

    public void setDateOf(Date dateOf) {
        this.dateOf = dateOf;
    }

    public Map<String, Double> getQuotes() {
        return quotes;
    }

    public void setQuotes(Map<String, Double> quotes) {
        this.quotes = quotes;
    }

    public String getDateOfName() {
        return createDateOfName(dateOf);
    }

    @Override
    public String toString(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(quotes);
        }catch(Exception e){
            logger.severe("Could not serialize fx record");
            logger.severe(e.getMessage());
            return "";
        }
    }
}
