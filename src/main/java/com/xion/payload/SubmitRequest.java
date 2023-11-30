package com.xion.payload;

public class SubmitRequest {

    private String legalName;
    private String uuid;
    private String adjustments;
    private String gstReturn;
    private String code;
    private String form;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(String adjustments) {
        this.adjustments = adjustments;
    }

    public String getGstReturn() {
        return gstReturn;
    }

    public void setGstReturn(String gstReturn) {
        this.gstReturn = gstReturn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    @Override
    public String toString() {
        return "SubmitRequest{" +
                "legalName='" + legalName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", adjustments='" + adjustments + '\'' +
                ", gstReturn='" + gstReturn + '\'' +
                ", code='" + code + '\'' +
                ", form='" + form + '\'' +
                '}';
    }
}
