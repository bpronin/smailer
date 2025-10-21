package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlin.reflect.KClass

/**
 * Base application activity specific in different build flavors.
 *
 * For PAID build flavor. Without ads.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class FlavorBaseActivity(fragmentClass: KClass<out Fragment>) :
    VariantBaseActivity(fragmentClass) {

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