package com.xion.payload;

import com.xion.resultObjectModel.resultSummeries.DocumentType;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StoreActionsRequest {

    private long summaryID;
    private DocumentType documentType;
    private List<Map<String, String>> actions;
    private Date date;
    private String legalName;
    private boolean supply;
    private String functionalCurrency;

    public long getSummaryID() {
        return summaryID;
    }

    public void setSummaryID(long summaryID) {
        this.summaryID = summaryID;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public List<Map<String, String>> getActions() {
        return actions;
    }

    public void setActions(List<Map<String, String>> actions) {
        this.actions = actions;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public boolean isSupply() {
        return supply;
    }

    public void setSupply(boolean supply) {
        this.supply = supply;
    }

    public String getFunctionalCurrency() {
        return functionalCurrency;
    }

    public void setFunctionalCurrency(String functionalCurrency) {
        this.functionalCurrency = functionalCurrency;
    }


}
