package com.example.journalapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainNoteListActivity extends AppCompatActivity {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView noteRecyclerView;
    private String folder_id;

    private TextView noteListTitleTextView;
    private ImageButton folderFilterImageButton;

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
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false))
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
        folderViewModel.getNotesByFolderId(folderId).observe(this, notes -> noteListAdapter.submitList(notes));

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
    }
}
