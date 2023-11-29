package com.example.journalapp.ui.main;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journalapp.R;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.ui.home.FolderViewModel;
import com.example.journalapp.ui.note.NoteActivity;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.ui.note.NoteItem;
import com.example.journalapp.utils.ConversionUtil;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom adapter for managing a Recyclerviews' list of notes
 * ListAdapter automatically executes our custom onCreateViewHolder(), onBindViewHolder(), and NoteDiff methods when needed
 */
public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    private MainViewModel mainViewModel;
    private LifecycleOwner lifecycleOwner;


    /**
     * Constructor for a new NoteListAdapter
     * Note: DiffUtil utility calculates differences between old and new datasets (for updating page)
     * @param diffCallback class that compares two items & contents in the old & new dataset
     */
    protected NoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback, MainViewModel mainViewModel, LifecycleOwner lifecycleOwner) {
        super(diffCallback);
        this.mainViewModel = mainViewModel;
        this.lifecycleOwner = lifecycleOwner;
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

        mainViewModel.getFirstNoteItemByNoteId(note.getId()).observe(lifecycleOwner, noteItem -> {
            if (noteItem != null) {
                holder.bind(note, noteItem);
            } else {
                // Handle the case where there is no first item (or it's not yet loaded)
                holder.bind(note, null);
            }
        });


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

    public Note getNoteAt(int position){
        return getItem(position);
    }

    public void removeNoteAt(int position) {
        // Create a new list that excludes the item at the specified position
        List<Note> currentList = new ArrayList<>(getCurrentList());
        if (position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            submitList(currentList);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout noteHolder;
        private TextView titleTextView;
        private TextView subheaderTextView;
        private ShapeableImageView subheaderImageView;
        private FrameLayout subheaderVideoView;
        private FrameLayout subheaderAudioView;
        private TextView dateTextView;
        private ImageView emotionImageView;

        // private TextView location


        public NoteViewHolder(@NonNull View itemView){
            super(itemView);

            // find parts
            noteHolder = itemView.findViewById(R.id.item_note_list_holder);
            titleTextView = itemView.findViewById(R.id.item_note_list_title);
            subheaderTextView = itemView.findViewById(R.id.item_note_list_subheader_text);
            subheaderImageView = itemView.findViewById(R.id.item_note_list_subheader_image);
            subheaderVideoView = itemView.findViewById(R.id.item_note_list_subheader_video);
            subheaderAudioView = itemView.findViewById(R.id.item_note_list_subheader_audio);

            dateTextView = itemView.findViewById(R.id.item_note_list_last_edited);
            emotionImageView = itemView.findViewById(R.id.item_note_list_emotion);
        }

        public void bind(Note note, NoteItemEntity firstNoteItem){
            // set the title
            titleTextView.setText(note.getTitle());

            // for subheader (depends on whether first item is a text or image)
            if (firstNoteItem != null) {
                if (firstNoteItem.getType() == NoteItem.ItemType.TEXT.ordinal() && !ConversionUtil.stripHtmlTags(firstNoteItem.getContent()).isEmpty()) {
                    setAllGone();
                    subheaderTextView.setVisibility(View.VISIBLE);
                    subheaderTextView.setText(ConversionUtil.htmlToSpannable(firstNoteItem.getContent()));
                } else if (firstNoteItem.getType() == NoteItem.ItemType.IMAGE.ordinal()) {
                    setAllGone();
                    subheaderImageView.setVisibility(View.VISIBLE);
                    Uri imageUri = Uri.parse(firstNoteItem.getContent());
                    Glide.with(subheaderImageView.getContext())
                            .load(imageUri)
                            .into(subheaderImageView);
                } else if (firstNoteItem.getType() == NoteItem.ItemType.VIDEO.ordinal()) {
                    setAllGone();
                    subheaderVideoView.setVisibility(View.VISIBLE);
                } else if (firstNoteItem.getType() == NoteItem.ItemType.VOICE.ordinal()){
                    setAllGone();
                    subheaderTextView.setVisibility(View.GONE);
                }
                else {
                    setAllGone();
                    subheaderTextView.setVisibility(View.VISIBLE);
                    subheaderTextView.setText("First item is empty :(");
                }
            }

            // take the date and reformat for setting
            String originalDateString = note.getLastEditedDate();

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);

            // transform the date string
            try {
                Date date = originalFormat.parse(originalDateString);
                String formattedDateString = "Last Edited: " + newFormat.format(date);
                dateTextView.setText(formattedDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // set the emotion
            switch (note.getEmotion()){
                case 1:
                    emotionImageView.setImageResource(R.drawable.ic_note_emotion_horrible);
                    // noteHolder.setBackgroundResource(R.color.colorAccentRed);
                    setBackgroundConstraintLayout(noteHolder,R.color.colorAccentRed);
                    break;
                case 2:
                    emotionImageView.setImageResource(R.drawable.ic_note_emotion_disappointed);
                    // noteHolder.setBackgroundResource(R.color.colorAccentLightRed);
                    setBackgroundConstraintLayout(noteHolder,R.color.colorAccentLightRed);
                    break;
                case 3:
                    emotionImageView.setImageResource(R.drawable.ic_note_emotion_neutral);
                    // noteHolder.setBackgroundResource(R.color.colorAccentGreyBlue);
                    setBackgroundConstraintLayout(noteHolder,R.color.colorAccentGreyBlue);
                    break;
                case 4:
                    emotionImageView.setImageResource(R.drawable.ic_note_emotion_happy);
                    // noteHolder.setBackgroundResource(R.color.colorAccentBlueGreen);
                    setBackgroundConstraintLayout(noteHolder,R.color.colorAccentBlueGreen);
                    break;
                case 5:
                    emotionImageView.setImageResource(R.drawable.ic_note_emotion_very_happy);
                    // noteHolder.setBackgroundResource(R.color.colorAccentYellow);
                    setBackgroundConstraintLayout(noteHolder,R.color.colorAccentYellow);
                    break;
            }

        }

        private void setAllGone(){
            subheaderTextView.setVisibility(View.GONE);
            subheaderImageView.setVisibility(View.GONE);
            subheaderVideoView.setVisibility(View.GONE);
            subheaderAudioView.setVisibility(View.GONE);
        }

        private void setBackgroundConstraintLayout(ConstraintLayout constraintLayout, @ColorRes int resId){
            int color = ContextCompat.getColor(itemView.getContext(), resId);
            Drawable background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ripple_note);
            if (background instanceof RippleDrawable) {
                RippleDrawable rippleDrawable = (RippleDrawable) background;
                Drawable colorDrawable = rippleDrawable.getDrawable(0); // Assuming the color is the first layer
                if (colorDrawable instanceof ShapeDrawable) {
                    ((ShapeDrawable) colorDrawable).getPaint().setColor(color);
                } else if (colorDrawable instanceof GradientDrawable) {
                    ((GradientDrawable) colorDrawable).setColor(color);
                }
                constraintLayout.setBackground(rippleDrawable);
            }
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
