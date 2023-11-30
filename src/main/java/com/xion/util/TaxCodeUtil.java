package com.xion.util;

import com.xion.models.gst.Action;

public class TaxCodeUtil {

    public static String mapTaxCode(Action action) {
        if(     action.getTaxCode().equals("SR")    ||
                action.getTaxCode().equals("SR8")    ||
                action.getTaxCode().equals("ZR")    ||
                action.getTaxCode().equals("DS")    ||
                action.getTaxCode().equals("BL")    ||
                action.getTaxCode().equals("EP")    ||
                action.getTaxCode().equals("IM")    ||
                action.getTaxCode().equals("IM8")    ||
                action.getTaxCode().equals("TX")    ||
                action.getTaxCode().equals("TX8")    ||
                action.getTaxCode().equals("NR")    ||
                action.getTaxCode().equals("OP")    ||
                action.getTaxCode().equals("ZP")    ||
                action.getTaxCode().equals("OS")    ||
                action.getTaxCode().equals("MX")    ||
                action.getTaxCode().equals("UN")    ||
                action.getTaxCode().equals("ES33")
        )return action.getTaxCode();

        if (action.isSupply()){
            if (action.getTaxCode().equals("STANDARD")) {
                return "SR";
            }
            if (action.getTaxCode().equals("ZERO")) {
                return "ZR";
            }
            if (action.getTaxCode().equals("EXEMPT_SUPPLY")) {
                return "ES33";
            }
        }else {
            if (action.getTaxCode().equals("STANDARD")) {
                return "TX";
            }
            if (action.getTaxCode().equals("ZERO")) {
                return "ZP";
            }
            if (action.getTaxCode().equals("IMPORTS")) {
                return "IM";
            }
        }
        return "";
    }

}
