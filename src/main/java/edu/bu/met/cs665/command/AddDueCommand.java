/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: AddDueCommand.java
 * Description: This class implements the Command pattern for adding dues.
 */

package edu.bu.met.cs665.command;

import edu.bu.met.cs665.model.Due;
import edu.bu.met.cs665.service.DueManager;

/**
 * Command implementation for adding a new due.
 * Provides functionality to execute and undo due addition operations.
 */
public class AddDueCommand implements DueCommand {
    private final Due due;
    private final DueManager dueManager;

    /**
     * Creates a new AddDueCommand.
     *
     * @param dueManager The DueManager instance to perform operations on
     * @param due        The Due instance to be added
     */
    public AddDueCommand(DueManager dueManager, Due due) {
        this.dueManager = dueManager;
        this.due = due;
    }

    /**
     * Executes the add operation by adding the due to the manager.
     */
    @Override
    public void execute() {
        dueManager.addDue(due);
    }

    /**
     * Undoes the add operation by removing the due from the manager.
     */
    @Override
    public void undo() {
        dueManager.removeDue(due.getId());
    }
}