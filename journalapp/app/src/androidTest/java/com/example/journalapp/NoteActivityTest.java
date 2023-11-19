package com.example.journalapp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.journalapp.ui.note.NoteActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NoteActivityTest {

    private NoteActivity noteActivity;

    public NoteActivityTest(NoteActivity noteActivity) {
        this.noteActivity = noteActivity;
    }

    @Test
    public void saveMediaToInternalStorage_Test(){
        Uri randomUri = Uri.parse("content://mock-uri");
        Uri newUri = noteActivity.saveMediaToInternalStorage(randomUri, true);
        assertNotNull(newUri);
    }


}