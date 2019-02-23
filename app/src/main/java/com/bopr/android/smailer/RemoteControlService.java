package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.Nullable;

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
                processMessage(message);
            }
        } catch (Exception x) {
            log.error("Remote control error", x);
        }
    }

    private void processMessage(MailMessage message) {
        RemoteCommandParser.Result result = parser.parse(message);
        log.debug("Performing action: " + result);

        if (result != null) {
            switch (result.action) {
                case RemoteCommandParser.ADD_PHONE_TO_BLACKLIST:
                    addPhoneToBlacklist(result.argument);
                    break;
                case RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST:
                    removePhoneFromBlacklist(result.argument);
                    break;
                case RemoteCommandParser.ADD_PHONE_TO_WHITELIST:
                    addPhoneToWhitelist(result.argument);
                    break;
                case RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST:
                    removePhoneFromWhitelist(result.argument);
                    break;
                case RemoteCommandParser.ADD_TEXT_TO_BLACKLIST:
                    addTextToBlacklist(result.argument);
                    break;
                case RemoteCommandParser.REMOVE_TEXT_FROM_BLACKLIST:
                    removeTextFromBlacklist(result.argument);
                    break;
                case RemoteCommandParser.ADD_TEXT_TO_WHITELIST:
                    addTextToWhitelist(result.argument);
                    break;
                case RemoteCommandParser.REMOVE_TEXT_FROM_WHITELIST:
                    removeTextFromWhitelist(result.argument);
                    break;
            }
        }
    }

    private void removeTextFromWhitelist(String argument) {

    }

    private void addTextToWhitelist(String argument) {

    }

    private void removeTextFromBlacklist(String argument) {

    }

    private void addTextToBlacklist(String argument) {

    }

    private void removePhoneFromWhitelist(String argument) {

    }

    private void addPhoneToWhitelist(String argument) {

    }

    private void removePhoneFromBlacklist(String argument) {

    }

    private void addPhoneToBlacklist(String phone) {

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
