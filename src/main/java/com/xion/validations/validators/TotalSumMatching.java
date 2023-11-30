package com.xion.validations.validators;



import com.xion.payload.accounting.DocumentRequest;
import com.xion.validations.ValidationException;
import com.xion.validations.Validator;

import java.util.Objects;

public class TotalSumMatching implements Validator {
    @Override
    public boolean applicable(DocumentRequest documentRequest) {
        if (documentRequest.getDocumentType().equals("invoice"))
            return true;
        if (documentRequest.getDocumentType().equals("credit_note"))
            return true;
        return false;
    }

    @Override
    public void validate(DocumentRequest documentRequest) throws ValidationException {
        double sumDiff = documentRequest.getTotalAmount() - (documentRequest.getTaxAmount() + documentRequest.getPreTaxAmount());
        if (sumDiff > .02)
            throw new ValidationException(reason(), documentRequest.getDocumentType(), Objects.nonNull(documentRequest.getInvoiceId()) ?documentRequest.getInvoiceId() : documentRequest.getId(), "The difference between the reported total and the sum of tax and pre tax is " + sumDiff);
    }

    @Override
    public String reason() {
        return "of an issue with the reported amounts";
    }
}
