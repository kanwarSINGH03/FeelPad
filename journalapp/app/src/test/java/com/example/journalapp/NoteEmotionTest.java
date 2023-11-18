package com.example.journalapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.journalapp.database.entity.Note;

import org.junit.Test;

/**
 * Test class for the emotional state field in the Note Entity
 */
public class NoteEmotionTest {

    // Defaults for the note constructor
    private final String TEST_NOTE = "Test Note";
    private final String TEST_NOTE_DATE = "Test Note Date";

    @Test
    public void testSetAndGetEmotion() {
        // Create a new Note
        Note note = new Note(TEST_NOTE, TEST_NOTE_DATE, 3);

        // Check that the initial emotion is set correctly
        assertEquals(3, note.getEmotion());

        // Update the emotion and check that it is set correctly
        note.setEmotion(4);
        assertEquals(4, note.getEmotion());
    }

    @Test
    public void testEqualsWithEmotion() {
        // Create two instances of notes with different emotional states
        Note note1 = new Note(TEST_NOTE, TEST_NOTE_DATE, 3);
        Note note2 = new Note(TEST_NOTE, TEST_NOTE_DATE, 4);

        // Check that notes are not the same
        assertNotEquals(note1, note2);

        // Set the two notes emotions to be the same
        note2.setEmotion(3);

        // Check that the notes are equal now
        assertEquals(note1, note2);
    }

    @Test
    public void testNoteEmotionMax() {
        // Create a note
        Note note = new Note(TEST_NOTE, TEST_NOTE_DATE, 1);
        assertEquals(1, note.getEmotion());

        // Set the note to the max value suggested to hold in note
        note.setEmotion(5);
        assertEquals(5, note.getEmotion());
    }

    @Test
    public void testNoteEmotionLessThanOrGreaterThanSuggestedValues() {
        // Create a note
        Note note = new Note(TEST_NOTE, TEST_NOTE_DATE, 3);
        note.setEmotion(-1);
        assertEquals(3, note.getEmotion());

        // Set the emotion to a value outside the range of excepted values
        note.setEmotion(6);
        assertEquals(3, note.getEmotion());
    }
}
