package com.example.journalapp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.journalapp.ui.note.NoteActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;

@RunWith(AndroidJUnit4.class)
public class NoteActivityTest {

    private static final boolean IS_IMAGE = true;

    @Test
    public void saveMediaToInternalStorage_Test() {

        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {
                ApplicationProvider.getApplicationContext();

                Uri randomUri = Uri.parse("content://mock-uri");

                NoteActivity noteActivity = new NoteActivity();

                Uri newUri = noteActivity.saveMediaToInternalStorage(randomUri, IS_IMAGE);

                assertNull(newUri);
            });
        }
    }


}