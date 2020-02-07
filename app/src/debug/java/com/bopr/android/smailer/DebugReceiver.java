package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.bopr.android.smailer.CallProcessorService.startCallProcessingService;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;

public class DebugReceiver extends BroadcastReceiver {

    /* use adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <YOUR_ACTION>
       to send intents to this receiver */

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("PROCESS_EVENT".equals(intent.getAction())) {
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

        startCallProcessingService(context, event);
    }

}
