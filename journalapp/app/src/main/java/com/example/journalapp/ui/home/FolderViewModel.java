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
    private final LiveData<List<Folder>> allFolders;


    public FolderViewModel(Application application) {
        super(application);
        folderRepository = new FolderRepository(application);
        allFolders = folderRepository.getAllFolders();
    }

    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }

    public void CreateFolder(Folder folder) {
        folderRepository.insertFolder(folder);
    }

    public LiveData<List<Note>> getNotesByFolderId(String folderId) {
        return folderRepository.getNotesByFolderId(folderId);
    }

    public Folder getFolderByIdSync(String folderId){
        return folderRepository.getFolderByIdSync(folderId);
    }

    public void insertFolder(Folder folder){
        folderRepository.insertFolder(folder);
    }

    public LiveData<List<Note>> getAllNotes(){
        return folderRepository.getAllNotes();
    }


}
