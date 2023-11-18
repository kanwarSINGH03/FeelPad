package com.example.journalapp.exportpdf;

import com.example.journalapp.note.Note;

public interface SelectNoteForPdf {
    void unSelectNoteForPdf(Note note);

    void selectNoteForPdf(Note note);
}
