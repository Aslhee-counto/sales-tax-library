package com.xion.validations.validators;



import com.xion.payload.accounting.Contact;
import com.xion.payload.accounting.DocumentRequest;
import com.xion.payload.accounting.LineItem;
import com.xion.validations.ValidationException;
import com.xion.validations.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequiredFieldsPresent implements Validator {
    @Override
    public boolean applicable(DocumentRequest documentRequest) {
        if (documentRequest.getDocumentType().equals("invoice"))
            return true;
        if (documentRequest.getDocumentType().equals("credit_note"))
            return true;
        return false;
    }

    @Override
    public void validate(DocumentRequest documentRequest) throws Exception, ValidationException {
        StringBuilder missing = new StringBuilder();
        boolean notes = false;
        boolean fail = false;
        if (Objects.isNull(documentRequest.getDocumentType()) || documentRequest.getDocumentType().isBlank()) {
            missing.append("DocumentType, ");
            notes = true;
            fail = true;
        }
        if (Objects.isNull(documentRequest.getContact()) || Objects.isNull(documentRequest.getContact().getName())) {
            missing.append("Contact, ");
            notes = true;
            Contact contact = new Contact();
            contact.setName("");
            documentRequest.setContact(contact);
        }
        if (Objects.isNull(documentRequest.getInvoiceId())) {
            missing.append("InvoiceId, ");
            notes = true;
            documentRequest.setInvoiceId("");
        }
        if (Objects.isNull(documentRequest.getDate())) {
            missing.append("Date, ");
            notes = true;
            documentRequest.setDate(0L);
        }
        if (Objects.isNull(documentRequest.getLabel())) {
            missing.append("Label, ");
            notes = true;
            fail = true;
        }
        if (Objects.isNull(documentRequest.getCurrencyCode())) {
            missing.append("CurrencyCode, ");
            notes = true;
            documentRequest.setCurrencyCode("SGD");
        }
//        if (Objects.isNull(documentRequest.getFunctionalCurrency())) {
//            missing.append("FunctionalCurrency, ");
//            notes = true;
//            documentRequest.setFunctionalCurrency("SGD");
//        }
        if (Objects.isNull(documentRequest.getCurrencyRate())) {
            missing.append("CurrencyRate, ");
            notes = true;
            documentRequest.setCurrencyRate(1D);
        }
        if (Objects.isNull(documentRequest.getPreTaxAmount())) {
            missing.append("PreTaxAmount(), ");
            notes = true;
            documentRequest.setPreTaxAmount(0D);
        }
        if (Objects.isNull(documentRequest.getTaxAmount())) {
            missing.append("TaxAmount(), ");
            notes = true;
            documentRequest.setTaxAmount(0D);
        }
        if (Objects.isNull(documentRequest.getTotalAmount())) {
            missing.append("TotalAmount, ");
            notes = true;
            documentRequest.setTotalAmount(0D);
        }
        if (Objects.isNull(documentRequest.getLineItems())) {
            missing.append("LineItems, ");
            notes = true;
            documentRequest.setLineItems(new ArrayList<>());
        }
        if (Objects.isNull(documentRequest.getId())) {
            missing.append("ID, ");
            notes = true;
            documentRequest.setId("");
        }
        if (Objects.isNull(documentRequest.getAdditionalProperties())) {
            missing.append("AdditionalProperties, ");
            notes = true;
            fail = true;
        }

        List<LineItem> lineItems = documentRequest.getLineItems();
        for (int i = 0, lineItemsSize = lineItems.size(); i < lineItemsSize; i++) {
            LineItem lineItem = lineItems.get(i);
            if (Objects.isNull(lineItem.getPreTaxAmount())) {
                missing.append("Line Item -> preTaxAmount, ");
                notes = true;
                lineItem.setPreTaxAmount(0D);
            }
            if (Objects.isNull(lineItem.getTaxAmount())) {
                missing.append("Line Item -> taxAmount, ");
                notes = true;
                lineItem.setTaxAmount(0D);
            }
            if (Objects.isNull(lineItem.getTaxType())) {
                missing.append("Line Item -> taxType, ");
                notes = true;
                lineItem.setTaxType("");
            }
            if (Objects.isNull(lineItem.getDescription())) {
                missing.append("Line Item -> description, ");
                notes = true;
                lineItem.setDescription("");
            }
            if (Objects.isNull(lineItem.getAccount())) {
                missing.append("Line Item -> account, ");
                notes = true;
                lineItem.setAccount("");
            }
            if (Objects.isNull(lineItem.getTotalAmount())) {
                missing.append("Line Item -> totalAmount, ");
                notes = true;
                lineItem.setTotalAmount("0");
            }
            if (Objects.isNull(lineItem.getAccountCode())) {
                missing.append("Line Item -> accountCode, ");
                notes = true;
                lineItem.setAccountCode("");
            }
            if (Objects.isNull(lineItem.getAdditionalProperties())) {
                missing.append("Line Item -> AdditionalProperties, ");
                notes = true;
                fail = true;
            }
        }

        if (notes) {
            missing.setLength(missing.length() - 2);
            throw new ValidationException(reason(), documentRequest.getDocumentType(), Objects.nonNull(documentRequest.getInvoiceId()) ?documentRequest.getInvoiceId() : documentRequest.getId(), "The field(s) " + missing.toString() + " are missing");
        }
        if (fail){
            throw new Exception("An invalid and unrecoverable document request was recieved");
        }

    }

    @Override
    public String reason() {
        return "of missing data";
    }
}
