package com.bopr.android.smailer.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;

/**
 * Class EncryptUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EncryptUtil {

    private static final String TAG = "bopr.EncryptUtil";
    public static AesCbcWithIntegrity.SecretKeys keys;

    private static AesCbcWithIntegrity.SecretKeys keys(Context context) {
        if (keys == null) {
            try {
                keys = AesCbcWithIntegrity.generateKeyFromPassword(DeviceUtil.getAndroidId(context), "3216978");
            } catch (GeneralSecurityException x) {
                throw new Error("Key generation error", x);
            }
        }
        return keys;
    }

    public static String decrypt(Context context, String s) {
        if (StringUtil.isEmpty(s)) {
            return s;
        }

        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(s);
            return AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys(context));
        } catch (Exception x) {
            Log.w(TAG, "Decryption failed", x);
            return null;
        }
    }

    public static String encrypt(Context context, String s) {
        if (StringUtil.isEmpty(s)) {
            return s;
        }

        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(s, keys(context));
            return cipherTextIvMac.toString();
        } catch (Exception x) {
            Log.w(TAG, "Encryption failed", x);
            return null;
        }
    }

}
