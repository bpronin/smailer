package com.bopr.android.smailer;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.Locale;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;

/**
 * {@link MailFormatter} tester.
 */
public class ContactsTest extends ApplicationTestCase<Application> {

    public ContactsTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, "+12345678901")
                .withValue(Phone.TYPE, Phone.TYPE_CUSTOM)
                .withValue(StructuredName.DISPLAY_NAME, "John Dou")
                .withValue(Email.DATA, "johndou@gmail.com")
                .build());

        getContext().getContentResolver().applyBatch(AUTHORITY, ops);
    }

    public void testContactName() throws Exception {
        String name = Contacts.getContactName(getContext(), "+12345678901");
        assertEquals("John Dou", name);
    }

//    public void testEmail() throws Exception {
////        ContentResolver cr = getContext().getContentResolver();
////        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
////        if (cursor != null) {
////            while (cursor.moveToNext()) {
////                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
////                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//////                Log.i("TEST", id + "  " + name);
////
////                Cursor emails = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = " + id, null, null);
////                if (emails != null) {
////                    while (emails.moveToNext()) {
////                        String emailId = emails.getString(emails.getColumnIndex(Email._ID));
////                        String email = emails.getString(emails.getColumnIndex(Email.DATA));
////                        Log.i("TEST", id + " " + name + " " + emailId + " " + email);
////                    }
////                    emails.close();
////                }
////
////            }
////            cursor.close();
////        }
//
//        String name = Contacts.getEmailAddress(getContext(), "75");
//        assertEquals("John Dou", name);
//    }

}