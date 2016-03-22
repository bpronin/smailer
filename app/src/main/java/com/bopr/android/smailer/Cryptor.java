package com.bopr.android.smailer;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;

import com.bopr.android.smailer.util.Util;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import static android.security.keystore.KeyProperties.DIGEST_SHA256;
import static android.security.keystore.KeyProperties.DIGEST_SHA512;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;

/**
 * RSA encryption operations.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Cryptor {

    private static final String KEY_ALIAS = "smailer";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    //    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private final Context context;

    public Cryptor(Context context) {
        this.context = context;
    }

    public String encrypt(String s) {
        if (Util.isEmpty(s)) {
            return s;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKeys().getPublic());
            byte[] bytes = cipher.doFinal(s.getBytes());
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception x) {
            throw new RuntimeException("Encryption failed", x);
        }
    }

    public String decrypt(String s) {
        if (Util.isEmpty(s)) {
            return s;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKeys().getPrivate());
            byte[] bytes = cipher.doFinal(Base64.decode(s, Base64.NO_WRAP));
            return new String(bytes);
        } catch (Exception x) {
            throw new RuntimeException("Decryption failed", x);
        }
    }

    private KeyPair getKeys() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        if (!ks.containsAlias(KEY_ALIAS)) {
            generateKeys();
        }
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(KEY_ALIAS, null);
        return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    @SuppressWarnings("deprecation")
    private void generateKeys() throws Exception {
        AlgorithmParameterSpec spec;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                    .setDigests(DIGEST_SHA256, DIGEST_SHA512)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_PKCS1)
                    .build();

        } else {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);
            spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS + ", O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        generator.initialize(spec);
        generator.generateKeyPair();
    }

}
