package com.example.journalapp.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteFtsEntity;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.ui.note.NoteItem;
import com.example.journalapp.utils.ConversionUtil;
import com.example.journalapp.utils.ItemTypeConverter;

import java.util.List;

/**
 * Repository class to manage interactions with database
 * Acts as a channel between data source and the ViewModel
 */
public class NoteRepository {

    private static NoteRepository instance;
    private final NoteDao noteDao;

    /**
     * Constructor for creating a new NoteRepository
     *
     * @param application Application context
     */
    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);

        // Initialize the variables
        noteDao = noteDatabase.noteDao();
    }

    /**
     * Get singleton instance of NoteRepository
     *
     * @param application The application.
     * @return A synchronized instance of the NoteRepository
     */
    public static synchronized NoteRepository getInstance(Application application) {
        if (instance == null) {
            instance = new NoteRepository(application);
        }
        return instance;
    }

    /**
     * Inserts a note into the database
     *
     * @param note Note object to be inserted
     */
    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNote(note);
            noteDao.insertNoteFts(new NoteFtsEntity(note.getId(), ""));
        });
    }

    /**
     * Update note title
     *
     * @param note The Note to be updated
     */
    public void updateNoteTitle(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteTitle(note.getTitle(), note.getId()));
    }

    public void updateNoteEmtotion(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteEmotion(note.getEmotion(), note.getId()));
    }

    /**
     * Retrieve all notes using LiveData list of notes. Live
     * data list of notes descending order by date.
     *
     * @return LiveData object containing a list of notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByCreatedDateDesc() {
        return noteDao.getAllNotesOrderByCreatedDateDesc();
    }

    /** @TODO NoteDao operation broken for this. Fix NoteDao first
     * Retrieve all note containing the provided string in their title, description, or date,
     * using LiveData list of notes.
     *
     * @param string The string to search for
     * @return LiveData object containing the list of notes
     */
    public LiveData<List<Note>> getAllNoteWhereTitleDateDescContains(String string) {
        return noteDao.getAllNotesWhereTitleDateDescContains(string);
    }

    /**
     * Get a note by id
     *
     * @param note_id The note id
     * @return The note with note id
     */
    public Note getNoteById(String note_id) {
        return noteDao.getNoteById(note_id);
    }

    /**
     * Delete a note from the database
     *
     * @param note The note to be removed
     */
    public void deleteNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.deleteNote(note);
            noteDao.deleteNoteFts(note.getId());
        });
    }

    // =================================
    // NoteItemEntity Operations
    // =================================

    /**
     * Insert a new NoteItemEntity into the database
     * @param noteItem
     */
    public void insertNoteItem(NoteItemEntity noteItem){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    /**
     * Updates an existing NoteItemEntity to the database
     * @param noteItem
     */
    public void updateNoteItem(NoteItemEntity noteItem){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.updateNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    /**
     * Deletes a NoteItemEntity from the database
     * @param noteItem
     */
    public void deleteNoteItem(NoteItemEntity noteItem){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.deleteNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is asynchronous and can be done using the main ui thread.
     * @param noteId The ID of the note whose items are to be retrieved.
     * @return LiveData containing a list of NoteItemEntity objects.
     */
    public LiveData<List<NoteItemEntity>> getNoteItemsForNote(String noteId) {
        return noteDao.getNoteItemsForNote(noteId);
    }

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is synchronous, so it must be done using a background thread.
     * @param noteId
     * @return
     */
    public List<NoteItemEntity> getNoteItemsForNoteSync(String noteId){
        return noteDao.getNoteItemsForNoteSync(noteId);
    }

    /**
     * Inserts a full note along with its associated items into the database in a single transaction
     *
     * @param note The Note object to be inserted.
     * @param noteItems The list of NoteItemEntity objects to be inserted.
     */
    public void insertFullNote(Note note, List<NoteItemEntity> noteItems) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertFullNote(note, noteItems);
        });
    }

    public void insertNoteFts(NoteFtsEntity noteFts) {
        noteDao.insertNoteFts(noteFts);
    }

    public void updateNoteFts(String noteId, String combinedText) {
        noteDao.updateNoteFts(noteId, combinedText);
    }

    public void deleteNoteFts(String noteId) {
        noteDao.deleteNoteFts(noteId);
    }

    // Method to search notes
    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public void synchronizeNoteFts(String noteId){
        NoteDatabase.databaseWriteExecutor.execute(()-> {
            List<NoteItemEntity> noteItems = noteDao.getNoteItemsForNoteSync(noteId);
            String combinedText = addAndCleanText(noteItems);
            noteDao.updateNoteFts(noteId,combinedText);
        });
    }

    private String addAndCleanText(List<NoteItemEntity> noteItems){
        StringBuilder combinedTextBuilder = new StringBuilder();
        for (NoteItemEntity item : noteItems){
            if (item.getType() == ItemTypeConverter.toInteger(NoteItem.ItemType.TEXT)){
                String textContent = ConversionUtil.stripHtmlTags(item.getContent());
                combinedTextBuilder.append(textContent).append(" ");
            }
        }
        Log.e("CombinedText", "Combined text is:" + combinedTextBuilder.toString().trim());
        return combinedTextBuilder.toString().trim();
    }


}