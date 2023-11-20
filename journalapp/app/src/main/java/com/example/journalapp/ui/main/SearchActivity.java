package com.example.journalapp.ui.main;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * the activity that receives the search query, searches for data,
 * and display the search results.
 * NOTE: Not working due to NoteDao getAllNotesWhereTitleDateDescContains
 */
public class SearchActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private NoteListAdapter noteListAdapter;
    private SearchView noteSearchView;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        createNoteObserver();
        setNoteRecyclerView();
        initSearchBar();
    }

    /**
     * Sets up an observer to watch for changes in the list of notes and updates the UI accordingly
     */
    private void createNoteObserver() {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
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
     * Initialize the data for the search bar
     */
    private void initSearchBar() {
        ImageButton cancelNoteSearchButton = findViewById(R.id.cancelNoteSearchButton);
        noteSearchView = findViewById(R.id.noteSearchView);
        noteSearchView.setIconifiedByDefault(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        noteSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        cancelNoteSearchButton.setOnClickListener(view -> {
            finish();
        });

        Observable<String> searchObservable = Observable.create(emitter -> noteSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                emitter.onNext(newText);
                return false;
            }
        }));

        // Subscribe to observables to trigger a save to database
        compositeDisposable.addAll(
                searchObservable.subscribe(this::performQuery));
    }

    /** @TODO Broken query
     * Perform the search query and update the recycler view
     *
     * @param query The search query
     */
    public void performQuery(String query) {
        Log.d("SearchActivity", "Query String: " + query);
        mainViewModel.searchNotes(query).observe(this, notes -> noteListAdapter.submitList(notes));
    }
}
