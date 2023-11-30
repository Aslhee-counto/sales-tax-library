package com.xion.payload;

import java.util.Date;

public class AddListingRequest {

    private String legalName;
    private String currency;
    private Date   startDate;

    public String getLegalName() {
        return legalName;
    }

    public AddListingRequest setLegalName(String legalName) {
        this.legalName = legalName;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public AddListingRequest setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
