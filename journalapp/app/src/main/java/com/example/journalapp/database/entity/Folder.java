package com.example.journalapp.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    @ColumnInfo(name = "emotion_percentage")
    private float emotionPercentage;

    @ColumnInfo(name = "num_items")
    private int numItems;
    @ColumnInfo(name = "icon_resource_id")
    private int iconResourceId;
    @ColumnInfo(name = "folder_color")
    private int folderColor;

    @ColumnInfo(name = "is_encrypted")
    private boolean ifEncrypted;

    @ColumnInfo(name = "encryption_password_hash")
    private String encryptionPasswordHash; // 存储密码的哈希值，而非明文密码

    public Folder(String folderName, String create_date,float emotionPercentage,int numItems,int iconResourceId, int folderColor) {
        this.folderId = UUID.randomUUID().toString();
        this.folderName = folderName;
        this.create_date = create_date;
        this.emotionPercentage = emotionPercentage;
        this.numItems = numItems;
        this.iconResourceId = iconResourceId;
        this.folderColor = folderColor;
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
        return emotionPercentage;
    }

    public void setEmotionPercentage(float emotionPercentage) {
        this.emotionPercentage = emotionPercentage;
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
}
