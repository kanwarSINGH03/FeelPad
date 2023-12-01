package com.example.journalapp.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;

import java.util.List;

/**
 * Serves as a data structure to represent 1-to-many relationship between a Folder and Note(s)
 * (SQLite can't store data structures)
 */
public class FolderWithNotes {
    @Embedded
    public Folder folder;
    @Relation(
            parentColumn = "id",
            entityColumn = "folder_id"
    )
    public List<Note> items;

}
