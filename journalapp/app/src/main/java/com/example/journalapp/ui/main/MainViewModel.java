package com.example.journalapp.ui.main;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.util.List;

/**
 * MainViewModel class - manages and provides access to repository concerning general operations
 * Basically responsible for sending data to data layer and retrieving data
 */
public class MainViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    /**
     * Constructor for creating a new MainViewModel
     * @param application the application context
     */
    public MainViewModel(Application application) {
        super(application);
        noteRepository = NoteRepository.getInstance(application);
    }

    // ==============================
    // Single Note Query Accessors
    // ==============================

    /**
     * Delete a note from the database given the Note object
     * Also deletes the associated NoteFtsEntity
     * @param note Note object to be deleted
     */
    public void deleteNote(Note note){
        noteRepository.deleteNote(note);
    }

    public LiveData<NoteItemEntity> getFirstNoteItemByNoteId(String noteId) {
        return noteRepository.getFirstNoteItemByNoteId(noteId);
    }

    // ==============================
    // <List> Note Query Accessors
    // ==============================

    /**
     * Retrieves all the notes in the note table
     * @return a LiveData object containing all notes
     */
    public LiveData<List<Note>> getAllNotes(){
        return noteRepository.getAllNotes();
    }

    /**
     * Retrieve all notes ordered by Last Edited Date in descending order
     * @return a LiveData object containing a list of notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByLastEditedDateDesc() {
        return noteRepository.getAllNotesOrderedByLastEditedDateDesc();
    }

    // ==============================
    // Search Query Accessors
    // ==============================

    /**
     * Searches notes by title/FTS Combined Content
     * @param query String query to search for in database
     * @return a LiveData object representing matching notes
     */
    public LiveData<List<Note>> searchNotes(String query) {
        return noteRepository.searchNotes(query);
    }

    public LiveData<List<Note>> searchNotesInFolder(String folderId, String query){
        return noteRepository.searchNotesInFolder(folderId, query);
    }

    public LiveData<List<Note>> searchNotesAndFilterEmotion(String folderId, String query, int emotion){
        return noteRepository.searchNotesAndFilterEmotion(folderId, query, emotion);
    }

    public LiveData<List<Note>> searchNotesAndFilterDate(String folderId, String query, String startDate, String endDate){
        return noteRepository.searchNotesAndFilterDate(folderId, query, startDate, endDate);
    }

    public LiveData<List<Note>> searchNotesAndFilterEmotionDate(String folderId, String query, int emotion, String startDate, String endDate){
        return noteRepository.searchNotesAndFilterEmotionDate(folderId, query, emotion, startDate, endDate);
    }

}
