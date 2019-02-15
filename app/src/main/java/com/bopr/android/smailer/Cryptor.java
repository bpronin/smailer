package com.bopr.android.smailer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.bopr.android.smailer.util.Util;

import java.security.KeyStore;

import javax.crypto.Cipher;

/**
 * RSA encryption operations.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Cryptor {

    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private final KeyStore keyStore;

    public Cryptor(Context context) {
        try {
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(context.getAssets().open("keystore.bks"), "return".toCharArray());
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    public String encrypt(String s) {
        if (Util.isEmpty(s)) {
            return s;
        }

        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keys().getCertificate().getPublicKey());
            byte[] bytes = cipher.doFinal(s.getBytes());
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception x) {
            throw new RuntimeException("Encryption failed", x);
        }
    }

    public String decrypt(@Nullable String s) {
        if (Util.isEmpty(s)) {
            return s;
        }

        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keys().getPrivateKey());
            byte[] bytes = cipher.doFinal(Base64.decode(s, Base64.NO_WRAP));
            return new String(bytes);
        } catch (Exception x) {
            throw new RuntimeException("Decryption failed", x);
        }
    }

    private KeyStore.PrivateKeyEntry keys() throws Exception {
        KeyStore.ProtectionParameter parameter = new KeyStore.PasswordProtection("byte[]".toCharArray());
        return (KeyStore.PrivateKeyEntry) keyStore.getEntry("mail", parameter);
    }

}
