package com.example.journalapp.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class to manage interactions with database
 * Acts as a channel between data source and the ViewModel
 */
public class FolderRepository {

    private static FolderRepository instance;
    private final NoteDao folderDao;

    /**
     * Constructor for creating  a new FolderRepository
     * @param application Application context
     */
    public FolderRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        folderDao = noteDatabase.noteDao();
    }

    /**
     * Get singleton instance of NoteRepository
     * @param application The application.
     * @return A synchronized instance of the NoteRepository
     */
    public static synchronized FolderRepository getInstance(Application application) {
        if (instance == null) {
            instance = new FolderRepository(application);
        }
        return instance;
    }

    /**
     * Insert a new folder into the database
     * @param folder a Folder object to be inserted
     */
    public void insertFolder(Folder folder) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.insertFolder(folder);
        });
    }

    /**
     * Update an existing folder in the database
     * @param folder a Folder object to be inserted
     */
    public void updateFolder(Folder folder) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.updateFolder(folder);
        });
    }

    /**
     * Update an existing folder's title
     * @param providedTitle String date in ISO 8601 format
     * @param folderId String folderId
     */
    public void updateFolderTitle(String providedTitle, String folderId){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.updateFolderTitle(providedTitle, folderId);
        });
    }

    /**
     * Update an existing folder's last edited date timestamp
     * @param folderId String folderId
     * @param timestamp String in ISO 8601 format representing the last edited date
     */
    public void updateFolderTimestamp(String folderId, String timestamp){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.updateFolderTimestamp(folderId, timestamp);
        });
    }

    /**
     * Delete an existing folder.
     * Associated Notes and NoteItems use `CASCADE`, so they will be deleted as well
     * However, associated NoteFtsEntities must be deleted on by one
     * @param folder a Folder obejct to be deleted
     */
    public void deleteFolder(Folder folder){
        NoteDatabase.databaseWriteExecutor.execute(() ->{

            // fetch notes in folder synchronously
            List<Note> notesInFolder = folderDao.getNotesByFolderIdSync(folder.getFolderId());

            folderDao.deleteFolder(folder);

            // for all notes, delete the associated NoteFtsEntity
            for (Note note : notesInFolder) {
                folderDao.deleteNoteFts(note.getId());
            }

        });
    }

    /**
     * Gets a folder synchronously
     * Future is used to "block" until the synchronous operation is finished
     * @param folderId String folderId
     * @return a Folder object retrieved via the id
     */
    public Folder getFolderByIdSync(String folderId) {
        Future<Folder> future = NoteDatabase.databaseWriteExecutor.submit(() -> folderDao.getFolderById(folderId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve all folders in the database asynchronously
     * @return a LiveData list of Folders
     */
    public LiveData<List<Folder>> getAllFolders() {
        return folderDao.getAllFolders();
    }

    /**
     * Get all notes from a folder using a folderId
     * @param folderId String folderId
     * @return a LiveData list of Notes
     */
    public LiveData<List<Note>> getNotesByFolderId(String folderId) {
        return folderDao.getNotesByFolderId(folderId);
    }

    /**
     * Retrieve all notes from a folder in descending edited date order ASYNCHRONOUSLY
     * @param folderId String folderId
     * @return a LiveData list of notes ordered from last edited date descending
     */
    public LiveData<List<Note>> getAllNotesFromFolderOrderByLastEditedDateDesc(String folderId){
        return folderDao.getAllNotesFromFolderOrderByLastEditedDateDesc(folderId);
    }

    /**
     * Retrieve all notes from a folder in descending edited date order SYNCHRONOUSLY
     * @param folderId String folderId
     * @return a normal List of notes ordered from last edited date descending
     */
    public List<Note> getAllNotesFromFolderOrderByLastEditedDateDescSync(String folderId){
        return folderDao.getAllNotesFromFolderOrderByLastEditedDateDescSync(folderId);
    }

    /**
     * Regular search for notes using title and combined NoteItems using FTSEntity
     * @param folderId String folderId
     * @param query String query to search for
     * @return
     */
    public LiveData<List<Note>> SearchNotesInFolder(String folderId, String query){
        return folderDao.searchNotesInFolder(folderId, query);
    }

}
