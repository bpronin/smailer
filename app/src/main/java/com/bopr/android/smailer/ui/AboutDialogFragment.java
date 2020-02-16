package com.bopr.android.smailer.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.util.TagFormatter;

/**
 * About dialog fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AboutDialogFragment extends DialogFragment {

    private Settings settings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);

        TextView versionLabel = view.findViewById(R.id.label_message);
        versionLabel.setText(formatVersion());

        versionLabel.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Settings.BuildInfo info = settings.getReleaseInfo();
                new AlertDialog.Builder(requireContext())
                        .setTitle("Release info")
                        .setMessage("Build number: " + info.getNumber() + "\nBuild time: " + info.getTime())
                        .show();
                return true;
            }
        });

        view.findViewById(R.id.label_open_source).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), LegalInfoActivity.class));
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
        return new TagFormatter(requireContext())
                .pattern(R.string.app_version)
                .put("version", settings.getReleaseVersion())
                .format();
    }

    static void showAboutDialog(@NonNull FragmentActivity activity) {
        new AboutDialogFragment().show(activity.getSupportFragmentManager(), "about_dialog");
    }
}
