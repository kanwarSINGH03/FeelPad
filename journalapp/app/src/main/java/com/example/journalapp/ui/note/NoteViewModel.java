package com.example.journalapp.ui.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.util.List;

/**
 * NoteViewModel class - manages and provides access to repository concerning note operations
 * Basically responsible for sending data to data layer and retrieving data
 * Keeps temporary data especially useful for when phone rotates
 */
public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    /**
     * Constructor for creating a new NoteViewModel
     * @param application
     */
    public NoteViewModel(Application application){
        super(application);
        noteRepository = NoteRepository.getInstance(application);
    }

    // ==============================
    // Single Note Query Accessors
    // ==============================

    /**
     * Get a Note object using its noteId
     * @param note_id String note_id
     * @return The Note object with corresponding noteId
     */
    public Note getNoteById(String note_id) {
        return noteRepository.getNoteById(note_id);
    }

    /**
     * Inserts a Note into the database
     * This also creates a new NoteFtsEntity
     * @param note Note object to be inserted
     */
    public void insertNote(Note note){
        noteRepository.insertNote(note);
    }

    /**
     * Updates the Note title given a Note object
     * @param note Note object to be updated
     */
    public void updateNoteTitle(Note note){
        noteRepository.updateNoteTitle(note);
    }

    /**
     * Updates the Note emotion value given a Note object
     * @param note Note object to be updated
     */
    public void updateNoteEmotion(Note note){
        noteRepository.updateNoteEmotion(note);
    }

    /**
     * Updates the Note's last edited date given the date String and noteId
     * @param date String date formatted in ISO 8601 format
     * @param noteId String noteId to find associated Note
     */
    public void updateNoteLastEditedDate(String date, String noteId){
        noteRepository.updateNoteLastEditedDate(date, noteId);
    }

    /**
     * Delete a note from the database given the Note object
     * Also deletes the associated NoteFtsEntity
     * @param note Note object to be deleted
     */
    public void deleteNote(Note note){
        noteRepository.deleteNote(note);
    }

    // =================================
    // Single NoteItemEntity Accessors
    // =================================

    /**
     * Inserts a new NoteItemEntity into the database
     * Also synchronizes the NoteFtsEntity after insertion
     * @param noteItem a NoteItem object that belongs to a Note
     */
    public void insertNoteItem(NoteItemEntity noteItem){
        noteRepository.insertNoteItem(noteItem);
    }

    /**
     * Updates an existing NoteItemEntity to the database
     * Also synchronizes the NoteFtsEntity after update
     * @param noteItem a NoteItem object that belongs to a Note
     */
    public void updateNoteItem(NoteItemEntity noteItem){
        noteRepository.updateNoteItem(noteItem);
    }

    /**
     * Deletes a NoteItemEntity from the database
     * Also synchronizes the NoteFtsEntity after deletion
     * @param noteItem a NoteItem object that belongs to a Note
     */
    public void deleteNoteItem(NoteItemEntity noteItem){
        noteRepository.deleteNoteItem(noteItem);
    }

    // ==============================
    // <List> NoteItem Accessors
    // ==============================

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is ASYNCHRONOUS and can be done using the main ui thread.
     * @param noteId String id of the Note for items to be retrieved from
     * @return LiveData containing a list of NoteItemEntity objects.
     */
    public LiveData<List<NoteItemEntity>> getNoteItemsForNote(String noteId) {
        return noteRepository.getNoteItemsForNote(noteId);
    }

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is SYNCHRONOUS, so it must be done using a background thread.
     * @param noteId String id of the Note for items to be retrieved from
     * @return LiveData containing a list of NoteItemEntity objects.
     */
    public List<NoteItemEntity> getNoteItemsForNoteSync(String noteId){
        return noteRepository.getNoteItemsForNoteSync(noteId);
    }

    public void updateNoteLocation(String noteId, String markerLocation){
        noteRepository.updateNoteLocation(noteId, markerLocation);
    }


}
