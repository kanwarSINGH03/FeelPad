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
    private MaterialButton changeNameBtn, themesBtn;
    private FolderAdapter folderAdapter;
    private FolderViewModel folderViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView folderRecyclerView;
    private int selectedFolderPosition = -1;

    // TouchHelper Callback for deleting folders onSwipe
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
        initGraph(); // emotions graph

    }

    // ==============================
    // REGION: UI Initialization
    // ==============================

    /**
     * Initializes UI
     */
    private void initButtons(){

        // set up UI
        userNameTextView = findViewById(R.id.userNameTextView);
        changeNameBtn = findViewById(R.id.home_left_btn);
        themesBtn = findViewById(R.id.home_right_btn);

        // populate preferred name
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        userNameTextView.setText(preferences.getString("PreferredName","Esteemed Guest"));

        // -- On Click Listeners -- //

        changeNameBtn.setOnClickListener(v ->{
            showNameDialog();
        });

        themesBtn.setOnClickListener(v ->{
            // Second Button to lead user to themes
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Initialize the RecyclerView
     */
    private void initRecyclerView(){
        // Initialize recyclerview and adapter
        folderRecyclerView = findViewById(R.id.folderRecyclerView);
        folderAdapter = new FolderAdapter(new FolderAdapter.NoteDiff());

        // set up recyclerview as a grid
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable scrolling
            }
        };
        folderRecyclerView.setLayoutManager(layoutManager);
        folderRecyclerView.setAdapter(folderAdapter);

        // for Callback from adapter when a folder is clicked
        folderAdapter.setFolderClickListener(this);

        // for Callback from adapter when a folder is held
        folderAdapter.setFolderLongClickListener(position -> {

            // prompt user to encrypt, decrypt
            Folder folder = folderAdapter.getFolderAt(position);
            showPasswordDialogEncrypt(folder);
        });

        // for Callback from activity when folder is swiped (delete)
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(folderRecyclerView);

    }

    /**
     * Give the Adapter list of all folders
     */
    private void createNoteObserver(){
        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        folderViewModel.getAllFolders().observe(this, folders -> folderAdapter.submitList(folders));
    }

    /**
     * Initialize the emotion graph
     */
    private void initGraph(){
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Dynamic graph observing notes from last 30 days
        mainViewModel.getNotesFromLast30Days().observe(this, notes ->{
            if (notes != null && !notes.isEmpty()) {
                BarChart barChart = findViewById(R.id.barChart);
                GraphHelperUtil.setupBarChart(barChart, notes);
            }
        });


    }

    // ==============================
    // REGION: Popups
    // ==============================

    /**
     * Dialog to change the preferred name
     */
    private void showNameDialog(){

        // -- Build the AlertDialog -- //
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What name would you like to be called by?");

        // inflate layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_name, null);
        builder.setView(dialogView);

        TextInputEditText editTextName = dialogView.findViewById(R.id.edit_text_name);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = editTextName.getText().toString();

            // Change the name to new text and save to SharedPreferences
            userNameTextView.setText(name);

            SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("PreferredName", name);
            editor.apply();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Dialog to ask to encrypt/decrypt a folder
     * Specifically called when a folder is long-clicked
     * @param folder Folder object
     */
    public void showPasswordDialogEncrypt(Folder folder) {

        // -- Build the AlertDialog -- //
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(folder.getEncrypted() ? "Enter Password to Remove Encryption" : "Encrypt Folder"); // change title based on if encrypted

        // inflate layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ask_password, null);
        builder.setView(dialogView);

        TextInputEditText editTextPassword = dialogView.findViewById(R.id.edit_text_password);
        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // if encrypted, decrypt; if not, then encrypt
        builder.setPositiveButton(folder.getEncrypted() ? "Decrypt" : "Encrypt", (dialog, which) -> {
            String password = editTextPassword.getText().toString(); // password i

            if (folder.getEncrypted()) {

                // decrypt and compare password
                String decryptedPassword = CryptoUtil.decrypt(folder.getPassword());
                if (password.equals(decryptedPassword)) {

                    // if password is correct, remove encryption
                    folder.setIsEncrypted(false);
                    folder.setPassword(null);
                    updateFolderInDatabase(folder);
                }
                else {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            } else {

                // if not encrypted, then ask to encrypt
                if (!password.isEmpty()){
                    String encryptedPassword = CryptoUtil.encrypt(password);
                    folder.setIsEncrypted(true);
                    folder.setPassword(encryptedPassword);
                    updateFolderInDatabase(folder);
                }
                else{
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Dialog before opening a folder's contents
     * Checks if encrypted, and if it is, then asks for a password
     * Specifically called when a folder is clicked
     * @param folder Folder object
     */
    public void showPasswordDialogOpen(Folder folder) {

        // if encrypted, ask for password before opening
        if (folder.getEncrypted()) {

            // -- Build the AlertDialog -- //
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Password to Open Folder");

            // inflate layout
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ask_password, null);
            builder.setView(dialogView);

            TextInputEditText editTextPassword = dialogView.findViewById(R.id.edit_text_password);
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


            builder.setPositiveButton("Unlock", (dialog, which) -> {
                String enteredPassword = editTextPassword.getText().toString();

                // decrypt password from database
                String decryptedPassword = CryptoUtil.decrypt(folder.getPassword());

                // check if both passwords are equal
                if (enteredPassword.equals(decryptedPassword)) {

                    // open folder
                    Intent intent = new Intent(this, MainNoteListActivity.class);
                    intent.putExtra("folder_id", folder.getFolderId());
                    startActivity(intent);
                }
                else {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();

        } else {
            // non-encrypted => go straight into folder
            Intent intent = new Intent(this, MainNoteListActivity.class);
            intent.putExtra("folder_id", folder.getFolderId());
            startActivity(intent);
        }
    }

    // ==============================
    // REGION: Other
    // ==============================
    @Override
    public void onFolderClicked(int position){
        this.selectedFolderPosition = position;

        // before opening, check if there is password and ask for it
        showPasswordDialogOpen(folderAdapter.getFolderAt(position));
    }

    /**
     * Retrieves the theme ID based on the provided theme name.
     * Exists in every activity when applying the assigned theme
     * @param themeName String themeName (from SharedPreferences)
     * @return an integer representing the theme
     */
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

    /**
     * Deletes a folder at a specified position
     * @param position int position in the RecyclerView
     */
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

    /**
     * Uses a background thread to update a folder in the database
     * @param folder
     */
    private void updateFolderInDatabase(Folder folder) {
        new Thread(()-> {
            folderViewModel.updateFolder(folder);
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // when user leaves, we destroy the activity to prevent memory leaks
        finish();
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
}
