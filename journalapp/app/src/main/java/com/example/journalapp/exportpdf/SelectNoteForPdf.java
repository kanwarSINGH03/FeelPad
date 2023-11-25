package com.example.journalapp.exportpdf;


import com.example.journalapp.database.entity.Note;

public interface SelectNoteForPdf {
    void unSelectNoteForPdf(Note note);

    void selectNoteForPdf(Note note);
}
