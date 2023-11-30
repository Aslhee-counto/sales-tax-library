package com.xion.controllers;


import com.xion.fx.FXService;
import com.xion.payload.GetFXRateRequest;
import com.xion.payload.GetFxRateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping("/fx")
public class FXController {

    private static Logger logger = Logger.getLogger(FXController.class.getName());

    @Autowired
    private FXService fxService;

    @PostMapping("/getFXRateRequest")
//    @PreAuthorize("#oauth2.hasScope('billing')")
    public ResponseEntity<?> saveForm(@RequestBody GetFXRateRequest request) throws Exception {
        try {
            logger.info("calling getFXRateRequest: " + request);
            double rate = fxService.getRate(request.getDate(), request.getFrom(), request.getTo()); //calculate rate >> retrieve first the params
            GetFxRateResponse response = new GetFxRateResponse(); //Response
            response.setRate(rate);
            return ResponseEntity.ok(response); //return response
        }catch (Exception e){
            logger.severe("Error in getFXRateRequest");
            logger.severe(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}
