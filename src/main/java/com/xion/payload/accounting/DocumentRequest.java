package com.xion.payload.accounting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"document_type", "contact", "invoice_id", "date", "label", "currency_code", "currency_rate",
        "pre_tax_amount", "tax_amount", "total_amount", "line_items", "credit_note_id"})
public class DocumentRequest {

    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("contact")
    private Contact contact;
    @JsonProperty("invoice_id")
    private String invoiceId;
    @JsonProperty("date")
    private long date;
    @JsonProperty("label")
    private String label;
    @JsonProperty("currency_code")
    private String currencyCode;
    @JsonProperty("functional_currency")
    private String functionalCurrency;
    @JsonProperty("currency_rate")
    private Double currencyRate;
    @JsonProperty("pre_tax_amount")
    private Double preTaxAmount;
    @JsonProperty("tax_amount")
    private Double taxAmount;
    @JsonProperty("total_amount")
    private Double totalAmount;
    @JsonProperty("line_items")
    private List<LineItem> lineItems = null;
    @JsonProperty("id")
    private String id;
    @JsonProperty("additional_properties")
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("document_type")
    public String getDocumentType() {
        return documentType;
    }

    @JsonProperty("document_type")
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @JsonProperty("contact")
    public Contact getContact() {
        return contact;
    }

    @JsonProperty("contact")
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @JsonProperty("invoice_id")
    public String getInvoiceId() {
        return invoiceId;
    }

    @JsonProperty("invoice_id")
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    @JsonProperty("date")
    public long getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(long date) {
        this.date = date;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("currency_code")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonProperty("currency_code")
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @JsonProperty("currency_rate")
    public Double getCurrencyRate() {
        return currencyRate;
    }

    @JsonProperty("currency_rate")
    public void setCurrencyRate(Double currencyRate) {
        this.currencyRate = currencyRate;
    }

    @JsonProperty("pre_tax_amount")
    public Double getPreTaxAmount() {
        return preTaxAmount;
    }

    @JsonProperty("pre_tax_amount")
    public void setPreTaxAmount(Double preTaxAmount) {
        this.preTaxAmount = preTaxAmount;
    }

    @JsonProperty("tax_amount")
    public Double getTaxAmount() {
        return taxAmount;
    }

    @JsonProperty("tax_amount")
    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    @JsonProperty("total_amount")
    public Double getTotalAmount() {
        return totalAmount;
    }

    @JsonProperty("total_amount")
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @JsonProperty("line_items")
    public List<LineItem> getLineItems() {
        return lineItems;
    }

    @JsonProperty("line_items")
    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("additional_properties")
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("additional_properties")
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("functional_currency")
    public String getFunctionalCurrency() {
        return functionalCurrency;
    }

    @JsonProperty("functional_currency")
    public void setFunctionalCurrency(String functionalCurrency) {
        this.functionalCurrency = functionalCurrency;
    }

    @Override
    public String toString() {
        return "DocumentRequest{" +
                "documentType='" + documentType + '\'' +
                ", contact=" + contact +
                ", invoiceId='" + invoiceId + '\'' +
                ", date=" + date +
                ", label='" + label + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", functionalCurrency='" + functionalCurrency + '\'' +
                ", currencyRate=" + currencyRate +
                ", preTaxAmount=" + preTaxAmount +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                ", lineItems=" + lineItems +
                ", id='" + id + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}