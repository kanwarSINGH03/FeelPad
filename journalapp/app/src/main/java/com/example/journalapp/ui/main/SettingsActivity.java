package com.example.journalapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        setContentView(R.layout.activity_settings);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false))
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("settings", "none", null))
                .commit();

        // Define the color names and corresponding icon resource IDs
        String[] colorNames = {"Blushing Tomato", "Dragon's Fury", "Mermaid Tail", "Elephant in the Room", "Stormy Monday", "Sunshine Sneezing"};
        Integer[] colorIcons = {R.drawable.ic_folder_color_light_red, R.drawable.ic_folder_color_red, R.drawable.ic_folder_color_blue_green, R.drawable.ic_folder_color_grey, R.drawable.ic_folder_color_grey_blue, R.drawable.ic_folder_color_yellow};

        TextInputLayout dropdownLayout = findViewById(R.id.settings_layout_dropdown);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_dropdown, R.id.text, colorNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView icon = view.findViewById(R.id.icon);
                icon.setImageResource(colorIcons[position]);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                LinearLayout itemLayout = (LinearLayout) view;
                ImageView icon = itemLayout.findViewById(R.id.icon);
                icon.setImageResource(colorIcons[position]);
                return view;
            }
        };

        AutoCompleteTextView autoCompleteTextView = dropdownLayout.findViewById(R.id.settings_auto_complete_text_view);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedColor = colorNames[position];
            saveThemeChoice(selectedColor);
            applyTheme(selectedColor);
            Toast.makeText(this,"Please restart app to load changes",Toast.LENGTH_SHORT).show();
        });
    }

    private void saveThemeChoice(String themeName) {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("SelectedTheme", themeName);
        editor.apply();
    }

    private void applyTheme(String themeName) {
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        // Restart the activity or the entire app for the theme to take effect
        // Example: restarting the current activity
        finish();
        startActivity(getIntent());
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