package com.bopr.android.smailer.ui

import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.bopr.android.smailer.R

/**
 * Base application activity to be replaced in different flavors.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class AppActivity : BaseActivity() {

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adView = findViewById(R.id.ad_view)
        adView.loadAd(AdRequest.Builder().build())
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}