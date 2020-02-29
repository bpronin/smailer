package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Main application activity. Individual in different build variants.
 *
 * RELEASE build variant
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class MainAppActivity(fragmentClass: KClass<out Fragment>) : BaseAppActivity(fragmentClass)
