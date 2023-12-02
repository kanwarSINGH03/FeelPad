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
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteFtsEntity;
import com.example.journalapp.utils.ConversionUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FilterTest {

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
    public void filterNotesInFolderEmotion() throws Exception {

        int emotion = 0;
        Folder folder = new Folder("epicFolder", ConversionUtil.getDateAsString(), 15, 15);
        noteDao.insertFolder(folder);
        String folderId = folder.getFolderId();
        Note note = new Note("Test Title", "Test Date", emotion, folderId);
        noteDao.insertNote(note);

        // Search for notes containing "Test" in the title or content
        LiveData<List<Note>> searchResultLiveData = noteDao.searchNotesAndFilterEmotion(folderId, "", emotion);
        List<Note> searchResults = LiveDataTestUtil.getValue(searchResultLiveData);

        // Verify that the search result is not empty and contains the inserted note
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.contains(note));
    }
    @Test
    public void filterNoteInFolderDate() throws Exception {
        int emotion = 0;
        Folder folder = new Folder("epicFolder", ConversionUtil.getDateAsString(), 15, 15);
        noteDao.insertFolder(folder);
        String folderId = folder.getFolderId();
        Note note = new Note("Test Title", ConversionUtil.getDateAsString(), emotion, folderId);
        noteDao.insertNote(note);

        // Search for notes containing "Test" in the title or content
        LiveData<List<Note>> searchResultLiveData = noteDao.searchNotesAndFilterDate(folderId, "", ConversionUtil.getDateAsString(), ConversionUtil.getDateAsString());
        List<Note> searchResults = LiveDataTestUtil.getValue(searchResultLiveData);

        // Verify that the search result is not empty and contains the inserted note
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.contains(note));
    }



}
