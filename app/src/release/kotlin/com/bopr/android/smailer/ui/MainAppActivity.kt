package com.bopr.android.smailer.ui

import kotlin.reflect.KClass

/**
 * Main application activity. Individual in different build variants.
 *
 * RELEASE BUILD VARIANT
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class MainAppActivity(fragmentClass: KClass<out Fragment>) : BaseAppActivity(fragmentClass)
