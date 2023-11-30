package com.xion.payload;

import java.util.List;
import java.util.Map;

public class SendSimpleTemplateRequest {

    private Map<String, List<String>> to;
    private String id;
    private String subject;
    private Map<String,String> replacementMap;

    public Map<String, List<String>> getTo() {
        return to;
    }

    public void setTo(Map<String, List<String>> to) {
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Map<String, String> getReplacementMap() {
        return replacementMap;
    }

    public void setReplacementMap(Map<String, String> replacementMap) {
        this.replacementMap = replacementMap;
    }

    @Override
    public String toString() {
        return "SendSimpleTemplateRequest{" +
                "to=" + to +
                ", id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", replacementMap=" + replacementMap +
                '}';
    }
}
