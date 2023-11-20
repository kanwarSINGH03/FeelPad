package com.example.journalapp.ui.note;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.journalapp.R;

/**
 * Class to play both videos AND audio
 */
public class VideoPlayerFragment extends DialogFragment {
    private ExoPlayer player;
    private PlayerView playerview;
    private Uri videoUri;

    /**
     * Creates a new instance of the fragment using VideoURI.
     * Note: Fragments usually uses newInstance rather than a constructor.
     * @param videoUri URI of the video as a String
     * @return a new instance of the VideoPlayerFragment
     */
    public static VideoPlayerFragment newInstance(String videoUri) {
        // Serves as a constructor; states the needed arguments
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString("videoUri", videoUri);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of the fragment
     * Extracts video URI from the arguments
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            videoUri = Uri.parse(getArguments().getString("videoUri"));
        }
    }

    /**
     * Called to have fragment instantiate the UI view
     * Initializes player view and sets up exoplayer
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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        playerview = view.findViewById(R.id.player_view);

        // initialize exoplayer
        initializePlayer();
        return view;
    }

    /**
     * Initializes ExoPlayer for video playback and starts playback
     */
    private void initializePlayer(){
        if (player == null){
            player = new ExoPlayer.Builder(requireContext()).build();
            playerview.setPlayer(player);

            // Create and set media item using URI
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);
        }
        player.prepare();
        player.play();
        playerview.setUseController(true); // enables controls
    }

    /**
     * Called when view previously creates is detached from fragment
     * Releases ExoPlayer to free up resources
     */
    public void onDestroyView(){
        super.onDestroyView();
        if (player != null){
            player.release();
            player = null;
        }
    }
}
