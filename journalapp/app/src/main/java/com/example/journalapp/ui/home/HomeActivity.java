package com.example.journalapp.ui.home;

import android.content.Intent;
import android.os.Bundle;

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

public class HomeActivity extends AppCompatActivity{

    private FolderAdapter folderAdapter;
    private FolderViewModel folderViewModel;


    // will remove
    private RecyclerView folderRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false))
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
        MaterialButton btn1 = findViewById(R.id.button1);
        MaterialButton btn2 = findViewById(R.id.button2);

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

    }

}
