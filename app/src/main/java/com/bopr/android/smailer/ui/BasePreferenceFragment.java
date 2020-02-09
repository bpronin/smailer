package com.bopr.android.smailer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

import com.bopr.android.smailer.PermissionsHelper;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.bopr.android.smailer.ui.AboutDialogFragment.showAboutDialog;
import static com.bopr.android.smailer.util.ResourceUtil.accentedText;
import static com.bopr.android.smailer.util.ResourceUtil.underwivedText;
import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Base {@link PreferenceFragmentCompat } with default behaviour.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    static final int STYLE_DEFAULT = 0;
    static final int STYLE_UNDERWIVED = 1;
    static final int STYLE_ACCENTED = 2;

    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

    PermissionsHelper permissionsHelper;
    Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(requireContext());
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshPreferenceViews();

        permissionsHelper = new PermissionsHelper(getActivity(), settings) {

            @Override
            protected void onPermissionsDenied(Collection<String> permissions) {
                super.onPermissionsDenied(permissions);
                refreshPreferenceViews();
            }
        };
    }

    @Override
    public void onStop() {
        permissionsHelper.dispose();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsHelper.handleRequestResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        /* check if dialog is already showing */
        assert getFragmentManager() != null;
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            showAboutDialog(requireActivity());
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates summary of {@link Preference}.
     *
     * @param value      value
     * @param preference preference
     */
    void updateSummary(@NonNull Preference preference, @Nullable String value, int style) {
        switch (style) {
            case STYLE_DEFAULT:
                preference.setSummary(value);
                break;
            case STYLE_UNDERWIVED:
                preference.setSummary(underwivedText(getContext(), value));
                break;
            case STYLE_ACCENTED:
                preference.setSummary(accentedText(getContext(), value));
                break;
        }
    }

    /**
     * Reads fragment's {@link SharedPreferences} and updates settings value.
     */
    void refreshPreferenceViews() {
        doRefreshPreferenceViews(getPreferenceScreen());
    }

    @SuppressWarnings("unchecked")
    private void doRefreshPreferenceViews(PreferenceGroup group) {
        Map<String, ?> map = settings.getAll();
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                doRefreshPreferenceViews((PreferenceGroup) preference);
            } else {
                Object value = map.get(preference.getKey());
                Preference.OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
                if (listener != null) {
                    listener.onPreferenceChange(preference, value);
                }

                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText((String) value);
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(value != null && (boolean) value);
                } else if (preference instanceof CheckBoxPreference) {
                    ((CheckBoxPreference) preference).setChecked(value != null && (boolean) value);
                } else if (preference instanceof ListPreference) {
                    ((ListPreference) preference).setValue((String) value);
                } else if (preference instanceof MultiSelectListPreference) {
                    Set<String> set = value == null ? Collections.<String>emptySet() : (Set<String>) value;
                    ((MultiSelectListPreference) preference).setValues(set);
                }
            }
        }
    }

    @NonNull
    <T extends Preference> T requirePreference(@NonNull CharSequence key) {
        return requireNonNull(this.<T>findPreference(key));
    }
}
