package com.xion.payload;

public class SingPassRequest {

    private String legalName;
    private String form;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    @Override
    public String toString() {
        return "SingPassRequest{" +
                "legalName='" + legalName + '\'' +
                ", form='" + form + '\'' +
                '}';
    }
}
