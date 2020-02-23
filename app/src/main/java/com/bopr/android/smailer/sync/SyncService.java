package com.bopr.android.smailer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * Service that returns an IBinder for the sync adapter class, allowing the sync adapter
 * framework to call onPerformSync()
 * <p>
 * Required by synchronization framework.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */

/* To debug it (to put breakpoints) remove android:process=":sync" from AndroidManifest */

public class SyncService extends Service {

    private static final Object lock = new Object();
    private static SyncAdapter adapter;

    @Override
    public void onCreate() {
        synchronized (lock) {
            if (adapter == null) {
                adapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return adapter.getSyncAdapterBinder();
    }

}
