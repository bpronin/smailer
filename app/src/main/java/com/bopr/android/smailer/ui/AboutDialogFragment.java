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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bopr.android.smailer.BuildInfo;
import com.bopr.android.smailer.R;

/**
 * About dialog fragment.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AboutDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);

        BuildInfo info = new BuildInfo(requireContext());

        TextView versionLabel = view.findViewById(R.id.label_message);
        versionLabel.setText(getString(R.string.app_version, info.name));
        versionLabel.setOnLongClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Release info")
                    .setMessage("Build number: " + info.number + "\nBuild time: " + info.time)
                    .show();
            return true;
        });

        view.findViewById(R.id.label_open_source).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), LegalInfoActivity.class));
            dismiss();
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

    static void showAboutDialog(@NonNull FragmentActivity activity) {
        new AboutDialogFragment().show(activity.getSupportFragmentManager(), "about_dialog");
    }
}
