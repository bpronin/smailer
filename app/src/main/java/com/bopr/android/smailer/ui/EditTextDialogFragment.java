package com.bopr.android.smailer.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.R;

import static com.bopr.android.smailer.util.TextUtil.escapeRegex;
import static com.bopr.android.smailer.util.TextUtil.unescapeRegex;

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class EditTextDialogFragment extends EditFilterListItemDialogFragment {


    private String initialText;
    private boolean initialRegex;
    private EditText editText;
    private CheckBox checkBox;

    @NonNull
    @Override
    protected String createTag() {
        return "edit_text_filter_item_dialog";
    }

    void setInitialValue(String text) {
        String regex = unescapeRegex(text);
        initialRegex = (regex != null);
        initialText = initialRegex ? regex : text;
    }

    @Override
    protected String getValue() {
        String text = editText.getText().toString();
        return checkBox.isChecked() ? escapeRegex(text) : text;
    }

    @NonNull
    protected View createView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.editor_text, null, false);

        editText = view.findViewById(R.id.edit_text);
        editText.setText(initialText);

        checkBox = view.findViewById(R.id.checkbox_regex);
        checkBox.setChecked(initialRegex);

        /* custom message view. do not use setMessage() } */
        TextView messageText = view.findViewById(R.id.dialog_message);
        messageText.setText(R.string.text_fragment);

        return view;
    }

}
