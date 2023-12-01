package com.example.journalapp.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.journalapp.ui.home.HomeActivity;
import com.example.journalapp.utils.ConversionUtil;

import java.util.concurrent.Executors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainNoteListActivity extends AppCompatActivity implements TopNavBarFragment.OnSearchQueryChangeListener, FilterFragment.PopupDialogListener {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView noteRecyclerView;

    // UI
    private EditText noteListTitleEditText;
    private ImageButton folderFilterImageButton;

    // For Searching
    private String folderId;
    private String currentSearchQuery = "";
    private Long filterStartDate = null;
    private Long filterEndDate = null;
    private String filterEmotion = null;

    // ItemTouchHelper Callback for onSwipe deletion of notes
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
            folderId = intent.getStringExtra("folder_id");
            createNoteObserverForFolder(folderId);
        }
        else{
            throw new RuntimeException("Illegal folder created");
        }

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false, true, folderId))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("add", "note", folderId))
                .commit();

    }

    /**
     * Initialize the Recyclerview
     */
    private void initRecyclerView(){
        noteRecyclerView = findViewById(R.id.notes_list_recyclerview);
        noteListAdapter = new NoteListAdapter(new NoteListAdapter.NoteDiff(),mainViewModel,this);
        noteRecyclerView.setAdapter(noteListAdapter);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // attach item touch helper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(noteRecyclerView);
    }

    /**
     * Attaches the Observer to the RecyclerView to always have all notes belonging to folder
     * @param folderId
     */
    private void createNoteObserverForFolder(String folderId){
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllNotesFromFolderOrderByLastEditedDateDesc(folderId).observe(this, notes -> noteListAdapter.submitList(notes));

        // Synchronously retrieve folder using id
        Executors.newSingleThreadExecutor().execute(() -> {
            Folder folder = folderViewModel.getFolderByIdSync(folderId);
            runOnUiThread(() -> initComponents(folder));
        });
    }

    /**
     * Initialize UI components
     * @param folder Folder object
     */
    private void initComponents(Folder folder) {
        // set title
        noteListTitleEditText = findViewById(R.id.notes_list_folder_name);
        if (folder != null) {

            // Check if user changes title of folder
            noteListTitleEditText.setText(folder.getFolderName());

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

        // When filter button is pressed
        folderFilterImageButton = findViewById(R.id.notes_list_filter_btn);
        folderFilterImageButton.setOnClickListener(v ->{
            FilterFragment filterFragment = new FilterFragment();
            filterFragment.show(getSupportFragmentManager(), "PopupDialogFragment");
        });

    }

    /**
     * Callback for search query being changed
     * from the FilterFragment
     * @param query
     */
    @Override
    public void onSearchQueryChanged(String query){
        currentSearchQuery = query;
        updateNotesList();
    }

    /**
     * Callback that user has confirmed their filtering options
     * @param startDate Long startDate (Long is returned by MaterialDatePickers)
     * @param endDate Long endDate (Long is returned by MaterialDatePickers)
     * @param emotion int from 1-5 for the emotion
     */
    @Override
    public void onConfirmButtonClick(Long startDate, Long endDate, String emotion) {

        // first, reset filters
        filterStartDate = null;
        filterEndDate = null;
        filterEmotion = null;

        // check if dates are valid
        boolean validDatesSelected = startDate != null && endDate != null && startDate <= endDate;

        // check if emotions are valid
        boolean validEmotionSelected = emotion != null && !emotion.equals("N/A");

        // update filters based on selection
        if (validDatesSelected) {
            filterStartDate = startDate;
            filterEndDate = endDate;
        }

        if (validEmotionSelected) {
            filterEmotion = emotion;
        }

        updateNotesList();
    }

    /**
     * Updates the observable notes list based on Search Query and/or filters
     */
    private void updateNotesList() {

        // Case 1: Search a query while filtering both date and emotion
        if (filterStartDate != null && filterEndDate != null && filterEmotion != null) {
            int emotionNum = emotionStringToInt(filterEmotion);
            if (emotionNum != 0){
                mainViewModel.searchNotesAndFilterEmotionDate(folderId, currentSearchQuery, emotionNum, ConversionUtil.convertLongToIso8601(filterStartDate), ConversionUtil.convertLongToIso8601(filterEndDate))
                        .observe(this, notes -> noteListAdapter.submitList(notes));
            }

        // Case 2: Search a query while filtering only date
        } else if (filterStartDate != null && filterEndDate != null) {
            mainViewModel.searchNotesAndFilterDate(folderId, currentSearchQuery, ConversionUtil.convertLongToIso8601(filterStartDate), ConversionUtil.convertLongToIso8601(filterEndDate))
                    .observe(this, notes -> noteListAdapter.submitList(notes));

        // Case 3: Search a query while filtering only emotion
        } else if (filterEmotion != null) {
            int emotionNum = emotionStringToInt(filterEmotion);
            if (emotionNum != 0){
                mainViewModel.searchNotesAndFilterEmotion(folderId, currentSearchQuery, emotionNum)
                        .observe(this, notes -> noteListAdapter.submitList(notes));
            }

        }

        // Default Case: Only searching for a query
        else {
            // Search without filters
            mainViewModel.searchNotesInFolder(folderId, currentSearchQuery)
                    .observe(this, notes -> noteListAdapter.submitList(notes));
        }
    }

    /**
     * Helper function to retrieve the integer from an emotion name (string)
     * @param emotion String emotion of the emotion names
     * @return int 1-5 representing emotion
     */
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

    /**
     * Deletes an item from the RecyclerView
     * Specifically for use from onSwipe delete callback
     * @param position
     */
    private void deleteItem(int position){
        Note noteToDelete = noteListAdapter.getNoteAt(position);
        noteListAdapter.removeNoteAt(position);
        noteListAdapter.notifyItemRemoved(position);

        mainViewModel.deleteNote(noteToDelete);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        // so dataset updates when going back
        Intent goBack = new Intent(this, HomeActivity.class);
        startActivity(goBack);
    }




}
