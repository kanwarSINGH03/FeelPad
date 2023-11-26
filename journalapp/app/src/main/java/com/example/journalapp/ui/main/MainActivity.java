package com.example.journalapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.ui.home.FolderViewModel;
import com.example.journalapp.ui.home.LaunchActivity;
import com.example.journalapp.ui.home.ViewAllFoldersActivity;
import com.example.journalapp.ui.note.NoteActivity;
import com.example.journalapp.R;
import com.example.journalapp.ui.note.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/**
 * Main activity starts as the entry point for the app
 */
public class MainActivity extends AppCompatActivity {

    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private FloatingActionButton folderButton;
    private String folderId;

    /**
     * onCreate is called when any instance or activity is created
     *
     * @param savedInstanceState Bundle containing the saved state of the activity
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);

        setNoteRecyclerView(); // initialize RecyclerView (display notes)
        createNoteObserver(); // observer to watch for changes in list of notes
        initMainMenu(); // initialize main menu buttons


    }

    /**
     * Initializes main menu buttons and sets their click listeners
     */
    private void initMainMenu() {
        ImageButton arrowButton = findViewById(R.id.arrowdown);
        ImageButton combinePdfButton = findViewById(R.id.combinePDF);
        ImageButton addNoteButton = findViewById(R.id.addNote);
        ImageButton searchButton = findViewById(R.id.search);
        ImageButton templateButton = findViewById(R.id.template);

        arrowButton.setOnClickListener(v -> {
            // Handle the click for the arrow button here
            Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
            startActivity(intent);
        });
        combinePdfButton.setOnClickListener(v -> {
            // Handle the click for the combine PDF button here
            Intent intent = new Intent(MainActivity.this, ViewAllFoldersActivity.class);
            startActivity(intent);
        });
        addNoteButton.setOnClickListener(v -> {
            // Handle the click for the add note button here
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);

            if (folderId == null){
                startActivity(intent);
            }
            else{
                intent.putExtra("folder_id", folderId);
                startActivity(intent);
            }
            startActivity(intent);
        });
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        templateButton.setOnClickListener(v -> {
            // Handle the click for the template button here
        });
    }

    /**
     * Sets up an observer to watch for changes in the list of notes and updates the UI accordingly
     */
    private void createNoteObserver() {
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getAllNotesOrderedByLastEditedDateDesc().observe(this, notes -> noteListAdapter.submitList(notes));
    }

    /**
     * Initializes RecyclerView to display the list of notes
     * RecyclerView: display a scrollable list of items with item animations, decorations, and touch handling
     */
    private void setNoteRecyclerView() {
        RecyclerView noteRecycleView = findViewById(R.id.noteListView);
        noteListAdapter = new NoteListAdapter(new NoteListAdapter.NoteDiff());
        noteRecycleView.setAdapter(noteListAdapter);
        noteRecycleView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Called when activity is resumed from pause state.
     * Displays latest notes in the Note RecyclerView
     */
    @Override
    protected void onResume() {
        super.onResume();
        setNoteRecyclerView();
        setNoteRecyclerView();
        if (folderId == null){
            createNoteObserver();
        }
        handleFolderId();
        String folderId= getIntent().getStringExtra("FOLDER_ID");
        if (Objects.nonNull(folderId)) {
            handleFolderId();
        }
    }

    private void handleFolderId() {
        folderId = getIntent().getStringExtra("FOLDER_ID");


        if (folderId != null && !folderId.isEmpty()) {
            Log.e("FolderBug", "First if statement");
            folderViewModel.getNotesByFolderId(folderId).observe(this, notes -> {
                ////
                Log.d("LiveData", "Received notes for folderId: " + folderId + ", Notes count: " + notes.size());
                noteListAdapter.submitList(notes);
            });
        }
        else {
            Log.e("FolderBug", "Else statement");
            folderViewModel.getAllNotes().observe(this, notes -> {
                //////
                Log.d("LiveData", "Received all notes, Notes count: " + notes.size());
                noteListAdapter.submitList(notes);
            });
        }

    }

}