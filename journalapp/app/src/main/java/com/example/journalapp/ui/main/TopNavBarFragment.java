package com.example.journalapp.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.ui.home.FolderViewModel;
import com.example.journalapp.ui.note.NoteItem;
import com.example.journalapp.ui.note.NoteViewModel;
import com.example.journalapp.ui.note.PdfViewerFragment;
import com.example.journalapp.utils.ItemTypeConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopNavBarFragment extends Fragment {

    private static final String ARG_HIDE_BUTTONS = "hideButtons";
    private static final String ARG_SEARCH_TOGGLE = "searchToggle";
    private static final String ARG_FOLDER_ID = "folderId";
    private OnSearchQueryChangeListener searchQueryChangeListener;
    public static final int REQUEST_CODE = 1234;
    private List<Note> folderNotes;
    private String folderId;

    FolderViewModel folderViewModel;
    NoteViewModel noteViewModel;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static TopNavBarFragment newInstance(boolean hideButtons, boolean searchToggle, String folderId){
        TopNavBarFragment fragment = new TopNavBarFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HIDE_BUTTONS, hideButtons);
        args.putBoolean(ARG_SEARCH_TOGGLE, searchToggle);
        args.putString(ARG_FOLDER_ID, folderId);
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

                // set up popup menu
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.note_list_menu, popupMenu.getMenu());



                // handle menu item choices and clicks
                popupMenu.setOnMenuItemClickListener(menuItem ->{
                    if (menuItem.getItemId() == R.id.action_export_to_pdf){
                        getPdfPermissions();
                        pdfBookletGeneration();
                        Toast.makeText(getContext(), "Exporting to PDF", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });

                popupMenu.show();

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

    private void getPdfPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( //Method of Fragment
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private void pdfBookletGeneration() {
        folderId = getArguments().getString(ARG_FOLDER_ID);

        folderViewModel = new ViewModelProvider(this).get(FolderViewModel.class);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        executorService.execute(() ->{
            folderNotes = folderViewModel.getAllNotesFromFolderOrderByLastEditedDateDescSync(folderId);

            Uri bookletPdfUri = generatePdf();
            if (Objects.nonNull(bookletPdfUri)) {
                assert bookletPdfUri != null; // Redundant check for compiler
                openPdf(bookletPdfUri);
            }
        });

    }

    private Uri generatePdf() {
        PdfDocument noteBooklet = new PdfDocument();
        final int PAGE_HEIGHT = 595;
        final int PAGE_WIDTH = 420;
        final int PAGE_MARGIN = 14;

        final int TITLE_TEXT_SIZE = 24;
        final int TITLE_START_X = 10;
        final int TITLE_START_Y = 34;
        final int TITLE_TEXT_COLOR = Color.GRAY;

        final int DATE_START_X = 10;
        final int DATE_START_Y = 44;
        final int DATE_TEXT_SIZE = 8;
        final int DATE_TEXT_COLOR = Color.GRAY;

        final int CONTENT_TEXT_SIZE = 12;
        final int CONTENT_START_X = 14;
        final int CONTENT_MARGIN = 14;
        final int CONTENT_START_Y = DATE_START_Y + CONTENT_MARGIN + 10;
        final int CONTENT_WIDTH = PAGE_WIDTH - (CONTENT_MARGIN * 2);
        final int CONTENT_TEXT_COLOR = Color.BLACK;

        int PAGE_BOTTOM = PAGE_HEIGHT - PAGE_MARGIN;

        int pageNumber = 0;
        // TODO: Query to get all notes in folder ordered by creation date -> selectedNote List<Notes>
        for (Note note : folderNotes) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            PdfDocument.Page page = noteBooklet.startPage(pageInfo);
            Canvas pageCanvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setColor(TITLE_TEXT_COLOR);
            titlePaint.setTextSize(TITLE_TEXT_SIZE);
            titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
            pageCanvas.drawText(note.getTitle(), TITLE_START_X, TITLE_START_Y, titlePaint);

            Paint contentPaint = new Paint();
            contentPaint.setColor(DATE_TEXT_COLOR);
            contentPaint.setTextSize(DATE_TEXT_SIZE);
            pageCanvas.drawText(note.getCreatedDate(), DATE_START_X, DATE_START_Y, contentPaint);

            List<NoteItemEntity> noteItems = noteViewModel.getNoteItemsForNoteSync(note.getId());

            int y_cursor = CONTENT_START_Y;
            for (NoteItemEntity item : noteItems) {
                int typeValue = item.getType();

                NoteItem.ItemType itemType = ItemTypeConverter.toItemType(typeValue);
                switch (itemType) {
                    case TEXT:
                        titlePaint.setTextSize(12);
                        TextPaint textPaint = new TextPaint(titlePaint);
                        textPaint.setTextSize(CONTENT_TEXT_SIZE);
                        textPaint.setColor(CONTENT_TEXT_COLOR);
                        String htmlContent = item.getContent();
                        Spanned spannedText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY);
                        StaticLayout layout = StaticLayout.Builder.obtain(spannedText, 0, spannedText.length(), textPaint, CONTENT_WIDTH)
                                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                .build();
                        String[] lines = layout.getText().toString().split(System.lineSeparator());
                        for (String line : lines) {
                            String[] words = line.split(" ");

                            StringBuilder stringBuilder = new StringBuilder();
                            int i = 0;
                            for (String word : words) {
                                i = i + 1;
                                if (y_cursor > PAGE_BOTTOM) {
                                    if (item.getOrderIndex() != noteItems.size()) {
                                        noteBooklet.finishPage(page);
                                        pageNumber = pageNumber + 1;
                                        pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                                        page = noteBooklet.startPage(pageInfo);
                                        pageCanvas = page.getCanvas();
                                        y_cursor = PAGE_MARGIN;
                                    }
                                }

                                stringBuilder.append(word).append(" ");
                                if ((stringBuilder.length() > (CONTENT_WIDTH / 10)) | (i == words.length)) {
                                    pageCanvas.drawText(stringBuilder.toString(), CONTENT_START_X, y_cursor, textPaint);
                                    y_cursor = y_cursor + CONTENT_MARGIN;
                                    stringBuilder = new StringBuilder();
                                }
                            }
                        }
                        break;
                    case PDF:
                        /* No support for pdf documents */
                        break;
                    case IMAGE:
                        Uri imageUri = Uri.parse(item.getContent());
                        Bitmap imageBitmap = null;

                        try {
                            imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                        } catch (FileNotFoundException e) {
                            Log.d("SelectPdfContents", "Image not found for note element with id " + item.getItemId() + " in note with id " + note.getId());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if (Objects.nonNull(imageBitmap)) {
                            assert imageBitmap != null;
                            boolean needsAdjustment = false;
                            int imageBitmapTempWidth = imageBitmap.getWidth();
                            int imageBitmapTempHeight = imageBitmap.getHeight();

                            // Adjust the image to fit on page width wise
                            if (imageBitmapTempWidth > CONTENT_WIDTH) {
                                needsAdjustment = true;
                                float bitmapWidthAdjustmentRation = (float) CONTENT_WIDTH / (float) imageBitmapTempWidth;
                                imageBitmapTempWidth = (int) (imageBitmapTempWidth * bitmapWidthAdjustmentRation);
                                imageBitmapTempHeight = (int) (imageBitmapTempHeight * bitmapWidthAdjustmentRation);
                            }

                            // Adjust the newly adjust image to fit on the page height wise
                            int heightAvailable = PAGE_BOTTOM - y_cursor;
                            if (imageBitmapTempHeight > heightAvailable) {
                                float bitmapHeightAdjustmentMaximum = 0.75f;

                                needsAdjustment = true;
                                float bitmapHeightAdjustmentRation = (float) heightAvailable / (float) imageBitmapTempHeight;

                                // Images that exceed a limit can be placed on a new page
                                if (bitmapHeightAdjustmentRation < bitmapHeightAdjustmentMaximum) {
                                    noteBooklet.finishPage(page);
                                    pageNumber = pageNumber + 1;
                                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                                    page = noteBooklet.startPage(pageInfo);
                                    pageCanvas = page.getCanvas();
                                    y_cursor = PAGE_MARGIN;
                                    heightAvailable = PAGE_BOTTOM - y_cursor;
                                    bitmapHeightAdjustmentRation = (float) heightAvailable / (float) imageBitmapTempHeight;
                                }

                                // Adjust the height and width to fit on rest of page or new page
                                imageBitmapTempWidth = (int) (imageBitmapTempWidth * bitmapHeightAdjustmentRation);
                                imageBitmapTempHeight = (int) (imageBitmapTempHeight * bitmapHeightAdjustmentRation);
                            }

                            if (needsAdjustment) {
                                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmapTempWidth, imageBitmapTempHeight, true);
                            }

                            pageCanvas.drawBitmap(imageBitmap, CONTENT_START_X, y_cursor, new Paint());
                            y_cursor = y_cursor + imageBitmap.getHeight() + CONTENT_MARGIN;
                        }
                        break;
                    default:
                        break;
                }
            }

            noteBooklet.finishPage(page);
        }

            /*

            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "notebook.pdf";
            File file = new File(downloadFolder, fileName);

             */
        String fileExtension = ".pdf";
        String fileNamePrefix = "pdf_";

        // Create appropriate filename with timestamp and create new file object
        String fileName = fileNamePrefix + System.currentTimeMillis() + fileExtension;
        File outputFile = new File(requireContext().getFilesDir(), fileName);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            noteBooklet.writeTo(fileOutputStream);
            noteBooklet.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.d("ExportPdfDialog", "Error finding file " + e);
        } catch (IOException e) {
            Log.d("ExportPdfDialog", "I/O Exception");
            throw new RuntimeException(e);
        }

        return FileProvider.getUriForFile(requireContext(), "com.example.journalapp.fileprovider", outputFile);
    }

    private void openPdf(Uri pdfUri) {
        Log.i("SelectPdfContentsAct", "Pdf of given URI: " + pdfUri.toString());
        PdfViewerFragment pdfViewerFragment = PdfViewerFragment.newInstance(pdfUri);
        FragmentManager fragmentManager = getParentFragmentManager();
        pdfViewerFragment.show(fragmentManager, "audioPlayer");
    }

}