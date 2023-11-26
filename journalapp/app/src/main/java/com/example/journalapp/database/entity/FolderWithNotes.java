package com.example.journalapp.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;

import java.util.List;

public class FolderWithNotes {
    @Embedded
    public Folder folder;
    @Relation(
            parentColumn = "id",
            entityColumn = "folder_id"
    )
    public List<Note> items;

}
