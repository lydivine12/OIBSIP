package com.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validation {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNumeric(String value) {
        if (isEmpty(value)) return false;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String value) {
        if (isEmpty(value)) return false;
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return !date.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private Validation() {}
}
