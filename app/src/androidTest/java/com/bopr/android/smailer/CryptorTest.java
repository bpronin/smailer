package com.bopr.android.smailer;

import org.junit.Test;

/**
 * {@link Cryptor} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CryptorTest extends BaseTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

/*
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            Log.d("CRYPTO", "provider: " + provider.getName());
            Set<Provider.Service> services = provider.getServices();
            for (Provider.Service service : services) {
                Log.d("CRYPTO", "  algorithm: " + service.getAlgorithm());
            }
        }
*/

/*
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        ks.deleteEntry(Cryptor.KEY_ALIAS);
*/
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        Cryptor cryptor = new Cryptor(getContext());
        String text = "the text";

        String encrypted = cryptor.encrypt(text);
        assertNotNull(encrypted);

        String decrypted = cryptor.decrypt(encrypted);
        assertEquals(text, decrypted);
    }

    @Test
    public void testInvalidInput() throws Exception {
        Cryptor cryptor = new Cryptor(getContext());

        String encrypted = cryptor.encrypt(null);
        assertNull(encrypted);

        String decrypted = cryptor.decrypt(null);
        assertNull(decrypted);
    }

}