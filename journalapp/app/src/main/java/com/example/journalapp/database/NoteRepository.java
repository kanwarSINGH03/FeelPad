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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

            // update folder vars
            Folder folder = noteDao.getFolderById(note.getFolderId());
            if (folder != null){
                folder.setNumItems(folder.getNumItems() + 1);
                folder.setTotalEmotionValue(folder.getTotalEmotionValue() + note.getEmotion());

                noteDao.updateFolder(folder);
            }
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
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            // First, get the current state of the note to obtain the old emotion value
            Note currentNote = noteDao.getNoteById(note.getId());
            if (currentNote != null) {
                int oldEmotionValue = currentNote.getEmotion();

                // Update the note's emotion in the database
                noteDao.updateNoteEmotion(note.getEmotion(), note.getId());

                // Retrieve the folder associated with this note
                Folder folder = noteDao.getFolderById(currentNote.getFolderId());
                if (folder != null) {
                    // Adjust the folder's total emotion value
                    int newTotalEmotion = folder.getTotalEmotionValue() - oldEmotionValue + note.getEmotion();
                    folder.setTotalEmotionValue(newTotalEmotion);

                    // Update the folder in the database
                    noteDao.updateFolder(folder);
                }
            }
        });
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
     * Update a Note's location variable using a NoteId
     * @param noteId String noteId
     * @param markerLocation String representing the location of a Note
     */
    public void updateNoteLocation(String noteId, String markerLocation){
        NoteDatabase.databaseWriteExecutor.execute(() ->{
            noteDao.updateNoteLocation(noteId, markerLocation);
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

            // update folder vars
            Folder folder = noteDao.getFolderById(note.getFolderId());
            if (folder != null){
                folder.setNumItems(folder.getNumItems() - 1);
                folder.setTotalEmotionValue(folder.getTotalEmotionValue() - note.getEmotion());

                noteDao.updateFolder(folder);
            }
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

    /**
     * Retrieve all notes from the last 30 days using a separate function to retrieve the string
     * for the date 30 days ago
     * @return a LiveData list containing notes only from the last 30 days
     */
    public LiveData<List<Note>> getNotesFromLast30Days(){
        return noteDao.getNotesFromLast30Days(getThirtyDaysAgo());
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

    /**
     * Retrieve the first NoteItem given a Note's id
     * @param noteId String noteId
     * @return LiveData containing a single NoteItemEntity
     */
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

    /**
     * Regular search (inside a specific folder) for notes using title and note contents using FTS Table
     * @param folderId String folderId
     * @param query String query that the user is searching for
     * @return a LiveData list of notes matching the search
     */
    public LiveData<List<Note>> searchNotesInFolder(String folderId, String query){
        return noteDao.searchNotesInFolder(folderId, query);
    }

    /**
     * Regular note folder search with a combination of filtering a certain emotion (1-5)
     * @param folderId String folderId
     * @param query String query that the user is searching for
     * @param emotion int from 1-5 representing the emotion of to filter for
     * @return a LiveData list of notes matching the search and emotion filter
     */
    public LiveData<List<Note>> searchNotesAndFilterEmotion(String folderId, String query, int emotion){
        return noteDao.searchNotesAndFilterEmotion(folderId, query, emotion);
    }

    /**
     * Regular note folder search with a combination of filtering between two dates
     * @param folderId String folderId
     * @param query String query that the user is searching for
     * @param startDate String date in ISO 8601 format
     * @param endDate String date in ISO 8601 format
     * @return a LiveData list of notes matching the search and date filter
     */
    public LiveData<List<Note>> searchNotesAndFilterDate(String folderId, String query, String startDate, String endDate){
        return noteDao.searchNotesAndFilterDate(folderId, query, startDate, endDate);
    }

    /**
     * Regular note folder search with filtering of a certain emotion between two dates
     * @param folderId String folderId
     * @param query String query that the user is searching for
     * @param emotion int from 1-5 representing the emotion of to filter for
     * @param startDate String date in the ISO 8601 format
     * @param endDate String date in the ISO 8601 format
     * @return
     */
    public LiveData<List<Note>> searchNotesAndFilterEmotionDate(String folderId, String query, int emotion, String startDate, String endDate){
        return noteDao.searchNotesAndFilterEmotionDate(folderId, query, emotion, startDate, endDate);
    }

    // ==============================
    // Internal Methods
    // ==============================

    /**
     * A function to get the string for the 30th day back
     * @return a String date in ISO 8601 format representing the day 30 days ago from today
     */
    private String getThirtyDaysAgo(){
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // this is ISO 8601
        iso8601Format.setTimeZone(TimeZone.getTimeZone("CST"));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30); // today - 30
        String thirtyDaysAgoIso = iso8601Format.format(calendar.getTime());

        return thirtyDaysAgoIso;
    }

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