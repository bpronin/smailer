package com.bopr.android.smailer.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.MailerProperties;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.ui.ContextAsyncTask;

import javax.mail.MessagingException;

import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_AUTHENTICATION;
import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_NOT_CONNECTED;
import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_OK;

/**
 * A {@link Preference} for testing server settings.
 * This preference will store an encrypted string into the {@link android.content.SharedPreferences}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TestServerPreference extends Preference {

    private View view;

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestServerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, View view) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.view = view;
    }

    @SuppressWarnings("unused")
    public TestServerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public TestServerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public TestServerPreference(Context context) {
        super(context);
    }

/*
    TODO: Migration
    @Override
    protected void onBindView(View view) {
        this.view = view;
        setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                new SendTestMailTask(TestServerPreference.this).execute();
                return true;
            }
        });
        super.onBindView(view);
    }
*/

    private static class SendTestMailTask extends ContextAsyncTask<Void, Void, Integer> {

        private TestServerPreference owner;

        private SendTestMailTask(TestServerPreference owner) {
            super(owner.getContext());
            this.owner = owner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            owner.view.findViewById(R.id.text_title).setEnabled(false);
            owner.view.findViewById(R.id.progress_wait).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            owner.view.findViewById(R.id.text_title).setEnabled(true);
            owner.view.findViewById(R.id.progress_wait).setVisibility(View.INVISIBLE);

            int message;
            if (result == CHECK_RESULT_NOT_CONNECTED) {
                message = R.string.notification_error_connect;
            } else if (result == CHECK_RESULT_AUTHENTICATION) {
                message = R.string.notification_error_authentication;
            } else {
                message = R.string.message_server_test_success;
            }

            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MailTransport transport = new MailTransport();
            Cryptor cryptor = new Cryptor(getContext());

            MailerProperties pp = new MailerProperties(Settings.getPreferences(getContext()));
            transport.init(pp.getUser(), cryptor.decrypt(pp.getPassword()), pp.getHost(), pp.getPort());

            int result = transport.checkConnection();
            if (result == CHECK_RESULT_OK) {
                try {
                    transport.send("[" + getContext().getString(R.string.app_name) + "] TEST", "This is the test message", pp.getUser(), pp.getUser());
                } catch (MessagingException e) {
                    result = CHECK_RESULT_NOT_CONNECTED;
                }
            }
            return result;
        }
    }

}
