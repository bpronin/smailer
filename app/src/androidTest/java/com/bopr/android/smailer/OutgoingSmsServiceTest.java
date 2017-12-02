package com.bopr.android.smailer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.test.ServiceTestCase;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link OutgoingSmsService} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class OutgoingSmsServiceTest extends ServiceTestCase<OutgoingSmsService> {

    private final AtomicBoolean mailServiceStarted = new AtomicBoolean();

    public OutgoingSmsServiceTest() {
        super(OutgoingSmsService.class);
    }

    @Override
    protected void setupService() {
        TestContentResolver resolver = new TestContentResolver();
        TestContentProvider provider = new TestContentProvider(resolver);
        resolver.addProvider("sms", provider);
        TestContentResolverWrapper delegate = new TestContentResolverWrapper(provider);
        setContext(new TestContext(resolver));

        super.setupService();
        getService().setContentResolver(delegate);
    }

    /**
     * Test service's normal behaviour.
     *
     * @throws Exception when fails
     */
    @SmallTest
    public void testStart() throws Exception {
        Intent intent = OutgoingSmsService.createServiceIntent(getContext());
        startService(intent);

        ContentValues values = new ContentValues();
        values.put("_id", 0);
        values.put("address", "123456789");
        values.put("date", System.currentTimeMillis());
        values.put("body", "Sms text");

        mailServiceStarted.set(false);

        getContext().getContentResolver().insert(Uri.parse("content://sms"), values);

        assertTrue(mailServiceStarted.get());
    }

    private class TestContext extends ContextWrapper {

        private final MockContentResolver resolver;

        public TestContext(MockContentResolver resolver) {
            super(OutgoingSmsServiceTest.this.getContext());
            this.resolver = resolver;
        }

        @Override
        public ComponentName startService(Intent service) {
            if (service.getComponent().getClassName().equals(MailerService.class.getName())) {
                mailServiceStarted.set(true);
            }
            return super.startService(service);
        }

        @Override
        public ContentResolver getContentResolver() {
            return resolver;
        }

    }

    private class TestContentProvider extends MockContentProvider {

        private ContentObserver observer;
        private MockContentResolver resolver;
        private MatrixCursor cursor;

        public TestContentProvider(MockContentResolver resolver) {
            this.resolver = resolver;
            cursor = new MatrixCursor(new String[]{"_id", "address", "date", "body"});
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            String id = values.getAsString("_id");
            cursor.addRow(new Object[]{
                    id,
                    values.get("address"),
                    values.get("date"),
                    values.get("body")
            });
            resolver.notifyChange(Uri.parse("content://sms/" + id), observer);
            return uri;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                            final String[] selectionArgs,
                            String sortOrder) {
            return cursor;
        }
    }

    private class TestContentResolver extends MockContentResolver {

        @Override
        public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
            observer.onChange(false, uri);
        }
    }

    private class TestContentResolverWrapper extends OutgoingSmsService.ContentResolverWrapper {

        private TestContentProvider provider;

        public TestContentResolverWrapper(TestContentProvider provider) {
            super(null);
            this.provider = provider;
        }

        @Override
        public void registerContentObserver(@NonNull Uri uri,
                                            @NonNull ContentObserver observer) {
            provider.observer = observer;
        }

        @Override
        public void unregisterContentObserver(@NonNull ContentObserver observer) {
            provider.observer = null;
        }
    }
}