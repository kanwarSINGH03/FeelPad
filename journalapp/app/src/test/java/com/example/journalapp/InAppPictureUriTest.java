package com.example.journalapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.example.journalapp.ui.note.NoteActivity;

import android.Manifest;
import android.os.Looper;
import android.provider.MediaStore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Test the URI that is passed to in-app picture launcher
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {26})
public class InAppPictureUriTest {

    private NoteActivity noteActivity;
    private ShadowApplication shadowApplication;

    @Before
    public void setUp() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), NoteActivity.class);
        intent.putExtra("note_id", "mock_note_id");
        noteActivity = Robolectric.buildActivity(NoteActivity.class, intent).create().get();
        shadowApplication = Shadows.shadowOf(noteActivity.getApplication());
    }

    @Test
    public void createImageUriTest() throws Exception {
        // Access and invoke the createImageUri method using reflection
        Method createImageUriMethod = NoteActivity.class.getDeclaredMethod("createImageUri");
        createImageUriMethod.setAccessible(true);
        Uri resultUri = (Uri) createImageUriMethod.invoke(noteActivity);

        // Assertions to verify the URI
        Assert.assertNotNull("Uri should not be null", resultUri);
        Assert.assertTrue("Uri should contain the file prefix 'image_'", resultUri.toString().contains("image_"));
        Assert.assertTrue("Uri should end with '.png'", resultUri.toString().endsWith(".png"));
    }

    @Test
    public void checkPermissionAndOpenCameraTest_PermissionGranted() throws Exception {
        // Mock permission granted
        shadowApplication.grantPermissions(Manifest.permission.CAMERA);

        // Invoke checkPermissionAndOpenCamera method using reflection
        Method checkPermissionMethod = NoteActivity.class.getDeclaredMethod("checkPermissionAndOpenCamera");
        checkPermissionMethod.setAccessible(true);
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        checkPermissionMethod.invoke(noteActivity);

        // Assuming createImageUri() and takePicture() are invoked, verify the URI creation
        Method createImageUriMethod = NoteActivity.class.getDeclaredMethod("createImageUri");
        createImageUriMethod.setAccessible(true);
        Uri createdUri = (Uri) createImageUriMethod.invoke(noteActivity);
        Assert.assertNotNull("Uri should not be null", createdUri);

        // Verify an intent to launch the camera was started
        ShadowActivity shadowActivity = Shadows.shadowOf(noteActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        Assert.assertNotNull("An intent should have been started", startedIntent);
        Assert.assertEquals("Intent action should be for capturing an image", MediaStore.ACTION_IMAGE_CAPTURE, startedIntent.getAction());
        // Additional checks can be made on the intent, such as checking data, extras, etc.
    }
}