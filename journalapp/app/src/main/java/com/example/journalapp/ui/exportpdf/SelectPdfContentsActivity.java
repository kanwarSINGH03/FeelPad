package com.example.journalapp.ui.exportpdf;

import static com.example.journalapp.ui.exportpdf.PdfNoteConstants.EXPORT;
import static com.example.journalapp.ui.exportpdf.PdfNoteConstants.REQUEST_CODE;
import static com.example.journalapp.ui.exportpdf.PdfNoteConstants.SELECT_ALL;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.ui.main.MainViewModel;
import com.example.journalapp.ui.note.NoteItem;
import com.example.journalapp.ui.note.PdfViewerFragment;
import com.example.journalapp.utils.ItemTypeConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Select the contents for a pdf booklet and export to Pdf.
 */
public class SelectPdfContentsActivity extends AppCompatActivity implements SelectNoteForPdf {

    private RecyclerView selectNoteRecyclerView;
    private PdfNoteListAdapter pdfNoteListAdapter;
    private NoteRepository noteRepository;
    private Button selectAll;
    private final PdfNoteList<Note> selectedNotes = new PdfNoteList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteRepository = NoteRepository.getInstance(getApplication());
        setContentView(R.layout.activity_select_pdf);
        initWidgets();
        setNoteRecyclerView();
    }

    /**
     * Initialize the widgets for the select notes for pdf page
     */
    private void initWidgets() {
        selectAll = findViewById(R.id.noteSearchSelectAll);
        ImageButton cancelButton = findViewById(R.id.cancelNoteSearchButton);

        selectAll.setOnClickListener(View -> {
            if (selectedNotes.isEmpty()) {
                selectAll();
            } else {
                getPermissions();
                pdfBookletGeneration();
            }
        });

        cancelButton.setOnClickListener(View -> finish());
    }

    /**
     * Select all button to select all notes in current view
     */
    private void selectAll() {
        selectedNotes.addAll(pdfNoteListAdapter.getCurrentList());
        int elementCount = pdfNoteListAdapter.getItemCount();

        if (elementCount > 0) {
            selectAll.setText(EXPORT);
        }

        for (int i = 0; i < elementCount; i++) {
            Log.d("SelectPdfContentAct", "Creating on checked listener " + i);
            PdfNoteViewHolder pdfNoteViewHolder = (PdfNoteViewHolder) selectNoteRecyclerView.findViewHolderForAdapterPosition(i);
            if (pdfNoteViewHolder != null) {
                CheckBox addToPdf = pdfNoteViewHolder.itemView.findViewById(R.id.noteCheckBox);
                addToPdf.setChecked(true);
            }
        }
    }

    /**
     * Request permission for saving pdf to external storage
     */
    private void getPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{(Manifest.permission.WRITE_EXTERNAL_STORAGE)}, REQUEST_CODE);
    }

    /**
     * Sets the Recycle view for the select notes for pdf page
     */
    private void setNoteRecyclerView() {
        selectNoteRecyclerView = findViewById(R.id.selectNoteListView);
        pdfNoteListAdapter = new PdfNoteListAdapter(new PdfNoteListAdapter.NoteDiff(), this);
        selectNoteRecyclerView.setAdapter(pdfNoteListAdapter);
        selectNoteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        MainViewModel noteViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        noteViewModel.getAllNotesOrderedByLastEditedDateDesc().observe(this, notes -> pdfNoteListAdapter.submitList(notes));
    }

    /**
     * Generate the pdf booklet for the selected notes, and launches viewer
     */
    private void pdfBookletGeneration() {
        organizeSelectedNotesByDate();
        executorService.execute(() -> {
            Uri bookletPdfUri = generatePdf();
            if (Objects.nonNull(bookletPdfUri)) {
                assert bookletPdfUri != null; // Redundant check for compiler
                openPdf(bookletPdfUri);
            }
        });
    }

    /**
     * Reorder the list of selected notes by created date
     */
    private void organizeSelectedNotesByDate() {
        Collections.sort(selectedNotes, (note1, note2) -> {
            Date dateNote1 = note1.getCreatedDateByType();
            Date dateNote2 = note2.getCreatedDateByType();
            return dateNote1.compareTo(dateNote2);
        });
    }

    /**
     * Generate a combine pdf of selected notes, and save the booklet to external storage
     */
    private Uri generatePdf() {
        if (Build.VERSION.SDK_INT >= 34) {
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
            // TODO: Properly style the generated pdf, add page number and title page
            for (Note note : selectedNotes) {
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

                List<NoteItemEntity> noteItems = noteRepository.getNoteItemsForNoteSync(note.getId());

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
                                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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

            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "notebook.pdf";
            File file = new File(downloadFolder, fileName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                noteBooklet.writeTo(fileOutputStream);
                noteBooklet.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                Log.d("ExportPdfDialog", "Error finding file " + e);
            } catch (IOException e) {
                Log.d("ExportPdfDialog", "I/O Exception");
                throw new RuntimeException(e);
            }

            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Open pdf with Uri
     *
     * @param pdfUri The pdf uri
     */
    private void openPdf(Uri pdfUri) {
        Log.i("SelectPdfContentsAct", "Pdf of given URI: " + pdfUri.toString());
        PdfViewerFragment pdfViewerFragment = PdfViewerFragment.newInstance(pdfUri);
        FragmentManager fragmentManager = getSupportFragmentManager();
        pdfViewerFragment.show(fragmentManager, "audioPlayer");
    }

    @Override
    public void unSelectNoteForPdf(Note note) {
        selectedNotes.remove(note);
        if (selectedNotes.isEmpty()) {
            selectAll.setText(SELECT_ALL);
        }
    }

    @Override
    public void selectNoteForPdf(Note note) {
        selectedNotes.add(note);
        if (selectedNotes.size() == 1) {
            selectAll.setText(EXPORT);
        }
    }
}
