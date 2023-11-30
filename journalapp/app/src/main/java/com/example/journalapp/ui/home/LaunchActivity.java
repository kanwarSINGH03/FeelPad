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
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, new BottomNavBarFragment())
                .commit();

        btnWrite = findViewById(R.id.launch_activity_btn);

        btnWrite.setOnClickListener(v ->{
            showNameDialog();

        });
    }

    private boolean isFirstTime() {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        return preferences.getBoolean("isFirstTime", true);
    }

    private void showNameDialog() {
        NameDialogFragment dialog = new NameDialogFragment();
        dialog.show(getSupportFragmentManager(), "NameDialogFragment");

    }

    private void setFirstTimeFlag(boolean isFirstTime) {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", isFirstTime);
        editor.apply();
    }

    @Override
    public void onNameEntered(String name) {
        // Save the name in SharedPreferences or handle it as needed
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("PreferredName", name);
        editor.apply();

        // set the firstTimeflag after user enters their name
        if (isFirstTime()) {
            setFirstTimeFlag(false);
        }

        // after this, start Activity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

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
