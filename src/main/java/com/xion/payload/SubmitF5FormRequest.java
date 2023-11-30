package com.xion.payload;

public class SubmitF5FormRequest {

    // POST /submitF5Return_UA
    // https://apisandbox.iras.gov.sg/iras/devportal/sb/api/gst-unauthenticated-returns-submission-sandbox-0

    private FilingInfo filingInfo;
    private Supplies supplies;      // box1-3
    private Purchases purchases;    // box5
    private Taxes taxes;            // box7-8
    private Schemes schemes;        // box9-12
    private Revenue revenue;        // box13
    private IGDScheme igdScheme;
    private Declaration declaration;
    private Reasons reasons;

    public FilingInfo getFilingInfo() {
        return filingInfo;
    }

    public void setFilingInfo(FilingInfo filingInfo) {
        this.filingInfo = filingInfo;
    }

    public Supplies getSupplies() {
        return supplies;
    }

    public void setSupplies(Supplies supplies) {
        this.supplies = supplies;
    }

    public Purchases getPurchases() {
        return purchases;
    }

    public void setPurchases(Purchases purchases) {
        this.purchases = purchases;
    }

    public Taxes getTaxes() {
        return taxes;
    }

    public void setTaxes(Taxes taxes) {
        this.taxes = taxes;
    }

    public Schemes getSchemes() {
        return schemes;
    }

    public void setSchemes(Schemes schemes) {
        this.schemes = schemes;
    }

    public Revenue getRevenue() {
        return revenue;
    }

    public void setRevenue(Revenue revenue) {
        this.revenue = revenue;
    }

    public IGDScheme getIgdScheme() {
        return igdScheme;
    }

    public void setIgdScheme(IGDScheme igdScheme) {
        this.igdScheme = igdScheme;
    }

    public Declaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(Declaration declaration) {
        this.declaration = declaration;
    }

    public Reasons getReasons() {
        return reasons;
    }

    public void setReasons(Reasons reasons) {
        this.reasons = reasons;
    }

    /* sub classes */
    public class FilingInfo {

        private String taxRefNo;
        private String formType;
        private String dtPeriodStart;
        private String dtPeriodEnd;

        public String getTaxRefNo() {
            return taxRefNo;
        }

        public void setTaxRefNo(String taxRefNo) {
            this.taxRefNo = taxRefNo;
        }

        public String getFormType() {
            return formType;
        }

        public void setFormType(String formType) {
            this.formType = formType;
        }

        public String getDtPeriodStart() {
            return dtPeriodStart;
        }

        public void setDtPeriodStart(String dtPeriodStart) {
            this.dtPeriodStart = dtPeriodStart;
        }

        public String getDtPeriodEnd() {
            return dtPeriodEnd;
        }

        public void setDtPeriodEnd(String dtPeriodEnd) {
            this.dtPeriodEnd = dtPeriodEnd;
        }
    }

    public class Supplies {

        private double totStdSupply;    // box1
        private double totZeroSupply;   // box2
        private double totExemptSupply; // box3

        public double getTotStdSupply() {
            return totStdSupply;
        }

        public void setTotStdSupply(double totStdSupply) {
            this.totStdSupply = totStdSupply;
        }

        public double getTotZeroSupply() {
            return totZeroSupply;
        }

        public void setTotZeroSupply(double totZeroSupply) {
            this.totZeroSupply = totZeroSupply;
        }

        public double getTotExemptSupply() {
            return totExemptSupply;
        }

        public void setTotExemptSupply(double totExemptSupply) {
            this.totExemptSupply = totExemptSupply;
        }
    }

    public class Purchases {

        private double totTaxPurchase;  // box5

        public double getTotTaxPurchase() {
            return totTaxPurchase;
        }

        public void setTotTaxPurchase(double totTaxPurchase) {
            this.totTaxPurchase = totTaxPurchase;
        }
    }

    public class Taxes {

        private double outputTaxDue;    // box6
        private double inputTaxRefund;  // box7

        public double getOutputTaxDue() {
            return outputTaxDue;
        }

        public void setOutputTaxDue(double outputTaxDue) {
            this.outputTaxDue = outputTaxDue;
        }

        public double getInputTaxRefund() {
            return inputTaxRefund;
        }

        public void setInputTaxRefund(double inputTaxRefund) {
            this.inputTaxRefund = inputTaxRefund;
        }
    }

    public class Schemes {

        private double totValueScheme;          // box9
        private boolean touristRefundChk;       // box10
        private double touristRefundAmt;
        private boolean badDebtChk;             // box11
        private double badDebtReliefClaimAmt;
        private boolean preRegistrationChk;     // box12
        private double preRegistrationClaimAmt;

        public double getTotValueScheme() {
            return totValueScheme;
        }

        public void setTotValueScheme(double totValueScheme) {
            this.totValueScheme = totValueScheme;
        }

        public boolean getTouristRefundChk() {
            return touristRefundChk;
        }

        public void setTouristRefundChk(boolean touristRefundChk) {
            this.touristRefundChk = touristRefundChk;
        }

        public double getTouristRefundAmt() {
            return touristRefundAmt;
        }

        public void setTouristRefundAmt(double touristRefundAmt) {
            this.touristRefundAmt = touristRefundAmt;
        }

        public boolean isBadDebtChk() {
            return badDebtChk;
        }

        public void setBadDebtChk(boolean badDebtChk) {
            this.badDebtChk = badDebtChk;
        }

        public double getBadDebtReliefClaimAmt() {
            return badDebtReliefClaimAmt;
        }

        public void setBadDebtReliefClaimAmt(double badDebtReliefClaimAmt) {
            this.badDebtReliefClaimAmt = badDebtReliefClaimAmt;
        }

        public boolean isPreRegistrationChk() {
            return preRegistrationChk;
        }

        public void setPreRegistrationChk(boolean preRegistrationChk) {
            this.preRegistrationChk = preRegistrationChk;
        }

        public double getPreRegistrationClaimAmt() {
            return preRegistrationClaimAmt;
        }

        public void setPreRegistrationClaimAmt(double preRegistrationClaimAmt) {
            this.preRegistrationClaimAmt = preRegistrationClaimAmt;
        }
    }

    public class Revenue {

        private double revenue; // box13

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }
    }

    public class IGDScheme {

        private double defImpPayableAmt;
        private double defTotalGoodsImp;

        public double getDefImpPayableAmt() {
            return defImpPayableAmt;
        }

        public void setDefImpPayableAmt(double defImpPayableAmt) {
            this.defImpPayableAmt = defImpPayableAmt;
        }

        public double getDefTotalGoodsImp() {
            return defTotalGoodsImp;
        }

        public void setDefTotalGoodsImp(double defTotalGoodsImp) {
            this.defTotalGoodsImp = defTotalGoodsImp;
        }
    }

    public class Declaration {

        private String declarantDesgtn;
        private String contactPerson;
        private String contactNumber;
        private String contactEmail;

        public String getDeclarantDesgtn() {
            return declarantDesgtn;
        }

        public void setDeclarantDesgtn(String declarantDesgtn) {
            this.declarantDesgtn = declarantDesgtn;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }
    }

    public class Reasons {

        private boolean grp1BadDebtRecoveryChk;
        private boolean grp1PriorToRegChk;
        private boolean grp1OtherReasonChk;
        private String grp1OtherReasons;
        private boolean grp2TouristRefundChk;
        private boolean grp2AppvBadDebtReliefChk;
        private boolean grp2CreditNotesChk;
        private boolean grp2OtherReasonsChk;
        private String grp2OtherReasons;
        private boolean grp3CreditNotesChk;
        private boolean grp3OtherReasonsChk;
        private String grp3OtherReasons;

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
            return grp1OtherReasons;
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
            return grp2OtherReasons;
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
            return grp3OtherReasons;
        }

        public void setGrp3OtherReasons(String grp3OtherReasons) {
            this.grp3OtherReasons = grp3OtherReasons;
        }
    }
}
