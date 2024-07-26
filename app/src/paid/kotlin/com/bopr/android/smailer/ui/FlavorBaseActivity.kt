package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Base application activity specific in different build flavors.
 *
 * For PAID build flavor. Without ads.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class FlavorBaseActivity(fragmentClass: KClass<out Fragment>) :
    VariantBaseActivity(fragmentClass)