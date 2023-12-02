package com.example.journalapp.Theme;

import static android.content.Context.MODE_PRIVATE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.journalapp.R;
import com.example.journalapp.ui.main.SettingsActivity;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    private SettingsActivity settingsActivity;
    private SharedPreferences sharedPreferences;
    @Before
    public void setUp() {
        settingsActivity = Robolectric.setupActivity(SettingsActivity.class);
        Context context = RuntimeEnvironment.getApplication();
        sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE);
    }
  //  @Test
    //make getThemeId public before testing
//    public void testGetThemeId() {
//        int actualThemeId1 = settingsActivity.getThemeId("Stormy Monday");
//        int actualThemeId2 = settingsActivity.getThemeId("Blushing Tomato");
//        int actualThemeId3= settingsActivity.getThemeId("Theme_Red");
//        int actualThemeId4 = settingsActivity.getThemeId("Theme_BlueGreen");
//        int actualThemeId5 = settingsActivity.getThemeId("Theme_Grey");
//
//
//        assertEquals(2131952209, actualThemeId1);
//        assertEquals(2131952210, actualThemeId2);
//        assertEquals(2131951691, actualThemeId3);
//        assertEquals(2131951691, actualThemeId4);
//        assertEquals(2131951691, actualThemeId5);
//    }
//    @Test
//    public void testSaveThemeBackground() {
//        int backgroundDrawable = R.drawable.background1;
//        //settingsActivity.saveThemeBackground(backgroundDrawable);
//        // change saveThemeBackground to public before testing
//        int savedBackground = sharedPreferences.getInt("NoteBackgroundDrawable", -1);
//        assertEquals(backgroundDrawable, savedBackground);
//    }
}
