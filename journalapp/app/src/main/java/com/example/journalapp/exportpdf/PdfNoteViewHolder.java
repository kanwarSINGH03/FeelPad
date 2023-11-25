package com.example.journalapp.exportpdf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Note;

/**
 * The PdfNoteViewHolder
 */
public class PdfNoteViewHolder extends RecyclerView.ViewHolder {
    private final TextView selectNoteViewTitle;
    private final TextView selectNoteViewDate;
    private final CheckBox checkBox;


    public PdfNoteViewHolder(@NonNull View itemView) {
        super(itemView);
        selectNoteViewTitle = itemView.findViewById(R.id.noteSelectTitle);
        selectNoteViewDate = itemView.findViewById(R.id.noteSelectDate);
        checkBox = itemView.findViewById(R.id.noteCheckBox);
    }

    public void bind(Note note, SelectNoteForPdf selectNoteForPdf) {
        selectNoteViewTitle.setText(note.getTitle());
        selectNoteViewDate.setText(note.getCreatedDate());
        checkBox.setOnCheckedChangeListener((compoundButton, isChanged) -> {
            if (isChanged) {
                selectNoteForPdf.selectNoteForPdf(note);
            } else {
                selectNoteForPdf.unSelectNoteForPdf(note);
            }
        });
    }

    public static PdfNoteViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_note_cell, parent, false);
        return new PdfNoteViewHolder(view);
    }
}
