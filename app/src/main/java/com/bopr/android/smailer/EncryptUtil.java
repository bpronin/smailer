package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;

/**
 * Class EncryptUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EncryptUtil {

    public static AesCbcWithIntegrity.SecretKeys keys;

    private static AesCbcWithIntegrity.SecretKeys keys(Context context) {
        if (keys == null) {
            try {
                Resources r = context.getResources();
                keys = AesCbcWithIntegrity.generateKeyFromPassword(
                        r.getString(R.string.key_passphrase),
                        r.getString(R.string.key_salt));
            } catch (GeneralSecurityException x) {
                throw new Error("Key generation error", x);
            }
        }
        return keys;
    }

    public static String decrypt(Context context, String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(s);
        try {
            return AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys(context));
        } catch (Exception x) {
            throw new Error("Unable decrypt password", x);
        }
    }

    public static String encrypt(Context context, String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(s, keys(context));
            return cipherTextIvMac.toString();
        } catch (Exception x) {
            throw new Error("Unable encrypt password", x);
        }
    }

}
