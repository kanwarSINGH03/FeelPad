package com.example.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.note.NoteViewModel;

/**
 * Main activity starts as the entry point for the app
 */
public class MainActivity extends AppCompatActivity {

    private NoteListAdapter noteListAdapter;

    /**
     * onCreate is called when any instance or activity is created
     *
     * @param savedInstanceState Bundle containing the saved state of the activity
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        });
        combinePdfButton.setOnClickListener(v -> {
            // Handle the click for the combine PDF button here
        });
        addNoteButton.setOnClickListener(v -> {
            // Handle the click for the add note button here
            Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);

            // intent has no note_id, so it is classified as a new note
            startActivity(intent);
        });
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        templateButton.setOnClickListener(v -> {
                // Start the ThemeActivity
                Intent intent = new Intent(MainActivity.this, ThemeSelectionActivity.class);
                startActivity(intent);

        });

    }


    /**
     * Sets up an observer to watch for changes in the list of notes and updates the UI accordingly
     */
    private void createNoteObserver() {
        NoteViewModel noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotesOrderedByCreateDateDesc().observe(this, notes -> noteListAdapter.submitList(notes));
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
        createNoteObserver();
    }

}