package com.example.journalapp.exportpdf;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteViewModel;

import java.util.Comparator;
import java.util.HashSet;

public class SelectPdfContentsActivity extends AppCompatActivity implements SelectNoteForPdf, Comparator<Note> {

    final static int REQUEST_CODE = 1234;
    private RecyclerView selectNoteRecyclerView;
    private PdfNoteListAdapter pdfNoteListAdapter;
    private Button selectAll;
    private HashSet<Note> selectedNotes = new HashSet<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedNotes = new HashSet<>();
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
        NoteViewModel noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotesOrderedByCreateDateDesc().observe(this, notes -> pdfNoteListAdapter.submitList(notes));
    }

    private void initWidgets() {
        selectAll = findViewById(R.id.noteSearchSelectAll);
        ImageButton cancelButton = findViewById(R.id.cancelNoteSearchButton);

        selectAll.setOnClickListener(View -> {
            if (selectedNotes.isEmpty()) {
                selectAll();
            } else {
                getPermissions();
                ExportPdfDialog exportPdfDialog = new ExportPdfDialog(this, selectedNotes);
                exportPdfDialog.show();
                Log.d("SelectPdfContentsAct", "Ready to export");
            }
        });

        cancelButton.setOnClickListener(View -> {
            finish();
        });
    }

    private void selectAll() {
        selectedNotes.addAll(pdfNoteListAdapter.getCurrentList());
        int elementCount = pdfNoteListAdapter.getItemCount();
        if (elementCount > 0) {
            selectAll.setText("Export");
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

    @Override
    public void unSelectNoteForPdf(Note note) {
        selectedNotes.remove(note);
        if (selectedNotes.isEmpty()) {
            selectAll.setText("Select All");
        }
    }

    @Override
    public void selectNoteForPdf(Note note) {
        selectedNotes.add(note);
        if (selectedNotes.size() == 1) {
            selectAll.setText("Export");
        }
    }

    @Override
    public int compare(Note note, Note t1) {
        return note.getCreatedDate().compareTo(t1.getCreatedDate());
    }
}
