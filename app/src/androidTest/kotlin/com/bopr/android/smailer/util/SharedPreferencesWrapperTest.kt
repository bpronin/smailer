package com.bopr.android.smailer.util

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SharedPreferencesWrapperTest : BaseTest() {

    private lateinit var preferences: SharedPreferences

    @Before
    fun setup() {
        preferences = targetContext.getSharedPreferences("test.preferences", MODE_PRIVATE)
        preferences.edit().clear().apply()
    }

    @Test
    fun testStringSet() {
        val wrapper = SharedPreferencesWrapper(preferences)
        val prefKey = "set_preference"

        wrapper.edit().putStringSet(prefKey, setOf("A", "B", "C")).apply()

        assertEquals(setOf("A", "B", "C"), wrapper.getStringSet(prefKey))

        wrapper.registerChangeListener(OnSharedPreferenceChangeListener { _, key ->
            val set = wrapper.getStringSet(key!!)
            assertEquals(setOf("A", "C"), set)
        })
        wrapper.edit().removeFromStringSet(prefKey, "B").apply()

        assertEquals(setOf("A", "C"), wrapper.getStringSet(prefKey))
    }
}