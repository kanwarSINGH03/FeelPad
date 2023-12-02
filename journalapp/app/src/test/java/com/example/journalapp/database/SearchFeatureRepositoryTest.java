package com.example.journalapp.database;

import androidx.test.core.app.ApplicationProvider;

import com.example.journalapp.database.entity.NoteFtsEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

import android.app.Application;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit Tests for Repository related to Search Function.
 * Must be ran one at a time due to single thread
 * Makes sure that Repository is having the correct access to the NoteDao Queries
 */
@RunWith(RobolectricTestRunner.class)
public class SearchFeatureRepositoryTest {
    private NoteRepository repository;
    private NoteDao noteDao;
    private NoteDatabase database;
    ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() {
        // ApplicationContext from Robolectric Library (simulates app environment)
        Application application = ApplicationProvider.getApplicationContext();
        repository = NoteRepository.getInstance(application);

        // Set up database and Dao
        database = NoteDatabase.getNoteDatabase(RuntimeEnvironment.application);
        noteDao = database.noteDao();
    }

    @After
    public void closeDb() {
        database.close();
    }

    /**
     * Purpose: Test the insertion of a NoteFtsEntity into the repository and verify its insertion
     * @throws InterruptedException
     */
    @Test
    public void testInsertNoteFts() throws InterruptedException {

        // insert NoteFtsEntity async
        NoteFtsEntity noteFts = new NoteFtsEntity("noteId", "sample text");
        CountDownLatch latch = new CountDownLatch(1);

        databaseExecutor.execute(() -> {
            repository.insertNoteFts(noteFts);
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS)); // after operation is done

        // verify succesful insertion
        databaseExecutor.execute(() ->{
            NoteFtsEntity inserted = noteDao.getNoteFtsById("noteId");
            assertNotNull(inserted);
            assertEquals("sample text", inserted.getCombinedText());
        });


    }

    /**
     * Purpose: Test the update of a NoteFtsEntity in the repository and verify the update
     * @throws InterruptedException
     */
    @Test
    public void testUpdateNoteFts() throws InterruptedException {
        // initial vars
        String noteId = "noteId";
        String initialText = "initial text";
        String updatedText = "updated text";
        NoteFtsEntity noteFts = new NoteFtsEntity(noteId, initialText);

        // insert NoteFtsEntity async
        databaseExecutor.execute(() -> {
            repository.insertNoteFts(noteFts);
        });

        // update entity async
        CountDownLatch updateLatch = new CountDownLatch(1);
        databaseExecutor.execute(() -> {
            repository.updateNoteFts(noteId, updatedText);
            updateLatch.countDown();
        });

        assertTrue(updateLatch.await(1, TimeUnit.SECONDS)); // after operation is done

        // verify succesful update
        databaseExecutor.execute(() -> {
            NoteFtsEntity updated = noteDao.getNoteFtsById(noteId);
            assertNotNull(updated);
            assertEquals(updatedText, updated.getCombinedText());
        });
    }

    /**
     * Purpose: test deletion of NoteFtsEntity from repository and verify deletion
     * @throws InterruptedException
     */
    @Test
    public void testDeleteNoteFts() throws InterruptedException {
        String noteId = "noteId";
        NoteFtsEntity noteFts = new NoteFtsEntity(noteId, "sample text");

        // insert NoteFtsEntity async
        databaseExecutor.execute(() -> {
            repository.insertNoteFts(noteFts);
        });

        // delete the entity async
        CountDownLatch deleteLatch = new CountDownLatch(1);
        databaseExecutor.execute(() -> {
            repository.deleteNoteFts(noteId);
            deleteLatch.countDown();
        });

        assertTrue(deleteLatch.await(1, TimeUnit.SECONDS)); // wait for operations to finish

        // verify succesful deletion
        databaseExecutor.execute(() -> {
            NoteFtsEntity deleted = noteDao.getNoteFtsById(noteId);
            assertNull(deleted);
        });
    }
}