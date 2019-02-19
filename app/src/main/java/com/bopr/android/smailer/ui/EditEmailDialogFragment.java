package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bopr.android.smailer.Contacts;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.validator.EmailTextValidator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Email editor dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EditEmailDialogFragment extends DialogFragment {

    private static final int PICK_CONTACT_REQUEST = 100;

    private int title;
    private String initialValue;
    private Callback callback;
    private TextView editText;

    void showDialog(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), "edit_recipient_dialog");
    }

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
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getContext()).inflate(R.layout.editor_email, null, false);

            editText = view.findViewById(android.R.id.edit);
            editText.addTextChangedListener(new EmailTextValidator(editText));
            editText.setText(initialValue);

            /* custom message view. do not use setMessage() } */
            TextView messageText = view.findViewById(android.R.id.message);
            messageText.setText(R.string.email_address);

            View browseButton = view.findViewById(R.id.button_browse_contacts);
            browseButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivityForResult(Contacts.createPickContactEmailIntent(), PICK_CONTACT_REQUEST);
                }
            });

            dialog = AndroidUtil.dialogBuilder(getContext())
                    .setTitle(title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            callback.onOkClick(editText.getText().toString());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();

            /* this is to show soft keyboard when dialog is open */
            //noinspection ConstantConditions
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            String email = Contacts.getEmailAddressFromIntent(getContext(), intent);
            editText.setText(email);
        }
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

    public interface Callback {

        void onOkClick(String result);

    }

}
