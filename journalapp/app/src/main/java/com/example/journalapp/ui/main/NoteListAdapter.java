package com.example.journalapp.ui.main;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.note.NoteActivity;
import com.example.journalapp.database.entity.Note;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * Custom adapter for managing a Recyclerviews' list of notes
 * ListAdapter automatically executes our custom onCreateViewHolder(), onBindViewHolder(), and NoteDiff methods when needed
 */
public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    /**
     * Constructor for a new NoteListAdapter
     * Note: DiffUtil utility calculates differences between old and new datasets (for updating page)
     * @param diffCallback class that compares two items & contents in the old & new dataset
     */
    protected NoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback) {
        super(diffCallback);
    }


    /**
     * Creates a new ViewHolder (entry) into the RecyclerView when needed
     * Automatically created and reused, each time calling onBindViewHolder()
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return a new NoteViewHolder that holds the View for a lsit item
     */
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note_list_holder, parent, false);
        return new NoteViewHolder(view);
    }

    /**
     * Binds data to the View within the ViewHolder at the specified position
     * Shows the Title & Desc in the RecyclerView
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = getItem(position);
        String title = note.getTitle();

        Log.d("NoteListAdapter", "onBindViewHolder title: " + title);
        holder.bind(note);


        /**
         * Click listener for each entry inside RecyclerView to link to a note
         */
        holder.itemView.setOnClickListener(v -> {

            // Intent to go to the note editing page
            Intent intent = new Intent(v.getContext(), NoteActivity.class);

            // If note has an ID, its existing, otherwise, it is a new note
            intent.putExtra("note_id", note.getId());
            v.getContext().startActivity(intent);

        });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;

        // depends on what the first item of the note is
        private TextView subheaderTextView;
        private ShapeableImageView subheaderImageView;
        private TextView dateTextView;

        // private TextView location
        // private ImageView emotion


        public NoteViewHolder(@NonNull View itemView){
            super(itemView);

            // find parts
            titleTextView = itemView.findViewById(R.id.item_note_list_title);
            subheaderTextView = itemView.findViewById(R.id.item_note_list_subheader_text);
            subheaderImageView = itemView.findViewById(R.id.item_note_list_subheader_image);
            dateTextView = itemView.findViewById(R.id.item_note_list_last_edited);
        }

        public void bind(Note note){
            titleTextView.setText(note.getTitle());
            
            // check if first notes noteitem is image or text
                // if text, just set subheader text view
                // if image, set textview to GONE, set imageView to VISIBLE, and set image source
        }
    }

    /**
     * Custom implementation of ItemCallBack for comparing Note objects
     */
    static class NoteDiff extends DiffUtil.ItemCallback<Note> {

        /**
         * Checks if two Notes represent the same item
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return boolean representing if items are the same
         */
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Checks if the content of two Notes are the same
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return boolean representing whether content is the same
         */
        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.equals(newItem);
        }
    }

}
