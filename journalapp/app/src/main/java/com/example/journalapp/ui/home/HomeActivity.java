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
        folderList.add(new FolderItem("id1","Travel Journal",50,5, R.drawable.ic_folder_flag,R.color.colorAccentLightRed));
        folderList.add(new FolderItem("id2","Gym Journal",50,32, R.drawable.ic_folder_workout,R.color.colorAccentYellow));
        folderList.add(new FolderItem("id3","Shopping",50,2, R.drawable.ic_folder_flag,R.color.colorAccentGreyBlue));
        folderList.add(new FolderItem("id4","Daily",50,50, R.drawable.ic_folder_workout,R.color.colorAccentBlueGreen));
        folderList.add(new FolderItem("id5","Shopping",50,4, R.drawable.ic_folder_flag,R.color.colorAccentRed));
        folderList.add(new FolderItem("id6","School",50,0, R.drawable.ic_folder_workout,R.color.colorAccentGrey));

        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(folderList);

        // set up recyclerview
        folderRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        folderRecyclerView.setAdapter(folderAdapter);




    }
}
