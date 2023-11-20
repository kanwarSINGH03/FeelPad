package com.example.journalapp.ui.note;

import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.example.journalapp.utils.ConversionUtil;

import java.util.UUID;

/**
 * Data model to represent items inside a Note
 * Think of this as the content inside a Note
 */
public class NoteItem {

    // Enum to define types of items that can exist within a note
    public enum ItemType{
        TEXT,
        IMAGE,
        VIDEO,
        VOICE
    }
    private String itemId; // unique identifier
    private ItemType type; // type of the note item (Enum)

    /*
    Content to be stored as a String, but can represent different formats that can be converted
    TEXT: Convert from Spannables to String
    IMAGE: Convert from URI to String
    VIDEO: Convert from URI to String
     */
    private String content;
    private int orderIndex; // keep track of order in a list

    /**
     * Constructor for creating a new NoteItem
     * @param type
     * @param itemId
     * @param content
     * @param orderIndex
     */
    public NoteItem(ItemType type, String itemId, String content, int orderIndex){
        this.type = type;
        if (itemId == null) {
            this.itemId = UUID.randomUUID().toString(); // Generate a new ID only if none is provided
        } else {
            this.itemId = itemId;
        }
        this.content = content;
        this.orderIndex = orderIndex;
        Log.e("Note created", "itemId = " + itemId);
    }

    /**
     * Getter for the note type
     * @return
     */
    public ItemType getType() {
        return type;
    }

    /**
     * Setter for the note type
     * @param type
     */
    public void setType(ItemType type) {
        this.type = type;
    }

    /**
     * Getter for the item id
     * @return
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Setter for the item id
     * @param itemId
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Getter for item content (EditTexts only)
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter for item content (EditTexts only)
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter for item content as an ImageURI
     * @return
     */
    public Uri getContentMediaUri(){
        if ( (this.type == ItemType.IMAGE || this.type == ItemType.VIDEO || this.type == ItemType.VOICE) && this.content != null){
            return Uri.parse(this.content);
        }
        return null;
    }

    /**
     * Setter for item content from URI to string
     * @param mediaUri
     */
    public void setContentMediaUri(Uri mediaUri){
        if (mediaUri != null){
            this.content = mediaUri.toString();
        }
        else{
            this.content = null;
        }
    }

    /**
     * Set the content using a Spannable
     * Converts the Spannable into a String HTML before storing
     */
    public void setContentWithSpannable(SpannableStringBuilder stringBuilder){
        this.content = ConversionUtil.spannableToHtml(stringBuilder);
    }

    /**
     * Gets the content as a Spannable
     * Converts the String HTML into a Spannable before returning
     * @return
     */
    public SpannableStringBuilder getContentSpannable(){
        return ConversionUtil.htmlToSpannable(this.content);
    }

    /**
     * Getter for order index
     * @return
     */
    public int getOrderIndex() {
        return orderIndex;
    }

    /**
     * Setter for order index
     * @param orderIndex
     */
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
