package com.bopr.android.smailer.ui

import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.GeoCoordinates
import com.bopr.android.smailer.GeoLocator

internal class GetLocationTask(activity: FragmentActivity, private val locator: GeoLocator)
    : LongAsyncTask<Unit, Unit, GeoCoordinates?>(activity) {

    override fun doInBackground(vararg params: Unit?): GeoCoordinates? {
        return locator.getLocation()
    }

    override fun onPostExecute(result: GeoCoordinates?) {
        super.onPostExecute(result)
        InfoDialog(message = result?.format() ?: "No location received").show(activity)
    }

}