package com.xion.services;


import com.xion.payload.accounting.DocumentDTO;
import com.xion.payload.accounting.DocumentRequest;
import com.xion.validations.ValidationException;
import com.xion.validations.Validator;
import com.xion.validations.validators.RequiredAdditionalFieldsPresent;
import com.xion.validations.validators.RequiredFieldsPresent;
import com.xion.validations.validators.TotalSumMatching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ValidationService {

    private static Logger logger = Logger.getLogger(ValidationService.class.getName());

    private XionMailService xionMailService;
    private List<Validator> validators;

    public ValidationService(@Autowired XionMailService xionMailService){
        this.xionMailService = xionMailService;
        validators = new ArrayList<>();
        validators.add(new RequiredFieldsPresent());
        validators.add(new RequiredAdditionalFieldsPresent());
        validators.add(new TotalSumMatching());
//        validators.add(new LineItemTotalSumMatching());
    }

    public boolean validateAndContinue(DocumentDTO documentDTO, String userEmail){
        boolean progress = true;
        boolean mail = false;
        StringBuilder validationResults = new StringBuilder();
        if(documentDTO.getDocumentRequest()!=null) {
            for (DocumentRequest documentRequest : documentDTO.getDocumentRequest()) {
                for (Validator validator : validators) {
                    if (validator.applicable(documentRequest)) {
                        try {
                            validator.validate(documentRequest);
                        } catch (ValidationException e) {
                            mail = true;
                            logger.warning(e.getMessage());
                            validationResults.append("<tr> <td align=\"left\" valign=\"top\"> <p class=\"tab\" style=\"font-style:italic; padding-left:20px; font-weight:400;\"> ");
                            validationResults.append(e.getMessage())
                                    .append("</p> </td> </tr>");
                        } catch (Exception e) {
                            mail = true;
                            progress = false;
                            logger.severe(e.getMessage());
                            validationResults.append("<tr> <td align=\"left\" valign=\"top\"> <p class=\"tab\" style=\"font-style:italic; padding-left:20px; font-weight:400;\"> ");
                            validationResults.append(e.getMessage())
                                    .append("</p> </td> </tr>");
                        }
                    }
                }
            }
        }else{
            mail = true;
            String msg = "No Invoices pulled";
            logger.warning(msg);
            validationResults.append("<tr> <td align=\"left\" valign=\"top\"> <p class=\"tab\" style=\"font-style:italic; padding-left:20px; font-weight:400;\"> ");
            validationResults.append(msg)
                    .append("</p> </td> </tr>");
        }
        if (mail){
            this.xionMailService.sendValidationMsg(userEmail, validationResults.toString());
        }
        return progress;
    }

}
