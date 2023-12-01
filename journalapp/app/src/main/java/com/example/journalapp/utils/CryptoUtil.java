package com.example.journalapp.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility to encrypt and decrypt strings using AES Algorithm
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    private static final String KEY = "YourSecretKeyHere";
    private static final byte[] IV = new byte[16];

    /**
     * Encrypts plain text string using AES algorithm
     * @param value the plain text string to be encrypted
     * @return String the encrypted string in Base64 encoding
     */
    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(), new IvParameterSpec(IV));
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts an encryped string using AES algorithm
     * @param encrypted the encrypted string in Base64 encoding to be decrypted
     * @return String the decrypted plain text string
     */
    public static String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(), new IvParameterSpec(IV));
            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a SecretKeySpec based on the predefined key string
     * @return a SecretKeySpec for use with AES Algorithm
     */
    private static Key generateKey() {
        byte[] keyBytes = new byte[16];
        byte[] keyStringBytes = KEY.getBytes(StandardCharsets.UTF_8);

        System.arraycopy(keyStringBytes, 0, keyBytes, 0, Math.min(keyStringBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
