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

        MaterialButton btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(v ->{
            AddFolderFragment folderFragment = new AddFolderFragment();
            folderFragment.show(getSupportFragmentManager(), "addFolder");
        });
    }

    private void initRecyclerView(){
        // initialize local data source
        folderList = new ArrayList<>();
        folderList.add(new FolderItem("id1","Travel Journal",25,5, R.drawable.ic_folder_icon1,R.color.colorAccentLightRed));
        folderList.add(new FolderItem("id2","Gym Journal",75,32, R.drawable.ic_folder_icon2,R.color.colorAccentYellow));
        folderList.add(new FolderItem("id3","Shopping",50,2, R.drawable.ic_folder_icon1,R.color.colorAccentGreyBlue));
        folderList.add(new FolderItem("id4","Daily",50,50, R.drawable.ic_folder_icon2,R.color.colorAccentBlueGreen));
        folderList.add(new FolderItem("id5","Shopping",0,4, R.drawable.ic_folder_icon1,R.color.colorAccentRed));
        folderList.add(new FolderItem("id6","School",100,0, R.drawable.ic_folder_icon2,R.color.colorAccentGrey));

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
}
