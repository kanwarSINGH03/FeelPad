package com.example.journalapp.ui.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements AddFolderFragment.FolderHandlerListener {

    private List<FolderItem> folderList;
    private RecyclerView folderRecyclerView;
    private FolderAdapter folderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(false))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("home"))
                .commit();

        initRecyclerView();

        MaterialButton btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(v ->{
            AddFolderFragment folderFragment = new AddFolderFragment();
            folderFragment.show(getSupportFragmentManager(), "addFolder");
        });
    }

    private void initRecyclerView(){
        // initialize local data source
        folderList = new ArrayList<>();

        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(folderList);

        // set up recyclerview
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable scrolling
            }
        };
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(folderAdapter);

    }

    @Override
    public void onSave(FolderItem folderItem) {

        // add the folderitem to local variable
        folderList.add(folderItem);
        folderAdapter.notifyItemInserted(folderList.size() - 1);
        // refresh recyclerview

    }
}
