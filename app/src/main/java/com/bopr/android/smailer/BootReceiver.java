package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bopr.android.smailer.util.Util;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

/**
 * Starts outgoing sms service device boot.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BootReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("BootReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (Util.safeEquals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Fabric.with(context, new Crashlytics());
            OutgoingSmsService.toggleService(context);
            ResendService.toggleService(context);
            MessagingService.initMessaging(context);
        }
    }

}