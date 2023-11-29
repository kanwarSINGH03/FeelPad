package com.example.journalapp.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.journalapp.R;

public class TopNavBarFragment extends Fragment {

    private static final String ARG_HIDE_BUTTONS = "hideButtons";
    private static final String ARG_SEARCH_TOGGLE = "searchToggle";
    private OnSearchQueryChangeListener searchQueryChangeListener;

    public static TopNavBarFragment newInstance(boolean hideButtons, boolean searchToggle){
        TopNavBarFragment fragment = new TopNavBarFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HIDE_BUTTONS, hideButtons);
        args.putBoolean(ARG_SEARCH_TOGGLE, searchToggle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getArguments() != null && getArguments().getBoolean(ARG_SEARCH_TOGGLE)){
            if (context instanceof OnSearchQueryChangeListener) {
                searchQueryChangeListener = (OnSearchQueryChangeListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnSearchQueryChangeListener");
            }
        }

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

        ImageButton btnSearchExpand = view.findViewById(R.id.btnSearchExpand);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        SearchView noteSearchView = view.findViewById(R.id.noteSearchView);

        // first set visibility depending on args passed
        if (getArguments() != null && getArguments().getBoolean(ARG_HIDE_BUTTONS)){
            btnSearchExpand.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        }

        btnSearchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if SearchView is not visible, then toggle
                if (noteSearchView.getVisibility() == View.GONE){
                    noteSearchView.setVisibility(View.VISIBLE);

                    // change the icon as well for search expansion/minimize
                    btnSearchExpand.setImageResource(R.drawable.ic_top_nav_bar_minimize_search);

                } else{
                    noteSearchView.setVisibility(View.GONE);
                    btnSearchExpand.setImageResource(R.drawable.ic_top_nav_bar_expand_search);
                }
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle menu button click
            }
        });

        // set search text listener
        noteSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optionally handle query submit action
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchQueryChangeListener != null) {
                    searchQueryChangeListener.onSearchQueryChanged(newText);
                }
                return true;
            }
        });

    }

    public interface OnSearchQueryChangeListener {
        void onSearchQueryChanged(String query);
    }
}