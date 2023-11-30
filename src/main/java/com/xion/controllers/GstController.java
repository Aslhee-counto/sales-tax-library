package com.xion.controllers;

import com.xion.app.dto.AbstractResponse;

import com.xion.app.dto.gst.*;
import com.xion.components.ActionService;
import com.xion.components.CompanyService;
import com.xion.components.InvoiceResultSummeryRepository;
import com.xion.components.ResultSummeryService;
import com.xion.config.IRASProperties;
import com.xion.data.F5Form;
import com.xion.data.Transaction;
import com.xion.fx.FXService;
import com.xion.models.gst.Action;
import com.xion.models.gst.GstStatus;
import com.xion.models.gst.Listing;
import com.xion.payload.*;
import com.xion.payload.DeleteActionsRequest;
import com.xion.payload.FormRequest;
import com.xion.payload.LoadF5PDFRequest;
import com.xion.payload.LoadTransactionsRequest;
import com.xion.payload.StoreActionsRequest;
import com.xion.payload.StoreFileRequest;
import com.xion.payload.SubmitRequest;
import com.xion.resultObjectModel.resultSummeries.*;
import com.xion.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
public class GstController {

    private static Logger logger = Logger.getLogger(GstController.class.getName());
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String RESET = "\033[0m";  // Text Reset
    private static String SUCCESS = "Success";

    @Autowired
    private StoreService storeService;
    @Autowired
    private TransactionsService transactionsService;
    @Autowired
    private IRASService irasService;
    @Autowired
    private GSTCalculationService gstService;
    @Autowired
    private ResultSummeryService resultSummeryService;
    @Autowired
    private FXService fxService;
    @Autowired
    private IRASProperties irasProperties;
    @Autowired
    private ListingService listingService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private ActivationService activationService;
    @Autowired
    private CompanyService companyService;

    @Value("#{ @environment['client.accountingBaseUrl'] }")
    private String accountingBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private InvoiceResultSummeryRepository invoiceResultSummeryRepository;

    @PostMapping("/loadForm") //launch F5
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> loadForm(@RequestBody FormRequest request) {
        ResponseEntity<?> result;
        logger.info(PURPLE_BOLD_BRIGHT + "/LoadForm " + RESET);
        try {
            logger.info("calling loadForm with legalName: " + request.getLegalName());
            logger.info("pulling listing for " + request.getUuid());
            Listing listing = storeService.loadListingById(Long.valueOf(request.getUuid()));
            logger.info(PURPLE_BOLD_BRIGHT + "Store: " + listing + RESET);
            Calendar cal = Calendar.getInstance();
            cal.setTime(listing.getPeriodEnd());
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date adjustedPeriodEnd = cal.getTime();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String formattedPeriodEnd = format.format(adjustedPeriodEnd);

            List<Transaction> transactionsRaw = transactionsService.loadTransactions(
                    request.getLegalName(),
                    format.format(listing.getPeriodStart()) + "_" + formattedPeriodEnd
            );

            logger.info("PERIOD START: " + listing.getPeriodStart() + "_ " + "PERIOD END: " + adjustedPeriodEnd);

            F5Form form = gstService.calculateF5(
                    request.getLegalName(),
                    listing.getCurrency(),
                    listing.getPeriodStart(),
                    adjustedPeriodEnd,
                    Long.valueOf(request.getUuid()),
                    request.getTaxNum(),
                    request.getGstNum()
            );

            form.setTransactions(transactionsRaw);
            logger.info("==PERIOD START: " + form.getStartDate() + "_ " + "==PERIOD END: " + form.getEndDate());

            storeService.storeF5Form(request.getLegalName(), request.getUuid(), form);

            result = ResponseEntity.ok(
                    new LoadFormResponse().setForm(form.toString()).setMessage(SUCCESS).setPass(true)
            );
        } catch (Exception e) {
            result = ResponseEntity.ok(
                    new LoadFormResponse().setForm(null).setMessage(e.getMessage()).setPass(false)
            );
        }
        return result;
    }


//    //TODO >>>>>>OUT OF DATE<<<<<<  >>>>>>PARSE_F5 NEEDS TO BE REPLACED<<<<<<
//    @PostMapping("/saveForm")
////    @PreAuthorize("#oauth2.hasScope('billing')")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<?> saveForm(@RequestBody AddFormRequest request) {
//        try {
//            logger.info("calling saveForm with legalName: " + request.getLegalName());
//            if (!storeService.storeF5Form(request.getLegalName(), request.getUuid(),
//                    F5Form.parseF5(request.getPayload()))) {
//                String msg = "Could not store form for " + request.getLegalName() + " and " + request.getUuid();
//                logger.severe(msg);
//                throw new GstException(msg);
//            }
//            return ResponseEntity.ok(new AbstractResponse().setMessage(SUCCESS).setPass(true));
//        }catch (Exception e){
//            return ResponseEntity.ok(new AbstractResponse().setMessage(e.getMessage()).setPass(false));
//        }
//    }

    @PostMapping("/submit")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> submit(@RequestBody SubmitRequest request) {
        try {
            logger.info("calling submit with : " + request.toString());
            logger.info("loading listing for id: " + request.getUuid());
            Listing listing = storeService.loadListingById(Long.valueOf(request.getUuid()));
            logger.info("==listing: " + listing.toString());

            F5Form form = storeService.loadF5Form(request.getLegalName(), request.getUuid());
            logger.info("==form: " + form.toString());
            logger.info("==request: " + request.toString());
            logger.info("about to update f5, form has " + form.getTransactions().size() + " transactions");
            logger.info("==transactions: " + form.getTransactions().toString());

            irasService.updateF5(form, request.getGstReturn(), request.getLegalName(), request.getUuid()); //5bce9314-eae2-4b02-84d2-12daecd73c67
            logger.info("==request: " + request.toString());
            logger.info("about to process adjustments, form has " + form.getTransactions().size() + " transactions");
            transactionsService.processAdjustments(form, request.getAdjustments());
            logger.info("about to store f5, form has " + form.getTransactions().size() + " transactions");
            storeService.storeF5Form(request.getLegalName(), request.getUuid(), form);
            logger.info("about to submit f5, form has " + form.getTransactions().size() + " transactions");
            if (irasService.submit(form, request.getLegalName(), request.getCode(), request.getForm()))

                listing.setStatus(GstStatus.Submitted);

            else
                listing.setStatus(GstStatus.PartiallySubmitted);

            listing.setNetGST(form.getBox8());
            logger.info("storing listing");
            storeService.storeListing(listing);
            logger.info("==listing " + storeService.storeListing(listing));

            storeService.storeTransactionsXlsl(request.getLegalName(), request.getUuid(), form.buildXlsxFile());
            logger.info("==storeTransactionsXlsl " + storeService.storeTransactionsXlsl(request.getLegalName(), request.getUuid(), form.buildXlsxFile()));
            return ResponseEntity.ok(new AbstractResponse().setMessage(SUCCESS).setPass(true));
        }catch (Exception e){
            return ResponseEntity.ok(new AbstractResponse().setMessage(e.getMessage()).setPass(false));
        }
    }

//    @PostMapping("/activate")
////    @PreAuthorize("#oauth2.hasScope('billing')")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<?> activate(@RequestBody ActivateCompanyRequest request) {
//        try {
//            logger.info("calling activate with legalName: " + request.getLegalName());
//
//            Pair<Listing, Listing> rolebackPair = null;
//            ActivationRecord rolebackRecord = null;
//
//            try {
//                Company company = companyService.loadCompanyByCompanyID(request.getLegalName());
//                company.setGstEnabled(true);
//                companyService.save(company);
//
//                ActivationRecord record = new ActivationRecord();
//                record.setBox1(request.isBox1());
//                record.setBox2(request.isBox2());
//                record.setDesignation(request.getDesignation());
//                record.setEmail(request.getEmail());
//                record.setName(request.getName());
//                record.setLegalName(request.getLegalName());
//                record.setPhone(request.getPhone());
//                rolebackRecord = record;
//                storeService.storeActivationRecord(record);
//                activationService.retroactiveFxCalculations(request.getLegalName());
//                switch (request.getTaxReportingFrequency())  {
//
//                    case "MONTHLY":
//                        logger.info("running monthly");
//                        Pair<Listing, Listing> monthlyListings = gstService.getMonthlyListingOnActivate(
//                                request.getFinancialYearEnd(),
//                                request.getLegalName(),
//                                "SGD",
//                                request.getTaxNum(),
//                                request.getGstNum()
//                        );
//                        rolebackPair = monthlyListings;
//                        storeService.storeListing(monthlyListings.getLeft());
//                        storeService.storeListing(monthlyListings.getRight());
//                        break;
//                    case "QUARTERLY":
//                        logger.info("running quarterly");
//                        Pair<Listing, Listing> quarterlyListings = gstService.getQuarterlyListingOnActivate(
//                                request.getFinancialYearEnd(),
//                                request.getLegalName(),
//                                "SGD",
//                                request.getTaxNum(),
//                                request.getGstNum()
//                        );
//                        rolebackPair = quarterlyListings;
//                        storeService.storeListing(quarterlyListings.getLeft());
//                        storeService.storeListing(quarterlyListings.getRight());
//                        break;
//                    default:
//                        throw new Exception("passed tax reporting freq " + request.getTaxReportingFrequency() + " is invalid");
//                }
//
//
//            } catch (Exception e) {
//                storeService.deleteActivationRecord(rolebackRecord);
//                storeService.deleteListing(rolebackPair.getLeft());
//                storeService.deleteListing(rolebackPair.getRight());
//                e.printStackTrace();
//                logger.severe("Error storing activation record for " + request.toString());
//                throw e;
//            }
//            return ResponseEntity.ok(new AbstractResponse().setMessage(SUCCESS).setPass(true));
//        }catch (Exception e){
//            return ResponseEntity.ok(new AbstractResponse().setMessage(e.getMessage()).setPass(false));
//        }
//    }
//
    @PostMapping("/getSingPassURL")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> getSingPassURL(@RequestBody SingPassRequest request) {
        try {
            logger.info("calling getSingPassURL with legalName: " + request);
            String url = "";
            for (int i = 0; i < 3; i++) {
                try {
                    logger.info("attempt " + i);
                    url = irasProperties.isCorpPass() ?
                            irasService.callCorpPathAuth(request.getLegalName(), request.getForm()) :
                            irasService.callSingPathAuth(request.getLegalName());
                    break;
                } catch (Exception e) {
                    if (i == 2) throw e;
                }
            }
            logger.info("sending url: " + url + " for " + request);
            return ResponseEntity.ok(
                    new GetSingPassURLResponse().setUrl(url).setMessage(SUCCESS).setPass(true)
            );
        }catch (Exception e){
            return ResponseEntity.ok(
                    new GetSingPassURLResponse().setUrl(null).setMessage(e.getMessage()).setPass(false)
            );
        }
    }

    @PostMapping("/loadTransactions") //launch F5 with /loadForm Controller
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> loadTransactions(@RequestBody LoadTransactionsRequest request) {
        logger.info(PURPLE_BOLD_BRIGHT + "/LoadTransactions " + RESET);

        try {
            logger.info("calling loadTransactions with legalName: " + request.getLegalName());
            List<Transaction> transactionsRaw = transactionsService.loadTransactions(
                    request.getLegalName(), request.getPeriod()
            );

            logger.info("DEBUG ---> Loaded " + transactionsRaw.size() + " transactions in transactionsRaw");
            logger.info("loading f5 for: " + request.getLegalName() + " and " + request.getUuid());


            F5Form f5;
            try{
                f5 = storeService.loadF5Form(request.getLegalName(), request.getUuid());
            }catch (Exception e){
                f5 = new F5Form();
            }
            f5.setTransactions(transactionsRaw);
            logger.info("Storing f5 for: " + request.getLegalName() + " and " + request.getUuid());
            storeService.storeF5Form(request.getLegalName(), request.getUuid(), f5);

            List<Map<String, String>> transactions = transactionsRaw.stream()
                    .map(transaction -> transaction.flattenToMap())
                    .collect(Collectors.toList());

            logger.info(transactions.toString());

            return ResponseEntity.ok(new LoadTransactionsResponse().setTransactions(transactions).setMessage(SUCCESS).setPass(true));
        }catch (Exception e){
            return ResponseEntity.ok(new LoadTransactionsResponse().setTransactions(null).setMessage(e.getMessage()).setPass(false));
        }
    }
//
//    @PostMapping("/loadListings") //1
////    @PreAuthorize("#oauth2.hasScope('billing')")
//    public ResponseEntity<?> loadListings(@RequestBody LoadListingsRequest request) {
//        try {
//            logger.info("calling loadListings with legalName: " + request.getLegalName());
//            List<Listing> listings = storeService.loadListings(request.getLegalName()); //==
//            listings = listingService.checkForStatusChange(removeDuplicateListings(listings));
//            listings = listingService.checkNewListing(listings);
//            List<Map<String, String>> payload = new ArrayList<>();
//            listings.forEach(listing -> {
//                Map<String, String> map = new HashMap<>();
//                map.put("period", concatPeriod(listing.getPeriodStart(), listing.getPeriodEnd()));
//
//                // Update the due date formatting to display the last day of the month
//                LocalDate dueDate = listing.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//                LocalDate lastDayOfMonth = dueDate.with(TemporalAdjusters.lastDayOfMonth());
//                map.put("dueDate", lastDayOfMonth.toString()); // Update the due date format
//
//                map.put("status", listing.getStatus().name());
//                map.put("currency", listing.getCurrency());
//                map.put("netGst", listing.getNetGST() + "");
//                map.put("id", listing.getId() + "");
//                payload.add(map);
//            });
//            return ResponseEntity.ok(
//                    new LoadListingsResponse().setListings(payload).setMessage(SUCCESS).setPass(true)
//            );
//        }catch (Exception e){
//            e.printStackTrace();
//            logger.severe(e.getMessage());
//            return ResponseEntity.ok(
//                    new LoadListingsResponse().setListings(null).setMessage(e.getMessage()).setPass(false)
//            );
//        }
//    }
//
//
//
//
    @PostMapping("/loadActions")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> loadActions(@RequestBody Long request) {
        try {
            logger.info("calling loadActions with summaryID: " + request);
            List<Action> actions = actionService.loadActions(request);
            List<Map<String, String>> payload = new ArrayList<>();
            for (Action action : actions) {
                Map<String, String> map = new HashMap<>();
                map.put("taxCode", action.getTaxCode());
                map.put("description", action.getDescription());
                map.put("amount", action.getAmount());
                map.put("pretax", action.getPreTax());
                map.put("tax", action.getTax());
                map.put("id", action.getId() + "");
                map.put("paidDate", printPaidDate(action.getPaidDate()));
                map.put("lineNumber", action.getLineNumber() + "");
                map.put("accountType", action.getAccountType());
                map.put("accountCode", action.getAccountCode());
                map.put("documentType", action.getDocumentType().name());

                payload.add(map);
            }
            return ResponseEntity.ok(new LoadActionsResponse().setActions(payload).setMessage(SUCCESS).setPass(true));
        }catch (Exception e){
            logger.severe(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new LoadActionsResponse().setActions(null).setMessage(e.getMessage()).setPass(false));
        }

    }

//    @PostMapping("/storeActions")
////    @PreAuthorize("#oauth2.hasScope('billing')")
//    public ResponseEntity<?> storeActions(@RequestBody StoreActionsRequest request) {
//        try {
//            logger.info("calling storeActions with summaryID: " + request.getSummaryID() + " and " + request.getActions() + " actions");
//
//            if (request.getDocumentType().equals(DocumentType.OTHER))
//                throw new Exception("Document type OTHER is not supported");
//
//            preprocessSAR(request);
//
//            List<Action> actions = new ArrayList<>();
//
//            for (Map<String, String> map : request.getActions()) {
//                Action action = new Action();
//                action.setSummaryID(request.getSummaryID());
//                action.setDocumentType(request.getDocumentType());
//                action.setDescription(map.get("description"));
//                action.setAmount(map.get("amount"));
//                action.setTax(map.get("tax"));
//                action.setPreTax(map.get("pretax"));
//                action.setTaxCode(map.get("taxCode"));
//                action.setType(map.get("type"));
//                action.setLineNumber(Integer.valueOf(map.get("lineNumber")));
//                action.setAccountType(map.get("accountType"));
//                action.setAccountCode(map.get("accountCode"));
//                if (map.containsKey("id")) {
//                    action.setId(Long.valueOf(map.get("id")));
//                }
//                action.setLegalName(request.getLegalName());
//                action.setDate(request.getDate());
//                action.setSupply(request.isSupply());
//                action.setPaidDate(parsePaidDate(map.get("paidDate")));
//                action.setFxRate(1d);
//                action.setSgdRate(1d);
//                action.setFunctionalCurrency(request.getFunctionalCurrency());
//                actions.add(action);
//
//            }
//
//            logger.info("created " + actions.size() + " actions");
//
//            List<ResultSummery> resultSummaries = resultSummeryService.loadAllSummariesByIDs(
//                    actions.stream().map(action -> new IdTypePair(action.getSummaryID(), action.getDocumentType())).distinct().collect(Collectors.toList())
//            );
//            for (Action action1 : actions) {
//                ResultSummery summery = resultSummaries.stream().filter(resultSummary -> resultSummary.getId().equals(action1.getSummaryID())).findAny().get();
//                logger.info("loaded " + summery.getType());
//                if (summery instanceof InvoiceResultSummery) {
//                    InvoiceResultSummery invoice = (InvoiceResultSummery) summery;
//                    if (invoice.getCurrency() == null || invoice.getCurrency().isBlank()) {
//                        logger.warning("No Currency detected for action (sid) " + action1.getSummaryID());
//                        continue;
//                    }
//                    try {
//                        if (action1.getPaidDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getPaidDate(), invoice.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getPaidDate(), invoice.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//
//                        } else if (action1.getDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getDate(), invoice.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getDate(), invoice.getCurrency(), "SGD"));
//
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//
//                        } else {
//                            action1.setFxRate(0d);
//                            action1.setSgdRate(0d);
//                        }
//                        continue;
//                    } catch (Exception e) {
//                        logger.severe(e.getMessage());
//                        continue;
//                    }
//                } else if (summery instanceof ReceiptResultSummary) {
//                    ReceiptResultSummary receipt = (ReceiptResultSummary) summery;
//                    if (receipt.getCurrency() == null || receipt.getCurrency().isBlank()) {
//                        logger.warning("No Currency detected for action (sid) " + action1.getSummaryID());
//                        continue;
//                    }
//                    try {
//                        if (action1.getPaidDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getPaidDate(), receipt.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getPaidDate(), receipt.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else if (action1.getDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getDate(), receipt.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getDate(), receipt.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else {
//                            action1.setFxRate(0d);
//                            action1.setSgdRate(0d);
//                        }
//                        continue;
//                    } catch (Exception e) {
//                        logger.severe(e.getMessage());
//                        continue;
//                    }
//                } else if (summery instanceof DebitNoteResultSummary) {
//                    DebitNoteResultSummary dn = (DebitNoteResultSummary) summery;
//                    if (dn.getCurrency() == null || dn.getCurrency().isBlank()) {
//                        logger.warning("No Currency detected for action (sid) " + action1.getSummaryID());
//                        continue;
//                    }
//                    try {
//                        if (action1.getPaidDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getPaidDate(), dn.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getPaidDate(), dn.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else if (action1.getDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getDate(), dn.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getDate(), dn.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else {
//                            action1.setFxRate(0d);
//                            action1.setSgdRate(0d);
//                        }
//                        continue;
//                    } catch (Exception e) {
//                        logger.severe(e.getMessage());
//                        continue;
//                    }
//                } else if (summery instanceof CreditNoteResultSummary) {
//                    CreditNoteResultSummary cn = (CreditNoteResultSummary) summery;
//                    if (cn.getCurrency() == null || cn.getCurrency().isBlank()) {
//                        logger.warning("No Currency detected for action (sid) " + action1.getSummaryID());
//                        continue;
//                    }
//                    try {
//                        if (action1.getPaidDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getPaidDate(), cn.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getPaidDate(), cn.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else if (action1.getDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getDate(), cn.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getDate(), cn.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else {
//                            action1.setFxRate(0d);
//                            action1.setSgdRate(0d);
//                        }
//                        continue;
//                    } catch (Exception e) {
//                        logger.severe(e.getMessage());
//                        continue;
//                    }
//                } else if (summery instanceof ImportPermitResultSummary) {
//                    ImportPermitResultSummary ip = (ImportPermitResultSummary) summery;
//                    if (ip.getCurrency() == null || ip.getCurrency().isBlank()) {
//                        logger.warning("No Currency detected for action (sid) " + action1.getSummaryID());
//                        continue;
//                    }
//                    try {
//                        if (action1.getPaidDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getPaidDate(), ip.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getPaidDate(), ip.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else if (action1.getDate() != null) {
//                            action1.setFxRate(fxService.getRate(action1.getDate(), ip.getCurrency(), action1.getFunctionalCurrency()));
//                            if (!action1.getFunctionalCurrency().equals("SGD"))
//                                action1.setSgdRate(fxService.getRate(action1.getDate(), ip.getCurrency(), "SGD"));
//                            else
//                                action1.setSgdRate(action1.getFxRate());
//                        } else {
//                            action1.setFxRate(0d);
//                            action1.setSgdRate(0d);
//                        }
//                        continue;
//                    } catch (Exception e) {
//                        logger.severe(e.getMessage());
//                        continue;
//                    }
//                }
//                continue;
//
//            }
//
//            actionService.replaceActions(actions);
//
//            List<Action> pulledActions = actionService.loadActions(request.getSummaryID());
//            List<Map<String, String>> payload = new ArrayList<>();
//            for (Action action : pulledActions) {
//                Map<String, String> map = new HashMap<>();
//                map.put("taxCode", action.getTaxCode());
//                map.put("description", action.getDescription());
//                map.put("amount", action.getAmount());
//                map.put("pretax", action.getPreTax());
//                map.put("tax", action.getTax());
//                map.put("id", action.getId() + "");
//                map.put("paidDate", printPaidDate(action.getPaidDate()));
//                map.put("lineNumber", action.getLineNumber() + "");
//                map.put("accountType", action.getAccountType());
//                map.put("accountCode", action.getAccountCode());
//                map.put("documentType", action.getDocumentType().name());
//
//                payload.add(map);
//            }
//
//            return ResponseEntity.ok(new LoadActionsResponse().setActions(payload).setMessage(SUCCESS).setPass(true));
//        }catch (Exception e){
//            logger.severe(e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.ok(new LoadActionsResponse().setActions(null).setMessage(e.getMessage()).setPass(false));
//        }
//
//    }

    @PostMapping("/deleteActions")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteActions(@RequestBody DeleteActionsRequest request) {
        try {
            logger.info("calling deleteActions with path: " + request.getLegalName());
            actionService.deleteActionsByID(request.getIds());
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(SUCCESS).setPass(true)
            );
        }catch (Exception e){
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(e.getMessage()).setPass(false)
            );
        }
    }

    @PostMapping("/deleteActionsBySummaryID")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteActionsBySummaryID(@RequestBody DeleteActionsRequest request) {
        try {
            logger.info("calling deleteActionsBySummaryID with path: " + request.getLegalName());
            actionService.deleteActionsBySummaryID(request.getIds());
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(SUCCESS).setPass(true)
            );
        }catch (Exception e){
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(e.getMessage()).setPass(false)
            );
        }
    }

    @PostMapping("/getChartOfAccounts")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> getChartOfAccounts(@RequestBody String request) {
        try{
            logger.info("calling gatChartOfAccounts with legalName: " + request);
            List<String> coa = actionService.loadChartOfAccounts(request);
            return ResponseEntity.ok(
                    new GetChartOfAccountsResponse().setChartOfAccounts(coa).setMessage(SUCCESS).setPass(true)
            );
        }catch (Exception e){
            return ResponseEntity.ok(
                    new GetChartOfAccountsResponse().setChartOfAccounts(null).setMessage(e.getMessage()).setPass(false)
            );
        }
    }

//    @PostMapping("/pushDocuments")
////    @PreAuthorize("#oauth2.hasScope('billing')")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<?> pushDocuments(@RequestBody String path) {
//        try{
//            logger.info("calling pushDocuments with path: " + path);
//            String legalName = path.split("/")[0];
//            logger.info("Loading data for path: " + path);
//            List<ResultSummery> summaries = resultSummeryService.loadAll(path);
//            logger.info("loaded " + summaries.size() + " summaries for path: " + path);
//            List<Pair<ResultSummery, List<Action>>> pairs = new ArrayList<>();
//            List<Action> actions = actionService.loadActionsBySummaryIDs(
//                    summaries.stream().map(s -> s.getId()).collect(Collectors.toList()));
//
//            for (ResultSummery summary : summaries) {
//                List<Action> relevant = actions.stream().filter(
//                        action -> action.getSummaryID().equals(summary.getId())
//                ).collect(Collectors.toList());
//                if (!relevant.isEmpty())
//                    pairs.add(new Pair<>(summary, relevant));
//            }
//
//            PushDocumentsRequest pushDocumentsRequest = new PushDocumentsRequest();
//
//            logger.info("Creating request for path: " + path);
//            pairs.forEach(pair -> {
//
//                PushDocument pushDocument = new PushDocument();
//                pushDocument.setDocument_type(pair.getLeft().getType().name());
//                pushDocument.setDate(pair.getLeft().getDate());
//                pushDocument.setLabel(pair.getLeft().getLabels().contains("Purchase") ? "Purchase" : "Supply");
//
//                pushDocument.setLine_items(pair.getRight().stream().map(action -> {
//                    LineItem lineItem = new LineItem();
//                    lineItem.setAccount(action.getAccountType());
//                    lineItem.setAccount_code(action.getAccountCode());
//                    lineItem.setDescription(action.getDescription());
//                    lineItem.setPre_tax_amount(Double.valueOf(cleanNumeric(action.getPreTax())));
//                    lineItem.setTax_amount(Double.valueOf(cleanNumeric(action.getTax())));
//                    lineItem.setTotal_amount(action.getAmount());
//                    if (TaxCodeUtil.mapTaxCode(action).equals("")) {
//                        lineItem.setTax_type(action.getTaxCode());
//                    } else {
//                        lineItem.setTax_type(TaxCodeUtil.mapTaxCode(action));
//                    }
//                    pushDocument.setFunctionalCurrency(action.getFunctionalCurrency());
//                    return lineItem;
//                }).collect(Collectors.toList()));
//
//                pushDocument.setCurrency_rate(pair.getRight().get(0).getFxRate());
//
//                if (pair.getLeft() instanceof InvoiceResultSummery) {
//                    InvoiceResultSummery invoiceResultSummery = (InvoiceResultSummery) pair.getLeft();
//                    pushDocument.setPre_tax_amount(invoiceResultSummery.getPreTaxAmount());
//                    pushDocument.setTax_amount(invoiceResultSummery.getTaxAmount());
//                    pushDocument.setTotal_amount(invoiceResultSummery.getTotalAmount());
//                    pushDocument.setCurrency_code(invoiceResultSummery.getCurrency());
//                    pushDocument.setId(invoiceResultSummery.getInvoiceNumber());
//                    pushDocument.setContact(
//                            new Contact().setName(
//                                    invoiceResultSummery.getLabels().contains("Purchase") ?
//                                            invoiceResultSummery.getSupplierName() : invoiceResultSummery.getCustomerName()
//                            )
//                    );
//                } else if (pair.getLeft() instanceof ReceiptResultSummary) {
//
//                    ReceiptResultSummary receiptResultSummary = (ReceiptResultSummary) pair.getLeft();
//                    pushDocument.setPre_tax_amount(receiptResultSummary.getPreTaxAmount());
//                    pushDocument.setTax_amount(receiptResultSummary.getTaxAmount());
//                    pushDocument.setTotal_amount(receiptResultSummary.getTotalAmount());
//                    pushDocument.setCurrency_code(receiptResultSummary.getCurrency());
//                    pushDocument.setId(receiptResultSummary.getInvoiceNumber());
//                    pushDocument.setContact(
//                            new Contact().setName(
//                                    receiptResultSummary.getLabels().contains("Purchase") ?
//                                            receiptResultSummary.getSupplierName() : receiptResultSummary.getCustomerName()
//                            )
//                    );
//                } else if (pair.getLeft() instanceof DebitNoteResultSummary) {
//                    DebitNoteResultSummary debitNoteResultSummary = (DebitNoteResultSummary) pair.getLeft();
//                    pushDocument.setPre_tax_amount(debitNoteResultSummary.getPreTaxAmount());
//                    pushDocument.setTax_amount(debitNoteResultSummary.getTaxAmount());
//                    pushDocument.setTotal_amount(debitNoteResultSummary.getTotalAmount());
//                    pushDocument.setCurrency_code(debitNoteResultSummary.getCurrency());
//                    pushDocument.setId(debitNoteResultSummary.getDebitNoteNumber());
//                    pushDocument.setInvoice_id(debitNoteResultSummary.getInvoiceNumber());
//                    pushDocument.setContact(
//                            new Contact().setName(
//                                    debitNoteResultSummary.getLabels().contains("Purchase") ?
//                                            debitNoteResultSummary.getSupplierName() : debitNoteResultSummary.getCustomerName()
//                            )
//                    );
//                } else if (pair.getLeft() instanceof CreditNoteResultSummary) {
//                    CreditNoteResultSummary creditNoteResultSummary = (CreditNoteResultSummary) pair.getLeft();
//                    pushDocument.setPre_tax_amount(creditNoteResultSummary.getPreTaxAmount());
//                    pushDocument.setTax_amount(creditNoteResultSummary.getTaxAmount());
//                    pushDocument.setTotal_amount(creditNoteResultSummary.getTotalAmount());
//                    pushDocument.setCurrency_code(creditNoteResultSummary.getCurrency());
//                    pushDocument.setId(creditNoteResultSummary.getCreditNoteNumber());
//                    pushDocument.setInvoice_id(creditNoteResultSummary.getInvoiceNumber());
//                    pushDocument.setContact(
//                            new Contact().setName(
//                                    creditNoteResultSummary.getLabels().contains("Purchase") ?
//                                            creditNoteResultSummary.getSupplierName() : creditNoteResultSummary.getCustomerName()
//                            )
//                    );
//                } else if (pair.getLeft() instanceof ImportPermitResultSummary) {
//                    ImportPermitResultSummary importPermitResultSummary = (ImportPermitResultSummary) pair.getLeft();
//                    pushDocument.setPre_tax_amount(importPermitResultSummary.getPreTaxAmount());
//                    pushDocument.setTax_amount(importPermitResultSummary.getTaxAmount());
//                    pushDocument.setTotal_amount(importPermitResultSummary.getTotalAmount());
//                    pushDocument.setCurrency_code("SGD");
//                    pushDocument.setCurrency_rate(1);
//                    pushDocument.setId(importPermitResultSummary.getPermitNumber());
//                    pushDocument.setContact(
//                            new Contact().setName(
//                                    importPermitResultSummary.getLabels().contains("Purchase") ?
//                                            importPermitResultSummary.getExporterName() : importPermitResultSummary.getImporterName()
//                            )
//                    );
//                } else if (pair.getLeft() instanceof OtherResultSummary) {
//                    return;
//                }
//
//                pushDocumentsRequest.addDocuments(pushDocument);
//            });
//
//            logger.info("request: \n" + new ObjectMapper().writeValueAsString(pushDocumentsRequest));
//            HttpEntity<PushDocumentsRequest> request = new HttpEntity<>(pushDocumentsRequest);
//            restTemplate.exchange(
//                    accountingBaseUrl + "/api/v1/" + legalName + "/document",
//                    HttpMethod.POST,
//                    request,
//                    DocumentResponse.class
//            );
//            return ResponseEntity.ok(
//                    new AbstractResponse().setMessage(SUCCESS).setPass(true)
//            );
//        }catch (Exception e){
//            return ResponseEntity.ok(
//                    new AbstractResponse().setMessage(e.getMessage()).setPass(false)
//            );
//        }
//    }

    @PostMapping("/storeF5PDF")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> storeF5PDF(@RequestBody StoreFileRequest request) {
        try {
            logger.info("calling storeF5PDF with legalName: " + request.getLegalName());
            storeService.storeF5PDF(request.getLegalName(), request.getFileName(), new ByteArrayInputStream(request.getPayload()));
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(SUCCESS).setPass(true)
            );
        }catch (Exception e){
            return ResponseEntity.ok(
                    new AbstractResponse().setMessage(e.getMessage()).setPass(false)
            );
        }
    }

    @PostMapping("/loadF5PDF")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> loadF5PDF(@RequestBody LoadF5PDFRequest request) throws Exception {
        logger.info("calling loadF5PDF with legalName: " + request.getLegalName());

        Resource resource = storeService.loadF5PDF(request.getLegalName(), request.getUuid());
        logger.info("==resource: " + resource.toString());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

//    @PostMapping("/loadTransactionXlsx")
//    public ResponseEntity<?> loadTransactionXlsx(@RequestBody LoadF5PDFRequest request) throws GstException {
//        logger.info("calling loadTransactionXlsx with legalName: " + request.getLegalName() + " and uuid: " + request.getUuid());
//
//        Resource resource = storeService.loadTransactionsXlsl(request.getLegalName(), request.getUuid());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//
//
//    }


    private String concatPeriod(Date start, Date end) {
        StringBuilder sb = new StringBuilder();

        Calendar startCal = GregorianCalendar.getInstance();
        Calendar endCal = GregorianCalendar.getInstance();
        startCal.setTime(start);
        endCal.setTime(end);

        sb.append(startCal.get(Calendar.YEAR))
                .append("-")
                .append(startCal.get(Calendar.MONTH) + 1)
                .append("-")
                .append(startCal.get(Calendar.DATE))
                .append("_")
                .append(endCal.get(Calendar.YEAR))
                .append("-")
                .append(endCal.get(Calendar.MONTH) + 1)
                .append("-")
                .append(endCal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Use last day of the month
        return sb.toString();
    }


    private Date parsePaidDate(String paidDate) throws ParseException {
        if (paidDate.isEmpty())
            return null;
        logger.info("parsing paidDate: " + paidDate + " with MM-dd-yyyy");
        DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        return format.parse(paidDate);
    }

    private String printPaidDate(Date paidDate) {
        if (paidDate == null)
            return "";
        DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        return format.format(paidDate);
    }

    private String cleanNumeric(String numeric) {
        return numeric.replaceAll("[^0-9.]", "");
    }

    private void preprocessSAR(StoreActionsRequest sar) {
        for (Map<String, String> actionMap : sar.getActions()) {
            if (actionMap.containsKey("amount")) {
                if (actionMap.get("amount") == null || actionMap.get("amount").equals("0") || actionMap.get("amount").isBlank()) {
                    double tax = Double.parseDouble(
                            (actionMap.get("tax") == null || actionMap.get("tax").isBlank()) ?
                                    "0" : actionMap.get("tax")
                    );
                    double pretax = Double.parseDouble(
                            (actionMap.get("pretax") == null || actionMap.get("pretax").isBlank()) ?
                                    "0" : actionMap.get("pretax")
                    );

                    actionMap.put("amount", (tax + pretax) + "");
                }
            }
        }
    }

        private List<Listing> removeDuplicateListings(List<Listing> originalListings) {
        logger.info("Removing Duplicate Listings");

        List<Listing> currentPeriodListings = new ArrayList<>();
        List<Listing> dueListings = new ArrayList<>();
        Map<String, Listing> uniqueListingsMap = new HashMap<>();

        // Segregate listings by status
        for (Listing listing : originalListings) {
            if (listing.getStatus().equals(GstStatus.CurrentPeriod)) {
                currentPeriodListings.add(listing);
            } else if (listing.getStatus().equals(GstStatus.Due)) {
                dueListings.add(listing);
            } else {
                String key = listing.getPeriodStart().toString() + listing.getStatus().toString();
                uniqueListingsMap.putIfAbsent(key, listing);
            }
        }

        // Find the unique current period listing with the latest due date
        Listing uniqueCurrentPeriodListing = currentPeriodListings.stream()
                .max(Comparator.comparing(Listing::getDueDate))
                .orElse(null);

        // Find the unique due listing with the latest period end
        Listing uniqueDueListing = dueListings.stream()
                .min(Comparator.comparing(Listing::getPeriodStart))
                .orElse(null);

        // Prepare the final list of unique listings
        List<Listing> uniqueListings = new ArrayList<>(uniqueListingsMap.values());

        // Add the unique current period listing if it exists and is not a duplicate
        if (uniqueCurrentPeriodListing != null && !uniqueListingsMap.containsKey(uniqueCurrentPeriodListing.getPeriodStart().toString())) {
            uniqueListings.add(uniqueCurrentPeriodListing);
        }

        // Add the unique due listing if it exists and is not a duplicate
        if (uniqueDueListing != null && !uniqueListingsMap.containsKey(uniqueDueListing.getPeriodStart().toString())) {
            uniqueListings.add(uniqueDueListing);
        }

        // Sort the final list based on periodStart for proper display in the UI
        uniqueListings.sort(Comparator.comparing(Listing::getPeriodStart));

        return uniqueListings;
    }
}
