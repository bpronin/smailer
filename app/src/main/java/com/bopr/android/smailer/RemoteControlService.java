package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.bopr.android.smailer.RemoteCommandParser.Task;
import com.bopr.android.smailer.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.Nullable;

import static com.bopr.android.smailer.GmailTransport.SCOPE_ALL;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_PHONE_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_PHONE_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_TEXT_TO_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.ADD_TEXT_TO_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_PHONE_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_PHONE_FROM_WHITELIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_TEXT_FROM_BLACKLIST;
import static com.bopr.android.smailer.RemoteCommandParser.REMOVE_TEXT_FROM_WHITELIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_FILTER_RECIPIENTS;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_NOTIFICATIONS;
import static com.bopr.android.smailer.util.PhoneUtil.findPhone;

/**
 * Remote control service.
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

    public RemoteControlService() {
        super("RemoteControlService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        transport = new GmailTransport(this);
        parser = new RemoteCommandParser();
        notifications = new Notifications(this);
        settings = new Settings(this);
        query = String.format("subject:Re:[%s] label:inbox", getString(R.string.app_name));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        log.debug("Handling intent: " + intent);

        try {
            transport.init(requireAccount(), SCOPE_ALL);
            List<MailMessage> list = transport.list(query);
            if (!list.isEmpty()) {
                for (MailMessage message : list) {
                    if (acceptMessage(message)) {
                        Task task = parser.parse(message);
                        transport.markAsRead(message);
                        if (task != null) {
                            performTask(task);
                            transport.trash(message);
                        } else {
                            log.debug("Not a service mail");
                        }
                    } else {
                        log.debug("Rejected");
                    }
                }
            } else {
                log.debug("No service mail");
            }
        } catch (Exception x) {
            log.error("Remote control error", x);
        }
    }

    private boolean acceptMessage(MailMessage message) {
        if (settings.getBoolean(KEY_PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)) {
            String address = Util.extractEmailAddress(message.getFrom());
            String recipients = settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, "").toLowerCase(Locale.ROOT);
            return address != null && recipients.contains(address.toLowerCase(Locale.ROOT));
        }
        return true;
    }

    private String requireAccount() {
        String s = settings.getString(KEY_PREF_REMOTE_CONTROL_ACCOUNT, null);
        if (s == null) {
            notifications.showRemoteError(R.string.service_account_not_specified);
            throw new IllegalArgumentException("Service account not specified");
        }
        return s;
    }

    private void performTask(Task task) {
        log.debug("Processing: " + task);
        switch (task.action) {
            case ADD_PHONE_TO_BLACKLIST:
                addPhoneToBlacklist(task.argument);
                break;
            case REMOVE_PHONE_FROM_BLACKLIST:
                removePhoneFromBlacklist(task.argument);
                break;
            case ADD_PHONE_TO_WHITELIST:
                addPhoneToWhitelist(task.argument);
                break;
            case REMOVE_PHONE_FROM_WHITELIST:
                removePhoneFromWhitelist(task.argument);
                break;
            case ADD_TEXT_TO_BLACKLIST:
                addTextToBlacklist(task.argument);
                break;
            case REMOVE_TEXT_FROM_BLACKLIST:
                removeTextFromBlacklist(task.argument);
                break;
            case ADD_TEXT_TO_WHITELIST:
                addTextToWhitelist(task.argument);
                break;
            case REMOVE_TEXT_FROM_WHITELIST:
                removeTextFromWhitelist(task.argument);
                break;
        }
    }

    private void removeTextFromWhitelist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        removeFromTextList(filter, filter.getTextWhitelist(), text, R.string.text_remotely_removed_from_whitelist);
    }

    private void addTextToWhitelist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        addToFilterList(filter, filter.getTextWhitelist(), text, R.string.text_remotely_added_to_whitelist);
    }

    private void removeTextFromBlacklist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        removeFromTextList(filter, filter.getTextBlacklist(), text, R.string.text_remotely_removed_from_blacklist);
    }

    private void addTextToBlacklist(String text) {
        PhoneEventFilter filter = settings.getFilter();
        addToFilterList(filter, filter.getTextBlacklist(), text, R.string.text_remotely_added_to_blacklist);
    }

    private void removePhoneFromWhitelist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        removeFromPhoneList(filter, filter.getPhoneWhitelist(), phone, R.string.phone_remotely_removed_from_whitelist);
    }

    private void addPhoneToWhitelist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        addToFilterList(filter, filter.getPhoneWhitelist(), phone, R.string.phone_remotely_added_to_whitelist);
    }

    private void removePhoneFromBlacklist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        removeFromPhoneList(filter, filter.getPhoneBlacklist(), phone, R.string.phone_remotely_removed_from_blacklist);
    }

    private void addPhoneToBlacklist(String phone) {
        PhoneEventFilter filter = settings.getFilter();
        addToFilterList(filter, filter.getPhoneBlacklist(), phone, R.string.phone_remotely_added_to_blacklist);
    }

    private void addToFilterList(PhoneEventFilter filter, Set<String> list, String text, int messageRes) {
        if (!list.contains(text)) {
            list.add(text);
            saveFilter(filter, text, messageRes);
        } else {
            log.debug("Already in list");
        }
    }

    private void removeFromTextList(PhoneEventFilter filter, Set<String> list, String text, int messageRes) {
        if (list.contains(text)) {
            list.remove(text);
            saveFilter(filter, text, messageRes);
        } else {
            log.debug("Not in list");
        }
    }

    private void removeFromPhoneList(PhoneEventFilter filter, Set<String> list, String number, int messageRes) {
        String existingNumber = findPhone(list, number);
        if (existingNumber != null) {
            list.remove(existingNumber);
            saveFilter(filter, number, messageRes);
        } else {
            log.debug("Not in list");
        }
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