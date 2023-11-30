package com.xion.services;

import com.xion.payload.SendSimpleTemplateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class XionMailService {

    private static Logger logger = Logger.getLogger(XionMailService.class.getName());

    @Value("#{ @environment['client.xionMailBaseURL'] }")
    private String serverBaseUrl;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    private RestTemplate restTemplate = new RestTemplate();

    public boolean sendValidationMsg(String recipient, String payload){
        String id = "import-validation-failure-template";

        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("PAYLOAD", payload);

        Map<String, List<String>> toMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add(recipient);
        toMap.put("to", values);

        SendSimpleTemplateRequest sstr = new SendSimpleTemplateRequest();
        sstr.setId(id);
        sstr.setReplacementMap(replacementMap);
        sstr.setSubject("Validation results for Counto-GST pull");
        sstr.setTo(toMap);

        return sendSendSimpleTemplateRequest(sstr);
    }

    private boolean sendSendSimpleTemplateRequest(SendSimpleTemplateRequest sstr){
        HttpEntity<SendSimpleTemplateRequest> request = new HttpEntity<>(sstr);
        ResponseEntity<Void> response = null;
        try {
            response = restTemplate.exchange(
                    serverBaseUrl + "/send/template/simple",
                    HttpMethod.POST,
                    request,
                    void.class
            );
        } catch (Exception e) {
            logger.severe("Error sending mail for " + request);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getEnv(){
        return activeProfile.equals("prod") ?
                "" :
                "-" + activeProfile;
    }

}
