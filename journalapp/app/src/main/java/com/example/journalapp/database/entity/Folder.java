package com.example.journalapp.database.entity;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

/**
 * An entity class used to create a Room database table called folder_table
 * Represents a folder's metadata
 * Has a one-to-many relationship with a Note
 */
@Entity(tableName = "folder_table")
public class Folder {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String folderId;

    @ColumnInfo(name = "folder_name")
    private String folderName;

    @ColumnInfo(name = "create_date")
    private String create_date;

    @ColumnInfo(name ="total_emotion_value")
    private int totalEmotionValue;

    @ColumnInfo(name = "num_items")
    private int numItems;

    @ColumnInfo(name = "icon_resource_id")
    private int iconResourceId;

    @ColumnInfo(name = "folder_color")
    private int folderColor;

    @ColumnInfo(name = "last_modified")
    private String lastModified;

    @ColumnInfo(name = "is_encrypted")
    private boolean ifEncrypted;

    @ColumnInfo(name = "password")
    private String password; // stored as an encrypted password

    /**
     * Constructor to create a Folder entity
     * @param folderName String name of the folder
     * @param create_date String date in ISO 8601 format
     * @param iconResourceId int representing a Drawable Resource for the icon
     * @param folderColor int representing a Color Resource for the folder color
     */
    public Folder(String folderName, String create_date, @DrawableRes int iconResourceId, @ColorRes int folderColor) {
        this.folderId = UUID.randomUUID().toString();
        this.folderName = folderName;
        this.create_date = create_date;
        this.iconResourceId = iconResourceId;
        this.folderColor = folderColor;
        this.numItems = 0;
        this.totalEmotionValue = 0;
        this.lastModified = getCreate_date();
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public boolean isIfEncrypted() {
        return ifEncrypted;
    }

    public void setIsEncrypted(boolean isEncrypted) {
        this.ifEncrypted = isEncrypted;
    }

    public float getEmotionPercentage() {
        if (totalEmotionValue == 0 && numItems == 0){
            return 0;
        }
        else{
            float averageEmotion = (float) totalEmotionValue / numItems;

            return (averageEmotion/5) * 100;
        }
    }

    public int getTotalEmotionValue(){
        return this.totalEmotionValue;
    }

    public void setTotalEmotionValue(int totalEmotionValue){
        this.totalEmotionValue = totalEmotionValue;
    }

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public int getFolderColor() {
        return folderColor;
    }

    public void setFolderColor(int folderColor) {
        this.folderColor = folderColor;
    }

    public boolean getEncrypted(){
        return ifEncrypted;
    }

    public void setIfEncrypted(boolean ifEncrypted) {
        this.ifEncrypted = ifEncrypted;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return folderId.equals(folder.folderId) && Objects.equals(folderName,folder.folderName) && Objects.equals(create_date,folder.create_date);
    }
}
