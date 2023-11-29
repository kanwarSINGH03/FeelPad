package com.example.journalapp.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FolderRepository {

    private final NoteDao folderDao;
    private final LiveData<List<Folder>> allFolders;

    public FolderRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        folderDao = noteDatabase.noteDao();
        allFolders = folderDao.getAllFolders();
    }

    // Retrieve all folders from the database
    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }

    // Insert a new folder into the database
    public void insertFolder(Folder folder) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.insertFolder(folder);
        });
    }

    // Update an existing folder in the database
    public void updateFolder(Folder folder) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.updateFolder(folder);
        });
    }

    // Delete a folder from the database
    public void deleteFolder(Folder folder) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.deleteFolder(folder);
        });
    }

    public LiveData<List<Note>> getNotesByFolderId(String folderId) {
        return folderDao.getNotesByFolderId(folderId);
    }

    public Folder getFolderByIdSync(String folderId) {
        Future<Folder> future = NoteDatabase.databaseWriteExecutor.submit(() -> folderDao.getFolderById(folderId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<Note>> getAllNotes(){
        return folderDao.getAllNotes();
    }
    public LiveData<List<Note>> getAllNotesFromFolderOrderByLastEditedDateDesc(String folderId){
        return folderDao.getAllNotesFromFolderOrderByLastEditedDateDesc(folderId);
    }

    public LiveData<List<Note>> SearchNotesInFolder(String folderId, String query){
        return folderDao.searchNotesInFolder(folderId, query);
    }
}
