package com.example.journalapp;

import static org.junit.Assert.assertEquals;

import com.example.journalapp.database.entity.Note;
import com.example.journalapp.utils.GraphHelperUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EmotionalStateOverviewTest {

    @Test
    public void testFindAverageOfEachEmotion() {
        // Test case 1: Empty list
        List<Note> emptyList = Arrays.asList();
        assertEquals(0f, GraphHelperUtil.findAverageOfEachEmotion(emptyList), 0.001);

        // Test case 2: List with a single note
        List<Note> singleNoteList = Arrays.asList(new Note("Title", "2023-12-01", 3, "FolderId"));
        assertEquals(3f, GraphHelperUtil.findAverageOfEachEmotion(singleNoteList), 0.001);

        // Test case 3: List with multiple notes
        List<Note> multipleNoteList = Arrays.asList(
                new Note("Title1", "2023-12-01", 4, "FolderId"),
                new Note("Title2", "2023-12-01", 3, "FolderId"),
                new Note("Title3", "2023-12-02", 5, "FolderId")
        );
        // Average: (4 + 3 + 5) / 3 = 4
        assertEquals(4f, GraphHelperUtil.findAverageOfEachEmotion(multipleNoteList), 0.001);
    }
}
