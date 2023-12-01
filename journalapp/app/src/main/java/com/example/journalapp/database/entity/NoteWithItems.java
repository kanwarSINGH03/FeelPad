package com.example.journalapp.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.util.List;

/**
 * Serves as a data structure to represent 1-to-many relationship between a Note and NoteItemEntities
 * (SQLite can't store data structures)
 */
public class NoteWithItems {
    @Embedded // indicates that fields of the Note class are to be treated as if they were part of this class
    public Note note;

    @Relation(
            parentColumn = "id",
            entityColumn = "note_id"
    )
    public List<NoteItemEntity> items; // List of note items associated with the note

}
