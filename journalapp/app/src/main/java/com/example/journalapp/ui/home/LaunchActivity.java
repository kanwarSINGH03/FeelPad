package com.example.journalapp.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.R;
import com.example.journalapp.ui.home.HomeActivity;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.google.android.material.button.MaterialButton;

public class LaunchActivity extends AppCompatActivity {

    private MaterialButton btnWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, new BottomNavBarFragment())
                .commit();

        btnWrite = findViewById(R.id.launch_activity_btn);

        btnWrite.setOnClickListener(v ->{
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });
    }

}
