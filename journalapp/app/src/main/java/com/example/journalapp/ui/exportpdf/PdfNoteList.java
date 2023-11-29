package com.example.journalapp.ui.exportpdf;

import java.util.ArrayList;

public class PdfNoteList<Note> extends ArrayList<Note> {

    public boolean add(Note note) {
        if (!contains(note)) {
            super.add(note);
        }
        return true;
    }
}
