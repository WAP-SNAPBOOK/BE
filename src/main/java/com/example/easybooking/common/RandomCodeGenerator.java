package com.example.easybooking.common;

import java.security.SecureRandom;

public class RandomCodeGenerator {
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RAND = new SecureRandom();

    public static String base62(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) buf[i] = ALPHABET[RAND.nextInt(ALPHABET.length)];
        return new String(buf);
    }

}
