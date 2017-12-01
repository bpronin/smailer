package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.bopr.android.smailer.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.MailerService.createEventIntent;
import static com.bopr.android.smailer.Settings.*;

/**
 * Receives SMS intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("SmsReceiver");
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private final SmsParser parser = new SmsParser();

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);
        if (Util.equals(intent.getAction(), SMS_RECEIVED_ACTION) && isServiceEnabled(context)
                && isTriggerEnabled(context, VAL_PREF_TRIGGER_IN_SMS)) {

            PhoneEvent event = parser.parse(intent);
            PhoneEventFilter filter = Settings.loadFilter(context);
            if (filter.accept(event)) {
                log.debug("Processing incoming sms");
                context.startService(createEventIntent(context, event));
            }
        }
    }

}