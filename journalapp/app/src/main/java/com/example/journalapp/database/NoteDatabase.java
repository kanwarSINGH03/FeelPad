package com.example.journalapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteFtsEntity;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Room Database storing notes
 * Note: entities section indicates database will have 1 table (represented inside Note.class)
 */
@Database(entities = {Note.class, NoteItemEntity.class, NoteFtsEntity.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao noteDao(); // provides access to the NoteDao database operations

    private static volatile NoteDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // Performs database operations using background threads
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Retrieves an instance of the NoteDatabase / creates it if it does not exist
     *
     * @param context application context
     * @return an instance of NoteDatabase
     */
    public static NoteDatabase getNoteDatabase(final Context context) {

        // to ensure only 1 instance of the database is created
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            NoteDatabase.class, "note_database").build(); // creates instance
                }
            }
        }
        return INSTANCE; // once created, is stored in INSTANCE variable
    }
}
