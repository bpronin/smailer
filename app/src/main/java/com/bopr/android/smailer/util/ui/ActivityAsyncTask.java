package com.bopr.android.smailer.util.ui;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Holding activity reference in nested static class throws memory leak warning.
 * This approach avoids it.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class ActivityAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private final WeakReference<Activity> activityReference;

    public ActivityAsyncTask(Activity activity) {
        this.activityReference = new WeakReference<>(activity);
    }

    public Activity getActivity() {
        return activityReference.get();
    }
}
