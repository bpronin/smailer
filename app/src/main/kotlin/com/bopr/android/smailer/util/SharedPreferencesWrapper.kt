package com.bopr.android.smailer.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

/* Must be subclass of SharedPreferences! Otherwise it leads to unpredictable results */
open class SharedPreferencesWrapper(private val wrappedPreferences: SharedPreferences) :
    SharedPreferences {

    override fun getAll(): MutableMap<String, *> {
        return wrappedPreferences.all
    }

    override fun getString(key: String, defValue: String?): String? {
        return wrappedPreferences.getString(key, defValue)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return wrappedPreferences.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return wrappedPreferences.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return wrappedPreferences.getFloat(key, defValue)
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        /* Must be a copy of values set. see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        return wrappedPreferences.getStringSet(key, null)?.toMutableSet() ?: defValues
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return wrappedPreferences.getBoolean(key, defValue)
    }

//    inline fun <reified T : Any> get(key: String): T? {
//        return when (T::class) {
//            String::class -> getString(key) as T
//            Boolean::class -> getBoolean(key) as T
//            Int::class -> getInt(key, 0) as T
//            Long::class -> getLong(key, 0) as T
//            Float::class -> getFloat(key, 0f) as T
//            else -> null
//        }
//    }

    fun getString(key: String): String? {
        return getString(key, null)
    }

    fun requireString(key: String, defValue: String): String {
        return requireNotNull(getString(key, defValue))
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getStringSet(key: String): MutableSet<String> {
        return getStringSet(key, mutableSetOf())!!
    }

    fun getStringList(key: String): MutableList<String> {
        return getString(key)?.let { commaSplit(it).toMutableList() } ?: mutableListOf()
    }

    override fun contains(key: String): Boolean {
        return wrappedPreferences.contains(key)
    }

    override fun edit(): EditorWrapper {
        return EditorWrapper(wrappedPreferences.edit())
    }

    fun update(action: EditorWrapper.() -> Unit) {
        val editor = edit()
        action(editor)
        editor.apply()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        wrappedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    open inner class EditorWrapper(private val wrappedEditor: SharedPreferences.Editor) :
        SharedPreferences.Editor {

        override fun putString(key: String, value: String?): EditorWrapper {
            wrappedEditor.putString(key, value)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): EditorWrapper {
            wrappedEditor.putStringSet(key, values)
            return this
        }

        fun putStringList(key: String, value: Collection<String>?): EditorWrapper {
            putString(key, value?.let { commaJoin(value) })
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

        fun putStringIfNotExists(key: String, value: String?): EditorWrapper {
            if (!contains(key)) {
                putString(key, value)
            }
            return this
        }

        fun putStringSetIfNotExists(key: String, values: Set<String>?): EditorWrapper {
            if (!contains(key)) {
                putStringSet(key, values)
            }
            return this
        }

        fun putBooleanIfNotExists(key: String, value: Boolean): EditorWrapper {
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