package com.bopr.android.smailer;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.bopr.android.smailer.util.Cryptor;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class CryptorTest extends ApplicationTestCase<Application> {

    public CryptorTest() {
        super(Application.class);
    }

    public void testRsaEncryptDecryptPassword() throws Exception {
        String password = "the password";

        String encrypted = Cryptor.encrypt(password, getContext());
        assertNotNull(encrypted);

        String decrypted = Cryptor.decrypt(encrypted, getContext());
        assertEquals(password, decrypted);
    }

}