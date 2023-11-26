package com.example.journalapp.ui.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.TopNavBarFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

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
    }

    private void initRecyclerView(){
        // initialize local data source
        folderList = new ArrayList<>();
        folderList.add(new FolderItem("id1","Travel Journal",50,5, R.drawable.ic_folder_flag,R.color.colorAccentYellow));
        folderList.add(new FolderItem("id2","Gym",50,5, R.drawable.ic_folder_flag,R.color.colorAccentYellow));

        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(folderList);

        // set up recyclerview
        folderRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        folderRecyclerView.setAdapter(folderAdapter);




    }
}
