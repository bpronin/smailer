package com.bopr.android.smailer.util;

import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class SharedPreferencesWrapper implements SharedPreferences {

    private final SharedPreferences wrappedPreferences;

    public SharedPreferencesWrapper(SharedPreferences wrappedPreferences) {
        this.wrappedPreferences = wrappedPreferences;
    }

    @Override
    public Map<String, ?> getAll() {
        return wrappedPreferences.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return wrappedPreferences.getString(key, defValue);
    }

    public String requireString(String key) {
        if (!contains(key)) {
            throw new IllegalStateException("No required preference: " + key);
        }
        return getString(key, "");
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        /* should be a copy of values set.
           see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        Set<String> set = wrappedPreferences.getStringSet(key, defValues);
        return set != null ? new LinkedHashSet<>(set) : null;
    }

    @Override
    public int getInt(String key, int defValue) {
        return wrappedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return wrappedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return wrappedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return wrappedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return wrappedPreferences.contains(key);
    }

    @Override
    public EditorWrapper edit() {
        return new EditorWrapper(wrappedPreferences.edit());
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        wrappedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean isNull(String key) {
        return getString(key, null) == null;
    }

    public class EditorWrapper implements Editor {

        private final Editor wrappedEditor;

        private EditorWrapper(Editor wrappedEditor) {
            this.wrappedEditor = wrappedEditor;
        }

        @Override
        public EditorWrapper putString(String key, @Nullable String value) {
            wrappedEditor.putString(key, value);
            return this;
        }

        @Override
        public EditorWrapper putStringSet(String key, @Nullable Set<String> values) {
            wrappedEditor.putStringSet(key, values);
            return this;
        }

        @Override
        public EditorWrapper putInt(String key, int value) {
            wrappedEditor.putInt(key, value);
            return this;
        }

        @Override
        public EditorWrapper putLong(String key, long value) {
            wrappedEditor.putLong(key, value);
            return this;
        }

        @Override
        public EditorWrapper putFloat(String key, float value) {
            wrappedEditor.putFloat(key, value);
            return this;
        }

        @Override
        public EditorWrapper putBoolean(String key, boolean value) {
            wrappedEditor.putBoolean(key, value);
            return this;
        }

        public EditorWrapper putStringOptional(String key, @Nullable String value) {
            if (!contains(key)) {
                wrappedEditor.putString(key, value);
            }
            return this;
        }

        public EditorWrapper putStringSetOptional(String key, @Nullable Set<String> values) {
            if (!contains(key)) {
                wrappedEditor.putStringSet(key, values);
            }
            return this;
        }

        public EditorWrapper putIntOptional(String key, int value) {
            if (!contains(key)) {
                wrappedEditor.putInt(key, value);
            }
            return this;
        }

        public EditorWrapper putLongOptional(String key, long value) {
            if (!contains(key)) {
                wrappedEditor.putLong(key, value);
            }
            return this;
        }

        public EditorWrapper putFloatOptional(String key, float value) {
            if (!contains(key)) {
                wrappedEditor.putFloat(key, value);
            }
            return this;
        }

        public EditorWrapper putBooleanOptional(String key, boolean value) {
            if (!contains(key)) {
                wrappedEditor.putBoolean(key, value);
            }
            return this;
        }

        @Override
        public EditorWrapper remove(String key) {
            wrappedEditor.remove(key);
            return this;
        }

        @Override
        public EditorWrapper clear() {
            wrappedEditor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return wrappedEditor.commit();
        }

        @Override
        public void apply() {
            wrappedEditor.apply();
        }
    }
}
