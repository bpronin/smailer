package com.bopr.android.smailer.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import java.util.*

open class SharedPreferencesWrapper(private val wrappedPreferences: SharedPreferences) : SharedPreferences {

    override fun getAll(): Map<String, *> {
        return wrappedPreferences.all
    }

    override fun getString(key: String, defValue: String?): String? {
        return wrappedPreferences.getString(key, defValue)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): MutableSet<String>? {
        /* should be a copy of values set.
           see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        return wrappedPreferences.getStringSet(key, defValues)?.let { LinkedHashSet(it) }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return wrappedPreferences.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return wrappedPreferences.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return wrappedPreferences.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return wrappedPreferences.getBoolean(key, defValue)
    }

    override fun contains(key: String): Boolean {
        return wrappedPreferences.contains(key)
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(wrappedPreferences.edit())
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
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
            getStringSet(key, null)?.apply {
                if (isNotEmpty()) {
                    removeAll(Arrays.asList<String>(*values))
                    putStringSet(key, this)
                }
            }
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
                wrappedEditor.putString(key, value)
            }
            return this
        }

        fun putStringSetOptional(key: String, values: Set<String?>?): EditorWrapper {
            if (!contains(key)) {
                wrappedEditor.putStringSet(key, values)
            }
            return this
        }

/*
        fun putIntOptional(key: String, value: Int): EditorWrapper {
            if (!contains(key)) {
                wrappedEditor.putInt(key, value)
            }
            return this
        }

        fun putLongOptional(key: String, value: Long): EditorWrapper {
            if (!contains(key)) {
                wrappedEditor.putLong(key, value)
            }
            return this
        }

        fun putFloatOptional(key: String, value: Float): EditorWrapper {
            if (!contains(key)) {
                wrappedEditor.putFloat(key, value)
            }
            return this
        }
*/

        fun putBooleanOptional(key: String, value: Boolean): EditorWrapper {
            if (!contains(key)) {
                wrappedEditor.putBoolean(key, value)
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