package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Task showing full screen infinite progress.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */

public abstract class LongAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    
    /* Holding activity reference in nested static class throws memory leak warning. This approach avoids it.*/
    private final WeakReference<Activity> activityReference;
    private ProgressDialog dialog;

    @SuppressWarnings("WeakerAccess")
    public LongAsyncTask(Activity activity) {
        this.activityReference = new WeakReference<>(activity);
    }

    @Override
    @SuppressWarnings("Deprecation")
    protected void onPreExecute() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Processing...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Result result) {
        dialog.dismiss();
        super.onPostExecute(result);
    }

    public Activity getActivity() {
        return activityReference.get();
    }
}
