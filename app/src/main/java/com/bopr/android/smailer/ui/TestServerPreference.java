package com.bopr.android.smailer.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.MailTransport;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import javax.mail.MessagingException;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_AUTHENTICATION;
import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_NOT_CONNECTED;
import static com.bopr.android.smailer.MailTransport.CHECK_RESULT_OK;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;

/**
 * A {@link Preference} for testing server settings.
 * This preference will store an encrypted string into the {@link android.content.SharedPreferences}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TestServerPreference extends Preference {

    private PreferenceViewHolder viewHolder;

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestServerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, View view) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        viewHolder = holder;
    }

    private void setWaiting(boolean waiting) {
        if (waiting) {
            viewHolder.itemView.findViewById(R.id.text_title).setEnabled(false);
            viewHolder.itemView.findViewById(R.id.progress_wait).setVisibility(View.VISIBLE);
        } else {
            viewHolder.itemView.findViewById(R.id.text_title).setEnabled(true);
            viewHolder.itemView.findViewById(R.id.progress_wait).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onClick() {
        new SendTestMailTask(this).execute();
    }

    private static class SendTestMailTask extends AsyncTask<Void, Void, Integer> {

        private TestServerPreference owner;

        private SendTestMailTask(TestServerPreference owner) {
            this.owner = owner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            owner.setWaiting(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MailTransport transport = new MailTransport();
            Cryptor cryptor = new Cryptor(owner.getContext());

            SharedPreferences preferences = Settings.preferences(owner.getContext());
            String user = preferences.getString(KEY_PREF_SENDER_ACCOUNT, null);
            String password = preferences.getString(KEY_PREF_SENDER_PASSWORD, null);
            String host = preferences.getString(KEY_PREF_EMAIL_HOST, null);
            String port = preferences.getString(KEY_PREF_EMAIL_PORT, null);

            transport.startSession(user, cryptor.decrypt(password), host, port);

            int result = transport.checkConnection();
            if (result == CHECK_RESULT_OK) {
                try {
                    transport.send("[" + owner.getContext().getString(R.string.app_name) + "] TEST", "This is the test message", user, user);
                } catch (MessagingException e) {
                    result = CHECK_RESULT_NOT_CONNECTED;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            owner.setWaiting(false);

            int message;
            if (result == CHECK_RESULT_NOT_CONNECTED) {
                message = R.string.notification_error_connect;
            } else if (result == CHECK_RESULT_AUTHENTICATION) {
                message = R.string.notification_error_authentication;
            } else {
                message = R.string.message_server_test_success;
            }

            Toast.makeText(owner.getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

}
