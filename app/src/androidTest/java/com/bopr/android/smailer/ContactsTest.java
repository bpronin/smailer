package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.test.mock.MockCursor;

import org.junit.Test;

import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Contacts} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ContactsTest extends BaseTest {

    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = mock(Context.class);
        MockContentResolver resolver = new MockContentResolver();
        resolver.addProvider(ContactsContract.AUTHORITY, new MockContentProvider(context) {

            @Override
            public Cursor query(Uri uri, String[] projection, String selection,
                                String[] selectionArgs,
                                String sortOrder) {
                List<String> segments = uri.getPathSegments();
                if (segments.get(0).equals("phone_lookup") && segments.get(1).equals("+12345678901")) {
                    return new TestCursor("display_name", "John Dou");
                } else if (segments.get(0).equals("data") && segments.get(1).equals("emails") && selection.equals("_id=75")) {
                    return new TestCursor("data1", "johndou@mail.com");
                } else {
                    return super.query(uri, projection, selection, selectionArgs, sortOrder);
                }
            }
        });

        when(context.getContentResolver()).thenReturn(resolver);
        when(context.getResources()).thenReturn(getContext().getResources());
    }

    /**
     * Method {@link Contacts#getContactName(Context, String)} tester.
     *
     * @throws Exception when failed
     */
    @Test
    public void testGetContactName() throws Exception {
        assertEquals("John Dou", Contacts.getContactName(context, "+12345678901"));
    }

    /**
     * Method {@link Contacts#getContactName(Context, String)} tester with no permission.
     *
     * @throws Exception when failed
     */
    @Test
    public void testGetContactNameNoPermission() throws Exception {
        when(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);
        assertNull(Contacts.getContactName(context, "+12345678901"));
    }

    /**
     * Method {@link Contacts#getEmailAddress(Context, String)} tester.
     *
     * @throws Exception when failed
     */
    @Test
    public void testGetEmailAddress() throws Exception {
        assertEquals("johndou@mail.com", Contacts.getEmailAddress(context, "75"));
    }

    /**
     * Method {@link Contacts#getEmailAddress(Context, String)} tester with no permission.
     *
     * @throws Exception when failed
     */
    @Test
    public void testGetEmailAddressNoPermission() throws Exception {
        when(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);
        assertNull(Contacts.getEmailAddress(context, "75"));
    }

    /**
     * Method {@link Contacts#getEmailAddressFromIntent(Context, Intent)} tester.
     *
     * @throws Exception when failed
     */
    @Test
    public void testEmailFromIntent() throws Exception {
        Intent intent = new Intent();
        intent.setData(Uri.parse("content://com.android.contacts/data/75"));

        String name = Contacts.getEmailAddressFromIntent(context, intent);
        assertEquals("johndou@mail.com", name);
    }

    /**
     * Method {@link Contacts#createPickContactEmailIntent()} tester.
     *
     * @throws Exception when failed
     */
    @Test
    public void testCreatePickContactEmailIntent() throws Exception {
        Intent intent = Contacts.createPickContactEmailIntent();
        assertEquals(Intent.ACTION_PICK, intent.getAction());
        assertEquals(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE, intent.getType());
    }

    /**
     * Method {@link Contacts#createPickContactPhoneIntent()} tester.
     *
     * @throws Exception when failed
     */
    @Test
    public void testCreatePickContactPhoneIntent() throws Exception {
        // TODO: 05.12.2017  
    }

    private class TestCursor extends MockCursor {

        private String columnName;
        private String value;

        public TestCursor(String columnName, String value) {
            this.columnName = columnName;
            this.value = value;
        }

        @Override
        public int getColumnIndex(String columnName) {
            return columnName.equals(this.columnName) ? 0 : -1;
        }

        @Override
        public String getString(int columnIndex) {
            return columnIndex == 0 ? value : null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean moveToFirst() {
            return true;
        }

        @Override
        public void close() {
        }
    }
}