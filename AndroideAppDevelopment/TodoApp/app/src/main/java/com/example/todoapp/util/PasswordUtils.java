package com.example.todoapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Basic password hashing helper.
 *
 * Passwords are never stored in plain text. Each user gets a random salt
 * (generated once at registration) which is combined with their password and
 * run through SHA-256 before being written to SQLite. Verifying a login
 * re-hashes the entered password with the stored salt and compares the
 * resulting hashes - the plain-text password itself is never persisted or
 * compared directly.
 */
public final class PasswordUtils {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordUtils() {
        // no instances
    }

    /** Generates a new random salt, hex-encoded for easy storage in a TEXT column. */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /** Hashes {@code password} combined with {@code saltHex}, returning a hex-encoded digest. */
    public static String hashPassword(String password, String saltHex) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(hexToBytes(saltHex));
            byte[] hashed = digest.digest(password.getBytes("UTF-8"));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // SHA-256 and UTF-8 are always available on Android; this should never happen.
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    /** Returns true if {@code enteredPassword} matches the stored hash/salt pair. */
    public static boolean verifyPassword(String enteredPassword, String storedHash, String storedSalt) {
        String computedHash = hashPassword(enteredPassword, storedSalt);
        return constantTimeEquals(computedHash, storedHash);
    }

    /** Avoids leaking timing information about how many leading characters matched. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
