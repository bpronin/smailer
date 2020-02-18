package com.bopr.android.smailer.ui

import android.app.Activity
import com.bopr.android.smailer.GeoCoordinates
import com.bopr.android.smailer.GeoLocator
import com.bopr.android.smailer.util.ui.InfoDialog

internal class GetLocationTask(activity: Activity, private val locator: GeoLocator)
    : LongAsyncTask<Void?, Void?, GeoCoordinates?>(activity) {

    override fun doInBackground(vararg params: Void?): GeoCoordinates? {
        return locator.getLocation()
    }

    override fun onPostExecute(result: GeoCoordinates?) {
        super.onPostExecute(result)
        InfoDialog(activity).apply {
            setMessage(result?.format() ?: "No location received")
        }.show()
    }

}