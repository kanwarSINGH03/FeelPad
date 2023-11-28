package com.example.journalapp.ui.home;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.journalapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class AddFolderFragment extends BottomSheetDialogFragment {

    private MaterialButton btnCancel;
    private MaterialButton btnCreate;
    private TextInputLayout editTextHolder;
    private TextInputEditText editTextFolderName;

    private int selectedColor = -1;
    private int selectedIcon = -1;

    public AddFolderFragment(){
        // empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize buttons and editText
        btnCancel = view.findViewById(R.id.folder_add_cancel_button);
        btnCreate = view.findViewById(R.id.folder_add_create_button);
        editTextHolder = view.findViewById(R.id.folder_add_title_holder);
        editTextFolderName = view.findViewById(R.id.folder_add_title);


        // initialize color circles
        initColorCircles(view);


        // Handle button listeners
        btnCancel.setOnClickListener(v ->{

        });

        btnCreate.setOnClickListener(v ->{
            // Take title data from input
            String folderName = Objects.requireNonNull(editTextFolderName.getText()).toString();

            // check if folderName is empty and check if user has not chosen color
            if (!folderName.isEmpty() && selectedColor != -1 && selectedIcon != -1){
                // pass the folder title
                Log.e("FolderTests", "Folder name: " + folderName);
                // pass the selected color
                Log.e("FolderTests", "Folder color: " + Integer.toHexString(selectedColor));
                // pass chosen icon
                Log.e("FolderTests", "Folder Icon: " + Integer.toHexString(selectedIcon));



                dismiss(); // close fragment
            }
            else{
                // handle empty folderName
                Toast.makeText(getContext(), "Cannot have empty folder name", Toast.LENGTH_SHORT).show();
            }

        });

        editTextHolder.setStartIconOnClickListener(v ->{
            showIconSelectionDialog();
        });

        // TODO: Implement color selection and other functionalities
        FrameLayout bottomSheet = (FrameLayout) getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Get the screen height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenHeight = displayMetrics.heightPixels;
        bottomSheetBehavior.setPeekHeight(screenHeight/2);
    }

    private void initColorCircles(View view) {
        int[] colorViewIds = {R.id.colorCircle1, R.id.colorCircle2,R.id.colorCircle3, R.id.colorCircle4, R.id.colorCircle5, R.id.colorCircle6}; // Add all color view IDs

        for (int id : colorViewIds) {
            View colorView = view.findViewById(id);
            colorView.setOnClickListener(v -> {
                // Reset all views
                for (int innerId : colorViewIds) {
                    view.findViewById(innerId).setSelected(false);
                }

                // Select the clicked one
                v.setSelected(true);

                // get background tint color
                ColorStateList tintList = ViewCompat.getBackgroundTintList(v);
                if (tintList != null){
                    selectedColor = tintList.getDefaultColor();
                }
            });
        }
    }

    private void showIconSelectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.fragment_icon_grid, null);
        GridView gridView = view.findViewById(R.id.gridView);

        // Load array of Drawable ID's
        TypedArray ta = getResources().obtainTypedArray(R.array.icon_array);
        Integer[] icons = new Integer[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            icons[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_list_item_1, icons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView icon;
                if (convertView == null) {
                    icon = new ImageView(getContext());
                    icon.setLayoutParams(new GridView.LayoutParams(60, 60));
                    icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    icon = (ImageView) convertView;
                }

                icon.setImageResource(getItem(position));
                return icon;
            }
        };
        gridView.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            selectedIcon = icons[position];
            editTextHolder.setStartIconDrawable(selectedIcon);
            // Dismiss the dialog
            dialog.dismiss();
        });

        // Set the background color of the dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorBackground)));
        }

        dialog.show();
    }
}
