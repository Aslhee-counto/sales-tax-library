package com.xion.payload;

public class ActivateCompanyRequest {

    private String legalName;
    private String name;
    private String designation;
    private String email;
    private String phone;
    private String taxReportingFrequency;
    private String financialYearEnd;
    private String taxNum;
    private String gstNum;
    private boolean box1;
    private boolean box2;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTaxReportingFrequency() {
        return taxReportingFrequency;
    }

    public void setTaxReportingFrequency(String taxReportingFrequency) {
        this.taxReportingFrequency = taxReportingFrequency;
    }

    public String getFinancialYearEnd() {
        return financialYearEnd;
    }

    public void setFinancialYearEnd(String financialYearEnd) {
        this.financialYearEnd = financialYearEnd;
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

    public boolean isBox1() {
        return box1;
    }

    public void setBox1(boolean box1) {
        this.box1 = box1;
    }

    public boolean isBox2() {
        return box2;
    }

    public void setBox2(boolean box2) {
        this.box2 = box2;
    }
}
