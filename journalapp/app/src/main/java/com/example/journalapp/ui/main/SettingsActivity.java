package com.example.journalapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;

import com.example.journalapp.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private ShapeableImageView b1;
    private ShapeableImageView b2;
    private ShapeableImageView b3;
    private AppCompatToggleButton togglebtn;

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
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false, ""))
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("settings", "none", null))
                .commit();

        initColorThemeDropdown();
        initNoteBackgroundTheme();

    }

    /**
     * Initialize the color theme dropdown menu
     */
    private void initColorThemeDropdown(){

        // define the color names and corresponding icon resource ids
        String[] colorNames = {"Blushing Tomato", "Dragon's Fury", "Mermaid Tail", "Elephant in the Room", "Stormy Monday", "Sunshine Sneezing"};
        Integer[] colorIcons = {R.drawable.ic_folder_color_light_red, R.drawable.ic_folder_color_red, R.drawable.ic_folder_color_blue_green, R.drawable.ic_folder_color_grey, R.drawable.ic_folder_color_grey_blue, R.drawable.ic_folder_color_yellow};

        TextInputLayout dropdownLayout = findViewById(R.id.settings_layout_dropdown);

        // Custom adapter to populate the dropdown menu
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

        // set the adapter to AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = dropdownLayout.findViewById(R.id.settings_auto_complete_text_view);
        autoCompleteTextView.setAdapter(adapter);

        // listeners for each menu item
        // when selected, apply theme selection and changes
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedColor = colorNames[position];
            saveThemeChoice(selectedColor);
            applyTheme(selectedColor);
            Toast.makeText(this,"Loading Theme Changes",Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Initialize note background theme selection
     */
    private void initNoteBackgroundTheme(){

        // check toggle button from shared preferences
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        togglebtn = findViewById(R.id.settings_background_toggle);
        boolean savedToggleState = preferences.getBoolean("ToggleBackgroundState", false); // Default value is false
        togglebtn.setChecked(savedToggleState);

        // if toggle button is pressed, save button to SharedPreferences
        togglebtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveButtonChecked(isChecked);

            if (isChecked){
                Toast.makeText(this, "You have enabled Note Editor Image Background", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "You have disabled Note Editor Image Background", Toast.LENGTH_SHORT).show();
            }
        });

        // set up background
        b1 = findViewById(R.id.settings_bg1);
        b2 = findViewById(R.id.settings_bg2);
        b3 = findViewById(R.id.settings_bg3);



        b1.setOnClickListener(v ->{
            saveThemeBackground(R.drawable.background1);
            Toast.makeText(this, "Changed Note Editor Background", Toast.LENGTH_SHORT).show();
        });
        b2.setOnClickListener(v ->{
            saveThemeBackground(R.drawable.background2);
            Toast.makeText(this, "Changed Note Editor Background", Toast.LENGTH_SHORT).show();
        });
        b3.setOnClickListener(v ->{
            saveThemeBackground(R.drawable.background3);
            Toast.makeText(this, "Changed Note Editor Background", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Saves the selected theme name to SharedPreferences
     * @param themeName
     */
    private void saveThemeChoice(String themeName) {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("SelectedTheme", themeName);
        editor.apply();
    }

    /**
     * Saves the selected background drawable resource ID to sharedPreferences
     * @param backgroundDrawable
     */
    private void saveThemeBackground(@DrawableRes int backgroundDrawable){
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("NoteBackgroundDrawable", backgroundDrawable);
        editor.apply();
    }

    /**
     * Saves the state of the background toggle button to SharedPreferences
     * @param isChecked
     */
    private void saveButtonChecked(boolean isChecked){
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ToggleBackgroundState", isChecked);
        editor.apply();
    }

    /**
     * Applys the theme by restarting the activity
     * @param themeName String theme name
     */
    private void applyTheme(String themeName) {
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        finish();
        startActivity(getIntent());
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