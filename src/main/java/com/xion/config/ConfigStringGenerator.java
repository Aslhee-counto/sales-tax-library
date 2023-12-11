package com.xion.config;

import java.util.Base64;
import java.util.UUID;

public class ConfigStringGenerator {

    public static String generateCodedString(String activeProfile, String companyId) {
        String rawString = "activeProfile=" + activeProfile + ",companyId=" + companyId + ",uniqueCode=" + UUID.randomUUID().toString();
        String encodedString = Base64.getEncoder().encodeToString(rawString.getBytes());
        return encodedString;
    }

}
/*
 TODO >> FOR DECODING
 import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ConfigStringDecoder {

    public static Map<String, String> decodeCodedString(String encodedString) {
        // Decode the string
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);

        // Parse the decoded string
        Map<String, String> values = new HashMap<>();
        String[] parts = decodedString.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                values.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return values;
    }


 */