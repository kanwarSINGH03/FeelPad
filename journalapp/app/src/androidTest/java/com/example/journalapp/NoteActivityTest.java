package com.example.journalapp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.journalapp.ui.note.NoteActivity;
import com.example.journalapp.ui.note.NoteItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;

import java.net.URISyntaxException;

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

    @Test
    public void checkPermissionAndOpenGalleryTest() {

        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {

                ApplicationProvider.getApplicationContext();

                NoteActivity noteActivity = new NoteActivity();

                if (noteActivity.getMediaUri() != null) {
                    noteActivity.checkPermissionAndOpenGallery();
                }
                assertNull(noteActivity.getMediaUri());
            });
        }
    }

    @Test
    public void mGetContent_Photo_Test() {

        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {

                ApplicationProvider.getApplicationContext();

                NoteActivity noteActivity = new NoteActivity();

                if (noteActivity.getMediaUri() != null) {
                    try {
                        noteActivity.mGetContent.launch(Intent.getIntent("image/*"));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
                assertNull(noteActivity.getMediaUri());
            });
        }
    }

    @Test
    public void mGetContent_Video_Test() {

        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {

                ApplicationProvider.getApplicationContext();

                NoteActivity noteActivity = new NoteActivity();

                if (noteActivity.getMediaUri() != null) {
                    try {
                        noteActivity.mGetContent.launch(Intent.getIntent("video/*"));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
                assertNull(noteActivity.getMediaUri());
            });
        }
    }
    @Test
    public void insertMedia_Photo_test(){
        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {
                ApplicationProvider.getApplicationContext();

                Uri randomUri = Uri.parse("content://mock-uri");

                NoteActivity noteActivity = new NoteActivity();

                if (noteActivity.getMediaUri() != null) {
                    noteActivity.insertMedia(randomUri, NoteItem.ItemType.IMAGE);
                }
                assertNull(noteActivity.getMediaUri());
            });
        }
    }
    @Test
    public void insertMedia_Video_Test(){
        try (ActivityScenario<NoteActivity> scenario = ActivityScenario.launch(NoteActivity.class)) {
            scenario.onActivity(activity -> {
                ApplicationProvider.getApplicationContext();

                Uri randomUri = Uri.parse("content://mock-uri");

                NoteActivity noteActivity = new NoteActivity();

                if (noteActivity.getMediaUri() != null) {
                    noteActivity.insertMedia(randomUri, NoteItem.ItemType.VIDEO);
                }
                assertNull(noteActivity.getMediaUri());
            });
        }
    }




}