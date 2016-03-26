package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

/**
 * Class EditRecipientDialogFragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EditRecipientDialogFragment extends DialogFragment {

    private static final int PICK_CONTACT_REQUEST = 100;

    private int title;
    private String initialValue;
    private Callback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        /* avoiding disappear on rotation */
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog == null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.editor_recipient, null, false);

            EditText editText = (EditText) view.findViewById(R.id.edit_text_address);
            editText.addTextChangedListener(new EmailTextValidator(editText));
            editText.setText(initialValue);

            View browseButton = view.findViewById(R.id.button_browse_contacts);
            browseButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivityForResult(Contacts.createPickContactEmailIntent(), PICK_CONTACT_REQUEST);
                }
            });

            dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(R.string.pref_dialog_message_recipient)
                    .setView(view)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            callback.onOkClick(getEditor().getText().toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();

            /* this is to show soft keyboard when dialog is open */
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            String email = Contacts.getEmailAddressFromIntent(getActivity(), intent);
            callback.onOkClick(email);
            dismiss();
        }
    }

    private EditText getEditor() {
        return (EditText) getDialog().findViewById(R.id.edit_text_address);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public void setInitialValue(String address) {
        this.initialValue = address;
    }

    public interface  Callback {

        void onOkClick(String result);

    }

}
