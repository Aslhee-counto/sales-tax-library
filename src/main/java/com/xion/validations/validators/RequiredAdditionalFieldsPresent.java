package com.xion.validations.validators;



import com.xion.payload.accounting.DocumentRequest;
import com.xion.validations.ValidationException;
import com.xion.validations.Validator;

import java.util.Objects;

public class RequiredAdditionalFieldsPresent implements Validator {

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
        StringBuilder missing = new StringBuilder();
        boolean notes = false;
        if (Objects.isNull(documentRequest.getAdditionalProperties().get("address"))) {
            missing.append("address, ");
            notes = true;
            documentRequest.getAdditionalProperties().put("address","");
        }
        if (Objects.isNull(documentRequest.getAdditionalProperties().get("dueOn"))) {
            missing.append("dueOn, ");
            notes = true;
            documentRequest.getAdditionalProperties().put("dueOn","0");
        }
        if (Objects.isNull(documentRequest.getAdditionalProperties().get("gstNumber"))) {
            missing.append("gstNumber, ");
            notes = true;
            documentRequest.getAdditionalProperties().put("gstNumber","");
        }

        if (notes) {
            missing.setLength(missing.length()-2);
            throw new ValidationException(reason(), documentRequest.getDocumentType(), Objects.nonNull(documentRequest.getInvoiceId()) ?documentRequest.getInvoiceId() : documentRequest.getId(), "The field(s) " + missing.toString() + " are missing");
        }
    }

    @Override
    public String reason() {
        return "of missing data";
    }
}
