package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.Nullable;

import static com.bopr.android.smailer.RemoteCommandParser.ADD_PHONE_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_PHONE_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_TEXT_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_TEXT_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_TEXT_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_TEXT_FROM_WHITELIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;

/**
 * class RemoteControlService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RemoteControlService extends IntentService {

    private static Logger log = LoggerFactory.getLogger("RemoteControlService");

    private GmailTransport transport;
    private Settings settings;
    private String query;
    private RemoteCommandParser parser;

    public RemoteControlService() {
        super("RemoteControlService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        transport = new GmailTransport(this);
        parser = new RemoteCommandParser();
        settings = new Settings(this);
        query = "subject:Re:[" + getString(R.string.app_name) + "]label:inbox";
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        log.debug("Executing");
        String account = settings.getString(KEY_PREF_REMOTE_CONTROL_ACCOUNT,
                settings.getString(KEY_PREF_SENDER_ACCOUNT, "")
        );

        try {
            transport.init(account, GmailTransport.SCOPE_ALL);
            for (MailMessage message : transport.list(query)) {
                if (processMessage(message)) {
                    transport.markAsRead(message);
                    transport.trash(message);
                }
            }
        } catch (Exception x) {
            log.error("Remote control error", x);
        }
    }

    private boolean processMessage(MailMessage message) {
        RemoteCommandParser.Result result = parser.parse(message);
        log.debug("Performing action: " + result);

        if (result != null) {
            switch (result.action) {
                case ADD_PHONE_TO_BLACKLIST:
                    return addPhoneToBlacklist(result.argument);
                case REMOVE_PHONE_FROM_BLACKLIST:
                    return removePhoneFromBlacklist(result.argument);
                case ADD_PHONE_TO_WHITELIST:
                    return addPhoneToWhitelist(result.argument);
                case REMOVE_PHONE_FROM_WHITELIST:
                    return removePhoneFromWhitelist(result.argument);
                case ADD_TEXT_TO_BLACKLIST:
                    return addTextToBlacklist(result.argument);
                case REMOVE_TEXT_FROM_BLACKLIST:
                    return removeTextFromBlacklist(result.argument);
                case ADD_TEXT_TO_WHITELIST:
                    return addTextToWhitelist(result.argument);
                case REMOVE_TEXT_FROM_WHITELIST:
                    return removeTextFromWhitelist(result.argument);
            }
        }
        return false;
    }

    private boolean removeTextFromWhitelist(String argument) {

        return false;
    }

    private boolean addTextToWhitelist(String argument) {

        return false;
    }

    private boolean removeTextFromBlacklist(String argument) {

        return false;
    }

    private boolean addTextToBlacklist(String argument) {

        return false;
    }

    private boolean removePhoneFromWhitelist(String argument) {

        return false;
    }

    private boolean addPhoneToWhitelist(String argument) {

        return false;
    }

    private boolean removePhoneFromBlacklist(String argument) {

        return false;
    }

    private boolean addPhoneToBlacklist(String phone) {

        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(Context context) {
        context.startService(new Intent(context, RemoteControlService.class));
    }
}
