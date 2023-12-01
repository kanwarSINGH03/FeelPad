package com.example.journalapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.journalapp.database.NoteDao;
import com.example.journalapp.database.NoteDatabase;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteFtsEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Tests for NoteDao Queries
 */
@RunWith(AndroidJUnit4.class)
public class NoteDaoTest {
    private NoteDatabase db;
    private NoteDao noteDao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        // Set up database
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase.class).allowMainThreadQueries().build();
        noteDao = db.noteDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    /**
     * Purpose: to test insertion and deletion queries for NoteFtsEntities
     * @throws Exception
     */
    @Test
    public void insertAndDeleteNoteFts() throws Exception {
        // insert new noteFtsEntity
        NoteFtsEntity noteFts = new NoteFtsEntity("noteId", "sample text");
        noteDao.insertNoteFts(noteFts);

        // retrieve inserted noteFtsEntity
        NoteFtsEntity inserted = noteDao.getNoteFtsById("noteId");
        assertNotNull(inserted);
        assertEquals("sample text", inserted.getCombinedText());

        // delete after
        noteDao.deleteNoteFts("noteId");
        NoteFtsEntity deleted = noteDao.getNoteFtsById("noteId");
        assertNull(deleted);
    }

    /**
     * Purpose: to test update query for NoteFtsEntities
     * @throws Exception
     */
    @Test
    public void updateNoteFts() throws Exception {
        // insert new noteFtsEntity with starting text
        NoteFtsEntity noteFts = new NoteFtsEntity("noteId", "initial text");
        noteDao.insertNoteFts(noteFts);

        // create new NoteFtsEntity with different text and see if its updated
        noteDao.updateNoteFts("noteId", "updated text");
        NoteFtsEntity updated = noteDao.getNoteFtsById("noteId");
        assertEquals("updated text", updated.getCombinedText());
    }

    /**
     * Purpose: to test search query for notes by title or content using LiveData
     * @throws Exception
     */
    @Test
    public void searchNotes() throws Exception {
        // insert a test note
        Note note = new Note("Test Title", "Test Date", 0, "FolderID");
        noteDao.insertNote(note);

        // Search for notes containing "Test" in the title or content
        LiveData<List<Note>> searchResultLiveData = noteDao.searchNotes("Test");
        List<Note> searchResults = LiveDataTestUtil.getValue(searchResultLiveData);

        // Verify that the search result is not empty and contains the inserted note
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.contains(note));
    }

}
