/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: CommandHistory.java
 * Description: This class manages the command history for undo/redo operations.
 */

package edu.bu.met.cs665.command;

import java.util.Stack;

/**
 * Manages the history of commands for implementing undo/redo functionality.
 * Uses two stacks to track commands that can be undone or redone.
 */
public class CommandHistory {
    private final Stack<DueCommand> undoStack = new Stack<>();
    private final Stack<DueCommand> redoStack = new Stack<>();

    /**
     * Executes a command and adds it to the undo stack.
     * Clears the redo stack as a new command execution creates a new future.
     *
     * @param command The command to execute
     */
    public void executeCommand(DueCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the most recent command and moves it to the redo stack.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            DueCommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redoes the most recently undone command and moves it back to the undo stack.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            DueCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    /**
     * Checks if there are commands that can be undone.
     *
     * @return true if there are commands in the undo stack, false otherwise
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if there are commands that can be redone.
     *
     * @return true if there are commands in the redo stack, false otherwise
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}