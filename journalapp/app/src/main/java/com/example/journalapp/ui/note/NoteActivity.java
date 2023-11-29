package com.example.journalapp.ui.note;

import static com.example.journalapp.utils.ConversionUtil.convertNoteItemEntitiesToNoteItems;
import static com.example.journalapp.utils.ConversionUtil.getDateAsString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * Activity representing a single note page
 * Contains creation and saving of a note
 */
public class NoteActivity extends AppCompatActivity implements NoteAdapter.OnNoteItemChangeListener, NoteAdapter.OnItemFocusChangeListener {

    // Note Component Variables
    private EditText titleEditText;
    private ImageButton openReactionMenu;

    // Styling Variables
    private ImageButton boldButton;
    private ImageButton italicsButton;
    private ImageButton underlineButton;
    private ImageButton strikethroughButton;
    private ImageButton pickColorButton;
    private ImageButton addTextBoxButton;

    // Note Contents Variables
    private RecyclerView noteContentRecyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteItems;
    private int focusedItem = -1; // starts at invalid
    private int highlightedItem = -1; // starts at invalid

    // Note Database Variables
    private NoteViewModel noteViewModel;
    private Note note;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // thread manager

    // Permission Variables
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_AUDIO_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    // Animation Variables
    ItemTouchHelper itemTouchHelper;

    // Temporary Variables (always changing, but need access to)
    private Uri tempUri;

    // Media Handling Variables
    // Special member variable used to launch activities that expect Media results
    private final ActivityResultLauncher<Intent> mGetContent =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            String mimeType = getContentResolver().getType(uri); // get MIME type
                            if (mimeType != null) {
                                if (mimeType.startsWith("image/")) {
                                    // Handle images
                                    Uri localUri = saveMediaToInternalStorage(uri, NoteItem.ItemType.IMAGE);
                                    insertMedia(localUri, NoteItem.ItemType.IMAGE);
                                } else if (mimeType.startsWith("video/")) {
                                    // Handle videos
                                    Uri localUri = saveMediaToInternalStorage(uri, NoteItem.ItemType.VIDEO);
                                    insertMedia(localUri, NoteItem.ItemType.VIDEO);
                                } else if (mimeType.startsWith("audio/")){
                                    // Handle audio
                                    Uri localUri = saveMediaToInternalStorage(uri, NoteItem.ItemType.VOICE);
                                    insertMedia(localUri, NoteItem.ItemType.VOICE);
                                } else if (mimeType.startsWith("application/pdf")){
                                    // Handle pdfs
                                    Uri localUri = saveMediaToInternalStorage(uri, NoteItem.ItemType.PDF);
                                    insertMedia(localUri, NoteItem.ItemType.PDF);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(NoteActivity.this, "Failed to insert media", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    // Special member variable used to launch camera and save contents to given URI
    private final ActivityResultLauncher<Uri> mTakePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), savedToUri ->{
                if (savedToUri){
                    try{
                        // Image saved successfully to provided URI
                        insertMedia(tempUri, NoteItem.ItemType.IMAGE);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        Toast.makeText(NoteActivity.this, "Failed to insert media", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    // handle failure
                    Toast.makeText(NoteActivity.this, "Failed to insert media", Toast.LENGTH_SHORT).show();
                }

            });

    // Special member variable for drag and dropping contents
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT) {

        @Override
        public boolean isLongPressDragEnabled(){
            return false;
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            noteAdapter.highlightItem(viewHolder.getAdapterPosition());
            highlightedItem = viewHolder.getAdapterPosition();

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(noteItems, fromPosition, toPosition);
            updateNoteItemsOrderIndex();

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            highlightedItem = toPosition;
            noteAdapter.highlightItem(toPosition);
            noteAdapter.notifyItemChanged(fromPosition);
            noteAdapter.notifyItemChanged(toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT){
                deleteItem(viewHolder.getAdapterPosition());
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(NoteActivity.this,R.color.theme_red))
                    .addSwipeLeftActionIcon(R.drawable.ic_note_delete)
                    .addSwipeLeftCornerRadius(TypedValue.COMPLEX_UNIT_DIP,10)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    /**
     * Called when activity is first created
     *
     * @param saveInstanceState Bundle containing the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        // Apply the theme
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String themeName = preferences.getString("SelectedTheme", "DefaultTheme");
        int themeId = getThemeId(themeName);
        setTheme(themeId);

        setContentView(R.layout.activity_note);

        // Initialize UI Widgets & set current date
        initWidgets();
        initOptionsMenu();
        initRecyclerView();
        initStyling();
        initLocation();

        // Check if the received intent is for a new note or existing note
        Intent intent = getIntent();

        if (intent.hasExtra("note_id")) {
            // Existing Note: retrieve note_id and set up existing note
            String note_id = intent.getStringExtra("note_id");
            setExistingNote(note_id);

        } else if (intent.hasExtra("folder_id")) {
            // New Note: create note_id and create new note
            String folder_id = intent.getStringExtra("folder_id");
            setNewNote(folder_id);
        }
        else{
            // catch
            Toast.makeText(this,"Illegal note insertion", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateEmotionImage() {
        int value = note.getEmotion();
        Log.i("Emotion", value + "");
        switch (value) {
            case 1:
                openReactionMenu.setImageResource(R.drawable.ic_note_emotion_horrible);
                break;
            case 2:
                openReactionMenu.setImageResource(R.drawable.ic_note_emotion_disappointed);
                break;
            case 3:
                openReactionMenu.setImageResource(R.drawable.ic_note_emotion_neutral);
                break;
            case 4:
                openReactionMenu.setImageResource(R.drawable.ic_note_emotion_happy);
                break;
            case 5:
                openReactionMenu.setImageResource(R.drawable.ic_note_emotion_very_happy);
                break;
            default:
                openReactionMenu.setImageResource(R.drawable.ic_note_add_emotion);
                break;
        }
    }

    // ==============================
    // REGION: UI Initialization
    // ==============================

    /**
     * Initializes UI widgets, ViewModel, and set the edit text watcher with debouncing.
     */
    private void initWidgets() {
        titleEditText = findViewById(R.id.titleEditText);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class); // initialize NoteViewModel

        /*
         * create an Observable to monitor changes in the title using debouncing
         * Process: Action is taken --> emitter is notified --> emitter notifies observable
         *          observable notifies subscribers --> subscribers take action
         */
        Observable<String> titleChangedObservable = Observable.create(emitter -> titleEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // actions before text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emitter.onNext(charSequence.toString()); // emitter is notified of an update
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // actions after text is changed
            }
        }));

        // Sets debounce time (ms) for title changes
        /* 0.5 second */
        int SAVE_DELAY = 500;
        Observable<String> titleObservable = titleChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);


        // Subscribe to observables to trigger a save to database
        compositeDisposable.addAll(
                titleObservable.subscribe(this::saveNoteTitle));


        /* Set up Reaction Menu */
        openReactionMenu = findViewById(R.id.reactionMenu);

        openReactionMenu.setOnClickListener(v -> {
            saveNoteContent();

            // if pressed, pop up window of the emotions
            PopupMenu popupMenu = new PopupMenu(this,v);

            // to force popup of icons
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon",boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            popupMenu.getMenuInflater().inflate(R.menu.emotion_menu, popupMenu.getMenu());



            popupMenu.setOnMenuItemClickListener(item ->{
                int emotionPicked = item.getItemId();
                int emotionLevel;

                if (emotionPicked == R.id.emotion_horrible){
                    emotionLevel = 1;
                } else if (emotionPicked == R.id.emotion_disappointed){
                    emotionLevel = 2;
                } else if (emotionPicked == R.id.emotion_neutral){
                    emotionLevel = 3;
                } else if (emotionPicked == R.id.emotion_happy){
                    emotionLevel = 4;
                } else if (emotionPicked == R.id.emotion_very_happy){
                    emotionLevel = 5;
                }
                else{
                    emotionLevel = 0;
                }

                note.setEmotion(emotionLevel);
                noteViewModel.updateNoteEmotion(note);
                updateEmotionImage();

                return true;
            });
            popupMenu.show();
        });
    }

    private void initLocation(){
        ImageButton location = (ImageButton) findViewById(R.id.location);
        location.setOnClickListener(v -> {
            Intent intent = new Intent(NoteActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Initialize the options menu in the notes page for additional actions
     */
    private void initOptionsMenu() {
        findViewById(R.id.optionsMenu).setOnClickListener(view -> {

            // popup menu (built-in)
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.journal_options_menu, popupMenu.getMenu());

            // attempt to show icons in the menu
            try {
                popupMenu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(popupMenu, true);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            } finally {
                popupMenu.show();
            }

            // handle menu item choices & clicks
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                /* Don't ask why it's not a switch statement, it's just not. */
                if (menuItem.getItemId() == R.id.item1a) {
                    checkPermissionAndOpenCamera();
                    return true;
                } else if (menuItem.getItemId() == R.id.item1b) {
                    Toast.makeText(getApplicationContext(), "Add Photo/Video From Library", Toast.LENGTH_SHORT).show();
                    checkPermissionAndOpenGallery();
                    return true;
                } else if (menuItem.getItemId() == R.id.item2) {
                    Toast.makeText(getApplicationContext(), "Add Voice Note", Toast.LENGTH_SHORT).show();
                    checkPermissionAndVoiceRecord();
                    return true;
                } else if (menuItem.getItemId() == R.id.item3) {
                    Toast.makeText(getApplicationContext(), "Insert PDF", Toast.LENGTH_SHORT).show();
                    selectPdf();
                    return true;
                } else if (menuItem.getItemId() == R.id.item5) {
                    Toast.makeText(getApplicationContext(), "Add Template", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return true;
            });
        });
    }

    /**
     * Initialize the RecyclerView that represents a Note's contents
     * Contents can include: EditTexts, ImageViews, etc.
     */
    private void initRecyclerView() {

        // First initialize the noteItems variable
        noteItems = new ArrayList<>();

        // Initialize the RecyclerView and Adapter
        noteContentRecyclerView = findViewById(R.id.recycler_view_notes); // Make sure this ID matches your layout
        noteAdapter = new NoteAdapter(noteItems);

        // Set up the RecyclerView
        noteContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteContentRecyclerView.setAdapter(noteAdapter);
        noteContentRecyclerView.setItemAnimator(null); // can remove if needed

        // Set all listeners
        noteAdapter.setOnNoteItemChangeListener(this); // notified to save if changes are made to noteItems
        noteAdapter.setOnItemFocusChangeListener(this); // notified if focus is shifted

        // @TODO set up to remove from highlights if outside of a recyclerView is pressed


        // For drag and dropping
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(noteContentRecyclerView);

    }

    // ==============================
    // REGION: Text Styling
    // ==============================

    /**
     * Initialize styling buttons & functionality
     */
    private void initStyling(){
        // Initialize all styling buttons and attach listeners

        boldButton = findViewById(R.id.boldButton);
        boldButton.setOnClickListener(v ->{
            if (focusedItem >= 0){
                RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(focusedItem);
                if (viewHolder instanceof NoteAdapter.TextViewHolder){
                    ((NoteAdapter.TextViewHolder) viewHolder).applyBold(highlightedItem);
                }
            }
        });

        italicsButton = findViewById(R.id.italicsButton);
        italicsButton.setOnClickListener(v -> {
            if (focusedItem >= 0){
                RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(focusedItem);
                if (viewHolder instanceof NoteAdapter.TextViewHolder){
                    ((NoteAdapter.TextViewHolder) viewHolder).applyItalics(highlightedItem);
                }
            }
        });

        underlineButton = findViewById(R.id.underlineButton);
        underlineButton.setOnClickListener(v -> {
            if (focusedItem >= 0){
                RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(focusedItem);
                if (viewHolder instanceof NoteAdapter.TextViewHolder){
                    ((NoteAdapter.TextViewHolder) viewHolder).applyUnderline(highlightedItem);
                }
            }
        });

        strikethroughButton = findViewById(R.id.strikethroughButton);
        strikethroughButton.setOnClickListener(v -> {
            if (focusedItem >= 0){
                RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(focusedItem);
                if (viewHolder instanceof NoteAdapter.TextViewHolder){
                    ((NoteAdapter.TextViewHolder) viewHolder).applyStrikethrough(highlightedItem);
                }
            }
        });



        AtomicInteger colorChosen = new AtomicInteger();

        pickColorButton = findViewById(R.id.note_color_btn);
        pickColorButton.setOnClickListener(v -> {
            // if pressed, then pop up window of 6 colors to choose from
            PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(this, com.google.android.material.R.style.Widget_MaterialComponents_PopupMenu), v);

            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon",boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            popupMenu.getMenuInflater().inflate(R.menu.note_color_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                int colorResourceId = 0;
                if (itemId == R.id.color_light_red){
                    colorResourceId = R.color.colorAccentLightRed;
                } else if (itemId == R.id.color_red){
                    colorResourceId = R.color.colorAccentRed;
                } else if (itemId == R.id.color_blue_green){
                    colorResourceId = R.color.colorAccentBlueGreen;
                } else if (itemId == R.id.color_grey){
                    colorResourceId = R.color.colorAccentGrey;
                } else if (itemId == R.id.color_grey_blue){
                    colorResourceId = R.color.colorAccentGreyBlue;
                } else if (itemId == R.id.color_yellow){
                    colorResourceId = R.color.colorAccentYellow;
                }


                if (colorResourceId != 0) {
                    int colorValue = ContextCompat.getColor(getApplicationContext(), colorResourceId);
                    colorChosen.set(colorValue);
                    if (focusedItem >= 0) {
                        RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(focusedItem);
                        if (viewHolder instanceof NoteAdapter.TextViewHolder) {
                            ((NoteAdapter.TextViewHolder) viewHolder).applyTextColor(highlightedItem, colorValue);
                            Log.e("ColorPicker", "Color chosen was: " + Integer.toHexString(colorValue));
                        }
                    }
                }
                return true;
            });

            popupMenu.show();
        });

        addTextBoxButton = findViewById(R.id.addTextButton);

        addTextBoxButton.setOnClickListener(v ->{
            // add text box to bottom of the list
            noteItems.add(new NoteItem(NoteItem.ItemType.TEXT, null, "", noteItems.size()));
            noteAdapter.notifyItemInserted(noteItems.size() - 1);

            saveNoteContent();

            Toast.makeText(this,"Added a new text box to the end of the items list", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * Callback Interface for the adapter to implement.
     */
    public interface TextFormattingHandler{
        void applyBold(int highlightedItem);
        void applyItalics(int highlightedItem);
        void applyUnderline(int highlightedItem);
        void applyStrikethrough(int highlightedItem);
        void applyTextColor(int highlightedItem, @ColorInt int color);

    }

    // ==============================
    // REGION: Listeners
    // ==============================

    /**
     * Called when Note Item content changes (e.g. user changes text)
     * Autosave function
     */
    @Override
    public void onNoteItemContentChanged() {
        saveNoteContent();
    }

    /**
     * Called when focus changes (either user types on EditText or clicks in image)
     * Switches focusedItem index
     * @param position
     * @param hasFocus
     */
    @Override
    public void onItemFocusChange(int position, boolean hasFocus) {
        if (hasFocus) {
            focusedItem = position;
            Log.e("FocusChange", "Focus has changed to position " + focusedItem);
        } else if (focusedItem == position) {
            focusedItem = -1;
            Log.e("FocusChange", "Focus has been set to invalid (-1)");
        }
    }

    /**
     * Called when user long clicks a view
     * Popup menu with options: delete, @TODO: move to different index
     * @param position
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onItemLongClick(int position) {
        Log.e("ItemLongClick", "Position #" + position + " has been long clicked.");

        // highlight item in adapter
        noteAdapter.highlightItem(position);
        // update highlighteditem
        highlightedItem = position;

        // Find the view by position
        View view = Objects.requireNonNull(noteContentRecyclerView.findViewHolderForAdapterPosition(position)).itemView;

        RecyclerView.ViewHolder viewHolder = noteContentRecyclerView.findViewHolderForAdapterPosition(position);

        // if text viewHolder
        if (viewHolder instanceof NoteAdapter.TextViewHolder){

            // convert to textViewHolder
            NoteAdapter.TextViewHolder textViewHolder = (NoteAdapter.TextViewHolder) viewHolder;

            // set listener to drag handle
            textViewHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }

        // if image viewHolder
        else if (viewHolder instanceof NoteAdapter.ImageViewHolder){

            // convert to imageviewHolder
            NoteAdapter.ImageViewHolder imageViewHolder = (NoteAdapter.ImageViewHolder) viewHolder;

            // set listener to drag handle
            imageViewHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }

        // if video viewholder
        else if (viewHolder instanceof NoteAdapter.VideoViewHolder){

            // convert to videoViewHolder
            NoteAdapter.VideoViewHolder videoViewHolder = (NoteAdapter.VideoViewHolder) viewHolder;

            // set listener to drag handle
            videoViewHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }

        // if audio
        else if (viewHolder instanceof NoteAdapter.VoiceViewHolder){

            // convert to audioViewHolder
            NoteAdapter.VoiceViewHolder voiceViewHolder = (NoteAdapter.VoiceViewHolder) viewHolder;

            // set listener to drag handle
            voiceViewHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }

        // if pdf
        else if (viewHolder instanceof NoteAdapter.PdfViewHolder){

            // convert to pdf
            NoteAdapter.PdfViewHolder pdfViewHolder = (NoteAdapter.PdfViewHolder) viewHolder;

            // set listener to drag handle
            pdfViewHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            });
        }


    }


    // ==============================
    // REGION: Media Handling
    // ==============================

    /**
     * Called by a button
     * Checks for permissions to read media (images/videos) from storage
     * Permission Granted: Opens gallery for media selection
     * Permission not Granted: Request for permissions
     */
    private void checkPermissionAndOpenGallery() {
        // API Level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API Level 33+
            boolean hasImagePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean hasVideoPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;

            if (!hasImagePermission || !hasVideoPermission) {
                // Request both permissions if either is not granted
                ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_STORAGE_PERMISSION);
            } else {
                // Permissions granted, open gallery
                selectMedia();
            }
        }
        // API Level below 33
        else {

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // if no permissions, request
                ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);

            } else {
                // if permission granted, go to gallery
                selectMedia();
            }
        }
    }

    /**
     * Checks for permissions to record audio.
     * If granted: start voice recording using another app
     * Not granted: request for permissions
     */
    private void checkPermissionAndVoiceRecord(){
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            // Permission not granted, request
            ActivityCompat.requestPermissions(NoteActivity.this,new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
        }
        else{
            startVoiceRecording();
        }
    }

    /**
     * Checks for permissions to open camera to take a picture
     * If granted: open camera and expect picture to be takne
     * not granted: request for permissions
     */
    void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted for using Camera, request it
            ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission has been granted, go to the camera
            takePicture();
        }
    }

    /**
     * Callback for the result from requesting permissions
     *
     * @param requestCode  The request code in
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle storage permission results
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permission granted, then allow access
                selectMedia();
            } else {
                // if permission denied, inform user
                Toast.makeText(this, "Storage Permission Denied!", Toast.LENGTH_SHORT).show();
            }

        }

        // Handle audio permission results
        else if (requestCode == REQUEST_AUDIO_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // if permission granted, then allow access
                startVoiceRecording();
            } else{
                // if permission denied, inform user
                Toast.makeText(this, "Audio Permission Denied!", Toast.LENGTH_SHORT).show();
            }

        }

        // Handle camera permission results
        else if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // if permission granted, then allow access
                takePicture();
            } else{
                // if permission denied, inform user
                Toast.makeText(this, "Camera Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Launches an intent to start voice recording using any viable apps that can handle the intent
     */
    private void startVoiceRecording(){
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION); // uses default voice recording app to record
        // first check if there is an app that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null){
            mGetContent.launch(intent);
        }
        else{
            // otherwise, inform user that the don't have an installed voice recording app.
            Toast.makeText(this, "No voice recording app found. Please install your default voice recording app.", Toast.LENGTH_LONG).show();
            // redirect to app store for a voice-recording app
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sec.android.app.voicenote")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sec.android.app.voicenote")));
            }
            Toast.makeText(this,"Redirecting to the Play Store for a Voice Recording app", Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * Launches an intent to open files for both images and videos
     */
    private void selectMedia() {
        // MIME type for both images and videos
        String[] mimeTypes = {"image/*", "video/*"};

        // Create intent to pick data
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Sets allowed types to ALL types then filters only image/video mimetypes
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        mGetContent.launch(intent);
    }

    /**
     * Launches an intent to open a PDF file. Does not need any permissions
     */
    private void selectPdf(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        mGetContent.launch(intent);
    }

    private void takePicture() {
        tempUri = createImageUri();
        mTakePicture.launch(tempUri);
    }

    /**
     * Save the media to internal storage so it still exists even if user deletes from gallery
     * -- should be called right after the user picks their media
     *
     * @param mediaUri the URI of the media type
     * @return the internal storage URI
     */
    @Nullable
    private Uri saveMediaToInternalStorage(Uri mediaUri, NoteItem.ItemType itemType) {
        try {
            String fileExtension;
            String fileNamePrefix;
            switch (itemType) {
                case IMAGE:
                    fileExtension = ".png";
                    fileNamePrefix = "image_";
                    break;
                case VIDEO:
                    fileExtension = ".mp4";
                    fileNamePrefix = "video_";
                    break;
                case VOICE:
                    fileExtension = ".mp3";
                    fileNamePrefix = "audio_";
                    break;
                case PDF:
                    fileExtension = ".pdf";
                    fileNamePrefix = "pdf_";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported media type");
            }

            // Create appropriate filename with timestamp and create new file object
            String fileName = fileNamePrefix + System.currentTimeMillis() + fileExtension;
            File outputFile = new File(getFilesDir(), fileName);

            // opens a stream to write data to new file
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream inputStream = getContentResolver().openInputStream(mediaUri);

            if (itemType == NoteItem.ItemType.IMAGE) {
                // If image, process as bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            } else {
                // if video, audio, or pdf copy data directly
                byte[] buf = new byte[1024]; // buffer for data transfer
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
            }

            // close both streams
            inputStream.close();
            fos.close();

            // Return URI to saved media
            return FileProvider.getUriForFile(this, "com.example.journalapp.fileprovider", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle null in calling function
        }
    }

    /**
     * Creates a URI linked to internal storage for an image
     * @return
     */
     private Uri createImageUri(){
         String fileExtension = ".png";
         String fileNamePrefix = "image_";

         // Create appropriate filename with timestamp and create new file object
         String fileName = fileNamePrefix + System.currentTimeMillis() + fileExtension;
         File outputFile = new File(getFilesDir(), fileName);

         // Return the file's URI using FileProvider
         return FileProvider.getUriForFile(this, "com.example.journalapp.fileprovider", outputFile);
     }

    /**
     * Deletes media (image,video) given the URI, from internal storage
     *
     * @param mediaUri
     * @return
     */
    private boolean deleteMediaFromInternalStorage(Uri mediaUri) {
        try {
            // Get the file's name from the URI, build, then delete file
            String fileName = new File(mediaUri.getPath()).getName();
            File fileToDelete = new File(getFilesDir(), fileName);
            return fileToDelete.delete();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts an image into the note based on the position of the current focused item
     * Case 1: Focused Item: EditText && isEmpty -> Replace with image and push EditText down
     * Case 2: Focused Item: EditText && !(isEmpty) -> Put image underneath EditText and create new EditText under image
     * Case 3: Focused Item: Image -> Put image underneath Image
     *
     * @TODO missed case... more testing
     */
    public void insertMedia(Uri mediaUri, NoteItem.ItemType mediaType) {
        int focusedIndex = focusedItem;

        // Check if focus is within bounds of list
        if (focusedIndex >= 0 && focusedIndex < noteItems.size()) {
            // Get currently focused note item
            NoteItem focusedNoteItem = noteItems.get(focusedIndex);

            Log.e("Focus", "Focused item when inserting media: " + focusedItem);

            // Determine type of focused note item
            switch (focusedNoteItem.getType()) {
                case TEXT:
                    String textContent = focusedNoteItem.getContent();
                    if (textContent.isEmpty()) {
                        // Case 1: Empty EditText
                        noteItems.set(focusedIndex, new NoteItem(mediaType, null, mediaUri.toString(), focusedIndex));
                        noteItems.add(focusedIndex + 1, new NoteItem(NoteItem.ItemType.TEXT, null, "", focusedIndex + 1));
                        noteAdapter.notifyItemChanged(focusedIndex);
                        noteAdapter.notifyItemInserted(focusedIndex + 1);
                        Log.e("Media", "Media: " + mediaUri.toString() + " inserted to position: " + focusedIndex);
                    } else {
                        // Case 2: Non-Empty EditText
                        noteItems.add(focusedIndex + 1, new NoteItem(mediaType, null, mediaUri.toString(), focusedIndex + 1));
                        noteItems.add(focusedIndex + 2, new NoteItem(NoteItem.ItemType.TEXT, null, "", focusedIndex + 2));
                        noteAdapter.notifyItemRangeInserted(focusedIndex + 1, 2);
                        Log.e("Media", "Media: " + mediaUri.toString() + " inserted to position: " + focusedIndex + 1);
                    }
                    break;
                case IMAGE:
                case VIDEO:
                case VOICE:
                case PDF:
                    // Case 3: Image or Video Focused
                    noteItems.add(focusedIndex + 1, new NoteItem(mediaType, null, mediaUri.toString(), focusedIndex + 1));
                    noteAdapter.notifyItemInserted(focusedIndex + 1);
                    Log.e("Media", "Media: " + mediaUri.toString() + " inserted to position: " + focusedIndex + 1);
                    break;
            }
        } else {
            // If no item is focused, add the media at the end
            // @TODO insert a text after?? (new case)
            noteItems.add(new NoteItem(mediaType, null, mediaUri.toString(), noteItems.size()));
            noteAdapter.notifyItemInserted(noteItems.size() - 1);
        }

        // Update order indexes for all items following the insertion point
        for (int i = focusedIndex + 1; i < noteItems.size(); i++) {
            noteItems.get(i).setOrderIndex(i);
        }

        saveNoteContent();

        logNoteItems("Inserted Media");
    }

    // ==============================
    // REGION: Note Handling
    // ==============================

    /**
     * Clears all highlights based on NoteAdapter function.
     */
    public void clearHighlight() {
        Log.e("HighlightBug", "Before clearing highlights, the highlightedItem is: " + highlightedItem);
        if (highlightedItem >= 0) {
            Log.e("Highlights", "clearing highlights");
            noteAdapter.clearHighlights();
            highlightedItem = -1;
        }
    }

    /**
     * Deletes a View (EditText, ImageView, etc.)
     *
     * @param position
     */
    private void deleteItem(int position) {
        Log.e("Deletion", "Deleting an item. Position: " + position);

        // remove items from the list
        noteItems.remove(position);

        // notify adapter
        noteAdapter.notifyItemRemoved(position);

        logNoteItems("Contents before deletion");

        // update ordering for subsequent items in the list
        for (int i = position; i < noteItems.size(); i++) {
            noteItems.get(i).setOrderIndex(i);
        }

        logNoteItems("Contents after deletion");

        // notify the adapter of the item range changed for updating the view
        noteAdapter.notifyItemRangeChanged(position, noteItems.size() - position);

        saveNoteContent();
    }

    private void updateNoteItemsOrderIndex() {
        for (int i = 0; i < noteItems.size(); i++) {
            noteItems.get(i).setOrderIndex(i);
        }
    }

    // ==============================
    // REGION: Setting up Note Data
    // ==============================


    /**
     * Initialize a new note with a date and store it
     * in the database
     */
    private void setNewNote(String folder_id) {

        // Sorted by ISO 8601 format which is sortable via queries
        String currentDateStr = getDateAsString();
        note = new Note("", currentDateStr, 3,folder_id);
        noteViewModel.insertNote(note);

        // Initialize the contents of noteItems as a single EditText
        noteItems.add(new NoteItem(NoteItem.ItemType.TEXT, null, "", 0)); // Empty text for the user to start typing

        // set focusedItem to first item
        focusedItem = 0;

    }

    /**
     * Initializes the UI with data from an existing note based on the provided note ID.
     * It retrieves the note details and note items from the database and updates the UI.
     *
     * @param note_id String ID of the note to be loaded into the UI
     */
    private void setExistingNote(String note_id) {

        // Retrieve the note using the id on a background thread
        executorService.execute(() -> {

            // Part I of setting up RecyclerView (note items)
            List<NoteItemEntity> noteItemEntities = noteViewModel.getNoteItemsForNoteSync(note_id);

            // Part I of setting up Note metadata (title, etc.)
            Note fetchedNote = noteViewModel.getNoteById(note_id);
            if (fetchedNote != null) {

                // Use UI Thread to update UI with the fetched note
                runOnUiThread(() -> {

                    // Part II of setting up RecyclerView (note items)
                    List<NoteItem> newNoteItems = convertNoteItemEntitiesToNoteItems(noteItemEntities);

                    // update the list
                    noteItems.clear();
                    noteItems.addAll(newNoteItems);

                    // refresh recycleview
                    noteAdapter.notifyDataSetChanged();

                    // Part II of setting up Note metadata (title, etc.)
                    note = fetchedNote;
                    titleEditText.setText(note.getTitle());
                    updateEmotionImage();
                    logNoteItems("Items after setting existing note");
                });
            } else {
                // Handle the case where the note is null (e.`g., not found in the database)
                runOnUiThread(this::finish);
            }


        });

        focusedItem = 0;
    }

    // ==============================
    // REGION: Database Operations
    // ==============================

    /**
     * Saves title of the note both locally and in the current activity instance
     * Called in response to changes in the title via auto save
     *
     * @param title The note's title
     */
    public void saveNoteTitle(String title) {
        Log.d("TextWatcher", "Updating the title: " + title);
        note.setTitle(title);
        noteViewModel.updateNoteTitle(note);

        runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Title Saved", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves the contents of the note to the database
     * First checks if it should update or add content to the database
     * Usually called by program when changes are detected by auto save
     */
    public void saveNoteContent() {
        // must be done on a background thread
        executorService.execute(() -> {
            // Get the current list of note items from the database
            List<NoteItemEntity> currentNoteItems = noteViewModel.getNoteItemsForNoteSync(note.getId());

            // Create a list to hold the IDs of LOCAL note items for comparison
            List<String> localNoteItemIds = new ArrayList<>();
            for (NoteItem noteItem : noteItems) {
                localNoteItemIds.add(noteItem.getItemId());
            }

            // Determine which items have been deleted
            List<NoteItemEntity> itemsToDelete = new ArrayList<>();
            for (NoteItemEntity entity : currentNoteItems) {
                if (!localNoteItemIds.contains(entity.getItemId())) {
                    itemsToDelete.add(entity);
                }
            }

            // Delete the removed items from the database
            for (NoteItemEntity entity : itemsToDelete) {
                noteViewModel.deleteNoteItem(entity);

                // If the entity is of type IMAGE OR VIDEO, delete from internal storage as well
                if (entity.getType() != NoteItem.ItemType.TEXT.ordinal()) {
                    Uri imageUri = Uri.parse(entity.getContent()); // assuming the content is the URI in string format
                    boolean deleted = deleteMediaFromInternalStorage(imageUri);
                    if (!deleted) {
                        // Handle the case where the image couldn't be deleted
                        Log.e("Delete Image", "Failed to delete image from internal storage.");
                    }
                }
            }

            // Iterate over the LOCAL note items
            for (NoteItem noteItem : noteItems) {
                // Check if there are any matches between LOCAL noteItems and DATABASE noteItems
                NoteItemEntity matchingEntity = null;
                for (NoteItemEntity entity : currentNoteItems) {
                    if (entity.getItemId().equals(noteItem.getItemId())) {
                        matchingEntity = entity;
                        break;
                    }
                }

                // If there is a match, then we want to update it
                if (matchingEntity != null) {
                    // Update the existing entity with new content and order
                    matchingEntity.setContent(noteItem.getContent());
                    matchingEntity.setOrderIndex(noteItem.getOrderIndex());
                    noteViewModel.updateNoteItem(matchingEntity); // Update immediately
                } else {
                    // It's a new NoteItem, so create it in the database
                    NoteItemEntity newEntity = new NoteItemEntity(
                            noteItem.getItemId(), // Use the ID from NoteItem
                            note.getId(), // The ID of the note
                            noteItem.getType().ordinal(), // Convert enum to int
                            noteItem.getContent(),
                            noteItem.getOrderIndex()
                    );
                    noteViewModel.insertNoteItem(newEntity); // Insert immediately
                }
            }

            logNoteItems("Contents after saving");

            // Inform user of the save on the UI thread
            runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show());
        });
    }


    // ==============================
    // REGION: Exiting Note
    // ==============================

    /**
     * If back button is pressed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitNote(null);
    }

    /**
     * If user pauses the application
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            exitNote(null);
        }
    }

    /**
     * Remove the note from the database if there is, no
     * title or if the contents of the note is empty.
     *
     * @param view The button view that triggers the save operation
     */
    public void exitNote(View view) {
        String title = note.getTitle();

        /*
        if ((noteItems.size() == 1 && noteItems.get(0).getContent().equals("")) && title.isEmpty()) {
            Log.e("Exiting note", "Deleting note");
            noteViewModel.deleteNote(note);
        }
        
         */
        if (title.isEmpty()){
            saveNoteTitle("Note");
            saveNoteContent();
        }

        else {
            saveNoteContent(); // add to onExit?? method instead?
        }

        // set last edited date before exiting
        //@TODO currently, it only updates whenever note is visited, however should update if note is edited not visited
        noteViewModel.updateNoteLastEditedDate(getDateAsString(), note.getId());
        finish();
    }


    // ==============================
    // REGION: Other
    // ==============================

    /**
     * Testing Purposes. Can log all of the contents inside the NoteItems list.
     */
    public void logNoteItems(String message) {
        Log.d("NoteItemLog", message);
        if (noteItems.size() == 0){
            Log.d("NoteItemLog", "Note List is Empty");
        }
        for (int i = 0; i < noteItems.size(); i++) {
            NoteItem item = noteItems.get(i);
            Log.d("NoteItemLog", "Index: " + item.getOrderIndex() + ", Type: " + item.getType() + ", Content: " + item.getContent());
        }

    }

    public void logFocus(){
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            // Get the ID of the focused view
            int focusedViewId = currentFocusedView.getId();
            // Find the resource entry name of the ID
            String resourceName = getResources().getResourceEntryName(focusedViewId);
            Log.d("Focused View", "Current focused view is: " + resourceName);
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
}
