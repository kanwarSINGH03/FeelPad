package com.example.journalapp.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.ui.main.MainActivity;

public class FolderViewHolder extends RecyclerView.ViewHolder {
    // 定义视图元素
    private final TextView folderNameTextView;
    private final TextView folderDateTextView;

    private String folderId;

    public FolderViewHolder(@NonNull View itemView) {
        super(itemView);
        //this.folderId = folderId;
        folderNameTextView = itemView.findViewById(R.id.folderName);
        folderDateTextView = itemView.findViewById(R.id.folder_date);
    }

    public void bind(String folderName, String folderDate, String folderId) {
        folderNameTextView.setText(folderName);
        folderDateTextView.setText(folderDate);
        this.folderId = folderId;

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("FOLDER_ID", folderId);
            v.getContext().startActivity(intent);
        });

        itemView.setOnLongClickListener(v -> {
            Folder folder = new Folder(folderName,folderDate,R.drawable.ic_folder_flag,R.color.colorAccentGrey); // 假设 Folder 类有相应的构造函数或者方法来设置属性
            folder.setFolderId(folderId);
            // 设置其他必要的 folder 属性

            ((PasswordActivity) v.getContext()).showEncryptionDialog(folder);
            return true;
        });


    }
    public static FolderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_cell, parent, false);
        return new FolderViewHolder(view);
    }



}