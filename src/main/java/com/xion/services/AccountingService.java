package com.xion.services;


import com.xion.components.CompanyService;
import com.xion.fx.FXService;
import com.xion.models.gst.Action;
import com.xion.models.user.permissions.Company;
import com.xion.payload.accounting.DocumentDTO;
import com.xion.payload.accounting.DocumentRequest;
import com.xion.payload.accounting.LineItem;
import com.xion.resultObjectModel.resultSummeries.CreditNoteResultSummary;
import com.xion.resultObjectModel.resultSummeries.DocumentType;
import com.xion.resultObjectModel.resultSummeries.InvoiceResultSummery;
import com.xion.resultObjectModel.resultSummeries.ResultSummery;
import com.xion.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Service
public class AccountingService {

    private static Logger logger = Logger.getLogger(AccountingService.class.getName());

    private RestTemplate restTemplate = new RestTemplate();
    @Value("#{ @environment['client.accountingBaseUrl'] }")
    private String accountingBaseUrl;
    @Value("#{ @environment['accounting.enable'] }")
    private boolean enabled;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private FXService fxService;
    @Autowired
    private CompanyService companyService;

    public boolean isOnboarded(String companyId) throws Exception{

        try {
            if (!enabled)
                return false;

            logger.info("---> calling findProvider");
            ResponseEntity<String> response = restTemplate.exchange(
                    accountingBaseUrl + "/api/v1/findProvider/" + companyId,
                    HttpMethod.POST,
                    null,
                    String.class
            );
            String body = response.getBody();
            logger.info("---> findProvider Body: " + body);
            if (body.equals("Quickbooks") || body.equals("Xero") ) {
                return true;
            }else {
                return false;
            }

        }catch (Exception e){
            logger.warning(e.getMessage());
            return false;
        }
    }

    public List<Action> pullCollapsedDataFromAccounting(String companyId, Date start, Date end, String email) throws Exception {
        Map<ResultSummery, List<Action>> raw = pullMappedDataFromAccounting(companyId, start, end, email);
        List<Action> actions = new ArrayList<>();
        for (ResultSummery key : raw.keySet())
            actions.addAll(raw.get(key));
        return actions;
    }

    public Map<ResultSummery, List<Action>> pullMappedDataFromAccounting(String companyId, Date start, Date end, String email) throws Exception{

        try{
            Company company = companyService.loadCompanyByCompanyID(companyId);
            logger.info("---> calling pullInvoices");
            String url = accountingBaseUrl + "/api/v1/" + companyId + "/document/pullInvoices/" + processDate(start) + "/" + processDate(end);
            logger.info("url -> " + url);

            ResponseEntity<DocumentDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    DocumentDTO.class
            );

            Map<ResultSummery, List<Action>> summaryActionMap = new HashMap<>();
            logger.info(response.getBody().toString());
            if(!validationService.validateAndContinue(response.getBody(), email)){
                throw new Exception("Validation failed, email sent");
            }

            for(DocumentRequest document : response.getBody().getDocumentRequest()) {

                switch (document.getDocumentType()) {
                    case "invoice": {
                        InvoiceResultSummery invoice = new InvoiceResultSummery();
                        invoice.setName("");
                        List<String> labels = new ArrayList<>();
                        labels.add(document.getLabel());
                        invoice.setLabels(labels);
                        invoice.setStatus(Status.GREEN);
                        invoice.setNotes("");
                        if(Objects.nonNull(document.getDate()))
                            invoice.setDate(new Date(document.getDate()));
                        invoice.setCreationDate(new Date());
                        if(Objects.nonNull(document.getPreTaxAmount()))
                            invoice.setPreTaxAmount(document.getPreTaxAmount());
                        if(Objects.nonNull(document.getTaxAmount()))
                            invoice.setTaxAmount(document.getTaxAmount());
                        if(Objects.nonNull(document.getTotalAmount()))
                            invoice.setTotalAmount(document.getTotalAmount());
                        if (document.getLabel().equals("supply")) {
                            if(Objects.nonNull(document.getContact()) && Objects.nonNull(document.getContact().getName()))
                                invoice.setSupplierName(document.getContact().getName());
                            if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("address"))
                                invoice.setSupplierAddress(document.getAdditionalProperties().get("address").toString());
                        }else{
                            if(Objects.nonNull(document.getContact()) && Objects.nonNull(document.getContact().getName()))
                                invoice.setCustomerName(document.getContact().getName());
                            if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("address"))
                                invoice.setCustomerAddress(document.getAdditionalProperties().get("address").toString());
                        }
                        if(Objects.nonNull(document.getInvoiceId()))
                            invoice.setInvoiceNumber(document.getInvoiceId());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
                        if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("dueOn"))
                            invoice.setDueOn(sdf.format(new Date((Long) document.getAdditionalProperties().get("dueOn"))));
                        invoice.setPoNumber("");
                        if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("gstNumber"))
                            invoice.setGstNumber(document.getAdditionalProperties().get("gstNumber").toString());
                        if(Objects.nonNull(document.getCurrencyCode()))
                            invoice.setCurrency(document.getCurrencyCode());

                        List<Action> actions = new ArrayList<>();
                        int count = 0;
                        if (Objects.nonNull(document.getLineItems())) {
                            for (LineItem lineItem : document.getLineItems()) {
                                Action action = new Action();

                                action.setDocumentType(DocumentType.INVOICE);
                                if (Objects.nonNull(lineItem.getTaxType()))
                                    action.setTaxCode(lineItem.getTaxType());
                                if (Objects.nonNull(lineItem.getDescription()))
                                    action.setDescription(lineItem.getDescription());
                                if (Objects.nonNull(lineItem.getTotalAmount()))
                                    action.setAmount(lineItem.getTotalAmount());
                                if (Objects.nonNull(lineItem.getPreTaxAmount()))
                                    action.setPreTax(lineItem.getPreTaxAmount().toString());
                                if (Objects.nonNull(lineItem.getTaxAmount()))
                                    action.setTax(lineItem.getTaxAmount().toString());
                                action.setType(DocumentType.INVOICE);
                                if (Objects.nonNull(document.getDate()))
                                    action.setDate(new Date(document.getDate()));
                                action.setLegalName(companyId);
                                if (Objects.nonNull(document.getLabel()))
                                    action.setSupply(document.getLabel().equals("supply"));
                                else
                                    action.setSupply(true);
                                action.setPaidDate(null);
                                if (Objects.nonNull(document.getCurrencyRate()))
                                    action.setFxRate(document.getCurrencyRate());
                                if (Objects.nonNull(document.getFunctionalCurrency()))
                                    action.setFunctionalCurrency(document.getFunctionalCurrency());
                                else
                                    action.setFunctionalCurrency(company.getFunctionalCurrency());
                                if (Objects.nonNull(document.getCurrencyCode())) {
                                    if (!document.getCurrencyCode().equals("SGD"))
                                        action.setSgdRate(fxService.getRate(action.getDate(), action.getFunctionalCurrency(), "SGD"));
                                    else
                                        action.setSgdRate(1d);
                                } else
                                    action.setSgdRate(1d);
                                action.setLineNumber(count);
                                action.setAccountType(null);
                                action.setAccountCode(null);
                                count = count++;

                                actions.add(action);
                            }
                        }
                        summaryActionMap.put(invoice, actions);

                    } case "credit_note": {
                        CreditNoteResultSummary creditNote = new CreditNoteResultSummary();
                        creditNote.setName("");
                        List<String> labels = new ArrayList<>();
                        labels.add(document.getLabel());
                        creditNote.setLabels(labels);
                        creditNote.setStatus(Status.GREEN);
                        creditNote.setNotes("");
                        if(Objects.nonNull(document.getDate()))
                            creditNote.setDate(new Date(document.getDate()));
                        creditNote.setCreationDate(new Date());
                        creditNote.setPreTaxAmount(document.getPreTaxAmount());
                        creditNote.setTaxAmount(document.getTaxAmount());
                        creditNote.setTotalAmount(document.getTotalAmount());
                        if(Objects.nonNull(document.getLabel())) {
                            if (document.getLabel().equals("supply")) {
                                if(Objects.nonNull(document.getContact()))
                                    creditNote.setSupplierName(document.getContact().getName());
                                if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("address"))
                                    creditNote.setSupplierAddress(document.getAdditionalProperties().get("address").toString());
                            } else {
                                if(Objects.nonNull(document.getContact()))
                                    creditNote.setCustomerName(document.getContact().getName());
                                if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("address"))
                                    creditNote.setCustomerAddress(document.getAdditionalProperties().get("address").toString());
                            }
                        }
                        creditNote.setInvoiceNumber(document.getInvoiceId());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
                        if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("dueOn"))
                            creditNote.setDueOn(sdf.format(new Date((Long) document.getAdditionalProperties().get("dueOn"))));
                        creditNote.setPoNumber("");
                        if(Objects.nonNull(document.getAdditionalProperties()) && document.getAdditionalProperties().containsKey("gstNumber"))
                            creditNote.setGstNumber(document.getAdditionalProperties().get("gstNumber").toString());
                        creditNote.setCurrency(document.getCurrencyCode());

                        List<Action> actions = new ArrayList<>();
                        int count = 0;
                        if(Objects.nonNull(document.getLineItems())) {
                            for (LineItem lineItem : document.getLineItems()) {
                                Action action = new Action();

                                action.setDocumentType(DocumentType.CREDIT_NOTE);
                                action.setTaxCode(lineItem.getTaxType());
                                action.setDescription(lineItem.getDescription());
                                action.setAmount(lineItem.getTotalAmount());
                                if (Objects.nonNull(lineItem.getPreTaxAmount()))
                                    action.setPreTax(lineItem.getPreTaxAmount().toString());
                                if (Objects.nonNull(lineItem.getTaxAmount()))
                                    action.setTax(lineItem.getTaxAmount().toString());
                                action.setType(DocumentType.CREDIT_NOTE);
                                if (Objects.nonNull(document.getDate()))
                                    action.setDate(new Date(document.getDate()));
                                action.setLegalName(companyId);
                                if (Objects.nonNull(document.getLabel()))
                                    action.setSupply(document.getLabel().equals("supply"));
                                action.setPaidDate(null);
                                action.setFxRate(document.getCurrencyRate());
                                if (Objects.nonNull(document.getLabel()))
                                    action.setFunctionalCurrency(document.getFunctionalCurrency());
                                else
                                    action.setFunctionalCurrency(company.getFunctionalCurrency());
                                if (Objects.nonNull(document.getCurrencyCode())) {
                                    if (!document.getCurrencyCode().equals("SGD"))
                                        action.setSgdRate(fxService.getRate(action.getDate(), action.getFunctionalCurrency(), "SGD"));
                                    else
                                        action.setSgdRate(1d);
                                } else
                                    action.setSgdRate(1d);
                                action.setLineNumber(count);
                                action.setAccountType(null);
                                action.setAccountCode(null);
                                count = count++;

                                actions.add(action);
                            }
                        }
                        summaryActionMap.put(creditNote, actions);
                    }
                }
            }

            return summaryActionMap;
        }catch (Exception e){
            logger.severe("Error in pullMappedDataFromAccounting");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String processDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

}
