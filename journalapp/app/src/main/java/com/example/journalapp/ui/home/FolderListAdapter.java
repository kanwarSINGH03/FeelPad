package com.example.journalapp.ui.home;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.journalapp.database.entity.Folder;

public class FolderListAdapter extends ListAdapter<Folder, FolderViewHolder> {

    public FolderListAdapter(@NonNull DiffUtil.ItemCallback<Folder> diffCallback) {
        super(diffCallback);
    }
    private void showEncryptionDialog(Folder folder, Context context) {
        if (folder.getEncrypted()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Remove Encryption");

            builder.setPositiveButton("OK", (dialog, which) -> {
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Set Encryption");

            builder.setPositiveButton("OK", (dialog, which) -> {
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
    }


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FolderViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull  FolderViewHolder holder, int position) {
        Folder currentFolder = getItem(position);
        String folderName = currentFolder.getFolderName();
        String folderDate = currentFolder.getCreate_date();
        String folderId = currentFolder.getFolderId();
        holder.bind(folderName,folderDate,folderId);
        //long click listener for secure function here
        holder.itemView.setOnLongClickListener(view -> {
            showEncryptionDialog(currentFolder, view.getContext());
            return true; // 返回 true 表示消费了长按事件
        });
    }

    public static class FolderDiff extends DiffUtil.ItemCallback<Folder> {
        @Override
        public boolean areItemsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return oldItem.getFolderId().equals(newItem.getFolderId());
        }
    }
}