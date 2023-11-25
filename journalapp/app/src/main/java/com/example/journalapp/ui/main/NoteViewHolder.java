package com.example.journalapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

/**
 * ViewHolder class for displaying individual notes inside a RecyclerView (preview)
 * One ViewHolder is created for each item that can be displayed and is reused, each time
 * calling a Binding function.
 */
public class NoteViewHolder extends RecyclerView.ViewHolder {

    private final TextView noteViewTitle;

    /**
     * Constructor for NoteViewHolder
     *
     * @param itemView View representing each Note item in the RecyclerView
     */
    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        noteViewTitle = itemView.findViewById(R.id.noteTitle);
    }

    /**
     * Binds data to the ViewHolder
     *
     * @param title title of the note to display
     */
    public void bind(String title) {
        noteViewTitle.setText(title);
        // noteViewDescription.setText(description); @TODO Part of ViewHolder problem. Show part of Recyclerview?
    }

    /**
     * Static factory method to create a new NoteViewHolder
     * Static Method Called --> Find XML file and inflate --> use inflated view in Constructor
     *
     * @param parent The parent ViewGroup (list) that the ViewHolder (list item) will be attached to
     * @return A new NoteViewHolder instance
     */
    public static NoteViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_cell, parent, false);
        return new NoteViewHolder(view);
    }

}
