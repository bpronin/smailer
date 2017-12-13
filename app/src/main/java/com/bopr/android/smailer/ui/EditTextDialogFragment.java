package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.bopr.android.smailer.R;

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EditTextDialogFragment extends EditFilterListItemDialogFragment {

    private String initialValue;

    @NonNull
    @Override
    protected String createTag() {
        return "edit_text_filter_item_dialog";
    }

    public void setInitialValue(String phone) {
        this.initialValue = phone;
    }

    @Override
    protected String getValue() {
        EditText editor = getDialog().findViewById(R.id.edit_text);
        return editor.getText().toString();
    }

    @NonNull
    protected View createView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.editor_text, null, false);

        EditText editText = view.findViewById(R.id.edit_text);
        editText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        editText.setText(initialValue);

        /* custom message view. do not use setMessage() } */
        TextView messageText = view.findViewById(R.id.dialog_message);
        messageText.setText(R.string.title_text_fragment);

        return view;
    }

}
