package com.example.journalapp.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;


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
    private int totalEmotionValue; // sum of all notes emotionValue

    @ColumnInfo(name = "num_items")
    private int numItems; // should be calculate as a sum of all note items
    @ColumnInfo(name = "icon_resource_id")
    private int iconResourceId;
    @ColumnInfo(name = "folder_color")
    private int folderColor;
    @ColumnInfo(name = "last_modified")
    private String lastModified;

    @ColumnInfo(name = "is_encrypted")
    private boolean ifEncrypted;

    @ColumnInfo(name = "encryption_password_hash")
    private String encryptionPasswordHash; // 存储密码的哈希值，而非明文密码

    public Folder(String folderName, String create_date,int iconResourceId, int folderColor) {
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

    public String getEncryptionPasswordHash() {
        return encryptionPasswordHash;
    }

    public void setEncryptionPasswordHash(String encryptionPasswordHash) {
        this.encryptionPasswordHash = encryptionPasswordHash;
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

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return folderId.equals(folder.folderId) && Objects.equals(folderName,folder.folderName) && Objects.equals(create_date,folder.create_date);
    }
}
