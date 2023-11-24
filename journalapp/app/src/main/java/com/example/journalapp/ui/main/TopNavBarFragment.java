package com.example.journalapp.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.journalapp.R;

public class TopNavBarFragment extends Fragment {

    private static final String ARG_HIDE_BUTTONS = "hideButtons";

    public static TopNavBarFragment newInstance(boolean hideButtons){
        TopNavBarFragment fragment = new TopNavBarFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HIDE_BUTTONS, hideButtons);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.nav_bar_top, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnSearch = view.findViewById(R.id.btnSearch);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);

        // first set visibility depending on args passed
        if (getArguments() != null && getArguments().getBoolean(ARG_HIDE_BUTTONS)){
            btnSearch.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle search button click
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle menu button click
            }
        });
    }
}