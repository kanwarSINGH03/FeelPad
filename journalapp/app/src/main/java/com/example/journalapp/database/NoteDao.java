package com.example.journalapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteFtsEntity;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.database.entity.Folder;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) for Room database operations
 */
@Dao
public interface NoteDao {

    // ==============================
    // Single Note Queries
    // ==============================

    // Retrieves a single Note by its id
    @Query("SELECT * FROM note_table WHERE id = :note_id")
    Note getNoteById(String note_id);

    // Inserts a new note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNote(Note note);

    // Updates the Note Title field
    @Query("UPDATE note_table SET title = :providedTitle WHERE id = :noteId")
    void updateNoteTitle(String providedTitle, String noteId);

    // Updates the Note Emotion value
    @Query("UPDATE note_table SET emotion = :providedEmotion WHERE id = :noteId")
    void updateNoteEmotion(int providedEmotion, String noteId);

    // Updates the last edited date
    @Query("UPDATE note_table SET last_edited_date = :date WHERE id = :noteId")
    void updateNoteLastEditedDate(String date, String noteId);

    // Deletes a note
    @Delete
    void deleteNote(Note note);

    // ==============================
    // <List> Note Queries
    // ==============================

    // Retrieves all notes from the database
    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();

    // Retrieves all notes in order by last edited date
    @Query("SELECT * FROM note_table ORDER BY last_edited_date DESC")
    LiveData<List<Note>> getAllNotesOrderByLastEditedDateDesc();

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId ORDER BY last_edited_date DESC")
    LiveData<List<Note>> getAllNotesFromFolderOrderByLastEditedDateDesc(String folderId);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId ORDER BY last_edited_date DESC")
    List<Note> getAllNotesFromFolderOrderByLastEditedDateDescSync(String folderId);

    /**
     * Retrieve all notes until 30 days ago
     * @param thirtyDaysAgoIso String date in ISO 8601 format to query against
     * @return
     */
    @Query("SELECT * FROM note_table WHERE create_date >= :thirtyDaysAgoIso ORDER BY create_date DESC")
    LiveData<List<Note>> getNotesFromLast30Days(String thirtyDaysAgoIso);

    // ==============================
    // Single NoteItem Queries
    // ==============================

    // Inserts a new NoteItemEntity
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNoteItem(NoteItemEntity noteItem);

    // Updates an existing NoteItemEntity
    @Update
    void updateNoteItem(NoteItemEntity noteItem);

    @Query("UPDATE note_table SET marker_title = :markerLocation WHERE id = :noteId ")
    void updateNoteLocation(String noteId, String markerLocation);
    // Deletes a NoteItemEntity
    @Delete
    void deleteNoteItem(NoteItemEntity noteItem);

    /**
     * Get the first NoteItem of the Note given the noteId
     * Specifically for use for Note ViewHolders
     */
    @Query("SELECT * FROM note_items WHERE note_id = :noteId AND order_index = 0")
    LiveData<NoteItemEntity> getFirstNoteItemByNoteId(String noteId);

    // Update a note's location

    // ==============================
    // <List> NoteItem Queries
    // ==============================

    /**
     * Retrieves all NoteItemEntity that belongs to a Note.
     * This is ASYNCHRONOUS and can be done using the main ui thread.
     */
    @Query("SELECT * FROM note_items WHERE note_id = :noteId ORDER BY order_index")
    LiveData<List<NoteItemEntity>> getNoteItemsForNote(String noteId);

    /**
     * Retrieves all NoteItemEntity that belongs to a Note.
     * This is SYNCHRONOUS so it must be done using a background thread (executorService)
     */
    @Query("SELECT * FROM note_items WHERE note_id = :noteId ORDER BY order_index")
    List<NoteItemEntity> getNoteItemsForNoteSync(String noteId);

    // Inserts a Full Note (Note and its associated items) in a single transaction
    @Transaction
    default void insertFullNote(Note note, List<NoteItemEntity> noteItems) {
        // Insert the note
        insertNote(note);
        // Insert all note items
        for (NoteItemEntity item : noteItems) {
            insertNoteItem(item);
        }
    }

    // ==============================
    // Single NoteFtsEntity Queries
    // ==============================

    // Inserts a NoteFtsEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNoteFts(NoteFtsEntity noteFts);

    /**
     * Updates a NoteFtsEntity with the given noteId
     * Custom query because of the nature of FTS tables and automatic row-id primary key assigning
     */
    @Query("UPDATE NoteFtsEntity SET combinedText = :combinedText WHERE noteId = :noteId")
    void updateNoteFts(String noteId, String combinedText);

    // Deletes a NoteFtsEntity given the noteId
    @Query("DELETE FROM NoteFtsEntity WHERE noteId = :noteId")
    void deleteNoteFts(String noteId);

    // Retrieve a NoteFtsEntity given the noteId
    @Query("SELECT rowid, noteId, combinedText FROM NoteFtsEntity WHERE noteId = :noteId")
    NoteFtsEntity getNoteFtsById(String noteId);


    // ==============================
    // Search Queries
    // ==============================

    /**
     * Regular search for notes using title and note contents using FTS Table
     */
    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)")
    LiveData<List<Note>> searchNotes(String query);

    /**
     * Regular search (inside a specific folder) for notes using title and note contents using FTS Table
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesInFolder(String folderId, String query);

    /**
     * Regular note folder search with a combination of filtering a certain emotion (1-5)
     * @param emotion int from 1-5 represents an emotion
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND emotion = :emotion AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterEmotion(String folderId, String query, int emotion);

    /**
     * Regular note folder search with a combination of filtering between two dates
     * @param startDate String date in ISO 8601 format
     * @param endDate String date in ISO 8601 format
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND create_date BETWEEN :startDate AND :endDate AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterDate(String folderId, String query, String startDate, String endDate);

    /**
     * Regular note folder search with filtering of a certain emotion between two dates
     * @param emotion int from 1-5 represents an emotion
     * @param startDate String date in ISO 8601 format
     * @param endDate String date in ISO 8601 format
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND emotion = :emotion AND create_date BETWEEN :startDate AND :endDate AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterEmotionDate(String folderId, String query, int emotion, String startDate, String endDate);

    // ==============================
    // Single Folder Queries
    // ==============================

    // Inserts a new folder
    @Insert
    void insertFolder(Folder folder);

    // Updates an existing folder
    @Update
    void updateFolder(Folder folder);

    // Update an existing folder's title
    @Query("UPDATE folder_table SET folder_name = :providedTitle WHERE id = :folderId")
    void updateFolderTitle(String providedTitle, String folderId);

    /**
     * Updates an existing folder's last modified timestamp
     * @param lastModified String date in ISO 8601 format
     */
    @Query("UPDATE folder_table SET last_modified = :lastModified WHERE id = :folderId")
    void updateFolderTimestamp(String folderId, String lastModified);

    // Delete an existing folder
    @Delete
    void deleteFolder(Folder folder);

    // Retrieve a folder given the ID
    @Query("SELECT * FROM folder_table WHERE id = :folderId")
    Folder getFolderById(String folderId);

    // ==============================
    // List<Folder> Queries
    // ==============================

    // Retrieve all folders
    @Query("SELECT * FROM folder_table")
    LiveData<List<Folder>> getAllFolders();

    /**
     * Retrieve all notes with a Folder ID asynchronously
     * This is ASYNCHRONOUS and can be done using the main ui thread.
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId")
    LiveData<List<Note>> getNotesByFolderId(String folderId);

    /**
     * Retrieve all notes with a Folder ID synchronously
     * This is SYNCHRONOUS so it must be done using a background thread (executorService)
     */
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId")
    List<Note> getNotesByFolderIdSync(String folderId);

}

