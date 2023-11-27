package com.example.journalapp.ui.main;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.journalapp.R;
import com.example.journalapp.ui.home.AddFolderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavBarFragment extends Fragment {

    private static final String ARG_FOCUSED_BTN = "focusedBtn";
    public static BottomNavBarFragment newInstance(String focusedBtn){
        BottomNavBarFragment fragment = new BottomNavBarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOCUSED_BTN, focusedBtn);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_bar_bottom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton btnHome = view.findViewById(R.id.nav_home);
        ImageButton btnAdd = view.findViewById(R.id.nav_add);
        ImageButton btnSettings = view.findViewById(R.id.nav_settings);

        // Retrieve the custom color from the theme
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        int customColor = typedValue.data;

        // check which button should be highlighted
        Bundle args = getArguments();
        if (args != null) {
            String focusedButton = args.getString(ARG_FOCUSED_BTN);
            if ("home".equals(focusedButton)) {
                btnHome.setColorFilter(customColor);
            } else if ("add".equals(focusedButton)) {
                btnAdd.setColorFilter(customColor);
            } else if ("settings".equals(focusedButton)) {
                btnSettings.setColorFilter(customColor);
            }
        }


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle search button click
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // @TODO make a case for if in folder list or home list
                AddFolderFragment folderFragment = new AddFolderFragment();
                folderFragment.show(getParentFragmentManager(), "addFolder");
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle menu button click
            }
        });
    }
}

