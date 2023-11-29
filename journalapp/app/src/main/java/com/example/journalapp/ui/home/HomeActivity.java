package com.example.journalapp.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.MainNoteListActivity;
import com.example.journalapp.ui.main.MainViewModel;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeActivity extends AppCompatActivity implements FolderAdapter.FolderClickListener {

    private FolderAdapter folderAdapter;
    private FolderViewModel folderViewModel;
    private int selectedFolderPosition = -1;

    private RecyclerView folderRecyclerView;

    // for deleting folders
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
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.theme_red))
                    .addSwipeLeftActionIcon(R.drawable.ic_note_delete)
                    .addSwipeLeftCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 10)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

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
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
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

        // callback
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(folderRecyclerView);

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

    private void deleteItem(int position){
        Folder folderToDelete = folderAdapter.getFolderAt(position);
        folderAdapter.removeFolderAt(position);
        folderAdapter.notifyItemRemoved(position);
        if (folderToDelete == null){
            Log.e("folderToDelete", "Folder is null");
        }
        else{
            folderViewModel.deleteFolder(folderToDelete);
        }
    }


}
