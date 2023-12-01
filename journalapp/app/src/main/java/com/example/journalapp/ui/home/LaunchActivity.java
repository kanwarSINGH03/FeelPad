package com.example.journalapp.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.R;
import com.example.journalapp.ui.home.HomeActivity;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.google.android.material.button.MaterialButton;

/**
 * Initial activity shown when User's first time opening the app; doesn't open after first time
 * Handles first-time setup and theme application
 */
public class LaunchActivity extends AppCompatActivity implements NameDialogFragment.NameDialogListener {

    private MaterialButton btnWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        setContentView(R.layout.activity_launch);

        // if not first time, then go straight to next activity
        if (!isFirstTime()){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false, ""))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, new BottomNavBarFragment())
                .commit();

        // initialize buttons
        btnWrite = findViewById(R.id.launch_activity_btn);
        btnWrite.setOnClickListener(v ->{
            showNameDialog();

        });
    }

    /**
     * Checks if it's the first time the app is being launched
     * @return true if it's the first time, otherwise, false
     */
    private boolean isFirstTime() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return preferences.getBoolean("isFirstTime", true);
    }

    /**
     * Shows the name dialog for first-time users.
     */
    private void showNameDialog() {
        NameDialogFragment dialog = new NameDialogFragment();
        dialog.show(getSupportFragmentManager(), "NameDialogFragment");

    }

    /**
     * Sets the first-time launch flag using SharedPreference
     * @param isFirstTime boolean flag indicating if it's the user's first time opening the app
     */
    private void setFirstTimeFlag(boolean isFirstTime) {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", isFirstTime);
        editor.apply();
    }

    /**
     * Callback from the NameDialogFragment
     * When name is entered in the fragment, will jump to this function
     * @param name String name
     */
    @Override
    public void onNameEntered(String name) {

        // save name to SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("PreferredName", name);
        editor.apply();

        // first-time-flag is set once user gives their name
        if (isFirstTime()) {
            setFirstTimeFlag(false);
        }

        // start activity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    /**
     * Retrieves the theme ID based on the provided theme name.
     * Exists in every activity when applying the assigned theme
     * @param themeName String themeName (from SharedPreferences)
     * @return an integer representing the theme
     */
    private int getThemeId(String themeName) {
        switch (themeName) {
            case "Blushing Tomato":
                return R.style.Theme_LightRed;
            case "Dragon's Fury":
                return R.style.Theme_Red;
            case "Mermaid Tail":
                return R.style.Theme_BlueGreen;
            case "Elephant in the Room":
                return R.style.Theme_Grey;
            case "Stormy Monday":
                return R.style.Theme_GreyBlue;
            case "Sunshine Sneezing":
                return R.style.Theme_Yellow;

            default:
                return R.style.Base_Theme;
        }
    }

}
