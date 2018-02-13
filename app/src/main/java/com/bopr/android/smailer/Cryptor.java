package com.bopr.android.smailer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Base64;
import com.bopr.android.smailer.util.Util;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;

/**
 * RSA encryption operations.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Cryptor {

    private static final String KEY_ALIAS = "smailer";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private final Context context;

    public Cryptor(Context context) {
        this.context = context;
    }

    public String encrypt(String s) {
        if (Util.isEmpty(s)) {
            return s;
        }

        try {
            @SuppressLint("GetInstance")
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
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKeys().getPrivate());
            byte[] bytes = cipher.doFinal(Base64.decode(s, Base64.NO_WRAP));
            return new String(bytes);
        } catch (Exception x) {
            throw new RuntimeException("Decryption failed", x);
        }
    }

    private KeyPair getKeys() throws Exception {
        KeyStore keyStore = initKeystore(context);
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    public static boolean isKeystoreInitialized() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.containsAlias(KEY_ALIAS);
        } catch (Exception x) {
            throw new Error("Unable init keystore", x);
        }
    }

    @SuppressLint({"InlinedApi", "TrulyRandom"})
    public static KeyStore initKeystore(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                AlgorithmParameterSpec spec;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    spec = getAlgorithmParameterSpec();
                } else {
                    spec = getAlgorithmParameterSpecLegacy(context);
                }

                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);
                generator.generateKeyPair();
            }
            return keyStore;
        } catch (Exception x) {
            throw new Error("Unable init keystore", x);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @NonNull
    private static AlgorithmParameterSpec getAlgorithmParameterSpec() {
        AlgorithmParameterSpec spec;
        spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();
        return spec;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private static AlgorithmParameterSpec getAlgorithmParameterSpecLegacy(Context context) {
        AlgorithmParameterSpec spec;
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 100);
        /* do not put KeyPairGeneratorSpec in imports to avoid deprecation warning */
        spec = new android.security.KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(new X500Principal("CN=" + KEY_ALIAS + ", O=Android Authority"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        return spec;
    }

}
