package com.example.journalapp.ui.note;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.journalapp.R;

/**
 * Class to open an image in a dialog fragment
 */
public class ImageFragment extends DialogFragment {

    private ImageView imageView;
    private Uri imageUri;

    /**
     * Sets the style so that there is no background color in the Fragment
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialogFragmentStyle);
    }

    /**
     * Gets the URI and loads the image into the page using glide
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Initialize views
        imageView = view.findViewById(R.id.image_view);

        // Retrieve image URI and caption from arguments
        Bundle args = getArguments();
        if (args != null) {
            imageUri = Uri.parse(args.getString("imageUri"));

            // Load image using Glide
            if (imageUri != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(imageView);
            }

        }

        return view;
    }



}
