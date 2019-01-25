package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.R;

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EditPhoneDialogFragment extends EditFilterListItemDialogFragment {

    private static final int PICK_CONTACT_REQUEST = 100;

    private String initialValue;

    public void setInitialValue(String phone) {
        this.initialValue = phone;
    }

    private EditText getEditor() {
        return getDialog().findViewById(R.id.edit_text_phone);
    }

    @Override
    protected String getValue() {
        return getEditor().getText().toString();
    }

    @NonNull
    @Override
    protected String createTag() {
        return "edit_phone_dialog";
    }

    @NonNull
    @Override
    protected View createView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.editor_phone, null, false);

        EditText editText = view.findViewById(R.id.edit_text_phone);
        //editText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        editText.setText(initialValue);

        /* custom message view. do not use setMessage() } */
        TextView messageText = view.findViewById(R.id.dialog_message);
        messageText.setText(R.string.title_enter_phone_number);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            String phone = Contacts.getPhoneFromIntent(getActivity(), intent);
            getEditor().setText(phone);
        }
    }
}
