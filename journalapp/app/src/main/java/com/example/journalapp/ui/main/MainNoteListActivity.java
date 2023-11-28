package com.example.journalapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.home.FolderViewModel;

public class MainNoteListActivity extends AppCompatActivity {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;
    private RecyclerView noteRecyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_note_list);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false))
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("add"))
                .commit();

        initRecyclerView();

        // check if folder id has been received
        Intent intent = getIntent();

        if (intent.hasExtra("folder_id")){
            String folder_id = intent.getStringExtra("folder_id");
            createNoteObserverForFolder(folder_id);
        }
        else{
            // catch
            Toast.makeText(this, "Illegal folder", Toast.LENGTH_SHORT).show();
        }

    }

    private void initRecyclerView(){
        noteRecyclerView = findViewById(R.id.notes_list_recyclerview);
        noteListAdapter = new NoteListAdapter(new NoteListAdapter.NoteDiff());
        noteRecyclerView.setAdapter(noteListAdapter);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void createNoteObserverForFolder(String folder_id){
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getNotesByFolderId(folder_id).observe(this, notes -> noteListAdapter.submitList(notes));
    }

    // initialize components

}
