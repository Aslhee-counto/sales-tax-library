package com.xion.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Logger;

public class F5Form {

    private static Logger logger = Logger.getLogger(F5Form.class.getName());

    private final double BLOCKSIZE = 1000;
    private final String VAULTVERSION = "vault.xion.ai-1.1";

    //filingInfo
    private String  legalName                   = "";
    private String  taxRefNo                    = "";
    private String  gstRefNo                    = "";
    private String  startDate                   = "";
    private String  endDate                     = "";
    //supplies
    private double  box1                        = 0;
    private double  box2                        = 0;
    private double  box3                        = 0;
    private double  box4                        = 0;
    //purchases
    private double  box5                        = 0;
    //taxes
    private double  box6                        = 0;
    private double  box7                        = 0;
    private double  box8                        = 0;
    //schemes
    private double  box9                        = 0;
    private boolean box10                       = false;
    private boolean box11                       = false;
    private double  box11Amt                    = 0;
    private boolean box12                       = false;
    //revenue
    private double  box13                       = 0;
    //RCElectronicMktplaceOpr
    private double  box14                       = 0;
    //igdScheme
    private double  box15                       = 0;
    private double  box16                       = 0;
    private double  box17                       = 0;
    //declaration
    private boolean declareTrueCompleteChk      = true;
    private boolean declareIncRtnFalseInfoChk   = true;
    private String  declarantDesgtn             = "";
    private String  contactPerson               = "";
    private String  contactNumber               = "";
    private String  contactEmail                = "";
    //reasons
    private boolean grp1BadDebtRecoveryChk      = false;
    private boolean grp1PriorToRegChk           = false;
    private boolean grp1OtherReasonChk          = false;
    private String  grp1OtherReasons            = "";
    private boolean grp2TouristRefundChk        = false;
    private boolean grp2AppvBadDebtReliefChk    = false;
    private boolean grp2CreditNotesChk          = false;
    private boolean grp2OtherReasonsChk         = false;
    private String  grp2OtherReasons            = "";
    private boolean grp3CreditNotesChk          = false;
    private boolean grp3OtherReasonsChk         = false;
    private String  grp3OtherReasons            = "";

    private List<Transaction> transactions      = new ArrayList<>();

    public String getLegalName() {
        return removeInvalidChars(legalName);
    }
    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }
    public String getTaxRefNo() {
        return removeInvalidChars(taxRefNo);
    }
    public void setTaxRefNo(String taxRefNo) {
        this.taxRefNo = taxRefNo;
    }
    public String getGstRefNo() {
        return removeInvalidChars(gstRefNo);
    }
    public void setGstRefNo(String gstRefNo) {
        this.gstRefNo = gstRefNo;
    }
    public String getStartDate() {
//        return "2019-10-01";
        return removeInvalidChars(startDate);
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setStartDate(Date startDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.startDate = simpleDateFormat.format(startDate);
    }
    public String getEndDate() {
//        return "2019-12-31";
        return removeInvalidChars(endDate);
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setEndDate(Date endDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.endDate = simpleDateFormat.format(endDate);
        this.endDate = getLastDayOfMonth(this.endDate);

    }
    public double getBox1() {
        return box1;
    }
    public int asIntBox1() {
        return (int) box1;
    }
    public void setBox1(double box1) {
        this.box1 = box1;
    }
    public double getBox2() {
        return box2;
    }
    public int asIntBox2() {
        return (int) box2;
    }
    public void setBox2(double box2) {
        this.box2 = box2;
    }
    public double getBox3() {
        return box3;
    }
    public int asIntBox3() {
        return (int) box3;
    }
    public void setBox3(double box3) {
        this.box3 = box3;
    }
    public double getBox4() {
        return box4;
    }
    public void setBox4(double box4) {
        this.box4 = box4;
    }
    public double getBox5() {
        return box5;
    }
    public int asIntBox5() {
        return (int) box5;
    }
    public void setBox5(double box5) {
        this.box5 = box5;
    }
    public double getBox6() {
        return box6;
    }
    public void setBox6(double box6) {
        this.box6 = box6;
    }
    public double getBox7() {
        return box7;
    }
    public void setBox7(double box7) {
        this.box7 = box7;
    }
    public double getBox8() {
        return box8;
    }
    public void setBox8(double box8) {
        this.box8 = box8;
    }
    public double getBox9() {
        return box9;
    }
    public int asIntBox9() {
        return (int) box9;
    }
    public void setBox9(double box9) {
        this.box9 = box9;
    }
    public boolean isBox10() {
        return box10;
    }
    public void setBox10(boolean box10) {
        this.box10 = box10;
    }
    public boolean isBox11() {
        return box11;
    }
    public void setBox11(boolean box11) {
        this.box11 = box11;
    }
    public double getBox11Amt() {
        return box11Amt;
    }
    public void setBox11Amt(double box11Amt) {
        this.box11Amt = box11Amt;
    }
    public boolean isBox12() {
        return box12;
    }
    public void setBox12(boolean box12) {
        this.box12 = box12;
    }
    public double getBox13() {
        return box13;
    }
    public int asIntBox13() {
        return (int) box13;
    }
    public void setBox13(double box13) {
        this.box13 = box13;
    }
    public double getBox14() {
        return box14;
    }
    public void setBox14(double box14) {
        this.box14 = box14;
    }
    public double getBox15() {
        return box15;
    }
    public void setBox15(double box15) {
        this.box15 = box15;
    }
    public double getBox16() {
        return box16;
    }
    public void setBox16(double box16) {
        this.box16 = box16;
    }
    public double getBox17() {
        return box17;
    }
    public int asIntBox17() {
        return (int) box17;
    }
    public void setBox17(double box17) {
        this.box17 = box17;
    }
    public boolean isDeclareTrueCompleteChk() {
        return declareTrueCompleteChk;
    }
    public void setDeclareTrueCompleteChk(boolean declareTrueCompleteChk) {
        this.declareTrueCompleteChk = declareTrueCompleteChk;
    }
    public boolean isDeclareIncRtnFalseInfoChk() {
        return declareIncRtnFalseInfoChk;
    }
    public void setDeclareIncRtnFalseInfoChk(boolean declareIncRtnFalseInfoChk) {
        this.declareIncRtnFalseInfoChk = declareIncRtnFalseInfoChk;
    }
    public String getDeclarantDesgtn() {
        return removeInvalidChars(declarantDesgtn);
    }
    public void setDeclarantDesgtn(String declarantDesgtn) {
        this.declarantDesgtn = declarantDesgtn;
    }
    public String getContactPerson() {
        return removeInvalidChars(contactPerson);
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    public String getContactNumber() {
        return removeInvalidChars(contactNumber);
    }
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    public String getContactEmail() {
        return removeInvalidChars(contactEmail);
    }
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    public boolean isGrp1BadDebtRecoveryChk() {
        return grp1BadDebtRecoveryChk;
    }
    public void setGrp1BadDebtRecoveryChk(boolean grp1BadDebtRecoveryChk) {
        this.grp1BadDebtRecoveryChk = grp1BadDebtRecoveryChk;
    }
    public boolean isGrp1PriorToRegChk() {
        return grp1PriorToRegChk;
    }
    public void setGrp1PriorToRegChk(boolean grp1PriorToRegChk) {
        this.grp1PriorToRegChk = grp1PriorToRegChk;
    }
    public boolean isGrp1OtherReasonChk() {
        return grp1OtherReasonChk;
    }
    public void setGrp1OtherReasonChk(boolean grp1OtherReasonChk) {
        this.grp1OtherReasonChk = grp1OtherReasonChk;
    }
    public String getGrp1OtherReasons() {
        return removeInvalidChars(grp1OtherReasons);
    }
    public void setGrp1OtherReasons(String grp1OtherReasons) {
        this.grp1OtherReasons = grp1OtherReasons;
    }
    public boolean isGrp2TouristRefundChk() {
        return grp2TouristRefundChk;
    }
    public void setGrp2TouristRefundChk(boolean grp2TouristRefundChk) {
        this.grp2TouristRefundChk = grp2TouristRefundChk;
    }
    public boolean isGrp2AppvBadDebtReliefChk() {
        return grp2AppvBadDebtReliefChk;
    }
    public void setGrp2AppvBadDebtReliefChk(boolean grp2AppvBadDebtReliefChk) {
        this.grp2AppvBadDebtReliefChk = grp2AppvBadDebtReliefChk;
    }
    public boolean isGrp2CreditNotesChk() {
        return grp2CreditNotesChk;
    }
    public void setGrp2CreditNotesChk(boolean grp2CreditNotesChk) {
        this.grp2CreditNotesChk = grp2CreditNotesChk;
    }
    public boolean isGrp2OtherReasonsChk() {
        return grp2OtherReasonsChk;
    }
    public void setGrp2OtherReasonsChk(boolean grp2OtherReasonsChk) {
        this.grp2OtherReasonsChk = grp2OtherReasonsChk;
    }
    public String getGrp2OtherReasons() {
        return removeInvalidChars(grp2OtherReasons);
    }
    public void setGrp2OtherReasons(String grp2OtherReasons) {
        this.grp2OtherReasons = grp2OtherReasons;
    }
    public boolean isGrp3CreditNotesChk() {
        return grp3CreditNotesChk;
    }
    public void setGrp3CreditNotesChk(boolean grp3CreditNotesChk) {
        this.grp3CreditNotesChk = grp3CreditNotesChk;
    }
    public boolean isGrp3OtherReasonsChk() {
        return grp3OtherReasonsChk;
    }
    public void setGrp3OtherReasonsChk(boolean grp3OtherReasonsChk) {
        this.grp3OtherReasonsChk = grp3OtherReasonsChk;
    }
    public String getGrp3OtherReasons() {
        return removeInvalidChars(grp3OtherReasons);
    }
    public void setGrp3OtherReasons(String grp3OtherReasons) {
        this.grp3OtherReasons = grp3OtherReasons;
    }
    public List<Transaction> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    public boolean addTransaction(Transaction transaction){
        return this.transactions.add(transaction);
    }
    public boolean addTransactions(List<Transaction> transactions){
        return this.transactions.addAll(transactions);
    }

    public String buildF5Form(String formType) throws Exception{
        if(formType==null) {
            logger.warning("no form type passed, default to f5");
            formType = "F5";
        }
        logger.info("Building " + formType + " form for " + taxRefNo);
        try {
            String doc = "{\n" +
                        "\"filingInfo\": {\n" +
                            "\"taxRefNo\": \"" + this.getTaxRefNo() + "\",\n" +
                            "\"formType\": \"" + formType + "\",\n" +
                            "\"dtPeriodStart\": \"" + this.getStartDate() + "\",\n" +
                            "\"dtPeriodEnd\": \"" + this.getEndDate() + "\"\n" +
                        "},\n" +
                        "\"supplies\": {\n" +
                            "\"totStdSupply\": \"" + this.asIntBox1() + "\",\n" +
                            "\"totZeroSupply\": \"" + this.asIntBox2() + "\",\n" +
                            "\"totExemptSupply\": \"" + this.asIntBox3() + "\"\n" +
                        "},\n" +
                        "\"purchases\": {\n" +
                            "\"totTaxPurchase\": \"" + this.asIntBox5() + "\"\n" +
                        "},\n" +
                        "\"taxes\": {\n" +
                            "\"outputTaxDue\": \"" + limit2Decimals(this.getBox6()) + "\",\n" +
                            "\"inputTaxRefund\": \"" + limit2Decimals(this.getBox7()) + "\"\n" +
                        "},\n" +
                        "\"schemes\": {\n" +
                            "\"totValueScheme\": \"" + this.asIntBox9() + "\",\n" +
                            "\"touristRefundChk\": \"" + this.isBox10() + "\",\n" +
                            "\"touristRefundAmt\": 0.00,\n" +
                            "\"badDebtChk\": \"" + this.isBox11() + "\",\n" +
                            "\"badDebtReliefClaimAmt\": \"" + this.getBox11Amt() + "\",\n" +
                            "\"preRegistrationChk\": \"" + this.isBox12() + "\",\n" +
                            "\"preRegistrationClaimAmt\": 0.00\n" +
                        "},\n" +
                        "\"revenue\": {\n" +
                            "\"revenue\": \"" + this.asIntBox13() + "\"\n" +
                        "},\n" +
                        "\"RevChargeLVG\": {\n" +
                            "\"RCLVGChk\": \"" + "false" + "\",\n" +
                            "\"totImpServLVGAmt\": " + limit2Decimals(this.getBox14()) + "\n" +
                        "},\n" +
                        "\"ElectronicMktplaceOprRedlvr\": {\n" +
                            "\"OVRRSChk\": \"" + "false" + "\",\n" +
                            "\"totRemServAmt\": " + limitInteger(this.getBox15()) + ",\n" +
                            "\"RedlvrMktOprLVGChk\": " + "false" + ",\n" +
                            "\"totRedlvrMktOprLVGAmt\": " + limitInteger(this.getBox16()) + "\n" +
                        "},\n" +
                        "\"SupplierOfImpLVG\": {\n" +
                            "\"OwnImpLVGChk\": \"" + "false" + "\",\n" +
                            "\"totOwnImpLVGAmt\": " + limitInteger(this.getBox17()) + "\n" +
                        "},\n" +
                        "\"igdScheme\": {\n" +
                            "\"defImpPayableAmt\": \"" + limit2Decimals(this.getBox15()) + "\",\n" +
                            "\"defTotalGoodsImp\": \"" + this.asIntBox17() + "\"\n" +
                        "},\n" +
                        "\"declaration\": {\n" +
                            "\"declareTrueCompleteChk\": \"" + this.isDeclareTrueCompleteChk() + "\",\n" +
                            "\"declareIncRtnFalseInfoChk\": \"" + this.isDeclareIncRtnFalseInfoChk() + "\",\n" +
                            "\"declarantDesgtn\": \"" + this.getDeclarantDesgtn() + "\",\n" +
                            "\"contactPerson\": \"" + this.getContactPerson() + "\",\n" +
                            "\"contactNumber\": \"" + this.getContactNumber() + "\",\n" +
                            "\"contactEmail\": \"" + this.getContactEmail() + "\"\n" +
                        "},\n" +
                        "\"reasons\": {\n" +
                            "\"grp1BadDebtRecoveryChk\": \"" + this.isGrp1BadDebtRecoveryChk() + "\",\n" +
                            "\"grp1PriorToRegChk\": \"" + this.isGrp1PriorToRegChk() + "\"," +
                            "\"grp1OtherReasonChk\": \"" + this.isGrp1OtherReasonChk() + "\",\n" +
                            "\"grp1OtherReasons\": \"" + this.getGrp1OtherReasons() + "\",\n" +
                            "\"grp2TouristRefundChk\": \"" + this.isGrp2TouristRefundChk() + "\",\n" +
                            "\"grp2AppvBadDebtReliefChk\": \"" + this.isGrp2AppvBadDebtReliefChk() + "\",\n" +
                            "\"grp2CreditNotesChk\": \"" + this.isGrp2CreditNotesChk() + "\",\n" +
                            "\"grp2OtherReasonsChk\": \"" + this.isGrp2OtherReasonsChk() + "\",\n" +
                            "\"grp2OtherReasons\": \"" + this.getGrp2OtherReasons() + "\",\n" +
                            "\"grp3CreditNotesChk\": \"" + this.isGrp3CreditNotesChk() + "\",\n" +
                            "\"grp3OtherReasonsChk\": \"" + this.isGrp3OtherReasonsChk() + "\",\n" +
                            "\"grp3OtherReasons\": \"" + this.getGrp3OtherReasons() + "\"\n" +
                        "}\n" +
                    "}";

            return doc;
        }catch (Exception e){
            logger.severe("Could not create iras f5 form");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    public List<String> buildTransactionsForm(){
        logger.info("Building Transactions form for " + taxRefNo);
        try{

            int blockNum = (int) Math.ceil((double) transactions.size() / BLOCKSIZE );
            logger.info("creating " + blockNum + " blocks for " + transactions.size() + " transactions");

            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dtIAFCreation = format.format(today);

            List<String> blocks = new ArrayList<>();

            UUID uuid = UUID.randomUUID();

            for (int i=0; i<blockNum; i++) {
                List<Transaction> relevant;
                if (i==blockNum-1)
                    relevant = transactions.subList(1*blockNum, transactions.size());
                else
                    relevant = transactions.subList(1*blockNum, i*(blockNum+1)-1);
                String doc =
                        "{\n" +
                            "\"filingInfo\": {\n" +
                                "\"taxRefNo\": \"" + this.getTaxRefNo() + "\",\n" +
                                "\"gstRegNo\": \"" + this.getGstRefNo() + "\",\n" +
                                "\"dtPeriodStart\": \"" + this.getStartDate() + "\",\n" +
                                "\"dtPeriodEnd\": \"" + this.getEndDate() + "\"\n" +
                            "},\n" +
                                "\"data\": {\n" +
                                "\"identifier\": \"" + uuid.toString() + "\",\n" +
                                "\"currentChunk\": " + (i+1) + ",\n" +
                                "\"totalChunks\": " + blockNum + ",\n" +
                                "\"message\":\"" + buildMsg(relevant, dtIAFCreation) + "\", \n" +
                                "\"dtIAFCreation\": \"" + dtIAFCreation + "\",\n" +
                                "\"iafVersion\": \"IAFv1.0.0\"\n" +
                            "}\n" +
                        "}";

                blocks.add(doc);
            }

            return blocks;

        }catch (Exception e){
            logger.severe("Could not create iras f5 form");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    public Workbook buildXlsxFile(){
        logger.info("creating xlsx file for " + legalName);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        Map<String, Integer> headerIndex = new HashMap<>();
        headerIndex.put("invoiceNumber",0);
        headerIndex.put("debitNoteNumber",1);
        headerIndex.put("creditNoteNumber",2);
        headerIndex.put("permitNumber",3);
        headerIndex.put("date",4);
        headerIndex.put("supplierName",5);
        headerIndex.put("gstNumber",6);
        headerIndex.put("customerName",7);
        headerIndex.put("lineNumber",8);
        headerIndex.put("description",9);
        headerIndex.put("currency",10);
        headerIndex.put("preTaxAmount",11);
        headerIndex.put("taxAmount",12);
        headerIndex.put("preTaxAmountFCY",13);
        headerIndex.put("taxAmountFCY",14);
        headerIndex.put("taxCode",15);
        headerIndex.put("supply/Purchase",16);

        Row header = sheet.createRow(0);
        for (String columnName : headerIndex.keySet()){
            Cell description = header.createCell(headerIndex.get(columnName));
            description.setCellValue(columnName.substring(0, 1).toUpperCase() + columnName.substring(1));
            description.setCellStyle(headerStyle);
        }
        int rowNumber = 1;
        for(Transaction transaction : transactions){
            Row row = sheet.createRow(rowNumber);
            rowNumber++;

            row.createCell(headerIndex.get("description")).setCellValue(transaction.getDescription());
            row.createCell(headerIndex.get("lineNumber")).setCellValue(transaction.getLineNumber());
            row.createCell(headerIndex.get("preTaxAmount")).setCellValue(transaction.isCreditNote() ? "-" + transaction.getPreTaxAmount() : transaction.getPreTaxAmount());
            row.createCell(headerIndex.get("taxAmount")).setCellValue(transaction.isCreditNote() ? "-" + transaction.getTaxAmount() : transaction.getTaxAmount());
            row.createCell(headerIndex.get("taxCode")).setCellValue(transaction.getTaxCode());
            row.createCell(headerIndex.get("supply/Purchase")).setCellValue(transaction.isSupply() ? "Supply" : "Purchase" );
            for (String field : transaction.getAdditional().keySet()){
                if (!headerIndex.containsKey(field)){
                    continue;
                }
                row.createCell(
                        headerIndex.get(field)
                ).setCellValue(
                        field.endsWith("FCY") ?
                                transaction.isCreditNote() ?
                                        "-" + transaction.getAdditional(field)
                                        : transaction.getAdditional(field)
                        : transaction.getAdditional(field)
                );
            }
        }
        for (String field : headerIndex.keySet()) {
            Cell headerCell = header.createCell(headerIndex.get(field));
            headerCell.setCellValue(field);
            headerCell.setCellStyle(headerStyle);
        }

        return workbook;
    }

    @Override
    public String toString(){
        logger.info("Attempting to serialize f5 form");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        }catch (Exception e){
            logger.severe("Could not serialize f5 form");
            logger.severe(e.getMessage());
            return "";
        }
    }

    public static F5Form parseF5(String raw) throws Exception{
        logger.info("Attempting to deserialize f5 form");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(raw, F5Form.class);
        }catch (Exception e){
            logger.severe("Could not deserialize f5 form");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private String buildMsg(List<Transaction> relevant, String creationDate){
        StringBuilder sb = new StringBuilder();

        sb.append("CompInfoStart|\n");
        sb.append("CompanyName|CompanyUEN|GSTNo|PeriodStart|PeriodEnd|IAFCreationDate|ProductVersion|IAFVersion|\n");
        sb.append( getLegalName() + "|" + getTaxRefNo() + "|" + getGstRefNo() + "|" + getStartDate() + "|" + getEndDate() + "|" + creationDate + "|" + VAULTVERSION + "|IAFv1.0.0|\n");
        sb.append("CompInfoEnd|\n");

        sb.append("PurcDataStart|\n");
        sb.append("SupplierName|SupplierUEN|InvoiceDate|InvoiceNo|PermitNo|LineNo|ProductDescription|PurchaseValueSGD|GSTValueSGD|TaxCode|FCYCode|PurchaseFCY|GSTFCY|\n");

        int purchaseCount = 0;
        double purchaseTotal = 0;
        double purchaseTax = 0;
        for (Transaction tran : relevant) {
            if (!tran.isSupply()) {
                sb.append(
                        tran.getAdditional("supplierName") + "|" +                      //SupplierName
                        (tran.getAdditional("gstNumber").equals(null) ?
                                "NaN" : tran.getAdditional("gstNumber")) + "|" +        //SupplierUEN
                        processDate(tran.getAdditional("date")) + "|" +                 //InvoiceDate
                        tran.getAdditional("invoiceNumber") + "|" +                     //InvoiceNo
                        tran.getAdditional("permitNumber") + "|" +                      //PermitNo
                        tran.getLineNumber() + "|" +                                         //LineNo
                        tran.getDescription() + "|" +                                        //ProductDescription
                        limit2Decimals(tran.getPreTaxAmount()) + "|" +                       //PurchaseValueSGD
                        limit2Decimals(tran.getTaxAmount()) + "|" +                          //GSTValueSGD
                        tran.getTaxCode() + "|" +                                            //TaxCode
                        tran.getAdditional("currency") + "|" +                          //FCYCode
                        limit2Decimals(tran.getAdditional("preTaxAmountFCY")) + "|" +   //PurchaseFCY
                        limit2Decimals(tran.getAdditional("taxAmountFCY")) + "|\n"      //GSTFCY
                );
                purchaseCount++;
                purchaseTotal = purchaseTotal + Double.parseDouble(tran.getAdditional("total"));
                purchaseTax = purchaseTax + Double.parseDouble(tran.getTaxAmount());
            }
        }
        sb.append("PurcDataEnd|" + limit2Decimals(purchaseTotal) + "|" + limit2Decimals(purchaseTax) + "|" + purchaseCount + "|\n");

        sb.append("SuppDataStart|\n");
        sb.append("CustomerName|CustomerUEN|InvoiceDate|InvoiceNo|LineNo|ProductDescription|SupplyValueSGD|GSTValueSGD|TaxCode|Country|FCYCode|SupplyFCY|GSTFCY|\n");

        int supplyCount = 0;
        double supplyTotal = 0;
        double supplyTax = 0;
        for (Transaction tran : relevant) {
            if (tran.isSupply()) {
                sb.append(
                        tran.getAdditional("customerName") + "|" +                      //CustomerName
                        (tran.getAdditional("customerID").equals(null) ?
                                "NaN" : tran.getAdditional("customerID")) + "|" +       //CustomerUEN
                        processDate(tran.getAdditional("date")) + "|" +                 //InvoiceDate
                        tran.getAdditional("invoiceNumber") + "|" +                     //InvoiceNo
                        tran.getLineNumber() + "|" +                                         //LineNo
                        tran.getDescription() + "|" +                                        //ProductDescription
                        limit2Decimals(tran.getPreTaxAmount()) + "|" +                       //SupplyValueSGD
                        limit2Decimals(tran.getTaxAmount()) + "|" +                          //GSTValueSGD
                        tran.getTaxCode() + "|" +                                            //TaxCode
                        tran.getAdditional("country") + "|" +                           //Country
                        tran.getAdditional("currency") + "|" +                          //FCYCode
                        limit2Decimals(tran.getAdditional("preTaxAmountFCY")) + "|" +   //SupplyFCY
                        limit2Decimals(tran.getAdditional("taxAmountFCY")) + "|\n"      //GSTFCY
                );
                supplyCount++;
                supplyTotal = supplyTotal + Double.parseDouble(tran.getAdditional("total"));
                supplyTax = supplyTax + Double.parseDouble(tran.getTaxAmount());
            }
        }
        sb.append("SuppDataEnd|" + limit2Decimals(supplyTotal) + "|" + limit2Decimals(supplyTax) + "|" + supplyCount + "|");

        logger.info("\n" + sb.toString());
        return Base64.getEncoder().encodeToString(sb.toString().getBytes());
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

    private String limitInteger(double amount){
        return Math.round(amount)+"";
    }

    private String limit2Decimals(double number){
        return String.format("%.2f", number);
    }

    private String limit2Decimals(String number){
        double val = Double.parseDouble(number);
        return String.format("%.2f", val);
    }

    private String processDate(String date){
        try{
            SimpleDateFormat parseSDF = new SimpleDateFormat("MM-dd-yyyy");
            Date parsedDate = parseSDF.parse(date);
            SimpleDateFormat printSDF = new SimpleDateFormat("yyyy-MM-dd");
            return printSDF.format(parsedDate);
        }catch (Exception e){
            logger.warning("date " + date + " did not parse to MM-dd-yyyy");
            return date;
        }
    }
    private String getLastDayOfMonth(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        LocalDate lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.format(formatter);
    }

}
