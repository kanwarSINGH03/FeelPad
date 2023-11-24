package com.example.journalapp.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

/**
 * An entity class used to create a Room database table called note_table
 * Represents an individual notes' metadata
 */
@Entity(tableName = "note_table")
public class  Note {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "create_date")
    private String createdDate;

    @ColumnInfo(name = "last_edited_date")
    private String lastEditedDate;

    @ColumnInfo(name = "emotion")
    private int emotion; // This will hold an int value 1-5


    /**
     * Constructor to create a new Note instance
     *
     * @param title       String representing title of the note
     * @param createdDate String representing when the note was created
     */
    public Note(String title, String createdDate, int emotion) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.createdDate = createdDate;
        this.emotion = emotion;
        this.lastEditedDate = createdDate; // when creating a new Note, lastEditedDate will be same as creation, but can be updated
    }



    /**
     * Getter for unique ID of a note
     *
     * @return integer ID of a note
     */

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public int getEmotion(){ return emotion;}

    public void setEmotion(int emotion){ this.emotion = emotion; }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createDate) {
        this.createdDate = createDate;
    }

    public String getLastEditedDate(){
        return lastEditedDate;
    }

    public void setLastEditedDate(String lastEditedDate){
        this.lastEditedDate = lastEditedDate;
    }


    /**
     * Indicates if two notes are equal
     *
     * @param o The object to compare with this instance
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id.equals(note.id) && Objects.equals(title, note.title) && Objects.equals(createdDate, note.createdDate);
    }
}
