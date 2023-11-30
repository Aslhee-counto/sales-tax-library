package com.xion.payload;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class AuditRequest {

    private String companyId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date start;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date end;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }


    @Override
    public String toString() {
        return "AuditRequest{" +
                "companyId='" + companyId + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
