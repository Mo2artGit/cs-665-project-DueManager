/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: DueManagerApp.java
 * Description: This class is responsible for the command line interface.
 */

package edu.bu.met.cs665;

import edu.bu.met.cs665.client.OpenRouterClient;
import edu.bu.met.cs665.model.Due;
import edu.bu.met.cs665.service.DueManager;
import edu.bu.met.cs665.command.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This is the DueManagerApp class.
 */
public class DueManagerApp {
    private final DueManager dueManager;
    private final Scanner scanner;
    private final OpenRouterClient openRouterClient;
    private final CommandHistory commandHistory = new CommandHistory();

    /**
     * A DueManagerApp method to run the application.
     */
    public DueManagerApp() {
        this.dueManager = new DueManager();
        this.scanner = new Scanner(System.in);
        this.openRouterClient = new OpenRouterClient();
    }

    /**
     * Starts the Due Manager application and runs the main command loop.
     * Displays the menu and processes user input until the user chooses to exit.
     */
    public void start() {

        while (true) {
            displayMenu();
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Please enter a valid choice (1-5)");
                    continue;
                }
                int choice = Integer.parseInt(input);

                switch (choice) {
                    case 0:
                        addNewDue();
                        break;
                    case 1:
                        addNewDueManually();
                        break;
                    case 2:
                        displayDues();
                        break;
                    case 3:
                        markDueAsCompleted();
                        break;
                    case 4:
                        removeDue();
                        break;
                    case 5:
                        removeAllDues();
                        break;
                    case 6:
                        if (commandHistory.canUndo()) {
                            commandHistory.undo();
                            System.out.println("Operation undone successfully!");
                        } else {
                            System.out.println("Nothing to undo!");
                        }
                        break;
                    case 7:
                        if (commandHistory.canRedo()) {
                            commandHistory.redo();
                            System.out.println("Operation redone successfully!");
                        } else {
                            System.out.println("Nothing to redo!");
                        }
                        break;
                    case 8:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Displays the main menu of the Due Manager application.
     * Shows available options for adding, viewing, marking, and removing dues.
     */
    private void displayMenu() {
        System.out.println("\n┌─ Due Manager ─────────────┐");
        System.out.println("│ Add: 0)AI 1)Manual        │");
        System.out.println("│ View: 2)All Dues          │");
        System.out.println("│ Mark: 3)Completed         │");
        System.out.println("│ Remove: 4)ID 5)All        │");
        System.out.println("│ Edit: 6)Undo 7)Redo       │");
        System.out.println("│ Exit: 8                   │");
        System.out.println("└───────────────────────────┘");
        System.out.print("Choice > ");
    }

    /**
     * Adds a new due using AI assistance to parse natural language input.
     * Communicates with OpenRouter API to interpret user input and create a structured due.
     */
    private void addNewDue() {
        System.out.println("Enter due details (e.g., 'CS665 assignment 1 next tuesday'):");
        String prompt = scanner.nextLine();

        if (prompt.trim().isEmpty()) {
            System.out.println("Error: Due details cannot be empty");
            return;
        }

        String response = openRouterClient.getResponse(prompt);

        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Check if there was an error in the response
            if (jsonResponse.has("error")) {
                String errorMessage = jsonResponse.getString("error");
                System.out.println("⚠️ " + errorMessage);
                System.out.println("Please try again.");
                return;
            }

            // Validate required fields exist in the response
            String[] requiredFields = {"course", "title", "due_date"};
            for (String field : requiredFields) {
                if (!jsonResponse.has(field) || jsonResponse.getString(field).trim().isEmpty()) {
                    System.out.println("Error: Missing or invalid " + field + " in the response");
                    System.out.println("Please try again with more specific details.");
                    return;
                }
            }

            Due due = dueManager.createDueFromJson(response);
            AddDueCommand addCommand = new AddDueCommand(dueManager, due);
            commandHistory.executeCommand(addCommand);
            System.out.println("Due added successfully!");
            System.out.println("Title: " + due.getTitle());
            System.out.println("Course: " + due.getCourse());
            System.out.println("Due Date: " + due.getFormattedDueDate());
            if (due.getNotes() != null && !due.getNotes().isEmpty()) {
                System.out.println("Notes: " + due.getNotes());
            }
        } catch (JSONException e) {
            System.out.println("Error: Invalid response format received");
            System.out.println("Please try again with more specific details.");
        } catch (Exception e) {
            System.out.println("Error adding due: " + e.getMessage());
            System.out.println("Please try again with a different format or more specific details.");
        }
    }

    /**
     * Adds a new due manually by prompting user for individual fields.
     * Collects course name, title, and due date information from user input.
     */
    private void addNewDueManually() {
        System.out.println("Enter course name (e.g., CS665):");
        String course = scanner.nextLine().trim();
        if (course.isEmpty()) {
            System.out.println("Error: Course name cannot be empty");
            return;
        }

        System.out.println("Enter title/description:");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be empty");
            return;
        }

        LocalDateTime dueDate = null;
        while (dueDate == null) {
            System.out.println("Enter due date (format: YYYY-MM-DD HH:mm):");
            String dueDateStr = scanner.nextLine().trim();
            try {
                dueDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD HH:mm (e.g., 2024-01-31 23:59)");
            }
        }

        // Create a JSON string to use the existing createDueFromJson method
        JSONObject dueJson = new JSONObject();
        dueJson.put("course", course);
        dueJson.put("title", title);
        dueJson.put("due_date", dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        Due due = dueManager.createDueFromJson(dueJson.toString());
        dueManager.addDue(due);

        System.out.println("Due added successfully!");
        System.out.println("Title: " + due.getTitle());
        System.out.println("Course: " + due.getCourse());
        System.out.println("Due Date: " + due.getFormattedDueDate());
    }

    /**
     * Displays all dues grouped by date in reverse chronological order.
     * Shows completed dues with strikethrough formatting and highlights dues due today.
     */
    private void displayDues() {
        Map<LocalDate, List<Due>> duesByDate = dueManager.getDuesByDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

        if (duesByDate.isEmpty()) {
            System.out.println("No dues found.");
            return;
        }

        LocalDate today = LocalDate.now();
        List<Map.Entry<LocalDate, List<Due>>> sortedDues = new ArrayList<>(duesByDate.entrySet());
        Collections.reverse(sortedDues);

        for (Map.Entry<LocalDate, List<Due>> entry : sortedDues) {
            LocalDate date = entry.getKey();
            if (date.equals(today)) {
                System.out.println("\nToday - " + today.format(dateFormatter));
            } else {
                System.out.println("\n" + date.format(dayFormatter) + " - " +
                        date.format(dateFormatter));
            }

            for (Due due : entry.getValue()) {
                String titleLine = due.getTitle();
                String dateLine = "Due date: " + due.getFormattedDueDate() +
                        " ∙ " + due.getCourse();

                // Add location if available
                if (due.getLocation() != null && !due.getLocation().isEmpty()) {
                    dateLine += " ∙ Location: " + due.getLocation();
                }

                // Add notes if available
                String notesLine = due.getNotes() != null && !due.getNotes().isEmpty()
                        ? "Notes: " + due.getNotes()
                        : null;

                if (due.isCompleted()) {
                    System.out.println(applyStrikethrough(titleLine));
                    System.out.println(applyStrikethrough(dateLine));
                    if (notesLine != null) {
                        System.out.println(applyStrikethrough(notesLine));
                    }
                } else {
                    System.out.println(titleLine);
                    System.out.println(dateLine);
                    if (notesLine != null) {
                        System.out.println(notesLine);
                    }
                }
            }
        }

        int todayDuesCount = duesByDate.getOrDefault(today, Collections.emptyList()).size();
        System.out.println("\nYou have " + todayDuesCount + " due(s) today!");
    }

    /**
     * Applies strikethrough formatting to the given text.
     * Used to visually indicate completed dues in the display.
     *
     * @param text The text to apply strikethrough to
     * @return The text with strikethrough formatting applied
     */
    private String applyStrikethrough(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            result.append(c).append('\u0336');
        }
        return result.toString();
    }

    /**
     * Allows user to mark a due as completed by selecting its ID.
     * Displays all dues with their IDs and processes user selection.
     */
    private void markDueAsCompleted() {
        List<Due> allDues = dueManager.getAllDues();
        if (allDues.isEmpty()) {
            System.out.println("No dues available to mark as completed.");
            return;
        }

        displayDuesWithIds();
        while (true) {
            try {
                System.out.println("Enter the ID of the due to mark as completed (or 0 to cancel):");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Please enter a valid ID");
                    continue;
                }
                int id = Integer.parseInt(input);
                if (id == 0) {
                    System.out.println("Operation cancelled.");
                    return;
                }
                Due dueToMark = dueManager.getDueById(id);
                if (dueToMark != null) {
                    MarkCompletedCommand markCommand = new MarkCompletedCommand(dueToMark);
                    commandHistory.executeCommand(markCommand);
                    System.out.println("Due marked as completed!");
                } else {
                    System.out.println("Due with ID " + id + " not found.");
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Removes a specific due by its ID.
     * Displays all dues with their IDs and processes user selection for removal.
     */
    private void removeDue() {
        List<Due> allDues = dueManager.getAllDues();
        if (allDues.isEmpty()) {
            System.out.println("No dues available to remove.");
            return;
        }

        displayDuesWithIds();
        while (true) {
            try {
                System.out.println("Enter the ID of the due to remove (or 0 to cancel):");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Please enter a valid ID");
                    continue;
                }
                int id = Integer.parseInt(input);
                if (id == 0) {
                    System.out.println("Operation cancelled.");
                    return;
                }
                Due dueToRemove = dueManager.getDueById(id);
                if (dueToRemove != null) {
                    RemoveDueCommand removeCommand = new RemoveDueCommand(dueManager, dueToRemove);
                    commandHistory.executeCommand(removeCommand);
                    System.out.println("Due removed successfully!");
                } else {
                    System.out.println("Due with ID " + id + " not found.");
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Removes all dues after user confirmation.
     * Requires explicit "YES" confirmation to prevent accidental deletion.
     */
    private void removeAllDues() {
        List<Due> allDues = dueManager.getAllDues();
        if (allDues.isEmpty()) {
            System.out.println("No dues available to remove.");
            return;
        }

        System.out.println("Are you sure you want to remove all dues? This action cannot be undone.");
        System.out.println("Type 'YES' to confirm: ");
        String confirmation = scanner.nextLine().trim();

        if (confirmation.equals("YES")) {
            dueManager.removeAllDues();
            System.out.println("All dues have been removed successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    /**
     * Displays all dues with their IDs for selection purposes.
     * Shows both completed and pending dues with appropriate formatting.
     */
    private void displayDuesWithIds() {
        List<Due> allDues = dueManager.getAllDues();
        for (Due due : allDues) {
            if (due.isCompleted()) {
                System.out.println("ID: " + due.getId());
                System.out.println(applyStrikethrough(due.getTitle() + " - " + due.getCourse()));
                System.out.println(applyStrikethrough("Due: " + due.getFormattedDueDate()));
            } else {
                System.out.println("ID: " + due.getId());
                System.out.println(due.getTitle() + " - " + due.getCourse());
                System.out.println("Due: " + due.getFormattedDueDate());
            }
            System.out.println();
        }
    }

    /**
     * The main entry point of the Due Manager application.
     * Creates a new DueManagerApp instance and starts the application.
     */
    public static void main(String[] args) {
        DueManagerApp app = new DueManagerApp();
        app.start();
    }
}