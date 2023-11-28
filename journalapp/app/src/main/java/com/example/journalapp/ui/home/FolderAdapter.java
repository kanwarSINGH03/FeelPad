package com.example.journalapp.ui.home;

import android.icu.text.CaseMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;

import java.util.List;

public class FolderAdapter extends ListAdapter<Folder, FolderAdapter.FolderViewHolder> {

    public FolderAdapter(@NonNull DiffUtil.ItemCallback<Folder> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = getItem(position);
        holder.bind(folder);
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        private ProgressBar dataBar;
        private TextView noteCount;
        private ConstraintLayout folderHolder;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.folder_icon);
            this.title = itemView.findViewById(R.id.folder_title);
            this.dataBar = itemView.findViewById(R.id.folder_data_bar);
            this.noteCount = itemView.findViewById(R.id.folder_note_count);
            this.folderHolder = itemView.findViewById(R.id.folder_holder);
        }

        public void bind(Folder folder) {
            folderHolder.setBackgroundColor(folder.getFolderColor());
            icon.setImageResource(folder.getIconResourceId());
            title.setText(folder.getFolderName());
            dataBar.setProgress((int) folder.getEmotionPercentage());
            noteCount.setText(String.valueOf(folder.getNumItems()));
        }
    }

    static class NoteDiff extends DiffUtil.ItemCallback<Folder>{
        @Override
        public boolean areItemsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return oldItem.getFolderId().equals(newItem.getFolderId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Folder oldItem, @NonNull Folder newItem) {
            return oldItem.equals(newItem);
        }
    }
}
