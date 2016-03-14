package com.bopr.android.smailer;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * {@link Cryptor} tester.
 */
public class CryptorTest extends ApplicationTestCase<Application> {

    public CryptorTest() {
        super(Application.class);
    }

    public void testEncryptDecrypt() throws Exception {
        Cryptor cryptor = new Cryptor(getContext());
        String text = "the text";

        String encrypted = cryptor.encrypt(text);
        assertNotNull(encrypted);

        String decrypted = cryptor.decrypt(encrypted);
        assertEquals(text, decrypted);
    }

    public void testInvalidInput() throws Exception {
        //todo: implement test
    }

}