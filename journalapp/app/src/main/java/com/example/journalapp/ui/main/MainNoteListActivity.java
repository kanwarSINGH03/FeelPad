package com.example.journalapp.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.journalapp.R;
import com.example.journalapp.ui.home.FolderViewModel;

public class MainNoteListActivity extends AppCompatActivity {
    private NoteListAdapter noteListAdapter;
    private FolderViewModel folderViewModel;

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

        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);


    }
}
