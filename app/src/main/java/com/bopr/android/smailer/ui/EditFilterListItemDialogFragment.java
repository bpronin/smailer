package com.bopr.android.smailer.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Base filter list item editor dialog.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class EditFilterListItemDialogFragment extends DialogFragment {

    private OnClose onClose;
    private int title;

    public void setTitle(int title) {
        this.title = title;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        /* avoiding of disappearing on rotation */
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
            View view = createView();
            dialog = new AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            onClose.onOkClick(getValue());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();

            /* show soft keyboard when dialog is open */
            //noinspection ConstantConditions
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return dialog;
    }

    protected abstract String getValue();

    @NonNull
    protected abstract String createTag();

    @NonNull
    protected abstract View createView();

    void showDialog(FragmentActivity activity) {
        show(activity.getSupportFragmentManager(), createTag());
    }

    void setOnClose(OnClose onClose) {
        this.onClose = onClose;
    }

    public interface OnClose {

        void onOkClick(String result);

    }
}
