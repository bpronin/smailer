package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.bopr.android.smailer.util.TagFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

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
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_NOTIFICATIONS;
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
    private Notifications notifications;
    private TagFormatter formatter;

    public RemoteControlService() {
        super("RemoteControlService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        formatter = new TagFormatter(this);
        transport = new GmailTransport(this);
        parser = new RemoteCommandParser();
        notifications = new Notifications(this);
        settings = new Settings(this);
        query = String.format("subject:Re:[%s] label:inbox", getString(R.string.app_name)
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        log.debug("Executing");
        String account = settings.getString(KEY_PREF_REMOTE_CONTROL_ACCOUNT,
                settings.getString(KEY_PREF_SENDER_ACCOUNT, "")
        );

        try {
            transport.init(account, GmailTransport.SCOPE_ALL);
            List<MailMessage> list = transport.list(query);
            if (!list.isEmpty()) {
                for (MailMessage message : list) {
                    if (processMessage(message)) {
                        transport.markAsRead(message);
                        transport.trash(message);
                    }
                }
            } else {
                log.debug("No service mail");
            }
        } catch (Exception x) {
            log.error("Remote control error", x);
        }
    }

    private boolean processMessage(MailMessage message) {
        RemoteCommandParser.Result result = parser.parse(message);

        if (result != null) {
            log.debug("Processing: " + result);
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
        } else {
            log.debug("Nothing to process");
        }
        return false;
    }

    private boolean removeTextFromWhitelist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        return removeFromFilterList(filter, filter.getTextWhitelist(), text, R.string.text_remotely_removed_from_whitelist);
    }

    private boolean addTextToWhitelist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        return addToFilterList(filter, filter.getTextWhitelist(), text, R.string.text_remotely_added_to_whitelist);
    }

    private boolean removeTextFromBlacklist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        return removeFromFilterList(filter, filter.getTextBlacklist(), text, R.string.text_remotely_removed_from_blacklist);
    }

    private boolean addTextToBlacklist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        return addToFilterList(filter, filter.getTextBlacklist(), text, R.string.text_remotely_added_to_blacklist);
    }

    private boolean removePhoneFromWhitelist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        return removeFromFilterList(filter, filter.getPhoneWhitelist(), phone, R.string.phone_remotely_removed_from_whitelist);
    }

    private boolean addPhoneToWhitelist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        return addToFilterList(filter, filter.getPhoneWhitelist(), phone, R.string.phone_remotely_added_to_whitelist);
    }

    private boolean removePhoneFromBlacklist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        return removeFromFilterList(filter, filter.getPhoneBlacklist(), phone, R.string.phone_remotely_removed_from_blacklist);
    }

    private boolean addPhoneToBlacklist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        return addToFilterList(filter, filter.getPhoneBlacklist(), phone, R.string.phone_remotely_added_to_blacklist);
    }

    private boolean addToFilterList(PhoneEventFilter filter, Set<String> list, String text, int messageRes) {
        if (!list.contains(text)) {
            list.add(text);
            saveFilter(filter, text, messageRes);
            return true;
        }
        return false;
    }

    private boolean removeFromFilterList(PhoneEventFilter filter, Set<String> list, String text, int messageRes) {
        if (list.contains(text)) {
            list.remove(text);
            saveFilter(filter, text, messageRes);
            return true;
        }
        return false;
    }

    private void saveFilter(PhoneEventFilter filter, String text, int messageRes) {
        settings.putFilter(filter);
        if (settings.getBoolean(KEY_PREF_REMOTE_CONTROL_NOTIFICATIONS, false)) {
            notifications.showRemoteAction(messageRes, text);
        }
    }

    public static void start(Context context) {
        context.startService(new Intent(context, RemoteControlService.class));
    }
}
