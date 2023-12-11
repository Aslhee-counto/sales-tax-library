package com.xion.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.logging.Logger;

@RestController
public class CallbackController {

    private static final Logger logger = Logger.getLogger(CallbackController.class.getName());

    @GetMapping("/redirect/corppass")
    public ResponseEntity<Void> stateChange(@RequestParam(value = "code") String code,
                                            @RequestParam(value = "state") String state) {
        // Log the received code and state
        logger.info("Received code: " + code + " and state: " + state);

        // Here you can add the logic to handle the code and state, for example:
        // - Exchange the code for an access token
        // - Use the state parameter to retrieve the associated user session or data

        // Since this is just an example, we will return a simple 200 OK response
        return ResponseEntity.ok().build();
    }
}

