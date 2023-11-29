package com.example.journalapp.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.ui.home.FolderViewModel;
import com.example.journalapp.utils.ConversionUtil;

import java.util.concurrent.Executors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainNoteListActivity extends AppCompatActivity implements TopNavBarFragment.OnSearchQueryChangeListener, FilterFragment.PopupDialogListener {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView noteRecyclerView;
    private String folder_id;

    private EditText noteListTitleEditText;
    private ImageButton folderFilterImageButton;

    // For Searching
    private String currentSearchQuery = "";
    private Long filterStartDate = null;
    private Long filterEndDate = null;
    private String filterEmotion = null;

    // swipe watcher for deleting notes
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            // Since you only want swipe functionality, no need to handle movement here
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                deleteItem(viewHolder.getAdapterPosition());
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainNoteListActivity.this, R.color.theme_red))
                    .addSwipeLeftActionIcon(R.drawable.ic_note_delete)
                    .addSwipeLeftCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 10)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

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

        // attach item touch helper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(noteRecyclerView);
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
        noteListTitleEditText = findViewById(R.id.notes_list_folder_name);
        if (folder != null) {
            noteListTitleEditText.setText(folder.getFolderName());

            // Add Text Change Listener
            noteListTitleEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    folder.setFolderName(s.toString()); // Update the folder title
                    folderViewModel.updateFolderTitle(s.toString(), folder.getFolderId()); // Save the updated title to database
                }
            });
        } else {
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

    private void deleteItem(int position){
        Note noteToDelete = noteListAdapter.getNoteAt(position);
        noteListAdapter.removeNoteAt(position);
        noteListAdapter.notifyItemRemoved(position);

        mainViewModel.deleteNote(noteToDelete);
    }




}
