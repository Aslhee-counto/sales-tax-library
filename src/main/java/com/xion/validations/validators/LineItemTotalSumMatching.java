package com.xion.validations.validators;



import com.xion.payload.accounting.DocumentRequest;
import com.xion.payload.accounting.LineItem;
import com.xion.validations.ValidationException;
import com.xion.validations.Validator;

import java.util.Objects;

public class LineItemTotalSumMatching implements Validator {
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
        StringBuilder stringBuilder = new StringBuilder();
        boolean pass = true;

        for (LineItem lineItem : documentRequest.getLineItems()){
            double total;
            try {
                total = Double.parseDouble(lineItem.getTotalAmount());
            }catch (Exception e){
                stringBuilder.append("The total field is " + lineItem.getTotalAmount() + " which is not a number, ");
                pass = false;
                continue;
            }
            double sumDiff = total - (lineItem.getTaxAmount() + lineItem.getPreTaxAmount());
            if (sumDiff > .02) {
                stringBuilder.append("The difference between the reported total and the sum of tax and pre tax is ").append(sumDiff).append(", ");
                pass = false;
            }
        }
        if (!pass) {
            stringBuilder.setLength(stringBuilder.length() - 2);
            throw new ValidationException(reason(), documentRequest.getDocumentType(), Objects.nonNull(documentRequest.getInvoiceId()) ?documentRequest.getInvoiceId() : documentRequest.getId(), stringBuilder.toString());
        }
    }

    @Override
    public String reason() {
        return "of an issue with the reported amounts";
    }
}
