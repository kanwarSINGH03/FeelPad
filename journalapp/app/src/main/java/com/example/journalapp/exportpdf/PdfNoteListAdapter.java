package com.example.journalapp.exportpdf;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.journalapp.database.entity.Note;


public class PdfNoteListAdapter extends ListAdapter<Note, PdfNoteViewHolder> {

    private final SelectNoteForPdf selectNoteForPdf;

    /**
     * Constructor for a new NoteListAdapter
     * Note: DiffUtil utility calculates differences between old and new datasets (for updating page)
     *
     * @param diffCallback class that compares two items & contents in the old & new dataset
     */
    protected PdfNoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback, SelectNoteForPdf selectNoteForPdf) {
        super(diffCallback);
        this.selectNoteForPdf = selectNoteForPdf;
    }

    @NonNull
    @Override
    public PdfNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PdfNoteViewHolder.create(parent);
    }

    public void onBindViewHolder(PdfNoteViewHolder holder, int position) {
        Note current = getItem(position);
        holder.bind(current, selectNoteForPdf);
    }

    /**
     * Custom implementation of ItemCallBack for comparing Note objects
     */
    static class NoteDiff extends DiffUtil.ItemCallback<Note> {

        /**
         * Checks if two Notes represent the same item
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return boolean representing if items are the same
         */
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        /**
         * Checks if the content of two Notes are the same
         *
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