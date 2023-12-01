package com.example.journalapp.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.journalapp.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment to allow user to type their preferred name
 * Specifically used to create & update name in HomeActivity
 */
public class NameDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("What name would you like to be called by?");

        // inflate layout
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_name, null);
        builder.setView(dialogView);

        TextInputEditText editTextName = dialogView.findViewById(R.id.edit_text_name);

        // set up buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = editTextName.getText().toString();
            NameDialogListener listener = (NameDialogListener) getActivity();
            listener.onNameEntered(name);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    /**
     * Interface used for Callback to attached activity
     */
    public interface NameDialogListener {
        void onNameEntered(String name);
    }

}
