package com.xion.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentResponse {

    @JsonProperty("error")
    private java.lang.Error error;

    public DocumentResponse() {
        super();

    }

    public DocumentResponse(java.lang.Error error) {
        super();
        this.error = error;
    }

    public java.lang.Error getError() {
        return error;
    }

}
