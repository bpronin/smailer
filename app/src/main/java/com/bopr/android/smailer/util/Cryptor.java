package com.bopr.android.smailer.util;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

/**
 * RSA encryption operations.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Cryptor {

    private static final String KEY_ALIAS = "smailer";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private Cryptor() {
    }

    public static String encrypt(String s, Context context) {
        if (StringUtil.isEmpty(s)) {
            return s;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKeys(context).getPublic());
            byte[] bytes = cipher.doFinal(s.getBytes());
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception x) {
            throw new RuntimeException("Encryption failed", x);
        }
    }

    public static String decrypt(String s, Context context) {
        if (StringUtil.isEmpty(s)) {
            return s;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKeys(context).getPrivate());
            byte[] bytes = cipher.doFinal(Base64.decode(s, Base64.NO_WRAP));
            return new String(bytes);
        } catch (Exception x) {
            throw new RuntimeException("Decryption failed", x);
        }
    }

    private static KeyPair getKeys(Context context) throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        if (!ks.containsAlias(KEY_ALIAS)) {
            generateKeys(context);
        }
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(KEY_ALIAS, null);
        return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    @SuppressWarnings("deprecation")
    private static KeyPair generateKeys(Context context) throws Exception {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 100);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(new X500Principal("CN=" + KEY_ALIAS + ", O=Android Authority"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        generator.initialize(spec);

        return generator.generateKeyPair();
    }

/*
    @TargetApi(Build.VERSION_CODES.M)
    private static KeyPair generateKeyApi() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .build();
        generator.initialize(spec);
        return generator.generateKeyPair();
    }
*/

}
