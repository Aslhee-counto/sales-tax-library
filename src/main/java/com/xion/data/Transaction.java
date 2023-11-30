package com.xion.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class Transaction {

    private int lineNumber;
    private int boxNumber;
    private String preTaxAmount;
    private String taxAmount;
    private String taxCode;
    private String description;
    private Map<String, String> additional;
    private boolean supply;
    private boolean creditNote;

    public Transaction() {
        this.additional = new LinkedHashMap<>();
    }


    public Map<String, String> flattenToMap(){
        Map<String, String> flattened = new LinkedHashMap<>();

        if (additional.containsKey("invoiceNumber"))
            flattened.put("invoiceNumber", removeInvalidChars(additional.get("invoiceNumber")));
        else flattened.put("invoiceNumber", "");

        if (additional.containsKey("debitNoteNumber"))
            flattened.put("debitNoteNumber", removeInvalidChars(additional.get("debitNoteNumber")));
        else flattened.put("debitNoteNumber", "");

        if (additional.containsKey("creditNoteNumber"))
            flattened.put("creditNoteNumber", removeInvalidChars(additional.get("creditNoteNumber")));
        else flattened.put("creditNoteNumber", "");

        if (additional.containsKey("permitNumber"))
            flattened.put("permitNumber", removeInvalidChars(additional.get("permitNumber")));
        else flattened.put("permitNumber", "");

        if (additional.containsKey("date"))
            flattened.put("date", removeInvalidChars(additional.get("date")));
        else flattened.put("date", "");

        if (additional.containsKey("supplierName"))
            flattened.put("supplierName", removeInvalidChars(additional.get("supplierName")));
        else flattened.put("supplierName", "");

        if (additional.containsKey("gstNumber"))
            flattened.put("gstNumber", removeInvalidChars(additional.get("gstNumber")));
        else flattened.put("gstNumber", "");

        if (additional.containsKey("customerName"))
            flattened.put("customerName", removeInvalidChars(additional.get("customerName")));
        else flattened.put("customerName", "");

        flattened.put("lineNumber", lineNumber+"");
        flattened.put("description", description);

        if (additional.containsKey("currency"))
            flattened.put("currency", removeInvalidChars(additional.get("currency")));
        else flattened.put("currency", "");

        flattened.put("preTaxAmount", preTaxAmount);
        flattened.put("taxAmount", taxAmount);

        if (additional.containsKey("totalAmountFCY"))
            flattened.put("totalAmountFCY", removeInvalidChars(additional.get("totalAmountFCY")));
        else flattened.put("totalAmountFCY", "");

        if (additional.containsKey("preTaxAmountFCY"))
            flattened.put("preTaxFCY", removeInvalidChars(additional.get("preTaxAmountFCY")));
        else flattened.put("preTaxFCY", "");

        if (additional.containsKey("taxAmountFCY"))
            flattened.put("TaxFCY", removeInvalidChars(additional.get("taxAmountFCY")));
        else flattened.put("TaxFCY", "");

        flattened.put("taxCode", removeInvalidChars(taxCode));
        flattened.put("boxNumber", removeInvalidChars(boxNumber +""));

        return flattened;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public String getPreTaxAmount() {
        if (preTaxAmount.isBlank())
            return "0.00";
        return preTaxAmount;
    }

    public void setPreTaxAmount(String preTaxAmount) {
        this.preTaxAmount = preTaxAmount;
    }

    public String getTaxAmount() {
        if (taxAmount.isBlank())
            return "0.00";
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description.replace("\n", "");
        this.description = description;
    }

    public Map<String, String> getAdditional() {
        return additional;
    }

    public String getAdditional(String key) {
        if (additional.containsKey(key) && !additional.get(key).isBlank())
            return removeInvalidChars(additional.get(key));
        else
            switch (key){
                case "date": return "9999-12-31";
                case "total": return "0.00";
                case "totalAmountFCY": return "0.00";
                case "taxAmountFCY": return "0.00";
                case "currency": return "Xxx";
                case "country": return "";
                default: return "";
            }
    }

    public void setAdditional(Map<String, String> additional) {
        this.additional = additional;
    }

    public void addAdditional(String key, String value){
        this.additional.put(key, value);
    }

    public boolean isSupply() {
        return supply;
    }

    public void setSupply(boolean supply) {
        this.supply = supply;
    }

    public boolean isCreditNote() {
        return creditNote;
    }

    public void setCreditNote(boolean creditNote) {
        this.creditNote = creditNote;
    }

    private String removeInvalidChars(String initial){
        return initial
                .replace(":", "")
                .replace("\"", "")
                .replace("{", "")
                .replace("}", "")
                .replace("|", "")
                .replace("\\", "");
    }
}
