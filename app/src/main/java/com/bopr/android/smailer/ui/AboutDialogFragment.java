package com.bopr.android.smailer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import static com.bopr.android.smailer.util.AndroidUtil.dialogBuilder;
import static com.bopr.android.smailer.util.TagFormatter.formatter;

/**
 * About dialog fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AboutDialogFragment extends DialogFragment {

    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "about_dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);

        TextView versionLabel = view.findViewById(R.id.label_message);
        versionLabel.setText(formatVersion());

        versionLabel.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Settings.BuildInfo info = Settings.getReleaseInfo(getActivity());
                dialogBuilder(getActivity())
                        .setTitle("Release info")
                        .setMessage("Build number: " + info.number + "\nBuild time: " + info.time)
                        .show();
                return true;
            }
        });

        view.findViewById(R.id.label_open_source).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LegalInfoActivity.class));
                dismiss();
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private String formatVersion() {
        return formatter(R.string.title_version, getResources())
                .put("version", Settings.getReleaseVersion(getActivity()))
                .format();
    }

}
