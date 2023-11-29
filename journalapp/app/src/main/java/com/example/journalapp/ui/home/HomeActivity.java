package com.example.journalapp.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.MainNoteListActivity;
import com.example.journalapp.ui.main.MainViewModel;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements FolderAdapter.FolderClickListener {

    private FolderAdapter folderAdapter;
    private FolderViewModel folderViewModel;
    private int selectedFolderPosition = -1;

    private RecyclerView folderRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        setContentView(R.layout.activity_home);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("home", "folder", ""))
                .commit();


        initRecyclerView(); // init recyclerView to display notes
        createNoteObserver(); // observer to watch for changes in list of notes
        initButtons(); // initialize buttons

    }

    private void initButtons(){
        TextView userNameTextView = findViewById(R.id.userNameTextView);
        MaterialButton btn1 = findViewById(R.id.button1);
        MaterialButton btn2 = findViewById(R.id.button2);

        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        userNameTextView.setText(preferences.getString("PreferredName","Esteemed Guest"));
        btn1.setOnClickListener(v ->{
            Intent intent = new Intent(this, MainNoteListActivity.class);
            startActivity(intent);

        });

        // set on click listeners
    }

    private void createNoteObserver(){
        FolderViewModel folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllFolders().observe(this, folders -> folderAdapter.submitList(folders));
    }

    private void initRecyclerView(){
        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(new FolderAdapter.NoteDiff());

        // set up recyclerview
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable scrolling
            }
        };
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(folderAdapter);

        folderAdapter.setFolderClickListener(this);

    }

    @Override
    public void onFolderClicked(int position){
        this.selectedFolderPosition = position;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedFolderPosition != -1){
            Log.e("ItemChange", "Notifying item changed at position " + selectedFolderPosition);
            folderAdapter.notifyItemChanged(selectedFolderPosition);
        }
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
