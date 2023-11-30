package com.xion.services;

import com.xion.components.ActionService;
import com.xion.components.ResultSummeryService;
import com.xion.fx.FXService;
import com.xion.models.gst.Action;
import com.xion.resultObjectModel.resultSummeries.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ActivationService {

    private static Logger logger = Logger.getLogger(ActivationService.class.getName());

    @Autowired
    private ActionService actionService;
    @Autowired
    private ResultSummeryService summaryService;
    @Autowired
    private FXService fxService;


    public void retroactiveFxCalculations(String companyId){
        logger.info("retroactiveFxCalculations called for " + companyId);
        try {
            List<ResultSummery> summaries = summaryService.loadAll(companyId);
            logger.info("loaded " + summaries.size() + " summaries");
            List<Long> ids = summaries.stream().map( s -> s.getId()).collect(Collectors.toList());
            List<Action> actions = actionService.loadActionsBySummaryIDs(ids);
            logger.info("loaded " + actions.size() + " actions");

            for (Action action : actions){
                ResultSummery summary = summaries.stream().filter( s -> s.getId().equals(action.getSummaryID())).findFirst().get();
                if (summary instanceof InvoiceResultSummery) {
                    InvoiceResultSummery casted = (InvoiceResultSummery) summary;
                    action.setFxRate(fxService.getRate(action.getDate(), casted.getCurrency(), action.getFunctionalCurrency()));
                    action.setSgdRate(fxService.getRate(action.getDate(), casted.getCurrency(), "SGD"));
                }else if (summary instanceof ReceiptResultSummary) {
                    ReceiptResultSummary casted = (ReceiptResultSummary) summary;
                    action.setFxRate(fxService.getRate(action.getDate(), casted.getCurrency(), action.getFunctionalCurrency()));
                    action.setSgdRate(fxService.getRate(action.getDate(), casted.getCurrency(), "SGD"));
                }else if (summary instanceof DebitNoteResultSummary) {
                    DebitNoteResultSummary casted = (DebitNoteResultSummary) summary;
                    action.setFxRate(fxService.getRate(action.getDate(), casted.getCurrency(), action.getFunctionalCurrency()));
                    action.setSgdRate(fxService.getRate(action.getDate(), casted.getCurrency(), "SGD"));
                }else if (summary instanceof CreditNoteResultSummary) {
                    CreditNoteResultSummary casted = (CreditNoteResultSummary) summary;
                    action.setFxRate(fxService.getRate(action.getDate(), casted.getCurrency(), action.getFunctionalCurrency()));
                    action.setSgdRate(fxService.getRate(action.getDate(), casted.getCurrency(), "SGD"));
                }else if (summary instanceof ImportPermitResultSummary) {
                    ImportPermitResultSummary casted = (ImportPermitResultSummary) summary;
                    action.setFxRate(fxService.getRate(action.getDate(), casted.getCurrency(), action.getFunctionalCurrency()));
                    action.setSgdRate(fxService.getRate(action.getDate(), casted.getCurrency(), "SGD"));
                }
                logger.info("FXRate: " + action.getFxRate());
                logger.info("SGDRate: " + action.getSgdRate());
            }

            if(!actionService.updateActions(actions))
                throw new Exception("Could not store actions successfully");

        }catch (Exception e){
            logger.severe("error in retroactiveFxCalculations for " + companyId);
            logger.severe(e.getMessage());
            e.printStackTrace();
        }
    }

}
