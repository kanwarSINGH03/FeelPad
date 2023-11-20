package com.example.journalapp.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

/**
 * Full-Text-Search (FTS) Table to allow easier searching/filtering queries
 */
@Entity
@Fts4
public class NoteFtsEntity {

    @PrimaryKey
    @ColumnInfo(name = "rowid")
    private int rowid; // required by room to be used as primary key in FTS tables. Must be named rowid

    @ColumnInfo(name = "noteId")
    private String noteId; // used to link FTS entry back to original Note

    @ColumnInfo(name = "combinedText")
    private String combinedText; // Contains all text content added together from TEXT enum types for a given note

    /**
     * Constructor for NoteFtsEntity
     * @param noteId
     * @param combinedText
     */
    public NoteFtsEntity(String noteId, String combinedText){
        this.noteId = noteId;
        this.combinedText = combinedText;
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getCombinedText() {
        return combinedText;
    }

    public void setCombinedText(String combinedText) {
        this.combinedText = combinedText;
    }

    /*
    Use to strip all HTML tags to be in NoteFtsEntity
    public static String stripHtmlTags(String html) {
    if (html == null) {
        return "";
    }
    return html.replaceAll("<[^>]*>", "").trim();
}
     */
}
