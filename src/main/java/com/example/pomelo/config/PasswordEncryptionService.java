package com.example.pomelo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

@Configuration
public class PasswordEncryptionService {

    private static String password;
    private static String salt;

    @Value("${encryption.secret_key}")
    public void setSecretKey(String password) {
        PasswordEncryptionService.password = password;
    }

    @Value("${encryption.salt}")
    public void setSalt(String salt) {
        PasswordEncryptionService.salt = salt;
    }

    public static String encrypt(String plaintext) throws Exception {
        SecretKey secretKey = generateSecretKey(password);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static SecretKey generateSecretKey(String password) throws Exception {

        int iterationCount = 65536;
        int keyLength = 256;


        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterationCount, keyLength);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = secretKeyFactory.generateSecret(keySpec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }
}
