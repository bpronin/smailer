package com.bopr.android.smailer.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.TagFormatter;

/**
 * Class AboutDialogFragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AboutDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);

        ((TextView) view.findViewById(R.id.label_message)).setText(formatVersion());
        view.findViewById(R.id.label_open_source).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LegalInfoActivity.class));
                dismiss();
            }
        });

        return view;
    }

    private String formatVersion() {
        return TagFormatter.from(R.string.about_dialog_title_version, getResources())
                .put("version", Settings.getReleaseVersion(getActivity()))
                .format();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}
