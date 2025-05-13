package com.mycompany.app.communication;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * For each message repeat this process:
 * <p>
 * Setup:
 * <p>
 * Generate a secret key (iv) using Encryption.generateKey()
 * This key must be shared securely between sender and receiver before message communication.
 * Sender Flow:
 * <p>
 * Create an IV using Encryption.generateIV().
 * Encrypt the message using AES-CTR with the key + IV. → using Encryption.encrypt()
 * Add the IV to the beginning of the encrypted message.
 * Send via UDP.
 * Receiver Flow:
 * <p>
 * Receive UDP packet.
 * Split the first 16 bytes as IV. → The rest is the encrypted message.
 * Decrypt using the same shared key and extracted IV. → using Encryption.decrypt()
 */
public class Encryption {

    /**
     * Generates a new AES secret key (128-bit).
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // or 256 if supported
        return keyGen.generateKey();
    }

    /**
     * Generates a 16-byte IV using a secure random source.
     */
    public static byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Encrypts the given data using AES-CTR with the provided key and IV.
     *
     * @param data the plaintext to encrypt
     * @param key the AES secret key
     * @param iv the initialization vector (16 bytes)
     * @return encrypted data (ciphertext)
     */
    public static byte[] encrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    /**
     * Decrypts AES-CTR encrypted data using the provided key and IV.
     *
     * @param encryptedData the ciphertext to decrypt
     * @param key the AES secret key
     * @param iv the initialization vector used during encryption
     * @return decrypted plaintext
     */
    public static byte[] decrypt(byte[] encryptedData, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedData);
    }
}
