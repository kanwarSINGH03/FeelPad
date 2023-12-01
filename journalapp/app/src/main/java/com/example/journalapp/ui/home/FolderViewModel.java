package com.example.journalapp.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.journalapp.database.FolderRepository;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;

import java.util.List;

public class FolderViewModel extends AndroidViewModel {

    private final FolderRepository folderRepository;

    /**
     * Constructor for creating a new FolderViewModel
     * @param application the application Context
     */
    public FolderViewModel(Application application) {
        super(application);
        folderRepository = FolderRepository.getInstance(application);
    }

    /**
     * Insert a new folder into the database
     * @param folder a Folder object to be inserted
     */
    public void insertFolder(Folder folder){
        folderRepository.insertFolder(folder);
    }

    /**
     * Update an existing folder in the database
     * @param folder a Folder object to be inserted
     */
    public void updateFolder(Folder folder){
        folderRepository.updateFolder(folder);
    }
    /**
     * Update an existing folder's title
     * @param providedTitle String date in ISO 8601 format
     * @param folderId String folderId
     */
    public void updateFolderTitle(String providedTitle, String folderId){
        folderRepository.updateFolderTitle(providedTitle, folderId);
    }

    /**
     * Update an existing folder's last edited date timestamp
     * @param folderId String folderId
     * @param timestamp String in ISO 8601 format representing the last edited date
     */
    public void updateFolderTimestamp(String folderId, String timestamp){
        folderRepository.updateFolderTimestamp(folderId, timestamp);
    }

    /**
     * Delete an existing folder.
     * Associated Notes and NoteItems use `CASCADE`, so they will be deleted as well
     * However, associated NoteFtsEntities must be deleted on by one
     * @param folder a Folder object to be deleted
     */
    public void deleteFolder(Folder folder){
        folderRepository.deleteFolder(folder);
    }

    /**
     * Gets a folder synchronously
     * Future is used to "block" until the synchronous operation is finished
     * @param folderId String folderId
     * @return a Folder object retrieved via the id
     */
    public Folder getFolderByIdSync(String folderId){
        return folderRepository.getFolderByIdSync(folderId);
    }

    /**
     * Retrieve all folders in the database asynchronously
     * @return a LiveData list of Folders
     */
    public LiveData<List<Folder>> getAllFolders() {
        return folderRepository.getAllFolders();
    }

    /**
     * Retrieve all notes from a folder in descending edited date order ASYNCHRONOUSLY
     * @param folderId String folderId
     * @return a LiveData list of notes ordered from last edited date descending
     */
    public LiveData<List<Note>> getAllNotesFromFolderOrderByLastEditedDateDesc(String folderId){
        return folderRepository.getAllNotesFromFolderOrderByLastEditedDateDesc(folderId);
    }

    /**
     * Retrieve all notes from a folder in descending edited date order SYNCHRONOUSLY
     * @param folderId String folderId
     * @return a normal List of notes ordered from last edited date descending
     */
    public List<Note> getAllNotesFromFolderOrderByLastEditedDateDescSync(String folderId){
        return folderRepository.getAllNotesFromFolderOrderByLastEditedDateDescSync(folderId);
    }

}
