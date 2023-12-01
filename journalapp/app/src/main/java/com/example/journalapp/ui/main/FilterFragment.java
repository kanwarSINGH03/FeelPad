package com.example.journalapp.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.journalapp.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class FilterFragment extends DialogFragment {

    // UI Components
    private MaterialDatePicker<Long> startDatePicker;
    private MaterialDatePicker<Long> endDatePicker;
    private Spinner emotionSpinner;
    private Button confirmButton;
    private TextView startDateTextView;
    private TextView endDateTextView;

    // Listener
    private PopupDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PopupDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PopupDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // -- Set up AlertDialog -- //
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_filter, null);
        builder.setView(view);

        // initialize Material Date Pickers
        MaterialDatePicker.Builder<Long> startDateBuilder = MaterialDatePicker.Builder.datePicker();
        startDateBuilder.setTitleText("Select Start Date");
        startDatePicker = startDateBuilder.build();

        MaterialDatePicker.Builder<Long> endDateBuilder = MaterialDatePicker.Builder.datePicker();
        endDateBuilder.setTitleText("Select End Date");
        endDatePicker = endDateBuilder.build();

        // initialize TextViews for displaying selected dates
        startDateTextView = view.findViewById(R.id.fragment_filter_start_date_text);
        endDateTextView = view.findViewById(R.id.fragment_filter_end_date_text);

        // setup emotionSpinner
        emotionSpinner = view.findViewById(R.id.emotion_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.emotion_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emotionSpinner.setAdapter(adapter);

        // setup buttons to show date pickers
        Button startDateButton = view.findViewById(R.id.fragment_filter_start_date_button);
        startDateButton.setOnClickListener(v -> {
            startDatePicker.show(getParentFragmentManager(), "start_date_picker");
        });

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            // adjust the timestamp for the time zone before displaying
            Date date = new Date(selection + TimeUnit.HOURS.toMillis(12));
            startDateTextView.setText(DateFormat.getDateInstance().format(date));
        });

        Button endDateButton = view.findViewById(R.id.fragment_filter_end_date_button);
        endDateButton.setOnClickListener(v -> {
            endDatePicker.show(getParentFragmentManager(), "end_date_picker");
        });

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            // adjust the timestamp for the time zone before displaying
            Date date = new Date(selection + TimeUnit.HOURS.toMillis(12));
            endDateTextView.setText(DateFormat.getDateInstance().format(date));
        });

        // confirm button logic
        confirmButton = view.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            Long startDate = startDatePicker.getSelection();
            Long endDate = endDatePicker.getSelection();
            String emotion = emotionSpinner.getSelectedItem().toString();
            listener.onConfirmButtonClick(startDate, endDate, emotion);
            dismiss();
        });

        return builder.create();
    }

    // Interface for sending data back to the activity
    public interface PopupDialogListener {
        void onConfirmButtonClick(Long startDate, Long endDate, String emotion);
    }
}

