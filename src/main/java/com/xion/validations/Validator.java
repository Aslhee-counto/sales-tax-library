package com.xion.validations;

import com.xion.payload.accounting.DocumentRequest;

public interface Validator {

    boolean applicable(DocumentRequest documentRequest);
    void validate(DocumentRequest documentRequest) throws Exception, ValidationException;
    String reason();
}
