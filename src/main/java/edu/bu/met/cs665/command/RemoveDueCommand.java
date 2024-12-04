/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: RemoveDueCommand.java
 * Description: This class implements the Command pattern for removing dues.
 */

package edu.bu.met.cs665.command;

import edu.bu.met.cs665.model.Due;
import edu.bu.met.cs665.service.DueManager;

/**
 * Command implementation for removing a due.
 * Provides functionality to execute and undo due removal operations.
 */
public class RemoveDueCommand implements DueCommand {
    private final Due due;
    private final DueManager dueManager;

    /**
     * Creates a new RemoveDueCommand.
     *
     * @param dueManager The DueManager instance to perform operations on
     * @param due The Due instance to be removed
     */
    public RemoveDueCommand(DueManager dueManager, Due due) {
        this.dueManager = dueManager;
        this.due = due;
    }

    /**
     * Executes the remove operation by removing the due from the manager.
     */
    @Override
    public void execute() {
        dueManager.removeDue(due.getId());
    }

    /**
     * Undoes the remove operation by adding the due back to the manager.
     */
    @Override
    public void undo() {
        dueManager.addDue(due);
    }
}