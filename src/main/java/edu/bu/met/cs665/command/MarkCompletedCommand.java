/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: MarkCompletedCommand.java
 * Description: This class implements the Command pattern for marking dues as completed.
 */

package edu.bu.met.cs665.command;

import edu.bu.met.cs665.model.Due;

/**
 * Command implementation for marking a due as completed.
 * Provides functionality to execute and undo completion status changes.
 */
public class MarkCompletedCommand implements DueCommand {
    private final Due due;
    private final boolean previousState;

    /**
     * Creates a new MarkCompletedCommand.
     *
     * @param due The Due instance to be marked as completed
     */
    public MarkCompletedCommand(Due due) {
        this.due = due;
        this.previousState = due.isCompleted();
    }

    /**
     * Executes the mark completed operation by setting the due's completed status to true.
     */
    @Override
    public void execute() {
        due.setCompleted(true);
    }

    /**
     * Undoes the mark completed operation by restoring the due's previous completion status.
     */
    @Override
    public void undo() {
        due.setCompleted(previousState);
    }
}