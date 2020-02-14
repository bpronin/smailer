package com.bopr.android.smailer.remote;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.bopr.android.smailer.GoogleMail;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.Notifications;
import com.bopr.android.smailer.PhoneEventFilter;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static com.bopr.android.smailer.Notifications.ACTION_SHOW_REMOTE_CONTROL;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_NOTIFICATIONS;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_PHONE_TO_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_PHONE_TO_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_TEXT_TO_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.ADD_TEXT_TO_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_PHONE_FROM_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_PHONE_FROM_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_TEXT_FROM_BLACKLIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.REMOVE_TEXT_FROM_WHITELIST;
import static com.bopr.android.smailer.remote.RemoteControlTask.SEND_SMS_TO_CALLER;
import static com.bopr.android.smailer.util.AddressUtil.containsEmail;
import static com.bopr.android.smailer.util.AddressUtil.extractEmail;
import static com.bopr.android.smailer.util.AddressUtil.findPhone;
import static com.bopr.android.smailer.util.Util.commaSplit;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static com.bopr.android.smailer.util.Util.safeEquals;
import static com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM;

/**
 * Remote control service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RemoteControlService extends JobIntentService {

    private static Logger log = LoggerFactory.getLogger("RemoteControlService");

    private static final int JOB_ID = 1002;

    private Settings settings;
    private String query;
    private RemoteControlTaskParser parser;
    private Notifications notifications;

    @Override
    public void onCreate() {
        super.onCreate();
        parser = new RemoteControlTaskParser();
        notifications = new Notifications(this);
        settings = new Settings(this);
        query = String.format("subject:Re:[%s] label:inbox", getString(R.string.app_name));
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        log.debug("Handling intent: " + intent);

        try {
            GoogleMail transport = new GoogleMail(this);
            transport.startSession(requireAccount(), MAIL_GOOGLE_COM);
            List<MailMessage> messages = transport.list(query);

            if (messages.isEmpty()) {
                log.debug("No service mail");
                return;
            }

            for (MailMessage message : messages) {
                if (acceptMessage(message)) {
                    RemoteControlTask task = parser.parse(message.getBody());
                    if (task.getAction() == null) {
                        log.debug("Not a service mail");
                    } else if (!safeEquals(task.getAcceptor(), settings.getDeviceName())) {
                        log.debug("Not my service mail");
                    } else {
                        transport.markAsRead(message);
                        performTask(task);
                        transport.trash(message);
                    }
                }
            }
        } catch (Exception x) {
            log.error("Remote control error", x);
        }
    }

    private boolean acceptMessage(MailMessage message) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)) {
            String address = extractEmail(message.getFrom());
            List<String> recipients = commaSplit(settings.getString(PREF_RECIPIENTS_ADDRESS, ""));
            if (!containsEmail(recipients, address)) {
                log.debug("Address " + address + " rejected");
                return false;
            }
        }
        return true;
    }

    private String requireAccount() {
        String account = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null);
        if (account == null) {
            notifications.showError(R.string.service_account_not_specified, ACTION_SHOW_REMOTE_CONTROL);
            throw new IllegalArgumentException("Service account not specified");
        }
        return account;
    }

    private void performTask(RemoteControlTask task) {
        log.debug("Processing: " + task);

        switch (requireNonNull(task.getAction())) {
            case ADD_PHONE_TO_BLACKLIST:
                addPhoneToBlacklist(task.getArgument());
                break;
            case REMOVE_PHONE_FROM_BLACKLIST:
                removePhoneFromBlacklist(task.getArgument());
                break;
            case ADD_PHONE_TO_WHITELIST:
                addPhoneToWhitelist(task.getArgument());
                break;
            case REMOVE_PHONE_FROM_WHITELIST:
                removePhoneFromWhitelist(task.getArgument());
                break;
            case ADD_TEXT_TO_BLACKLIST:
                addTextToBlacklist(task.getArgument());
                break;
            case REMOVE_TEXT_FROM_BLACKLIST:
                removeTextFromBlacklist(task.getArgument());
                break;
            case ADD_TEXT_TO_WHITELIST:
                addTextToWhitelist(task.getArgument());
                break;
            case REMOVE_TEXT_FROM_WHITELIST:
                removeTextFromWhitelist(task.getArgument());
                break;
            case SEND_SMS_TO_CALLER:
                sendSms(task.getArgument("text"), task.getArgument("phone"));
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

    private void sendSms(@Nullable String message, @Nullable String phone) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendMultipartTextMessage(phone, null, manager.divideMessage(message), null, null);

        log.debug("Sent SMS: " + message + " to " + phone);
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
        if (settings.getBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)) {
            notifications.showRemoteAction(messageRes, text);
        }
    }

    public static void startRemoteControlService(Context context) {
        log.debug("Starting service");

        enqueueWork(context, RemoteControlService.class, JOB_ID,
                new Intent(context, RemoteControlService.class));
    }
}
