package com.example.journalapp.ui.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.icu.text.CaseMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Folder;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.ui.main.MainNoteListActivity;
import com.example.journalapp.ui.note.NoteActivity;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends ListAdapter<Folder, FolderAdapter.FolderViewHolder> {

    FolderClickListener folderClickListener;
    FolderLongClickListener folderLongClickListener;

    /**
     * Constructor for FolderAdapter
     * @param diffCallback
     */
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

        // handle Callback to activity when a folder is clicked
        holder.itemView.setOnClickListener(v ->{

            folderClickListener.onFolderClicked(position);

        });

        // handle Callback to activity when a folder is long clicked
        holder.itemView.setOnLongClickListener(v -> {
            if (folderLongClickListener != null) {
                folderLongClickListener.onFolderLongClicked(position);
            }
            return true;
        });



    }

    /**
     * Retrieve the Folder at a given position in a list
     * @param position int position inside the database
     * @return Folder object
     */
    public Folder getFolderAt(int position){
        return getItem(position);
    }

    /**
     * Remove a folder at a given position in the list
     * @param position int position inside the database
     * @return Folder object
     */
    public void removeFolderAt(int position) {
        List<Folder> currentList = new ArrayList<>(getCurrentList());
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            submitList(currentList);
        }
    }

    /**
     * Callback click interface for the attached Activity
     */
    public interface FolderClickListener {
        void onFolderClicked(int position);
    }

    /**
     * Callback long-click interface for the attached Activity
     */
    public interface FolderLongClickListener {
        void onFolderLongClicked(int position);
    }

    /**
     * Set the listener
     * Usually done from the activity => adapter.setlistener(this)
     * @param listener
     */
    public void setFolderLongClickListener(FolderLongClickListener listener){
        this.folderLongClickListener = listener;
    }

    /**
     * Set the listener
     * Usually done from the activity => adapter.setlistener(this)
     * @param listener
     */
    public void setFolderClickListener(FolderClickListener listener){
        this.folderClickListener = listener;
    }

    /**
     * Class that represents the viewHolder for each folder
     */
    static class FolderViewHolder extends RecyclerView.ViewHolder {

        // UI Components
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

            // sets the color of the ripple drawable (combines ripple and color of folder)
            Drawable background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ripple_folder);
            if (background instanceof RippleDrawable) {
                RippleDrawable rippleDrawable = (RippleDrawable) background;
                Drawable colorDrawable = rippleDrawable.getDrawable(0);
                if (colorDrawable instanceof ShapeDrawable) {
                    ((ShapeDrawable) colorDrawable).getPaint().setColor(folder.getFolderColor());
                } else if (colorDrawable instanceof GradientDrawable) {
                    ((GradientDrawable) colorDrawable).setColor(folder.getFolderColor());
                }
                folderHolder.setBackground(rippleDrawable);
            }

            // set other UI
            icon.setImageResource(folder.getIconResourceId());
            title.setText(folder.getFolderName());

            // Update progress bar
            updateProgressBar(folder.getEmotionPercentage());
            noteCount.setText(String.valueOf(folder.getNumItems()));
        }

        /**
         * Updates the progress bar
         * @param emotionPercentage float percentage of average emotion level inside the folder
         */
        private void updateProgressBar(float emotionPercentage){
            Log.e("EmotionPercentage", "emotion percentage is: " + emotionPercentage);
            int color;
            if (emotionPercentage <= 25){
                color = R.color.colorAccentRed;
            } else if (emotionPercentage <= 50){
                color = R.color.colorAccentLightRed;
            } else if (emotionPercentage <= 75){
                color = R.color.colorAccentBlueGreen;
            } else {
                color = R.color.colorAccentYellow;
            }

            dataBar.setProgress((int) emotionPercentage);
            dataBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), color)));

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
