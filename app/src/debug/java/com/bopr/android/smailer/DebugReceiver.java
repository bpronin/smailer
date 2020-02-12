package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.util.AndroidUtil.deviceName;

public class DebugReceiver extends BroadcastReceiver {

    /* use
        adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <ACTION>
       to send intents to this receiver */

    private static Logger log = LoggerFactory.getLogger("DebugReceiver");

    private static final String PROCESS_EVENT = "PROCESS_EVENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (PROCESS_EVENT.equals(intent.getAction())) {
            onProcessSingleEvent(context);
        }
    }

    private void onProcessSingleEvent(Context context) {
        long start = System.currentTimeMillis();

        PhoneEvent event = new PhoneEvent();
        event.setRecipient(deviceName());
        event.setPhone("5556");
        event.setText("SMS TEXT");
        event.setIncoming(true);
        event.setStartTime(start);
        event.setEndTime(start + 10000);

        CallProcessorService.Companion.startCallProcessingService(context, event);
    }

}
