/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: DueCommand.java
 * Description: This interface defines the Command pattern contract for due operations.
 */

package edu.bu.met.cs665.command;

/**
 * Interface for implementing the Command pattern for Due operations.
 * Provides execute and undo operations for due management actions.
 */
public interface DueCommand {
    /**
     * Executes the command operation.
     */
    void execute();

    /**
     * Undoes the command operation.
     */
    void undo();
}