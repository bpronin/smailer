package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.app.Dialog;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.ui.ContextAsyncTask;

/**
 * Task showing full screen infinite progress.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class LongAsyncTask<Params, Progress, Result> extends ContextAsyncTask<Params, Progress, Result> {

    private Dialog dialog;

    public LongAsyncTask(Activity activity) {
        super(activity);
    }

    @Override
    protected void onPreExecute() {
        dialog = new Dialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setOwnerActivity((Activity) getContext());
        dialog.setContentView(R.layout.dialog_full_screen_progress);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Result result) {
        dialog.dismiss();
        super.onPostExecute(result);
    }

}
