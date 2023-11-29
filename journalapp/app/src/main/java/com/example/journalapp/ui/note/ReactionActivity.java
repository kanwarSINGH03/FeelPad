package com.example.journalapp.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.R;

public class ReactionActivity extends AppCompatActivity {

    private int emotionValue;
    private ImageView currentEmotionImageView;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.reaction_view);

        currentEmotionImageView = findViewById(R.id.current_emotion);

        ImageButton angryButton = findViewById(R.id.emotion_low);
        ImageButton mediumAngryButton = findViewById(R.id.emotion_low_medium);
        ImageButton neutralButton = findViewById(R.id.emotion_medium);
        ImageButton mediumHappyButton = findViewById(R.id.emotion_high_medium);
        ImageButton happyButton = findViewById(R.id.emotion_high);

        angryButton.setOnClickListener(v -> {
            emotionValue = 1;
            onEmotionButtonClick(R.drawable.ic_note_emotion_horrible);
        });
        mediumAngryButton.setOnClickListener(v -> {
            emotionValue = 2;
            onEmotionButtonClick(R.drawable.ic_note_emotion_disappointed);
        });
        neutralButton.setOnClickListener(v -> {
            emotionValue = 3;
            onEmotionButtonClick(R.drawable.ic_note_emotion_neutral);
        });

        mediumHappyButton.setOnClickListener(v -> {
            emotionValue = 4;
            onEmotionButtonClick(R.drawable.ic_note_emotion_happy);
        });
        happyButton.setOnClickListener(v -> {
            emotionValue = 5;
            onEmotionButtonClick(R.drawable.ic_note_emotion_very_happy);
        });

    }

    private void onEmotionButtonClick(int drawableId) {
        currentEmotionImageView.setImageResource(drawableId);
    }

    public void exitReaction(View view) {

        // save the reaction into the database
        Intent resultIntent = new Intent();
        resultIntent.putExtra("emotion", emotionValue);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

}
