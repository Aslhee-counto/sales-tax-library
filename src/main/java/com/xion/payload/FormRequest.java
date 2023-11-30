package com.xion.payload;

public class FormRequest {

    private String legalName;
    private String uuid;
    private String taxNum;
    private String gstNum;

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

    public String getTaxNum() {
        return taxNum;
    }

    public void setTaxNum(String taxNum) {
        this.taxNum = taxNum;
    }

    public String getGstNum() {
        return gstNum;
    }

    public void setGstNum(String gstNum) {
        this.gstNum = gstNum;
    }

    @Override
    public String toString() {
        return "FormRequest{" +
                "legalName='" + legalName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", taxNum='" + taxNum + '\'' +
                ", gstNum='" + gstNum + '\'' +
                '}';
    }
}
