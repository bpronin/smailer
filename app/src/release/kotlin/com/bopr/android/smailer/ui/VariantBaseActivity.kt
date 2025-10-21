package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Base application activity. Specific in different build variants.
 *
 * For RELEASE build variant. Without debug features.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
open class VariantBaseActivity(value: KClass<out Fragment>) : BaseActivity(value)
