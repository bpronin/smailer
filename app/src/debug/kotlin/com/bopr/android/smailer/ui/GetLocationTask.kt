package com.bopr.android.smailer.ui

import android.app.Activity
import com.bopr.android.smailer.GeoCoordinates
import com.bopr.android.smailer.GeoLocator
import com.bopr.android.smailer.util.showInfoDialog

internal class GetLocationTask(activity: Activity, private val locator: GeoLocator)
    : LongAsyncTask<Void?, Void?, GeoCoordinates?>(activity) {

    override fun doInBackground(vararg params: Void?): GeoCoordinates? {
        return locator.getLocation()
    }

    override fun onPostExecute(result: GeoCoordinates?) {
        super.onPostExecute(result)
        showInfoDialog(activity, message = result?.format() ?: "No location received")
    }

}