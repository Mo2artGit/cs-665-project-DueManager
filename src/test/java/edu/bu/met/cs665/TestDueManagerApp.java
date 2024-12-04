package edu.bu.met.cs665;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.*;

import edu.bu.met.cs665.model.Due;
import edu.bu.met.cs665.service.DueManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class TestDueManagerApp {
    private DueManager dueManager;
    private static final String DUES_FILE = "dues.json";
    private File backupFile;

    @Before
    public void setUp() {
        // Backup existing dues.json if it exists
        File duesFile = new File(DUES_FILE);
        if (duesFile.exists()) {
            backupFile = new File(DUES_FILE + ".backup");
            try {
                Files.copy(duesFile.toPath(), backupFile.toPath());
                duesFile.delete();
            } catch (IOException e) {
                System.err.println("Failed to backup dues file: " + e.getMessage());
            }
        }
        dueManager = new DueManager();
    }

    @After
    public void tearDown() {
        // Restore the original dues.json if it was backed up
        if (backupFile != null && backupFile.exists()) {
            File duesFile = new File(DUES_FILE);
            if (duesFile.exists()) {
                duesFile.delete();
            }
            try {
                Files.copy(backupFile.toPath(), Paths.get(DUES_FILE));
                backupFile.delete();
            } catch (IOException e) {
                System.err.println("Failed to restore dues file: " + e.getMessage());
            }
        }
    }

    @Test
    public void testAddDue() {
        // Given a new due with course CS665, title Assignment 1, and due date tomorrow
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(dueDate)
                .completed(false)
                .build();

        // When the due is added to the manager
        dueManager.addDue(due);

        // Then the due should be stored with correct attributes
        List<Due> allDues = dueManager.getAllDues();
        assertEquals(1, allDues.size());
        assertEquals("CS665", allDues.get(0).getCourse());
        assertEquals("Assignment 1", allDues.get(0).getTitle());
        assertEquals(dueDate, allDues.get(0).getDueDate());
        assertFalse(allDues.get(0).isCompleted());
    }

    @Test
    public void testMarkDueAsCompleted() {
        // Given a due that is not marked as completed
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .build();
        dueManager.addDue(due);

        // When the due is marked as completed
        boolean result = dueManager.markAsCompleted(1);

        // Then the due should be marked as completed
        assertTrue(result);
        assertTrue(dueManager.getAllDues().get(0).isCompleted());
    }

    @Test
    public void testRemoveDue() {
        // Given an existing due in the manager
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .build();
        dueManager.addDue(due);

        // When removing the due by its ID
        boolean result = dueManager.removeDue(1);

        // Then the due should be removed successfully
        assertTrue(result);
        assertTrue(dueManager.getAllDues().isEmpty());
    }

    @Test
    public void testGetDuesByDate() {
        // Given two dues with different due dates (today and tomorrow)
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime tomorrow = today.plusDays(1);

        Due due1 = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(today)
                .completed(false)
                .build();

        Due due2 = new Due.DueBuilder(2)
                .course("CS665")
                .title("Assignment 2")
                .dueDate(tomorrow)
                .completed(false)
                .build();

        dueManager.addDue(due1);
        dueManager.addDue(due2);

        // When retrieving dues grouped by date
        Map<LocalDate, List<Due>> duesByDate = dueManager.getDuesByDate();

        // Then dues should be correctly organized by their dates
        assertEquals(2, duesByDate.size());
        assertTrue(duesByDate.containsKey(today.toLocalDate()));
        assertTrue(duesByDate.containsKey(tomorrow.toLocalDate()));
        assertEquals(1, duesByDate.get(today.toLocalDate()).size());
        assertEquals(1, duesByDate.get(tomorrow.toLocalDate()).size());
    }

    @Test
    public void testRemoveAllDues() {
        // Given multiple dues in the manager
        Due due1 = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(LocalDateTime.now())
                .completed(false)
                .build();

        Due due2 = new Due.DueBuilder(2)
                .course("CS665")
                .title("Assignment 2")
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .build();

        dueManager.addDue(due1);
        dueManager.addDue(due2);

        // When removing all dues
        dueManager.removeAllDues();

        // Then the manager should have no dues remaining
        assertTrue(dueManager.getAllDues().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDue() {
        // Given a null due object
        Due nullDue = null;

        // When attempting to add the null due
        dueManager.addDue(nullDue);

        // Then an IllegalArgumentException should be thrown
    }


    // Tests the Builder pattern implementation for Due objects.
    @Test
    public void testDueBuilder() {
        // Given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        // When
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(dueDate)
                .completed(true)
                .location("Room 101")
                .notes("Submit via Blackboard")
                .build();

        // Then
        assertEquals(1, due.getId());
        assertEquals("CS665", due.getCourse());
        assertEquals("Assignment 1", due.getTitle());
        assertEquals(dueDate, due.getDueDate());
        assertTrue(due.isCompleted());
        assertEquals("Room 101", due.getLocation());
        assertEquals("Submit via Blackboard", due.getNotes());
    }

    // Tests the Builder pattern with minimal required fields.
    @Test
    public void testDueBuilderMinimalFields() {
        // Given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        // When
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(dueDate)
                .completed(false)
                .build();

        // Then
        assertEquals(1, due.getId());
        assertEquals("CS665", due.getCourse());
        assertEquals("Assignment 1", due.getTitle());
        assertEquals(dueDate, due.getDueDate());
        assertFalse(due.isCompleted());
        assertNull(due.getLocation());
        assertNull(due.getNotes());
    }

    // Tests the formatted date output from a Due object.
    @Test
    public void testDueDateFormatting() {
        // Given
        LocalDateTime dueDate = LocalDateTime.of(2024, 12, 25, 14, 30);

        // When
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(dueDate)
                .completed(false)
                .build();

        // Then
        assertEquals("12/25/24, 2:30 PM", due.getFormattedDueDate());
    }


    // Tests that the builder properly handles null values for optional fields.
    @Test
    public void testDueBuilderWithNullOptionalFields() {
        // Given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

        // When
        Due due = new Due.DueBuilder(1)
                .course("CS665")
                .title("Assignment 1")
                .dueDate(dueDate)
                .completed(false)
                .location(null)
                .notes(null)
                .build();

        // Then
        assertNull("Location should be null", due.getLocation());
        assertNull("Notes should be null", due.getNotes());
    }
}