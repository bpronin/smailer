package com.bopr.android.smailer.ui

import android.app.ProgressDialog
import android.os.AsyncTask
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Task showing full screen infinite progress.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Suppress("DEPRECATION")
abstract class LongAsyncTask<P, O, R>(activity: FragmentActivity) : AsyncTask<P, O, R>() {

    /* Holding activity reference in nested static class throws memory leak warning. This approach avoids it.*/
    private val activityReference: WeakReference<FragmentActivity> = WeakReference(activity)
    /* Do not replace qualifier with import to avoid deprecation warning */
    private lateinit var dialog: ProgressDialog

    val activity: FragmentActivity
        get() = activityReference.get()!!

    override fun onPreExecute() {
        dialog = ProgressDialog(activity)
        dialog.setMessage("Processing...")
        dialog.show()
    }

    override fun onPostExecute(result: R) {
        dialog.dismiss()
        super.onPostExecute(result)
    }

}