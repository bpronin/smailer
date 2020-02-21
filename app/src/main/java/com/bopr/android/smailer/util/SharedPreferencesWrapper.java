package com.bopr.android.smailer.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.SharedPreferences.Editor;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.bopr.android.smailer.util.TextUtil.commaJoin;
import static com.bopr.android.smailer.util.TextUtil.commaSplit;
import static com.bopr.android.smailer.util.Util.setOf;
import static java.util.Arrays.asList;

@SuppressWarnings({"UnusedReturnValue", "unused", "WeakerAccess"})
public class SharedPreferencesWrapper {

    private final SharedPreferences wrappedPreferences;

    public SharedPreferencesWrapper(@NonNull SharedPreferences wrappedPreferences) {
        this.wrappedPreferences = wrappedPreferences;
    }

    @NonNull
    public Map<String, ?> getAll() {
        return wrappedPreferences.getAll();
    }

    @Nullable
    public String getString(@NonNull String key) {
        return wrappedPreferences.getString(key, null);
    }

    @NonNull
    public String getString(@NonNull String key, @NonNull String defValue) {
        return wrappedPreferences.getString(key, defValue);
    }

    @NonNull
    public Set<String> getStringSet(@NonNull String key) {
        /* should be a copy of values set.
           see: https://stackoverflow.com/questions/17469583/setstring-in-android-sharedpreferences-does-not-save-on-force-close */
        return new LinkedHashSet<>(wrappedPreferences.getStringSet(key, setOf()));
    }

    public int getInt(@NonNull String key, int defValue) {
        return wrappedPreferences.getInt(key, defValue);
    }

    public long getLong(@NonNull String key, long defValue) {
        return wrappedPreferences.getLong(key, defValue);
    }

    public float getFloat(@NonNull String key, float defValue) {
        return wrappedPreferences.getFloat(key, defValue);
    }

    public boolean getBoolean(@NonNull String key, boolean defValue) {
        return wrappedPreferences.getBoolean(key, defValue);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(@NonNull String key) {
        return wrappedPreferences.contains(key);
    }

    @NonNull
    public List<String> getCommaList(@NonNull String key) {
        return commaSplit(getString(key, ""));
    }

    @NonNull
    public Set<String> getCommaSet(@NonNull String key) {
        return new LinkedHashSet<>(getCommaList(key));
    }

    @NonNull
    public EditorWrapper edit() {
        return new EditorWrapper(wrappedPreferences.edit());
    }

    public void registerOnSharedPreferenceChangeListener(@NonNull OnSharedPreferenceChangeListener listener) {
        wrappedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(@NonNull OnSharedPreferenceChangeListener listener) {
        wrappedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public class EditorWrapper implements Editor {

        private final Editor wrappedEditor;

        protected EditorWrapper(Editor wrappedEditor) {
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

        public void removeFromStringSet(String key, String... values) {
            Set<String> set = getStringSet(key);
            if (!set.isEmpty()) {
                set.removeAll(asList(values));
                putStringSet(key, set);
            }
        }

        public EditorWrapper putCommaSet(String key, Collection<String> set) {
            putString(key, commaJoin(set));
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
