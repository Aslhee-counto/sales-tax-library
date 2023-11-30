package com.xion.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.xion.components.ActionService;
import com.xion.components.ResultSummeryService;
import com.xion.data.DateRange;
import com.xion.data.F5Form;
import com.xion.data.Transaction;
import com.xion.exceptions.GstException;
import com.xion.fx.FXService;
import com.xion.models.gst.Action;
import com.xion.models.gst.ActivationRecord;
import com.xion.resultObjectModel.resultSummeries.DocumentType;
import com.xion.resultObjectModel.resultSummeries.ImportPermitResultSummary;
import com.xion.resultObjectModel.resultSummeries.ResultSummery;
import com.xion.util.IdTypePair;
import com.xion.util.TaxCodeUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TransactionsService {

    private static Logger logger = Logger.getLogger(TransactionsService.class.getName());

    @Autowired
    private ResultSummeryService summeryService;
    @Autowired private StoreService         storeService;
    @Autowired private ActionService actionService;
    @Autowired private AccountingService    accountingService;
    @Autowired private FXService fxService;

    public List<Transaction> loadTransactions(String legalName, String period) throws GstException { //
        logger.info("Running loadTransactions for " + legalName + " and " + period);
        try{
            DateRange range = DateRange.parsePeriod(period);

            Map<Action, ResultSummery> actionsMap = new HashMap<>();
            ActivationRecord activationRecord = storeService.loadActivationRecord(legalName);
            if (accountingService.isOnboarded(legalName)) {
                logger.info("Pulling action data from accounting service");
                Map<ResultSummery, List<Action>> actionsRaw = accountingService.pullMappedDataFromAccounting(legalName, range.getStart(), range.getEnd(), activationRecord.getEmail());
                for (ResultSummery summary : actionsRaw.keySet()){
                    for (Action action : actionsRaw.get(summary)){
                        actionsMap.put(action, summary);
                    }
                }
                logger.info("pulled " + actionsMap.keySet().size() + " actions");
            }else {
                logger.info("Pulling action data from vault");
                logger.info("pulling actions for " + legalName + " between " + range.getStart().toString() + " and " + range.getEnd().toString());
                List<Action> actionsRaw = actionService.loadActionsInRange(range.getStart(), range.getEnd(), legalName);
                List<Action> actions = actionsRaw.stream()
                        .filter(action -> action.getTaxCode()!=null)
                        .filter(action -> !action.getTaxCode().isBlank())
                        .collect(Collectors.toList());
                logger.info("loaded " + actions.size() + " actions");

                List<IdTypePair> ids = actions.stream().map(action -> new IdTypePair(action.getSummaryID(), action.getDocumentType())).collect(Collectors.toList());
                List<ResultSummery> summaries = summeryService.loadAllSummariesByIDs(ids);
                logger.info("Loaded " + summaries.size() + " summaries in loadTransactions");

                for (Action action : actions){
                    actionsMap.put(action, summaries.stream().filter( s -> s.getId().equals(action.getSummaryID())).findAny().orElse(null));
                }
            }


            List<Transaction> transactions = new ArrayList<>();
            for(Action action : actionsMap.keySet()) {
                ResultSummery summary = actionsMap.get(action);
//                 Parse the summary JSON to extract the "currency" field
                JSONObject summaryJson = new JSONObject(summary.toString()); // Convert summary to JSON
                String currency = summaryJson.optString("currency"); // Get the value of "currency" field


                if (summary==null) {
                    logger.warning("summary is null, skipping");
                    continue;
                }

                Transaction transaction = new Transaction();


                Class cls = summary.getClass();


                List<Field> fields = new ArrayList<>();
                fields.addAll(Arrays.asList(cls.getDeclaredFields()));
                fields.addAll(Arrays.asList(cls.getSuperclass().getDeclaredFields()));
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (	field.getName().equals("name") ||
                            field.getName().equals("supplierName") ||
                            field.getName().equals("customerName") ||
                            field.getName().equals("invoiceNumber") ||
                            field.getName().equals("creditNoteNumber") ||
                            field.getName().equals("debitNoteNumber") ||
                            field.getName().equals("permitNumber") ||
                            field.getName().equals("currency") ||
                            field.getName().equals("gstNumber") ||
                            field.getName().equals("total") ||
                            field.getName().equals("paidDate")
                    )
                        transaction.addAdditional(field.getName(), field.get(summary)+"");
                    if(field.getName().equals("exporterName"))
                        transaction.addAdditional("supplierName", field.get(summary)+"");
                    if(field.getName().equals("importerName"))
                        transaction.addAdditional("customerName", field.get(summary)+"");

                    if (summary instanceof ImportPermitResultSummary){
                        if (field.getName().equals("gstNumber")) {
                            if (!transaction.getAdditional().containsKey("gstNumber") ||
                                    transaction.getAdditional().get("gstNumber") == null ||
                                    transaction.getAdditional().get("gstNumber").isBlank()
                            ) {
                                transaction.addAdditional("gstNumber", "NaN");
                            }
                        }
                        if (field.getName().equals("permitNumber")) {
                            transaction.addAdditional("invoiceNumber", field.get(summary)+"");
                        }

                    }

                }

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                transaction.addAdditional("date", format.format(action.getDate()));

                transaction.setLineNumber(action.getLineNumber());
                transaction.setPreTaxAmount(action.getPreTax());
                transaction.setTaxAmount(action.getTax());
                transaction.setTaxCode(TaxCodeUtil.mapTaxCode(action));
                transaction.setDescription(action.getDescription());
                transaction.setSupply(action.isSupply());
                transaction.setCreditNote(action.getDocumentType().equals(DocumentType.CREDIT_NOTE));


                if (!currency.equalsIgnoreCase("SGD")) {
                    String preTaxFCY = summaryJson.optString("preTaxAmount");
                    String taxFCY = summaryJson.optString("taxAmount");
                    String totalAmount = summaryJson.optString("totalAmount");

                    action.setPreTax(preTaxFCY);
                    action.setTax(taxFCY);

                    transaction.addAdditional("preTaxAmountFCY", action.getPreTax());
                    transaction.addAdditional("taxAmountFCY", action.getTax());
//                    logger.info("TaxFCY: " + action.getTax());
//                    logger.info("PretaxFCY: " + action.getPreTax());
//                    logger.info("TOTAL AMOUNT: " + action.getAmount());


                    double sgdRate = fxService.getRate(action.getDate(), currency, "SGD");
                    logger.info("==SgD Rate: " + sgdRate);

                    double convertedPretaxAmount = new BigDecimal(Double.parseDouble(cleanNumeric(action.getPreTax())) * sgdRate)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    transaction.setPreTaxAmount(String.valueOf(convertedPretaxAmount));


                    double convertedTaxAmount = new BigDecimal(Double.parseDouble(cleanNumeric(action.getTax())) * sgdRate)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    transaction.setTaxAmount(String.valueOf(convertedTaxAmount));


                    transaction.setTaxAmount(String.valueOf(convertedTaxAmount));
                    transaction.addAdditional("totalAmountFCY", String.valueOf(totalAmount));
//                    logger.info("===TOTAL AMOUNTV2: " + String.valueOf(totalAmount));

                    transaction.setPreTaxAmount(String.valueOf(convertedPretaxAmount));
                    transaction.setTaxAmount(String.valueOf(convertedTaxAmount));

                    action.setPreTax(String.valueOf(convertedPretaxAmount));
                    action.setTax(String.valueOf(convertedTaxAmount));


                }else{
                    transaction.addAdditional("preTaxAmountFCY", "0");
                    transaction.addAdditional("taxAmountFCY", "0");
                    transaction.addAdditional("totalAmountFCY", "0");
                }

                int boxNum = 0;
                if (action.isSupply() && (transaction.getTaxCode().equals("SR") || transaction.getTaxCode().equals("SR8")))
                    boxNum = 1;
                else if (action.isSupply() && transaction.getTaxCode().equals("ZR"))
                    boxNum = 2;
                else if (action.isSupply() && transaction.getTaxCode().equals("ES33"))
                    boxNum = 3;
                else if (!action.isSupply() && transaction.getTaxCode().equals("ZP")){
                    boxNum = 5;
                }else if (!action.isSupply() && (
                        transaction.getTaxCode().equals("TX") ||
                                transaction.getTaxCode().equals("TX8") ||
                                transaction.getTaxCode().equals("IM") ||
                                transaction.getTaxCode().equals("IM8")
                ))
                    boxNum = 7;
                else {
                    logger.warning("no box, skipping. Taxcode: " + transaction.getTaxCode());
                    continue;
                }
                transaction.setBoxNumber(boxNum);
                transactions.add(transaction);
            }

            return transactions;
        }catch (NullPointerException e){
            logger.severe("NPE loading transactions for " + legalName);
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            logger.severe("Error loading transactions with " + legalName + " and " + period);
            logger.severe(e.getMessage());
            throw new GstException(e);
        }
    }

    public void processAdjustments(F5Form form, String rawAdjustments) throws Exception{
        logger.info("processAdjustments called with: " + rawAdjustments);
        try{
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> adjustmentElements = (Map<String, Object>) mapper.readValue(rawAdjustments, Map.class);
            for (String adjustmentElementName : adjustmentElements.keySet()){
                if (adjustmentElements.get(adjustmentElementName).equals("0")) {
                    logger.info("no adjustments passed for: " + adjustmentElementName);
                    continue;
                }
                if (!(adjustmentElements.get(adjustmentElementName) instanceof Map)){
                    continue;
                }
                Map<String, Object> adjustmentElement = (Map<String, Object>) adjustmentElements.get(adjustmentElementName);

                List<Map<String,String>> adjustmentItems = (List<Map<String,String>>) adjustmentElement.get("adjustmentItems");

                for (Map<String,String> adjustmentItem : adjustmentItems){
                    String description = adjustmentItem.get("description");
                    String date = adjustmentItem.get("date");
                    String pretax = adjustmentItem.get("pretax");
                    String tax = adjustmentItem.get("tax");
                    String amount = adjustmentItem.get("amount");
                    String taxCode = adjustmentItem.get("taxCode");
                    String invoiceNumber = adjustmentItem.get("invoiceNumber");

                    if (tax.equals("0") && pretax.equals("0") && amount.equals("0"))
                        continue;

                    int boxNum = interpretBoxNum(adjustmentElementName);

                    Transaction transaction = new Transaction();
                    transaction.setBoxNumber(boxNum);
                    transaction.setLineNumber(1);
                    transaction.setTaxAmount(tax);
                    transaction.setPreTaxAmount(pretax);
                    transaction.setDescription(description);
                    transaction.setTaxCode(taxCode);
                    transaction.addAdditional("date", date);
                    transaction.addAdditional("total", amount);
                    transaction.setSupply( ( boxNum == 1 || boxNum == 2 || boxNum == 3 ) ? true : false );
                    transaction.setCreditNote(false);
                    transaction.addAdditional("invoiceNumber", invoiceNumber);
                    transaction.addAdditional("currency", "SGD");

                    logger.info("Processing adjustments, transactions contains " + form.getTransactions().size() + " elements");
                    form.getTransactions().add(transaction);
                    logger.info("Processing adjustments, transaction added, transactions contains " + form.getTransactions().size() + " elements");
                }
            }

        }catch (NullPointerException e){
            logger.severe("NPE in processAdjustments");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            logger.severe("Error in processAdjustments");
            logger.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private int interpretBoxNum(String type)throws Exception{
        if (type.equals("SUPPLIES_STAFF")) return 1;
        else if (type.equals("SALES_BUSSINESS_ASSETS")) return 1;
        else if (type.equals("DEEMED_SUPPLIES")) return 1;
        else if (type.equals("OTHERS_SUPPLIES")) return 1;
        else if (type.equals("ZERO_RATED_BANK_DEPOSITS")) return 2;
        else if (type.equals("EXEMPT_SUPPLIES_BANK_DEPOSITS")) return 3;
        else if (type.equals("EXEMPT_SUPPLIES_FOREIGN_TRANSACTIONS")) return 3;
        else if (type.equals("BED_DEBTS_RELIEF")) return 7;
        else if (type.equals("INPUT_TAX_CLAIMED")) return 7;
        else throw new Exception(type + " is not a supported adjustment type");
    }

    private String cleanNumeric(String numeric){
        return numeric.replaceAll("[^0-9.]", "");
    }

}

