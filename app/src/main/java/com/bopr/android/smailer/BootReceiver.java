package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static com.bopr.android.smailer.Environment.setupEnvironment;

/**
 * Starts application at device boot.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BootReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("BootReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            setupEnvironment(context);
        }
    }

}