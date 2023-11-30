package com.xion.exceptions;

public class GstException extends Exception {

    public GstException(Exception e){
        super(e);
    }

    public GstException(String msg){
        super(msg);
    }

}