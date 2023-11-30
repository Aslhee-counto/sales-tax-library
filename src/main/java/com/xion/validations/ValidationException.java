package com.xion.validations;

public class ValidationException extends Throwable{

    public ValidationException(String reason, String docType, String docIndicator, String failure){
        super( new StringBuilder().append("The following validation step failed on ")
                .append(docType)
                .append(": ")
                .append(docIndicator)
                .append(" because ")
                .append(reason)
                .append(" -> ")
                .append(failure).toString());
    }

}
