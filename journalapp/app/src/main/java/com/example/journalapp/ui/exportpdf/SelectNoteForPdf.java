package com.example.journalapp.ui.exportpdf;


import com.example.journalapp.database.entity.Note;

/**
 * Interface for handling behaviours associated with selecting and unselecting a notes box in a
 * checked box icon
 */
public interface SelectNoteForPdf {

    /**
     * The method for handling when a note is unselected
     *
     * @param note The unselected Note
     */
    void unSelectNoteForPdf(Note note);

    /**
     * The method for handling when a note is selected
     *
     * @param note The selected Note
     */
    void selectNoteForPdf(Note note);
}
