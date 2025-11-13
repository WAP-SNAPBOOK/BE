package com.example.easybooking.form;

public class FormParsingUtil {
    public static Integer parseSafeInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
