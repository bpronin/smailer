package com.bopr.android.smailer.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.bopr.android.smailer.util.TextUtil.commaJoin
import com.bopr.android.smailer.util.TextUtil.commaSplit

open class SharedPreferencesWrapper(private val wrappedPreferences: SharedPreferences) {

    fun getString(key: String): String? {
        return wrappedPreferences.getString(key, null)
    }

    fun getString(key: String, defValue: String): String {
        return getString(key) ?: defValue
    }

    /* should be a copy of values set. see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
    fun getStringSet(key: String): MutableSet<String> {
        return wrappedPreferences.getStringSet(key, null)?.toMutableSet() ?: mutableSetOf()
    }

    fun getCommaSet(key: String): MutableSet<String> {
        return getString(key)?.let { commaSplit(it).toMutableSet() } ?: mutableSetOf()
    }

    fun getCommaList(key: String): MutableList<String> {
        return getString(key)?.let { commaSplit(it).toMutableList() } ?: mutableListOf()
    }

    fun getInt(key: String, defValue: Int): Int {
        return wrappedPreferences.getInt(key, defValue)
    }

    fun getLong(key: String, defValue: Long): Long {
        return wrappedPreferences.getLong(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return wrappedPreferences.getBoolean(key, defValue)
    }

    fun contains(key: String): Boolean {
        return wrappedPreferences.contains(key)
    }

    open fun edit(): EditorWrapper {
        return EditorWrapper(wrappedPreferences.edit())
    }

    fun registerChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    open inner class EditorWrapper(private val wrappedEditor: SharedPreferences.Editor) : SharedPreferences.Editor {

        override fun putString(key: String, value: String?): EditorWrapper {
            wrappedEditor.putString(key, value)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): EditorWrapper {
            wrappedEditor.putStringSet(key, values)
            return this
        }

        fun removeFromStringSet(key: String, vararg values: String): EditorWrapper {
            putStringSet(key, getStringSet(key).subtract(values.asList()))
            return this
        }

        fun putCommaSet(key: String, value: Collection<String>): EditorWrapper {
            putString(key, commaJoin(value))
            return this
        }

        override fun putInt(key: String, value: Int): EditorWrapper {
            wrappedEditor.putInt(key, value)
            return this
        }

        override fun putLong(key: String, value: Long): EditorWrapper {
            wrappedEditor.putLong(key, value)
            return this
        }

        override fun putFloat(key: String, value: Float): EditorWrapper {
            wrappedEditor.putFloat(key, value)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): EditorWrapper {
            wrappedEditor.putBoolean(key, value)
            return this
        }

        fun putStringOptional(key: String, value: String?): EditorWrapper {
            if (!contains(key)) {
                putString(key, value)
            }
            return this
        }

        fun putStringSetOptional(key: String, values: Set<String>?): EditorWrapper {
            if (!contains(key)) {
                putStringSet(key, values)
            }
            return this
        }

        fun putBooleanOptional(key: String, value: Boolean): EditorWrapper {
            if (!contains(key)) {
                putBoolean(key, value)
            }
            return this
        }

        override fun remove(key: String): EditorWrapper {
            wrappedEditor.remove(key)
            return this
        }

        override fun clear(): EditorWrapper {
            wrappedEditor.clear()
            return this
        }

        override fun commit(): Boolean {
            return wrappedEditor.commit()
        }

        override fun apply() {
            wrappedEditor.apply()
        }

    }

}