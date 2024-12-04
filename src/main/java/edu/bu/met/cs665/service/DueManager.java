/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: DueManager.java
 * Description: This class is responsible for the management of dues.
 */

package edu.bu.met.cs665.service;

import edu.bu.met.cs665.model.Due;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This is the DueManager class.
 */
public class DueManager {
    private final List<Due> dues = new ArrayList<>();
    private int nextId = 1;
    private static final String SAVE_FILE_PATH = "dues.json";

    /**
     * Constructs a new DueManager and loads existing dues from storage.
     */
    public DueManager() {
        loadDues();
    }

    /**
     * Loads dues from the JSON file storage.
     * Initializes the dues list and sets the next available ID.
     */
    private void loadDues() {
        File file = new File(SAVE_FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject dueJson = jsonArray.getJSONObject(i);
                Due.DueBuilder builder = new Due.DueBuilder(dueJson.getInt("id"))
                        .course(dueJson.getString("course"))
                        .title(dueJson.getString("title"))
                        .dueDate(LocalDateTime.parse(dueJson.getString("due_date"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .completed(dueJson.getBoolean("completed"));

                // Add optional fields if they exist in JSON
                if (dueJson.has("location")) {
                    builder.location(dueJson.getString("location"));
                }
                if (dueJson.has("notes")) {
                    builder.notes(dueJson.getString("notes"));
                }

                dues.add(builder.build());
                if (dueJson.getInt("id") >= nextId) {
                    nextId = dueJson.getInt("id") + 1;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dues: " + e.getMessage());
        }
    }

    /**
     * Saves the current dues list to JSON file storage.
     * Persists all due information including completion status.
     */
    private void saveDues() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Due due : dues) {
                JSONObject dueJson = new JSONObject();
                dueJson.put("id", due.getId());
                dueJson.put("course", due.getCourse());
                dueJson.put("title", due.getTitle());
                dueJson.put("due_date", due.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                dueJson.put("completed", due.isCompleted());
                // Add optional fields if they exist
                if (due.getLocation() != null) {
                    dueJson.put("location", due.getLocation());
                }
                if (due.getNotes() != null) {
                    dueJson.put("notes", due.getNotes());
                }
                jsonArray.put(dueJson);
            }

            Files.write(Paths.get(SAVE_FILE_PATH), jsonArray.toString().getBytes());
        } catch (Exception e) {
            System.err.println("Error saving dues: " + e.getMessage());
        }
    }

    /**
     * Gets a due by its ID.
     *
     * @param id The ID of the due to find
     * @return The due with the specified ID, or null if not found
     */
    public Due getDueById(int id) {
        return dues.stream()
                .filter(due -> due.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a new due to the manager and persists it.
     *
     * @param due The due to add
     * @throws IllegalArgumentException if due is null
     */
    public void addDue(Due due) {
        if (due == null) {
            throw new IllegalArgumentException("Due cannot be null");
        }
        dues.add(due);
        saveDues();
    }

    /**
     * Removes a due by its ID.
     *
     * @param id The ID of the due to remove
     * @return true if the due was found and removed, false otherwise
     */
    public boolean removeDue(int id) {
        boolean removed = dues.removeIf(due -> due.getId() == id);
        if (removed) {
            saveDues();
        }
        return removed;
    }

    /**
     * Removes all dues from the manager.
     * Clears the dues list and persists the empty state.
     */
    public void removeAllDues() {
        dues.clear();
        saveDues();
    }

    /**
     * Marks a due as completed by its ID.
     *
     * @param id The ID of the due to mark as completed
     * @return true if the due was found and marked as completed, false otherwise
     */
    public boolean markAsCompleted(int id) {
        Optional<Due> dueOpt = dues.stream()
                .filter(due -> due.getId() == id)
                .findFirst();

        if (dueOpt.isPresent()) {
            dueOpt.get().setCompleted(true);
            saveDues();
            return true;
        }
        return false;
    }

    /**
     * Creates a new Due object from a JSON string.
     * Handles both single object and array JSON formats.
     *
     * @param jsonString The JSON string containing due information
     * @return A new Due object
     * @throws IllegalArgumentException if JSON string is invalid or required fields are missing
     */
    public Due createDueFromJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        try {
            // Handle array response by taking the first item
            JSONObject json;
            if (jsonString.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(jsonString);
                if (jsonArray.length() == 0) {
                    throw new IllegalArgumentException("Empty JSON array received");
                }
                json = jsonArray.getJSONObject(0);
            } else {
                json = new JSONObject(jsonString);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Validate required fields
            validateJsonField(json, "course");
            validateJsonField(json, "title");
            validateJsonField(json, "due_date");

            LocalDateTime dueDate = LocalDateTime.parse(json.getString("due_date"), formatter);

            Due.DueBuilder builder = new Due.DueBuilder(nextId++)
                    .course(json.getString("course"))
                    .title(json.getString("title"))
                    .dueDate(dueDate)
                    .completed(false);

            // Handle optional field
            if (json.has("location") && !json.isNull("location")) {
                builder.location(json.getString("location"));
            }
            if (json.has("notes") && !json.isNull("notes")) {
                builder.notes(json.getString("notes"));
            }

            return builder.build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing due data: " + e.getMessage());
        }
    }

    /**
     * Validates that a required field exists in the JSON object.
     *
     * @param json  The JSON object to validate
     * @param field The required field name
     * @throws IllegalArgumentException if the field is missing
     */
    private void validateJsonField(JSONObject json, String field) {
        if (!json.has(field)) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
    }

    /**
     * Gets all dues organized by their due date.
     * Returns a sorted map with dates as keys and lists of dues as values.
     * Dues within each date are sorted by time.
     *
     * @return Map of dates to lists of dues
     */
    public Map<LocalDate, List<Due>> getDuesByDate() {
        Map<LocalDate, List<Due>> duesByDate = new TreeMap<>();

        for (Due due : dues) {
            LocalDate dueDate = due.getDueDate().toLocalDate();
            duesByDate.computeIfAbsent(dueDate, k -> new ArrayList<>()).add(due);
        }

        // Sort dues within each date by due time
        for (List<Due> dailyDues : duesByDate.values()) {
            dailyDues.sort(Comparator.comparing(Due::getDueDate));
        }

        return duesByDate;
    }

    /**
     * Gets a copy of all dues in the manager.
     *
     * @return A new list containing all dues
     */
    public List<Due> getAllDues() {
        return new ArrayList<>(dues);
    }
}