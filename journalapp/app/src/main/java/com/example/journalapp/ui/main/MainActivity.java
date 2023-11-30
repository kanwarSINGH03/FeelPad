package com.example.journalapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.ui.exportpdf.SelectPdfContentsActivity;
import com.example.journalapp.ui.note.NoteActivity;
import com.github.mikephil.charting.charts.BarChart;

import java.util.List;

/**
 * Main activity starts as the entry point for the app
 */
public class MainActivity extends AppCompatActivity {

    private NoteListAdapter noteListAdapter;
    private GraphViewModel graphViewModel;

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
        // Create an instance of GraphViewModel
        graphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);

        // Observe the LiveData from GraphViewModel to update the graph
        graphViewModel.getAllNotesLiveData().observe(this, notes -> {
            if (notes != null && !notes.isEmpty()) {
                BarChart barChart = findViewById(R.id.barChart);
                GraphHelper.setupBarChart(barChart, notes);
            }
        });
        displayEmotionalStateGraph();
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
            Intent intent = new Intent(MainActivity.this, PdfActivity.class);
            startActivity(intent);
        });
        combinePdfButton.setOnClickListener(v -> {
            // Handle the click for the combine PDF button here
            Intent intent = new Intent(MainActivity.this, SelectPdfContentsActivity.class);
            startActivity(intent);
        });
        addNoteButton.setOnClickListener(v -> {
            // Handle the click for the add note button here
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);

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

    private void displayEmotionalStateGraph() {
        // Get the NoteRepository instance
        NoteRepository noteRepository = NoteRepository.getInstance(getApplication());

        LiveData<List<Note>> allNotesLiveData = noteRepository.getAllNotesOrderedByLastEditedDateDesc();

        allNotesLiveData.observe(this, notes -> {
            // The list of notes has been updated, now you can use it to display the graph
            List<Note> allNotes = notes;

            // check if the list is not empty before displaying the graph
            if (allNotes != null && !allNotes.isEmpty()) {
                BarChart barChart = findViewById(R.id.barChart);

                // Call the setupBarChart method from GraphHelper to display the graph
                GraphHelper.setupBarChart(barChart, allNotes);
            }

            // Remove the observer after the initial update to prevent unnecessary updates
            allNotesLiveData.removeObservers(this);
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
        createNoteObserver();
    }

}