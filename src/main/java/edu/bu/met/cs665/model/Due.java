/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: Due.java
 * Description: This class is responsible for the methods and attributes of a Due.
 */

package edu.bu.met.cs665.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a due item in the Due Manager system.
 * Implements the Builder pattern for flexible object creation.
 */
public class Due {
    private final int id;
    private final String course;
    private final String title;
    private final LocalDateTime dueDate;
    private boolean completed;
    private final String location; // Optional location field
    private final String notes;    // Optional notes field

    /**
     * Create a Due object using a builder.
     *
     * @param builder for creating a Due
     */
    private Due(DueBuilder builder) {
        this.id = builder.id;
        this.course = builder.course;
        this.title = builder.title;
        this.dueDate = builder.dueDate;
        this.completed = builder.completed;
        this.location = builder.location;
        this.notes = builder.notes;
    }

    /**
     * Gets the unique identifier of the due.
     *
     * @return the due's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the course associated with the due.
     *
     * @return the course name
     */
    public String getCourse() {
        return course;
    }

    /**
     * Gets the title or description of the due.
     *
     * @return the due's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the due date and time.
     *
     * @return the LocalDateTime when the due is due
     */
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Checks if the due is marked as completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the completion status of the due.
     *
     * @param completed the new completion status
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Gets the due date formatted as a readable string.
     *
     * @return the due date formatted as "MM/dd/yy, h:mm a"
     */
    public String getFormattedDueDate() {
        return dueDate.format(DateTimeFormatter.ofPattern("MM/dd/yy, h:mm a"));
    }

    /**
     * Gets the location of the due if specified.
     *
     * @return the location or null if not specified
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the additional notes for the due if specified.
     *
     * @return the notes or null if not specified
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Builder class for creating Due objects.
     * Provides a fluent interface for setting Due properties.
     */
    public static class DueBuilder {
        private final int id;
        private String course;
        private String title;
        private LocalDateTime dueDate;
        private boolean completed;
        private String location;
        private String notes;

        /**
         * Creates a new DueBuilder with the specified ID.
         *
         * @param id the unique identifier for the due
         */
        public DueBuilder(int id) {
            this.id = id;
        }

        /**
         * Sets the course for the due being built.
         *
         * @param course the course name
         * @return this builder for method chaining
         */
        public DueBuilder course(String course) {
            this.course = course;
            return this;
        }

        /**
         * Sets the title for the due being built.
         *
         * @param title the due's title or description
         * @return this builder for method chaining
         */
        public DueBuilder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the due date for the due being built.
         *
         * @param dueDate the LocalDateTime when the due is due
         * @return this builder for method chaining
         */
        public DueBuilder dueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        /**
         * Sets the completion status for the due being built.
         *
         * @param completed the completion status
         * @return this builder for method chaining
         */
        public DueBuilder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        /**
         * Sets the location for the due being built.
         *
         * @param location the location where the due takes place
         * @return this builder for method chaining
         */
        public DueBuilder location(String location) {
            this.location = location;
            return this;
        }

        /**
         * Sets additional notes for the due being built.
         *
         * @param notes additional information about the due
         * @return this builder for method chaining
         */
        public DueBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * Builds and returns a new Due object with the configured properties.
         *
         * @return a new Due instance
         */
        public Due build() {
            return new Due(this);
        }
    }
}