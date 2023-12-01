package com.example.journalapp.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.ui.main.BottomNavBarFragment;
import com.example.journalapp.ui.main.MainNoteListActivity;
import com.example.journalapp.ui.main.MainViewModel;
import com.example.journalapp.ui.main.SettingsActivity;
import com.example.journalapp.ui.main.TopNavBarFragment;
import com.example.journalapp.utils.ConversionUtil;
import com.example.journalapp.utils.CryptoUtil;
import com.example.journalapp.utils.GraphHelperUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeActivity extends AppCompatActivity implements FolderAdapter.FolderClickListener {

    // UI Components
    private TextView userNameTextView;
    private MaterialButton changeNameBtn;
    private MaterialButton themesBtn;

    private FolderAdapter folderAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private int selectedFolderPosition = -1;

    private RecyclerView folderRecyclerView;

    // for deleting folders
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            // Since you only want swipe functionality, no need to handle movement here
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                deleteItem(viewHolder.getAdapterPosition());
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.theme_red))
                    .addSwipeLeftActionIcon(R.drawable.ic_note_delete)
                    .addSwipeLeftCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 10)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        setContentView(R.layout.activity_home);

        // add top navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.topNavBarFragmentContainer, TopNavBarFragment.newInstance(true, false, ""))
                .commit();

        // add bottom navbar fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavBarFragmentContainer, BottomNavBarFragment.newInstance("home", "folder", ""))
                .commit();


        initRecyclerView(); // init recyclerView to display notes
        createNoteObserver(); // observer to watch for changes in list of notes
        initButtons(); // initialize buttons
        initGraph();

    }

    private void initButtons(){
        userNameTextView = findViewById(R.id.userNameTextView);
        changeNameBtn = findViewById(R.id.home_left_btn);
        themesBtn = findViewById(R.id.home_right_btn);

        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        userNameTextView.setText(preferences.getString("PreferredName","Esteemed Guest"));


        changeNameBtn.setOnClickListener(v ->{
            showNameDialog();

        });

        themesBtn.setOnClickListener(v ->{
            // Second Button to lead user to themes
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // set on click listeners
    }

    private void showNameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What name would you like to be called by?");

        // inflate layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_name, null);
        builder.setView(dialogView);

        TextInputEditText editTextName = dialogView.findViewById(R.id.edit_text_name);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString();

                // Change the name to new text and save to SharedPreferences
                userNameTextView.setText(name);

                SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("PreferredName", name);
                editor.apply();


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }



    private void createNoteObserver(){
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllFolders().observe(this, folders -> folderAdapter.submitList(folders));
    }

    private void initRecyclerView(){
        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(new FolderAdapter.NoteDiff());

        // set up recyclerview
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable scrolling
            }
        };
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(folderAdapter);

        folderAdapter.setFolderClickListener(this);

        folderAdapter.setFolderLongClickListener(position -> {
            Folder folder = folderAdapter.getFolderAt(position);
            showPasswordDialogEncrypt(folder);
        });

        // callback
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(folderRecyclerView);

    }

    @Override
    public void onFolderClicked(int position){
        this.selectedFolderPosition = position;

        showPasswordDialogOpen(folderAdapter.getFolderAt(position));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedFolderPosition != -1){
            folderViewModel.updateFolderTimestamp(folderAdapter.getFolderAt(selectedFolderPosition).getFolderId(), ConversionUtil.getDateAsString());
            Log.e("ItemChange", "Notifying item changed at position " + selectedFolderPosition);
            folderAdapter.notifyItemChanged(selectedFolderPosition);
        }
    }

    private int getThemeId(String themeName) {
        switch (themeName) {
            case "Blushing Tomato":
                return R.style.Theme_LightRed;
            case "Dragon's Fury":
                return R.style.Theme_Red;
            case "Mermaid Tail":
                return R.style.Theme_BlueGreen;
            case "Elephant in the Room":
                return R.style.Theme_Grey;
            case "Stormy Monday":
                return R.style.Theme_GreyBlue;
            case "Sunshine Sneezing":
                return R.style.Theme_Yellow;

            default:
                return R.style.Base_Theme;
        }
    }

    private void deleteItem(int position){
        Folder folderToDelete = folderAdapter.getFolderAt(position);
        folderAdapter.removeFolderAt(position);
        folderAdapter.notifyItemRemoved(position);
        if (folderToDelete == null){
            Log.e("folderToDelete", "Folder is null");
        }
        else{
            folderViewModel.deleteFolder(folderToDelete);
        }
    }

    private void initGraph(){
        // observe livedata of all folders to update the graph

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getNotesFromLast30Days().observe(this, notes ->{
            if (notes != null && !notes.isEmpty()) {
                BarChart barChart = findViewById(R.id.barChart);
                GraphHelperUtil.setupBarChart(barChart, notes);
            }
        });


    }

    public void showPasswordDialogEncrypt(Folder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        if (folder.getEncrypted()) {
            builder.setTitle("Enter Password to Remove Encryption");
            builder.setPositiveButton("Decrypt", (dialog, which) -> {
                String enteredPassword = input.getText().toString();

                // decrypt the stored password to read
                String decryptedPassword = CryptoUtil.decrypt(folder.getPassword());
                if (enteredPassword.equals(decryptedPassword)) {
                    folder.setIsEncrypted(false);
                    folder.setPassword(null);
                    updateFolderInDatabase(folder);
                } else {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setTitle("Encrypt Folder");
            builder.setPositiveButton("Encrypt", (dialog, which) -> {
                String password = input.getText().toString();
                if (!password.isEmpty()){

                    // encrypt password before saving
                    String encryptedPassword = CryptoUtil.encrypt(password);
                    folder.setIsEncrypted(true);
                    folder.setPassword(encryptedPassword);
                    updateFolderInDatabase(folder);

                }
                else{
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    public void showPasswordDialogOpen(Folder folder) {

        if (folder.getEncrypted()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);


            builder.setTitle("Enter Password to Open Folder");
            builder.setPositiveButton("Decrypt", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                // Decrypt the stored password
                String decryptedPassword = CryptoUtil.decrypt(folder.getPassword());

                if (enteredPassword.equals(decryptedPassword)) {

                    // if password is correct
                    Intent intent = new Intent(this, MainNoteListActivity.class);
                    intent.putExtra("folder_id", folder.getFolderId());
                    startActivity(intent);
                } else {

                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }
        else{
            // go straight into the intent
            Intent intent = new Intent(this, MainNoteListActivity.class);
            intent.putExtra("folder_id", folder.getFolderId());
            startActivity(intent);
        }
    }
    private void updateFolderInDatabase(Folder folder) {
        new Thread(()-> {
            folderViewModel.updateFolder(folder);
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
