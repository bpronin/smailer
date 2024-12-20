package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Base application activity. Specific in different build variants.
 *
 * For RELEASE build variant. Without debug features.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
open class VariantBaseActivity(value: KClass<out Fragment>) : BaseActivity(value)
