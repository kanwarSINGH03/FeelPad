package com.example.journalapp.database;

import static org.junit.Assert.*;

import com.example.journalapp.database.entity.NoteFtsEntity;

import org.junit.Before;
import org.junit.Test;

/**
 * NoteFtsEntity Unit Tests
 */
public class NoteFtsEntityUnitTests {

    private NoteFtsEntity noteFtsEntity;

    // First set up a NoteFtsEntity

    @Before
    public void setUp() {
        noteFtsEntity = new NoteFtsEntity("testNoteId", "testCombinedText");
    }

    /**
     * Purpose: To check that constructor sets up data properly
     */
    @Test
    public void testConstructor(){
        assertEquals("testNoteId", noteFtsEntity.getNoteId());
        assertEquals("testCombinedText", noteFtsEntity.getCombinedText());
    }

    /**
     * Purpose: To check that the assigned RowID (assigned by Room Fts) is assigned properly
     */
    @Test
    public void testGetRowid() {
        assertEquals(0, noteFtsEntity.getRowid());
    }

    /**
     * Purpose: check that setters and getters for Note ID works properly
     */
    @Test
    public void testSetAndGetNoteId() {
        noteFtsEntity.setNoteId("newNoteId");
        assertEquals("newNoteId", noteFtsEntity.getNoteId());
    }

    /**
     * Purpose: check that setters and getters for combined text works properly
     */
    @Test
    public void testSetAndGetCombinedText() {
        noteFtsEntity.setCombinedText("newCombinedText");
        assertEquals("newCombinedText", noteFtsEntity.getCombinedText());
    }
}