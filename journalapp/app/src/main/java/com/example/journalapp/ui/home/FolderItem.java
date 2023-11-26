package com.example.journalapp.ui.home;

import java.util.UUID;

/**
 * Data model to represent folders
 */
public class FolderItem {

    private String folderId; // unique identifier
    private String title; // title of the folder

    // @TODO should be calculated as SUM(NoteEmotions)/Sum(Notes)
    private float emotionPercentage; // var for total emotion percentage and show in a bar

    // @TODO should be taken with database query
    private int numItems; // number of linked Notes to folderId

    private int iconResourceId; // Resource ID for icon chosen
    private int folderColor; // Resource ID for folder color

    public FolderItem(String folderId, String title, float emotionPercentage,
                      int numItems, int iconResourceId, int folderColor){
        if (folderId == null){ this.folderId = UUID.randomUUID().toString(); } // generate id
        else{ this.folderId = folderId; } // set folderId
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getEmotionPercentage() {
        return emotionPercentage;
    }

    public void setEmotionPercentage(float emotionPercentage) {
        this.emotionPercentage = emotionPercentage;
    }

    public String getNumItemsAsString() {
        return Integer.toString(numItems);
    }

    public int getNumItems(){
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
}
