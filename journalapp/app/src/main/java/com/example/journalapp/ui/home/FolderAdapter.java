package com.example.journalapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter{

    private List<FolderItem> folderList; // folderItems passed from activity

    /**
     * Constructs the FolderAdapter with a list of FolderItems
     * @param folderList
     */
    public FolderAdapter(List<FolderItem> folderList){
        this.folderList = folderList;
    }


    /**
     * Inflate the viewHolder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder,parent,false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FolderItem folderItem = folderList.get(position);
        ((FolderViewHolder) holder).bind(folderItem);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder{

        private ImageView icon;
        private TextView title;
        private ProgressBar dataBar;
        private TextView noteCount;
        private ConstraintLayout folderHolder;
        private FolderItem currentFolderItem;







        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.folder_icon);
            this.title = itemView.findViewById(R.id.folder_title);
            this.dataBar = itemView.findViewById(R.id.folder_data_bar);
            this.noteCount = itemView.findViewById(R.id.folder_note_count);
            this.folderHolder = itemView.findViewById(R.id.folder_holder);

        }

        public void bind(FolderItem folderItem){
            currentFolderItem = folderItem;

            // set folder color
            folderHolder.setBackgroundResource(folderItem.getFolderColor());

            // set icon
            icon.setImageResource(folderItem.getIconResourceId());

            // set title
            title.setText(currentFolderItem.getTitle());

            // set progress bar
            dataBar.setProgress((int) folderItem.getEmotionPercentage());

            // set noteCount
            noteCount.setText(folderItem.getNumItemsAsString());



        }
    }
}
