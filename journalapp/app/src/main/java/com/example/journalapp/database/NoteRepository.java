package com.example.journalapp.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.entity.Folder;
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
     * @param application Application context
     */
    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);

        // Initialize the variables
        noteDao = noteDatabase.noteDao();
    }

    /**
     * Get singleton instance of NoteRepository
     * @param application The application.
     * @return A synchronized instance of the NoteRepository
     */
    public static synchronized NoteRepository getInstance(Application application) {
        if (instance == null) {
            instance = new NoteRepository(application);
        }
        return instance;
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
        return noteDao.getNoteById(note_id);
    }

    /**
     * Inserts a Note into the database
     * This also creates a new NoteFtsEntity
     * @param note Note object to be inserted
     */
    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNote(note);
            noteDao.insertNoteFts(new NoteFtsEntity(note.getId(), ""));
        });
    }

    /**
     * Updates the Note title given a Note object
     * @param note Note object to be updated
     */
    public void updateNoteTitle(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteTitle(note.getTitle(), note.getId()));
    }

    /**
     * Updates the Note emotion value given a Note object
     * @param note Note object to be updated
     */
    public void updateNoteEmotion(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteEmotion(note.getEmotion(), note.getId()));
    }

    /**
     * Updates the Note's last edited date given the date String and noteId
     * @param date String date formatted in ISO 8601 format
     * @param noteId String noteId to find associated Note
     */
    public void updateNoteLastEditedDate(String date, String noteId){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.updateNoteLastEditedDate(date, noteId);
        });
    }

    /**
     * Delete a note from the database given the Note object
     * Also deletes the associated NoteFtsEntity
     * @param note Note object to be deleted
     */
    public void deleteNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.deleteNote(note);
            noteDao.deleteNoteFts(note.getId());
        });
    }

    // ==============================
    // <List> Note Query Accessors
    // ==============================

    /**
     * Retrieves all the notes in the note table
     * @return a LiveData object containing all notes
     */
    public LiveData<List<Note>> getAllNotes(){
        return noteDao.getAllNotes();
    }

    /**
     * Retrieve all notes ordered by Last Edited Date in descending order
     * @return a LiveData object containing a list of notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByLastEditedDateDesc() {
        return noteDao.getAllNotesOrderByLastEditedDateDesc();
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
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    /**
     * Updates an existing NoteItemEntity to the database
     * Also synchronizes the NoteFtsEntity after update
     * @param noteItem a NoteItem object that belongs to a Note
     */
    public void updateNoteItem(NoteItemEntity noteItem){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.updateNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    /**
     * Deletes a NoteItemEntity from the database
     * Also synchronizes the NoteFtsEntity after deletion
     * @param noteItem a NoteItem object that belongs to a Note
     */
    public void deleteNoteItem(NoteItemEntity noteItem){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.deleteNoteItem(noteItem);
            synchronizeNoteFts(noteItem.getNoteId());
        });
    }

    public LiveData<NoteItemEntity> getFirstNoteItemByNoteId(String noteId){
        return noteDao.getFirstNoteItemByNoteId(noteId);
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
        return noteDao.getNoteItemsForNote(noteId);
    }

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is SYNCHRONOUS, so it must be done using a background thread.
     * @param noteId String id of the Note for items to be retrieved from
     * @return LiveData containing a list of NoteItemEntity objects.
     */
    public List<NoteItemEntity> getNoteItemsForNoteSync(String noteId){
        return noteDao.getNoteItemsForNoteSync(noteId);
    }

    /**
     * Inserts a full note along with its associated items into the database in a single transaction
     * @param note The Note object to be inserted.
     * @param noteItems The list of NoteItemEntity objects to be inserted.
     */
    public void insertFullNote(Note note, List<NoteItemEntity> noteItems) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertFullNote(note, noteItems);
        });
    }

    // ==============================
    // Single NoteFtsEntity Accessors
    // ==============================

    /**
     * Inserts a NoteFtsEntity into the table
     * @param noteFts NoteFtsEntity object to be inserted
     */
    public void insertNoteFts(NoteFtsEntity noteFts) {
        noteDao.insertNoteFts(noteFts);
    }

    /**
     * Updates a NoteFtsEntity in the table
     * @param noteId String noteId to find the associated Note
     * @param combinedText String all text combined to update NoteFtsEntity with
     */
    public void updateNoteFts(String noteId, String combinedText) {
        noteDao.updateNoteFts(noteId, combinedText);
    }

    /**
     * Deletes a NoteFtsEntity given its noteId
     * @param noteId String noteId to find the associated Note
     */
    public void deleteNoteFts(String noteId) {
        noteDao.deleteNoteFts(noteId);
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
        return noteDao.searchNotes(query);
    }

    // ==============================
    // Internal Methods
    // ==============================

    /**
     * Synchronizes (updates) the NoteFtsEntity with a corresponding Note (given its ID)
     * @param noteId String noteId to identify which NoteFtsEntity to update
     */
    public void synchronizeNoteFts(String noteId){
        NoteDatabase.databaseWriteExecutor.execute(()-> {
            List<NoteItemEntity> noteItems = noteDao.getNoteItemsForNoteSync(noteId);

            // gather only the text components of noteItems
            // strip of HTML tags and add all text together
            String combinedText = addAndCleanText(noteItems);
            noteDao.updateNoteFts(noteId,combinedText);
        });
    }

    /**
     * Takes a list of NoteItemEntities, strips the HTML tags, and concatenates them together
     * @param noteItems a List of NoteItemEntities
     * @return
     */
    private String addAndCleanText(List<NoteItemEntity> noteItems){
        StringBuilder combinedTextBuilder = new StringBuilder();
        for (NoteItemEntity item : noteItems){
            if (item.getType() == ItemTypeConverter.toInteger(NoteItem.ItemType.TEXT)){

                // deal with text contents
                String textContent = ConversionUtil.stripHtmlTags(item.getContent());
                combinedTextBuilder.append(textContent).append(" ");
            }
        }
        Log.e("CombinedText", "Combined text is:" + combinedTextBuilder.toString().trim());
        return combinedTextBuilder.toString().trim();
    }




}