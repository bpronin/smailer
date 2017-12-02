package com.bopr.android.smailer.util.ui;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Holding context in nested static class throw memory leak warning.
 * This class avoids it.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class ContextAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private final WeakReference<Context> contextReference;

    public ContextAsyncTask(Context context) {
        this.contextReference = new WeakReference<>(context);
    }

    public Context getContext() {
        return contextReference.get();
    }
}
