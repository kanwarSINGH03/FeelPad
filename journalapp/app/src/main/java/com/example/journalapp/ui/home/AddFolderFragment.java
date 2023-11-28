package com.example.journalapp.ui.home;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.example.journalapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class AddFolderFragment extends BottomSheetDialogFragment {

    private MaterialButton btnCancel;
    private MaterialButton btnCreate;
    private TextInputEditText editTextFolderName;

    private int selectedColor = -1;




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
            if (!folderName.isEmpty() && selectedColor != -1){
                // pass the folder title
                Log.e("FolderTests", "Folder name: " + folderName);
                // pass the selected color
                Log.e("FolderTests", "Folder color: " + Integer.toHexString(selectedColor));
                // pass chosen icon
                


                dismiss(); // close fragment
            }
            else{
                // handle empty folderName
                Toast.makeText(getContext(), "Cannot have empty folder name", Toast.LENGTH_SHORT).show();
            }

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
}
