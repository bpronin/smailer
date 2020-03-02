package com.bopr.android.smailer.util

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

        var putValues = mutableSetOf("A", "B", "C")
        wrapper.update { putStringSet(prefKey, putValues) }

        var getValues = wrapper.getStringSet(prefKey, null)!!

        assertEquals(putValues, getValues)
        assertFalse(putValues === getValues)

        putValues = getValues
        putValues.remove("B")
        wrapper.update { putStringSet(prefKey, putValues) }

        getValues = wrapper.getStringSet(prefKey, null)!!

        assertEquals(setOf("A", "C"), getValues)
        assertEquals(putValues, getValues)
        assertFalse(putValues === getValues)

//
//        wrapper.registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener { _, key ->
//            val set = wrapper.getStringSet(key!!)
//            assertEquals(setOf("A", "C"), set)
//        })

//        assertEquals(setOf("A", "C"), wrapper.getStringSet(prefKey))
    }
}