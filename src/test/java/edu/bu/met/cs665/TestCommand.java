package edu.bu.met.cs665;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import edu.bu.met.cs665.model.Due;
import edu.bu.met.cs665.service.DueManager;
import edu.bu.met.cs665.command.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class TestCommand {
    private DueManager dueManager;
    private CommandHistory commandHistory;
    private Due testDue;
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
        commandHistory = new CommandHistory();
        testDue = new Due.DueBuilder(1)
                .course("CS665")
                .title("Test Assignment")
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .build();
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
    public void testAddDueCommand() {
        // Given a new AddDueCommand
        AddDueCommand addCommand = new AddDueCommand(dueManager, testDue);

        // When executing the command
        commandHistory.executeCommand(addCommand);

        // Then the due should be added
        List<Due> dues = dueManager.getAllDues();
        assertEquals(1, dues.size());
        assertEquals("Test Assignment", dues.get(0).getTitle());

        // When undoing the command
        commandHistory.undo();

        // Then the due should be removed
        assertTrue(dueManager.getAllDues().isEmpty());

        // When redoing the command
        commandHistory.redo();

        // Then the due should be added back
        dues = dueManager.getAllDues();
        assertEquals(1, dues.size());
        assertEquals("Test Assignment", dues.get(0).getTitle());
    }

    @Test
    public void testRemoveDueCommand() {
        // Given a due in the manager
        dueManager.addDue(testDue);
        RemoveDueCommand removeCommand = new RemoveDueCommand(dueManager, testDue);

        // When executing the remove command
        commandHistory.executeCommand(removeCommand);

        // Then the due should be removed
        assertTrue(dueManager.getAllDues().isEmpty());

        // When undoing the remove
        commandHistory.undo();

        // Then the due should be restored
        List<Due> dues = dueManager.getAllDues();
        assertEquals(1, dues.size());
        assertEquals("Test Assignment", dues.get(0).getTitle());
    }

    @Test
    public void testMarkCompletedCommand() {
        // Given a due in the manager
        dueManager.addDue(testDue);
        MarkCompletedCommand markCommand = new MarkCompletedCommand(testDue);

        // When executing the mark completed command
        commandHistory.executeCommand(markCommand);

        // Then the due should be marked as completed
        assertTrue(testDue.isCompleted());

        // When undoing the mark completed
        commandHistory.undo();

        // Then the due should be marked as not completed
        assertFalse(testDue.isCompleted());
    }

    @Test
    public void testCommandHistory() {
        // Given a series of commands
        AddDueCommand addCommand = new AddDueCommand(dueManager, testDue);
        MarkCompletedCommand markCommand = new MarkCompletedCommand(testDue);

        // When executing multiple commands
        commandHistory.executeCommand(addCommand);
        commandHistory.executeCommand(markCommand);

        // Then both commands should be executed
        List<Due> dues = dueManager.getAllDues();
        assertEquals(1, dues.size());
        assertTrue(dues.get(0).isCompleted());

        // When undoing both commands
        commandHistory.undo(); // Undo mark completed
        commandHistory.undo(); // Undo add

        // Then all changes should be reversed
        assertTrue(dueManager.getAllDues().isEmpty());

        // When redoing one command
        commandHistory.redo(); // Redo add

        // Then only the first command should be redone
        dues = dueManager.getAllDues();
        assertEquals(1, dues.size());
        assertFalse(dues.get(0).isCompleted());
    }

    @Test
    public void testRedoStackClearedOnNewCommand() {
        // Given a command that has been undone
        AddDueCommand addCommand = new AddDueCommand(dueManager, testDue);
        commandHistory.executeCommand(addCommand);
        commandHistory.undo();
        assertTrue(commandHistory.canRedo());

        // When executing a new command
        Due newDue = new Due.DueBuilder(2)
                .course("CS665")
                .title("New Assignment")
                .dueDate(LocalDateTime.now().plusDays(2))
                .completed(false)
                .build();
        AddDueCommand newCommand = new AddDueCommand(dueManager, newDue);
        commandHistory.executeCommand(newCommand);

        // Then the redo stack should be cleared
        assertFalse(commandHistory.canRedo());
    }

    @Test
    public void testUndoEmptyHistory() {
        // Given an empty command history
        // When trying to undo
        commandHistory.undo();
        // Then nothing should happen (no exception should be thrown)
        assertTrue(dueManager.getAllDues().isEmpty());
    }

    @Test
    public void testRedoEmptyHistory() {
        // Given an empty command history
        // When trying to redo
        commandHistory.redo();
        // Then nothing should happen (no exception should be thrown)
        assertTrue(dueManager.getAllDues().isEmpty());
    }
}