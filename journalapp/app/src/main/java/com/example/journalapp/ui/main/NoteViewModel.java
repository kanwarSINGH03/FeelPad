package com.example.journalapp.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.util.List;

/**
 * ViewModel class - manages and provides access to note data
 * Basically responsible for sending data to data layer and retrieving data
 */
public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    /**
     * Constructor for creating a new NoteViewModel
     *
     * @param application the application context
     */
    public NoteViewModel(Application application) {
        super(application);
        noteRepository = NoteRepository.getInstance(application);
    }

    /**
     * Retrieve LiveData containing a list of all notes in descending order by date
     *
     * @return LiveData containing all notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByCreateDateDesc() {
        return noteRepository.getAllNotesOrderedByCreatedDateDesc();
    }

    /** @TODO Broken function -> Repository -> NoteDao
     * Retrieve LiveData containing a list of notes where title, description, or date, contains
     * the provided string.
     *
     * @param string The provided string
     * @return LiveData containing all notes
     */
    public LiveData<List<Note>> getAllNoteWhereTitleDateDescContains(String string) {
        return noteRepository.getAllNoteWhereTitleDateDescContains(string);
    }

    public LiveData<List<Note>> searchNotes(String string) {
        return noteRepository.searchNotes(string);
    }

    // ========================================
    // New NoteItem Functions
    // ========================================
    /**
     * Inserts a NoteItemEntity into the database.
     *
     * @param noteItem The NoteItemEntity to be inserted.
     */
    public void insertNoteItem(NoteItemEntity noteItem) {
        noteRepository.insertNoteItem(noteItem);
    }

    /**
     * Updates a NoteItemEntity in the database.
     *
     * @param noteItem The NoteItemEntity to be updated.
     */
    public void updateNoteItem(NoteItemEntity noteItem) {
        noteRepository.updateNoteItem(noteItem);
    }

    /**
     * Deletes a NoteItemEntity from the database.
     *
     * @param noteItem The NoteItemEntity to be deleted.
     */
    public void deleteNoteItem(NoteItemEntity noteItem) {
        noteRepository.deleteNoteItem(noteItem);
    }

    /**
     * Retrieves all NoteItemEntity objects for a specific note, ordered by their order index.
     *
     * @param noteId The ID of the note whose items are to be retrieved.
     * @return LiveData containing a list of NoteItemEntity objects.
     */
    public LiveData<List<NoteItemEntity>> getNoteItemsForNote(String noteId) {
        return noteRepository.getNoteItemsForNote(noteId);
    }

    // Method to insert a full note with items
    /**
     * Inserts a full note with its items into the database.
     *
     * @param note The Note object to be inserted.
     * @param noteItems The list of NoteItemEntity objects to be inserted.
     */
    public void insertFullNote(Note note, List<NoteItemEntity> noteItems) {
        noteRepository.insertFullNote(note, noteItems);
    }
}
