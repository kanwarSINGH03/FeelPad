package com.example.journalapp.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

import java.io.File;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfViewHolder> {

    private Context context;
    private List<File> pdfFiles;


    public PdfAdapter(Context context, List<File> pdfFiles) {
        this.context = context;
        this.pdfFiles = pdfFiles;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PdfViewHolder(LayoutInflater.from(context).inflate(R.layout.pdf_item,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {

        holder.txtName.setText(pdfFiles.get(position).getName());
        holder.txtName.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }
}
