package com.cuhlippa.client.sync;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private EncryptionService(){}

    public static String generateKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
            keygen.init(256);
            SecretKey secretKey = keygen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new SyncEncyptionException("Failed to generate encryption key", e);
        }
    }

    public static String encrypt(String data, String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new SyncEncyptionException("Failed to encrypt data", e);
        }
    }

    public static String decrypt(String encryptedData, String key) {
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SyncEncyptionException("Failed to decrypt data", e);
        }
    }
}
