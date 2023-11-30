package com.xion.payload;

import com.xion.models.gst.Action;
import com.xion.resultObjectModel.resultSummeries.DocumentType;

import java.util.List;

public class StoreGstAuditDocumentRequest {

    private DocumentType documentType;
    private String summaryJson;
    private List<Action> actions;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getSummaryJson() {
        return summaryJson;
    }

    public void setSummaryJson(String summaryJson) {
        this.summaryJson = summaryJson;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
