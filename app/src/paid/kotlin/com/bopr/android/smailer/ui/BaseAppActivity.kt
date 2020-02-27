package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Base application activity. Individual in different build variants.
 *
 * PAID BUILD VARIANT
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseAppActivity(fragmentClass: KClass<out Fragment>) : BaseActivity(fragmentClass)