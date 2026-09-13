package com.reservation;

import java.security.SecureRandom;

public class PNRGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        long value = 10000000L + RANDOM.nextLong(90000000L);
        return "PNR" + value;
    }

    private PNRGenerator() {}
}
