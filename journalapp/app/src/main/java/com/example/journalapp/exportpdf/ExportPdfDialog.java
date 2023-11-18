package com.example.journalapp.exportpdf;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.journalapp.R;
import com.example.journalapp.note.Note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExportPdfDialog extends Dialog {
    private final List<Note> noteList = new ArrayList<>();

    public ExportPdfDialog(@NonNull Context context, Set<Note> pdfNotes) {
        super(context);
        setContentView(R.layout.activity_export_pdf);
        if (pdfNotes.isEmpty()) {
            dismiss();
        }
        noteList.addAll(pdfNotes);
        initWidgets();
        generatePdf();
    }

    private void generatePdf() {
        PdfDocument noteBooklet = new PdfDocument();

        for (Note note : noteList) {
            PageInfo pageInfo = new PdfDocument.PageInfo.Builder(420, 595, 826)
                    .create();
            Page page = noteBooklet.startPage(pageInfo);

            Canvas pageCanvas = page.getCanvas();
            Paint pagePaint = new Paint();
            pagePaint.setColor(Color.GRAY);
            pagePaint.setTextSize(24);
            pagePaint.setTypeface(Typeface.create("", Typeface.ITALIC));
            pageCanvas.drawText(note.getTitle(), 10, 34, pagePaint);

            pagePaint.setColor(Color.BLACK);
            pagePaint.setTextSize(14);
            pageCanvas.drawText(note.getCreatedDate(), 10, 55, pagePaint);

            pagePaint.setTextSize(12);
            pageCanvas.drawText(note.getDescription(), 10, 75, pagePaint);

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
    }

    private void initWidgets() {
        ImageButton exitPdfDialog = findViewById(R.id.cancelGeneratePdf);

        exitPdfDialog.setOnClickListener(view -> dismiss());
    }

}
