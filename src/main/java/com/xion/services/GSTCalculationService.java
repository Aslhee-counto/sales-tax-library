package com.xion.services;


import com.xion.components.ActionService;
import com.xion.components.ResultSummeryService;
import com.xion.data.F5Form;
import com.xion.models.gst.Action;
import com.xion.models.gst.ActivationRecord;
import com.xion.models.gst.GstStatus;
import com.xion.models.gst.Listing;
import com.xion.resultObjectModel.resultSummeries.DocumentType;
import com.xion.util.DateCalcUtil;
import com.xion.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class GSTCalculationService {

    private static Logger logger = Logger.getLogger(GSTCalculationService.class.getName());

    @Autowired private StoreService storeService;
    @Autowired private ResultSummeryService resultSummeryService;
    @Autowired private ActionService actionService;
    @Autowired private AccountingService accountingService;

    public Pair<Listing, Listing> getMonthlyListingOnActivate(String finYearEnd, String legalName, String currency, String taxNum, String gstNum) throws Exception{
        try{
            logger.info("==getMonthlyListingOnActivate");
            Date finYearEndInCurrentMonth = monthlyCalculate(finYearEnd);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(finYearEndInCurrentMonth);

            Listing current = new Listing();
            Listing due = new Listing();
            current.setId(generateUniqueId());
            due.setId(generateUniqueId());

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DATE));
            Date currentStartDate = calendar.getTime();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
            Date currentEndDate = calendar.getTime();

            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
            Date currentDueDate = calendar.getTime();

            calendar.add(Calendar.MONTH, -2);

            Calendar dueStartCal = GregorianCalendar.getInstance();
            dueStartCal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.getActualMinimum(Calendar.DATE));
            Date dueStartDate = dueStartCal.getTime();

            Calendar dueEndCal = GregorianCalendar.getInstance();
            dueEndCal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 28);
            dueEndCal.set(Calendar.DAY_OF_MONTH, dueEndCal.getActualMaximum(Calendar.DATE));
            Date dueEndDate = dueEndCal.getTime();

            current.setStatus(GstStatus.CurrentPeriod);
            current.setPeriodStart(currentStartDate);
            current.setPeriodEnd(currentEndDate);
            current.setCurrency(currency);
            current.setDueDate(currentDueDate);
            current.setLegalName(legalName);
            current.setNetGST(calculateF5(legalName,currency, currentStartDate, currentEndDate, current.getId(), taxNum, gstNum).getBox8());

            due.setStatus(GstStatus.Due);
            due.setPeriodStart(dueStartDate);
            due.setPeriodEnd(dueEndDate);
            due.setCurrency(currency);
            due.setDueDate(currentEndDate);
            due.setLegalName(legalName);
            due.setNetGST(calculateF5(legalName,currency, dueStartDate, dueEndDate, due.getId(), taxNum, gstNum).getBox8());

//            return new Pair<>(due, current);
            return new Pair<>(due, current);

        }catch (Exception e){
            logger.severe("error computing listing pair for monthly");
            logger.severe(e.getMessage());
            throw e;
        }

    }

    public Pair<Listing, Listing> getQuarterlyListingOnActivate(String finYearEnd, String legalName, String currency, String taxNum, String gstNum) throws Exception{
        try{

            Date finYearEndInCurrentQuarter = quarterlyCalculate(finYearEnd);
            logger.info("---> QDC finYearEnd ---> " + finYearEnd);
            logger.info("---> QDC finYearEndInCurrentQuarter ---> " + finYearEndInCurrentQuarter.toString());
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(finYearEndInCurrentQuarter);

            Listing current = new Listing();
            Listing due = new Listing();
            current.setId(generateUniqueId());
            due.setId(generateUniqueId());

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date currentEndDate = calendar.getTime();
            calendar.add(Calendar.MONTH,-2);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date currentStartDate = calendar.getTime();
            calendar.add(Calendar.MONTH,-3);

            Calendar dueStartCal = GregorianCalendar.getInstance();
            dueStartCal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
            Date dueStartDate = dueStartCal.getTime();

            Calendar dueEndCal = GregorianCalendar.getInstance();
            dueEndCal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+2, 28);
            dueEndCal.set(Calendar.DATE, dueEndCal.getActualMaximum(Calendar.DATE));
            Date dueEndDate = dueEndCal.getTime();

            calendar.add(Calendar.MONTH, 6);
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
            Date currentDueDate = calendar.getTime();


            calendar.add(Calendar.MONTH, -3);
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
            Date dueDueDate = calendar.getTime();


            current.setStatus(GstStatus.CurrentPeriod);
            current.setPeriodStart(currentStartDate);
            current.setPeriodEnd(currentEndDate);
            current.setCurrency(currency);
            current.setDueDate(currentDueDate);
            current.setLegalName(legalName);
            current.setNetGST(calculateF5(legalName,currency, currentStartDate, currentEndDate, current.getId(), taxNum, gstNum).getBox8());

            due.setStatus(GstStatus.Due);
            due.setPeriodStart(dueStartDate);
            due.setPeriodEnd(dueEndDate);
            due.setCurrency(currency);
            due.setDueDate(dueDueDate);
            due.setLegalName(legalName);
            due.setNetGST(calculateF5(legalName,currency, dueStartDate, dueEndDate, due.getId(), taxNum, gstNum).getBox8());

            if (due.getDueDate().before(new Date())){
                due.setStatus(GstStatus.OverDue);
            }

            logger.info("---> QDC DUE: ---> start: " + due.getPeriodStart().toString() + " end: " + due.getPeriodEnd().toString() + " due: " + due.getDueDate().toString());
            logger.info("---> QDC CURRENT: ---> start: " + current.getPeriodStart().toString() + " end: " + current.getPeriodEnd().toString() + " due: " + current.getDueDate().toString());
            return new Pair<>(due, current);

        }catch (Exception e){
            logger.severe("error computing listing pair for quarterly");
            logger.severe(e.getMessage());
            throw e;
        }

    }

    /**
     * @return netGST
     */
    public F5Form calculateF5(String legalName, String targetCurrency, Date start, Date end, long uuid, String taxNum, String gstNum) throws Exception {
        logger.info("calculating f5 for " + legalName + " between " + start.toString() + " and " + end.toString());
        try {

            F5Form form = new F5Form();

            List<Action> actions;
            ActivationRecord activationRecord = storeService.loadActivationRecord(legalName);
            if (accountingService.isOnboarded(legalName)) {
                logger.info("Pulling action data from accounting service");
                actions = accountingService.pullCollapsedDataFromAccounting(legalName, start, end, activationRecord.getEmail());
            }else {
                logger.info("Pulling action data from vault");
                actions = actionService.loadActionsInRange(start, end, legalName);
            }
            List<Action> supplyActions = actions.stream().filter( action -> action.isSupply()).collect(Collectors.toList());
            List<Action> purchaseActions = actions.stream().filter( action -> !action.isSupply()).collect(Collectors.toList());

            form.setBox1(new BigDecimal(
                    boxPreTaxCalculation(supplyActions, List.of("SR", "SR8"), 1)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            form.setBox2(new BigDecimal(
                    boxPreTaxCalculation(supplyActions, List.of("ZR"), 2)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            form.setBox3(new BigDecimal(
                    boxPreTaxCalculation(supplyActions, List.of("ES33"), 3)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            logger.info("calculating box 4");
            form.setBox4(new BigDecimal(
                    form.getBox1() + form.getBox2() + form.getBox3()
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            logger.info("calculated box 4 as " + form.getBox4());
            form.setBox5(new BigDecimal(
                    box5Calculation(purchaseActions)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            form.setBox6(new BigDecimal(
                    box6Calculation(supplyActions)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            form.setBox7(new BigDecimal(
                    box7Calculation(purchaseActions)
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            logger.info("calculating box 8");
            form.setBox8(new BigDecimal(
                    form.getBox6()-form.getBox7()
            ).setScale(2, RoundingMode.HALF_UP).doubleValue());
            logger.info("calculated box 8 as " + form.getBox8());

            form.setLegalName(legalName);
            form.setTaxRefNo(taxNum);
            form.setGstRefNo(gstNum);
            form.setStartDate(start);
            form.setEndDate(end);
            form.setDeclarantDesgtn(activationRecord.getDesignation());
            form.setContactPerson(activationRecord.getName());
            form.setContactNumber(activationRecord.getPhone());
            form.setContactEmail(activationRecord.getEmail());

            logger.info("==form END DATES: " + form.getEndDate());
            logger.info("==form START DATES: " + form.getStartDate());

            storeService.storeF5Form(legalName, uuid+"", form);
            return form;

        }catch (NullPointerException e) {
            logger.severe("NPE in calculate f5: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("NPE in calculateF5");
        }catch (Exception e){
            logger.severe("Error in calculateF5");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private double boxPreTaxCalculation(List<Action> actions, List<String> taxCode, int boxNum) throws Exception {
        logger.info("calculating box " + boxNum + " with " + actions.size() + " actions");

        try {

            double calcVal =  actions.stream()
                    .filter( action ->
                            taxCode.contains(action.getTaxCode())
                    ).map( action ->{


                        if (action.getType().equals(DocumentType.CREDIT_NOTE)){
                            if (action.getSgdRate()!=1) {
                                logger.info("=action get SGD =" + action.getSgdRate());
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                            }else
                                logger.info("=action =" + action.getSgdRate());

                            return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                        }else {
                            if (action.getSgdRate() != 1) {
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                            } else
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                        }
                    }).reduce(
                            0.0, Double::sum
                    );

            logger.info("calculated box " + boxNum + " as " + calcVal );
            return calcVal; //10283.45

        }catch (NullPointerException e) {
            logger.severe("NPE in calculate box1: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("NPE in calculate box1");
        }catch (Exception e){
            logger.severe("Error in calculate box1");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private double box5Calculation(List<Action> actions) throws Exception {
        logger.info("calculating box 5 with " + actions.size() + " actions");

        try {

            double calcVal =  actions.stream()
                    .filter( action ->
                            action.getTaxCode().equals("TX") || action.getTaxCode().equals("TX8") || action.getTaxCode().equals("ZP") || action.getTaxCode().equals("IM") || action.getTaxCode().equals("IM8")
                    ).map( action ->{
                        if (action.getType().equals(DocumentType.CREDIT_NOTE)){
                            if (action.getSgdRate()!=1) {
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                            }else
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                        }else {
                            if (action.getSgdRate() != 1) {
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                            } else
                                return Double.valueOf(action.getPreTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                        }
                    }).reduce(
                            0.0, Double::sum
                    );

            logger.info("calculated box 5 as " + calcVal);
            return calcVal;

        }catch (NullPointerException e) {
            logger.severe("NPE in calculate box5: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("NPE in calculate box5");
        }catch (Exception e){
            logger.severe("Error in calculate box5");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private double box6Calculation(List<Action> actions) throws Exception {
        logger.info("calculating box 6 with " + actions.size() + " actions");

        try {

            double calcVal = actions.stream()
                    .filter( action ->
                            action.getTaxCode().equals("SR") || action.getTaxCode().equals("SR8")
                    ).map( action ->{

                        if (action.getType().equals(DocumentType.CREDIT_NOTE)){
                            if (action.getSgdRate()!=1) {
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                            }else
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                        }else {
                            if (action.getSgdRate() != 1) {
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                            } else
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                        }
                    }).reduce(
                            0.0, Double::sum
                    );

            logger.info("calculated box 6 as " + calcVal);
            return calcVal;

        }catch (NullPointerException e) {
            logger.severe("NPE in calculate box6: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("NPE in calculate box6");
        }catch (Exception e){
            logger.severe("Error in calculate box6");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private double box7Calculation(List<Action> actions) throws Exception {
        logger.info("calculating box 7 with " + actions.size() + " actions");

        try {

            double calcVal =  actions.stream()
                    .filter( action ->
                            action.getTaxCode().equals("TX") || action.getTaxCode().equals("IM") || action.getTaxCode().equals("TX8") || action.getTaxCode().equals("IM8")
                    ).map( action ->{
                        if (action.getType().equals(DocumentType.CREDIT_NOTE)){
                            if (action.getSgdRate()!=1) {
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                            }else
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", ""))*(-action.getSgdRate());
                        }else {
                            if (action.getSgdRate() != 1) {
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                            } else
                                return Double.valueOf(action.getTax().replaceAll("[^-^\\d.]", "")) * (action.getSgdRate());
                        }
                    }).reduce(
                            0.0, Double::sum
                    );

            logger.info("calculated box 7 as " + calcVal);
            return calcVal;

        }catch (NullPointerException e) {
            logger.severe("NPE in calculate box7: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("NPE in calculate box7");
        }catch (Exception e){
            logger.severe("Error in calculate box7");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private Date monthlyCalculate(String finYearEnd) throws Exception{
        try{
            String[] split = finYearEnd.split("-");
            if (split.length!=2) {
                String msg = "FinancialYearEnd incorrectly formatted " + finYearEnd;
                logger.severe(msg);
                throw new Exception("FinancialYearEnd incorrectly formatted " + finYearEnd);
            }

            int day = Integer.parseInt(split[1]);

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day-1);

            return calendar.getTime();
        }catch (Exception e){
            logger.severe("error computing monthly");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    private Date quarterlyCalculate(String finYearEnd) throws Exception{
        try{
            String[] split = finYearEnd.split("-");
            if (split.length!=2) {
                String msg = "FinancialYearEnd incorrectly formatted " + finYearEnd;
                logger.severe(msg);
                throw new Exception("FinancialYearEnd incorrectly formatted " + finYearEnd);
            }

            int month = Integer.parseInt(split[0])-1;
            int day = Integer.parseInt(split[1]);

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(new Date());
            int rpMonth = calendar.get(Calendar.MONTH);

            int distance = DateCalcUtil.getDistanceToQuarterEnd(month, rpMonth);

            int monthTotal = calendar.get(Calendar.MONTH) + distance;
            if(monthTotal>11)
                calendar.set(calendar.get(Calendar.YEAR)+1, monthTotal-12, 1);
            else
                calendar.set(calendar.get(Calendar.YEAR), monthTotal, 1);

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            return calendar.getTime();

        }catch (Exception e){
            logger.severe("error computing monthly");
            logger.severe(e.getMessage());
            throw e;
        }
    }

    public long generateUniqueId() {
        long val = -1;
        do {
            val = UUID.randomUUID().getMostSignificantBits();
        } while (val < 0);
        return val;
    }
}
