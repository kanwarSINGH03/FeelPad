package com.example.journalapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.example.journalapp.R;
import com.example.journalapp.database.NoteDatabase;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.ui.home.FolderViewModel;
import com.example.journalapp.utils.ConversionUtil;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainNoteListActivity extends AppCompatActivity implements TopNavBarFragment.OnSearchQueryChangeListener, FilterFragment.PopupDialogListener {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView noteRecyclerView;
    private String folder_id;

    private TextView noteListTitleTextView;
    private ImageButton folderFilterImageButton;

    // For Searching
    private String currentSearchQuery = "";
    private Long filterStartDate = null;
    private Long filterEndDate = null;
    private String filterEmotion = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_note_list);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initRecyclerView();

        // check if folder id has been received
        Intent intent = getIntent();

        if (intent.hasExtra("folder_id")){
            folder_id = intent.getStringExtra("folder_id");
            createNoteObserverForFolder(folder_id);
        }
        else{
            // catch
            Toast.makeText(this, "Illegal folder", Toast.LENGTH_SHORT).show();
        }

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false, true))
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("add", "note", folder_id))
                .commit();

    }

    private void initRecyclerView(){
        noteRecyclerView = findViewById(R.id.notes_list_recyclerview);
        noteListAdapter = new NoteListAdapter(new NoteListAdapter.NoteDiff(),mainViewModel,this);
        noteRecyclerView.setAdapter(noteListAdapter);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void createNoteObserverForFolder(String folderId){
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllNotesFromFolderOrderByLastEditedDateDesc(folderId).observe(this, notes -> noteListAdapter.submitList(notes));

        Executors.newSingleThreadExecutor().execute(() -> {
            Folder folder = folderViewModel.getFolderByIdSync(folderId);
            runOnUiThread(() -> initComponents(folder));
        });
    }
    // initialize components
    private void initComponents(Folder folder) {
        noteListTitleTextView = findViewById(R.id.notes_list_folder_name);
        if (folder != null) {
            noteListTitleTextView.setText(folder.getFolderName());
        } else {
            // Handle the case where the folder is null
            Toast.makeText(this, "Folder not found", Toast.LENGTH_SHORT).show();
        }

        folderFilterImageButton = findViewById(R.id.notes_list_filter_btn);
        folderFilterImageButton.setOnClickListener(v ->{
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.show(getSupportFragmentManager(), "PopupDialogFragment");
        });

    }

    @Override
    public void onSearchQueryChanged(String query){
        currentSearchQuery = query;
        updateNotesList();


    }
    @Override
    public void onConfirmButtonClick(Long startDate, Long endDate, String emotion) {
        // Reset filters
        filterStartDate = null;
        filterEndDate = null;
        filterEmotion = null;

        // Check if valid dates are selected
        boolean validDatesSelected = startDate != null && endDate != null && startDate <= endDate;

        // Check if a valid emotion is selected
        boolean validEmotionSelected = emotion != null && !emotion.equals("N/A");

        // Update filters based on user selection
        if (validDatesSelected) {
            filterStartDate = startDate;
            filterEndDate = endDate;
        }

        if (validEmotionSelected) {
            filterEmotion = emotion;
        }

        updateNotesList();
    }

    private void updateNotesList() {
        if (filterStartDate != null && filterEndDate != null && filterEmotion != null) {
            // Filter by both dates and emotion
            int emotionNum = emotionStringToInt(filterEmotion);

            if (emotionNum != 0){
                mainViewModel.searchNotesAndFilterEmotionDate(folder_id, currentSearchQuery, emotionNum, ConversionUtil.convertLongToIso8601(filterStartDate), ConversionUtil.convertLongToIso8601(filterEndDate))
                        .observe(this, notes -> noteListAdapter.submitList(notes));
            }


        } else if (filterStartDate != null && filterEndDate != null) {

            mainViewModel.searchNotesAndFilterDate(folder_id, currentSearchQuery, ConversionUtil.convertLongToIso8601(filterStartDate), ConversionUtil.convertLongToIso8601(filterEndDate))
                    .observe(this, notes -> noteListAdapter.submitList(notes));


        } else if (filterEmotion != null) {
            // Filter only by emotion

            int emotionNum = emotionStringToInt(filterEmotion);

            if (emotionNum != 0){
                mainViewModel.searchNotesAndFilterEmotion(folder_id, currentSearchQuery, emotionNum)
                        .observe(this, notes -> noteListAdapter.submitList(notes));
            }

        } else {
            // Search without filters
            mainViewModel.searchNotesInFolder(folder_id, currentSearchQuery)
                    .observe(this, notes -> noteListAdapter.submitList(notes));
        }
    }

    private int emotionStringToInt(String emotion){
        switch (emotion){
            case "Horrible":
                return 1;

            case "Disappointed":
                return 2;

            case "Neutral":
                return 3;

            case "Happy":
                return 4;

            case "Very Happy":
                return 5;

            default:
                return 0;

        }

    }



}
