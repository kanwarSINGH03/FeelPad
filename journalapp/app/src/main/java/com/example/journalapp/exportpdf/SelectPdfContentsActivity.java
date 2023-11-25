package com.example.journalapp.exportpdf;

import static com.example.journalapp.exportpdf.PdfNoteConstants.EXPORT;
import static com.example.journalapp.exportpdf.PdfNoteConstants.REQUEST_CODE;
import static com.example.journalapp.exportpdf.PdfNoteConstants.SELECT_ALL;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectPdfContentsActivity extends AppCompatActivity implements SelectNoteForPdf, Comparator<Note> {

    private RecyclerView selectNoteRecyclerView;
    private PdfNoteListAdapter pdfNoteListAdapter;
    private NoteRepository noteRepository;
    private Button selectAll;
    private final List<Note> selectedNotes = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteRepository = NoteRepository.getInstance(getApplication());
        setContentView(R.layout.activity_select_pdf);
        initWidgets();
        setNoteRecyclerView();
    }

    private void getPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{(Manifest.permission.WRITE_EXTERNAL_STORAGE)},
                REQUEST_CODE);
    }

    private void setNoteRecyclerView() {
        selectNoteRecyclerView = findViewById(R.id.selectNoteListView);
        pdfNoteListAdapter = new PdfNoteListAdapter(new PdfNoteListAdapter.NoteDiff(), this);
        selectNoteRecyclerView.setAdapter(pdfNoteListAdapter);
        selectNoteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        MainViewModel noteViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        noteViewModel.getAllNotesOrderedByLastEditedDateDesc().observe(this, notes -> pdfNoteListAdapter.submitList(notes));
    }

    private void initWidgets() {
        selectAll = findViewById(R.id.noteSearchSelectAll);
        ImageButton cancelButton = findViewById(R.id.cancelNoteSearchButton);

        selectAll.setOnClickListener(View -> {
            if (selectedNotes.isEmpty()) {
                selectAll();
            } else {
                getPermissions();
                generatePdf();
            }
        });

        cancelButton.setOnClickListener(View -> finish());
    }

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

    private void generatePdf() {
        PdfDocument noteBooklet = new PdfDocument();
        executorService.execute(() -> {
            Collections.sort(selectedNotes, (note1, note2) -> {
                Date dateNote1 = note1.getCreatedDateByType();
                Date dateNote2 = note2.getCreatedDateByType();
                return dateNote1.compareTo(dateNote2);
            });

            for (Note note : selectedNotes) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(420, 595, 826)
                        .create();
                PdfDocument.Page page = noteBooklet.startPage(pageInfo);

                Canvas pageCanvas = page.getCanvas();
                Paint pagePaint = new Paint();
                pagePaint.setColor(Color.GRAY);
                pagePaint.setTextSize(24);
                pagePaint.setTypeface(Typeface.create("", Typeface.ITALIC));
                pageCanvas.drawText(note.getTitle(), 10, 34, pagePaint);

                pagePaint.setColor(Color.BLACK);
                pagePaint.setTextSize(14);
                pageCanvas.drawText(note.getCreatedDate(), 10, 55, pagePaint);

                List<NoteItemEntity> noteItems =
                        noteRepository.getNoteItemsForNoteSync(note.getId());

                for (NoteItemEntity item : noteItems) {
                    int typeValue = item.getType();
                    NoteItem.ItemType itemType = ItemTypeConverter.toItemType(typeValue);
                    switch (itemType) {
                        case PDF:
                            break;
                        case TEXT:
                            pagePaint.setTextSize(12);
                            String htmlContent = item.getContent();
                            Spanned spannedText = Html.fromHtml(htmlContent);
                            pageCanvas.drawText(spannedText.toString(), 10, 75, pagePaint);
                            break;
                        case IMAGE:
                            break;
                        case VIDEO:
                            break;
                        case VOICE:
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

            Uri uri = Uri.fromFile(file);
            openPdf(uri);
        });
    }

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

    @Override
    public int compare(Note note, Note t1) {
        return note.getCreatedDate().compareTo(t1.getCreatedDate());
    }
}
