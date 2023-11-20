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

import java.util.List;

/**
 * Data Access Object (DAO) for Room database operations
 */
@Dao
public interface NoteDao {

    // ==============================
    // Note Queries
    // ==============================

    // Inserts a new note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNote(Note note);

    // Updates the Note Title field
    @Query("UPDATE note_table SET title = :providedTitle WHERE id = :noteId")
    void updateNoteTitle(String providedTitle, String noteId);

    // Updates the Note Emotion value
    @Query("UPDATE note_table SET emotion = :providedEmotion WHERE id = :noteId")
    void updateNoteEmotion(int providedEmotion, String noteId);

    // Deletes a note
    @Delete
    void deleteNote(Note note);

    // Retrieves all notes from the database
    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();

    // Retrieves a single Note by its id
    @Query("SELECT * FROM note_table WHERE id = :note_id")
    Note getNoteById(String note_id);

    /**
     * Retrieves all notes from the database ordered by created date
     * descending
     *
     * @return LiveData list of all notes descending order by date
     */
    @Query("SELECT * FROM note_table ORDER BY create_date DESC")
    LiveData<List<Note>> getAllNotesOrderByCreatedDateDesc();

    /** @TODO removed Description. Must be fixed to check all EditTexts
     * Select a distinct record from the database that contains the provided string in the
     * title, description, or date.
     *
     * @param string The string to search for
     * @return LiveData list of all notes
     */
    @Query("SELECT DISTINCT * FROM note_table " +
            "WHERE title LIKE '%' || :string || '%' " +
            "OR create_date LIKE '%' || :string || '%'")
    LiveData<List<Note>> getAllNotesWhereTitleDateDescContains(String string);

    /**
     * Retrieves notes with a specific title from the database
     *
     * @param providedTitle String Title to search for
     * @return a LiveData list of notes with the specified title
     */
    @Query("SELECT * FROM note_table WHERE title = :providedTitle")
    LiveData<List<Note>> getNotesWithTitle(String providedTitle);

    // ==============================
    // Normal Note CRUD
    // ==============================



    // ==============================
    // Normal NoteItem CRUD
    // ==============================

    /**
     * Inserts a new NoteItemEntity into the database
     * @param noteItem
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNoteItem(NoteItemEntity noteItem);

    /**
     * Updates an existing NoteItemEntity in the database
     * @param noteItem
     */
    @Update
    void updateNoteItem(NoteItemEntity noteItem);

    /**
     * Deletes a NoteItemEntity from the datbabase
     * @param noteItem
     */
    @Delete
    void deleteNoteItem(NoteItemEntity noteItem);

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is asynchronous and can be done using the main ui thread.
     * @param noteId
     * @return
     */
    @Query("SELECT * FROM note_items WHERE note_id = :noteId ORDER BY order_index")
    LiveData<List<NoteItemEntity>> getNoteItemsForNote(String noteId);

    /**
     * Retrieves all NoteItemEntity objects for a specific note ordered by the order_index
     * This is synchronous, so it must be done using a background thread.
     * @param noteId
     * @return
     */
    @Query("SELECT * FROM note_items WHERE note_id = :noteId ORDER BY order_index")
    List<NoteItemEntity> getNoteItemsForNoteSync(String noteId);

    /**
     * Inserts a full note along with its associated items into the database in a single transaction
     * @param note
     * @param noteItems
     */
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
    // Normal NoteFts CRUD
    // ==============================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNoteFts(NoteFtsEntity noteFts);

    @Query("UPDATE NoteFtsEntity SET combinedText = :combinedText WHERE noteId = :noteId")
    void updateNoteFts(String noteId, String combinedText);

    @Query("DELETE FROM NoteFtsEntity WHERE noteId = :noteId")
    void deleteNoteFts(String noteId);

    // ==============================
    // NoteFts Search Queries
    // ==============================

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :query || '%' OR id IN (SELECT noteId FROM NoteFtsEntity WHERE NoteFtsEntity MATCH :query)")
    LiveData<List<Note>> searchNotes(String query);
}
