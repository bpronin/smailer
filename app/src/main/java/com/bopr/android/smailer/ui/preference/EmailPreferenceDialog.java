package com.bopr.android.smailer.ui.preference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

/**
 * A {@link EmailPreference } dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EmailPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {

    private static final int PICK_CONTACT_REQUEST = 100;

    private TextView editText;

    public static EmailPreferenceDialog newInstance(String key) {
        EmailPreferenceDialog fragment = new EmailPreferenceDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editText = view.findViewById(android.R.id.edit);
        editText.addTextChangedListener(new EmailTextValidator(editText));

        ImageButton button = view.findViewById(R.id.button_browse_contacts);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(Contacts.createPickContactEmailIntent(), PICK_CONTACT_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            String email = Contacts.getEmailAddressFromIntent(getContext(), intent);
            editText.setText(email);
        }
    }
}
