package com.example.journalapp.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.R;
/*
Allows user to select a theme for their note page from a list of themes
 */

public class ThemeSelectionActivity extends AppCompatActivity {

    public static final String THEME_PREFERENCES = "theme_preferences";
    public static final String SELECTED_THEME = "selected_theme";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        selection();
    }


    private void selection() {
        ImageButton theme1 = findViewById(R.id.sky);
        ImageButton theme2 = findViewById(R.id.beach);
        ImageButton theme3 = findViewById(R.id.map);
        ImageButton theme4 = findViewById(R.id.idk);


        theme1.setOnClickListener(v -> {
            Log.d("ThemeSelectionActivity", "Theme1 button clicked");
            noteTheme(R.layout.activity_theme1);
        });
        theme2.setOnClickListener(v -> {
            Log.d("ThemeSelectionActivity", "Theme2 button clicked");
            noteTheme(R.layout.activity_theme2);
        });
        theme3.setOnClickListener(v -> {
            Log.d("ThemeSelectionActivity", "Theme3 button clicked");
            noteTheme(R.layout.activity_theme3);
        });
        theme4.setOnClickListener(v -> {
            Log.d("ThemeSelectionActivity", "Theme4 button clicked");
            noteTheme(R.layout.activity_theme4);
        });
    }


    private void noteTheme(int themeBackground){
        saveThemeToPreferences(String.valueOf(themeBackground));

        // an intent to return the selected theme to the MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("themeBackground", themeBackground);
        setResult(RESULT_OK, resultIntent);

        // Finish the ThemeSelectionActivity
        finish();

    }
    private void saveThemeToPreferences(String themeId) {
        SharedPreferences preferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_THEME, themeId);
        editor.apply();
    }
}
