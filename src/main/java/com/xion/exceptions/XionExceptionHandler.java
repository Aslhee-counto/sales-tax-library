package com.xion.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class XionExceptionHandler {
    @ExceptionHandler({HttpClientErrorException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Error handleHttpClientErrorException(HttpServletRequest req, HttpClientErrorException ex) throws IOException {
        return new ObjectMapper().readValue(ex.getResponseBodyAsString(), Error.class);
    }

    @ExceptionHandler({XionException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Error handleXionException(HttpServletRequest req, XionException ex) {
        return new Error(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), parseError(ex.getMessage()));
    }

    @ExceptionHandler({GstException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Error handleGstException(HttpServletRequest req, GstException ex) {
        return new Error(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), parseError(ex.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Error handleException(HttpServletRequest req, Exception ex) {
        return new Error(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), parseError(ex.getMessage()));
    }

    @ExceptionHandler({RuntimeException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Error handleRuntimeException(HttpServletRequest req, RuntimeException ex) {
        return new Error(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), parseError(ex.getMessage()));
    }

    private List<String> parseError(String message) {
        if (Objects.nonNull(message) && !StringUtils.isEmpty(message)) {
            List<String> parsedErrors = new LinkedList<>();
            parsedErrors.add(message);
            return parsedErrors;
        }
        return null;
    }
}