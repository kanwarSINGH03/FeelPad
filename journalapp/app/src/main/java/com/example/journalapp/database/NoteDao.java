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

    // ==============================
    // Single NoteItem Queries
    // ==============================

    // Inserts a new NoteItemEntity
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNoteItem(NoteItemEntity noteItem);

    // Updates an existing NoteItemEntity
    @Update
    void updateNoteItem(NoteItemEntity noteItem);

    // Deletes a NoteItemEntity
    @Delete
    void deleteNoteItem(NoteItemEntity noteItem);

    @Query("SELECT * FROM note_items WHERE note_id = :noteId AND order_index = 0")
    LiveData<NoteItemEntity> getFirstNoteItemByNoteId(String noteId);

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

    // ==============================
    // Search Queries
    // ==============================

    /**
     * Searches notes by title or FTS combined content
     */
    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)")
    LiveData<List<Note>> searchNotes(String query);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesInFolder(String folderId, String query);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND emotion = :emotion AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterEmotion(String folderId, String query, int emotion);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND create_date BETWEEN :startDate AND :endDate AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterDate(String folderId, String query, String startDate, String endDate);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND emotion = :emotion AND create_date BETWEEN :startDate AND :endDate AND (title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)) ORDER BY last_edited_date DESC")
    LiveData<List<Note>> searchNotesAndFilterEmotionDate(String folderId, String query, int emotion, String startDate, String endDate);


    // Retrieves all notes with selected title
    @Query("SELECT * FROM note_table WHERE title = :providedTitle")
    LiveData<List<Note>> getNotesWithTitle(String providedTitle);

    @Query("SELECT rowid, noteId, combinedText FROM NoteFtsEntity WHERE noteId = :noteId")
    NoteFtsEntity getNoteFtsById(String noteId);


    @Query("DELETE FROM note_table")
    void deleteAllNotes();


    // ==============================
    // Folder Queries
    // ==============================

    @Insert
    void insertFolder(Folder folder);

    @Update
    void updateFolder(Folder folder);

    @Delete
    void deleteFolder(Folder folder);

    @Query("UPDATE folder_table SET folder_name = :providedTitle WHERE id = :folderId")
    void updateFolderTitle(String providedTitle, String folderId);

    @Query("SELECT * FROM folder_table")
    LiveData<List<Folder>> getAllFolders();
    @Query("SELECT * FROM folder_table WHERE id = :folderId")
    Folder getFolderById(String folderId);

    @Query("SELECT * FROM note_table WHERE folder_id = :folderId")
    LiveData<List<Note>> getNotesByFolderId(String folderId);
}

