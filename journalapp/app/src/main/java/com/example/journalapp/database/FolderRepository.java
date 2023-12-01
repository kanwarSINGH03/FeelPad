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

    public FolderRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        folderDao = noteDatabase.noteDao();
    }

    // Retrieve all folders from the database
    public LiveData<List<Folder>> getAllFolders() {
        return folderDao.getAllFolders();
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

    public void deleteFolder(Folder folder){
        NoteDatabase.databaseWriteExecutor.execute(() ->{
            // Fetch notes synchronously
            List<Note> notesInFolder = folderDao.getNotesByFolderIdSync(folder.getFolderId());

            folderDao.deleteFolder(folder);

            for (Note note : notesInFolder) {
                folderDao.deleteNoteFts(note.getId());
            }

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

    public List<Note> getAllNotesFromFolderOrderByLastEditedDateDescSync(String folderId){
        return folderDao.getAllNotesFromFolderOrderByLastEditedDateDescSync(folderId);
    }

    public LiveData<List<Note>> SearchNotesInFolder(String folderId, String query){
        return folderDao.searchNotesInFolder(folderId, query);
    }

    public void updateFolderTitle(String providedTitle, String folderId){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            folderDao.updateFolderTitle(providedTitle, folderId);
        });
    }

    public void updateFolderTimestamp(String folderId, String timestamp){
        NoteDatabase.databaseWriteExecutor.execute(() -> {
           folderDao.updateFolderTimestamp(folderId, timestamp);
        });
    }
}
