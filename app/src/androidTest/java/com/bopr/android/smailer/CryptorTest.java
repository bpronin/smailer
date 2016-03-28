package com.bopr.android.smailer;

/**
 * {@link Cryptor} tester.
 */
public class CryptorTest extends BaseTest {

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