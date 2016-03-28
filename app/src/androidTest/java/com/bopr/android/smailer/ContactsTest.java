package com.bopr.android.smailer;

import android.content.ContentProviderOperation;
import android.provider.ContactsContract;

import java.util.ArrayList;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;

/**
 * {@link MailFormatter} tester.
 */
public class ContactsTest extends BaseTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (Contacts.getContactName(getContext(), "+12345678901") == null) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, "John Dou")
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, "+12345678901")
                    .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                    .build());

            getContext().getContentResolver().applyBatch(AUTHORITY, ops);
        }
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