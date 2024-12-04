/**
 * Name: Raymond Chen
 * Course: CS-665 Software Designs & Patterns
 * Date: 12/03/2024
 * File Name: OpenRouterClient.java
 * Description: This class is responsible for interaction with the OpenRouter AI API.
 */

package edu.bu.met.cs665.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is responsible for interacting with the OpenRouter AI API.
 * Handles API key management and natural language processing of due requests.
 */
public class OpenRouterClient {

    private static String OPENROUTER_API_KEY;
    private static final String BASE_URL = "https://openrouter.ai/api/v1";

    static {
        loadApiKey();
    }

    /**
     * Loads the OpenRouter API key from environment variables or config file.
     * If no API key is found, creates a config file template and exits.
     * The API key can be set either through the OPENROUTER_API_KEY environment variable
     * or in the config.properties file.
     */
    private static void loadApiKey() {
        // Load from environment variable
        OPENROUTER_API_KEY = System.getenv("OPENROUTER_API_KEY");

        // If not found in environment, try to load from properties file
        if (OPENROUTER_API_KEY == null || OPENROUTER_API_KEY.trim().isEmpty()) {
            Properties props = new Properties();
            File configFile = new File("config.properties");

            // Create config file if it doesn't exist
            if (!configFile.exists()) {
                try (FileOutputStream out = new FileOutputStream(configFile)) {
                    props.setProperty("openrouter.api.key", "API_KEY");
                    props.store(out, "OpenRouter API Configuration");
                    System.out.println("Created config.properties file. Please set your API key in the file.");
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("Error creating config file: " + e.getMessage());
                    System.exit(1);
                }
            }

            // Load API key from config file
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
                OPENROUTER_API_KEY = props.getProperty("openrouter.api.key");

                if (OPENROUTER_API_KEY == null || OPENROUTER_API_KEY.trim().isEmpty()
                        || "YOUR_API_KEY_HERE".equals(OPENROUTER_API_KEY)) {
                    System.out.println("Please set your API key in config.properties file");
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("Error loading config file: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Sends a natural language due request to the OpenRouter API and processes the response.
     * Converts user input into a structured JSON format containing course, title, and due date.
     *
     * @param prompt The natural language description of the due (e.g., "CS665 assignment 1 next tuesday")
     * @return A JSON string containing the structured due information or error message
     */
    public static String getResponse(String prompt) {
        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            LocalDateTime TodayDate = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = TodayDate.format(formatter);
            DayOfWeek dayOfWeek = TodayDate.getDayOfWeek();
            String currentDay = dayOfWeek.toString();
            message.put("content", "You are a Due Manager Assistant. Return ONLY a raw JSON object without any markdown formatting or code blocks. "
                    + "The JSON must contain these required fields: course, title, and due_date. "
                    + "Optional fields that may be included if mentioned: location, notes. "
                    + "The current time is " + formattedDate + " and today is " + currentDay + ". "
                    + "Rules:"
                    + "1. The 'due_date' must be in the format 'yyyy-MM-dd HH:mm' (e.g., '2024-11-15 23:59')"
                    + "2. Do not include any explanations or markdown formatting"
                    + "3. Do not assume any data except for fixing obvious typos"
                    + "4. Always set time to 23:59 if no specific time is given"
                    + "5. Include 'location' if a place is mentioned"
                    + "6. Include 'notes' for any additional details"
                    + "7. Return ONLY the JSON object"
                    + "\nNow parse this due request: "
                    + prompt);
            JSONObject payload = new JSONObject();
            payload.put("model", "meta-llama/llama-3.2-90b-vision-instruct:free");
            JSONArray messages = new JSONArray();
            messages.put(message);
            payload.put("messages", messages);

            // Create the URL object
            URL url = new URL(BASE_URL + "/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + OPENROUTER_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print raw response for debugging
                //System.out.println("Debug - Raw API Response: " + response.toString());

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Check for error response
                    if (jsonResponse.has("error")) {
                        JSONObject error = jsonResponse.getJSONObject("error");
                        String errorMessage = "API Error";

                        // Check for nested error message in metadata
                        if (error.has("metadata")) {
                            JSONObject metadata = error.getJSONObject("metadata");
                            if (metadata.has("raw")) {
                                JSONObject rawError = new JSONObject(metadata.getString("raw"));
                                if (rawError.has("error")) {
                                    JSONObject detailedError = rawError.getJSONObject("error");
                                    if (detailedError.has("message")) {
                                        errorMessage = detailedError.getString("message");
                                    }
                                }
                            }
                        } else if (error.has("message")) {
                            errorMessage = error.getString("message");
                        }

                        return "{\"error\": \"" + errorMessage + "\"}";
                    }

                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageResponse = choice.getJSONObject("message");
                    return messageResponse.getString("content");
                } catch (Exception e) {
                    return "{\"error\": \"Failed to process API response: " + e.getMessage() + "\"}";
                }
            } else {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                } catch (IOException e) {
                    return "{\"error\": \"Failed to read error response\"}";
                }
                return "{\"error\": \"HTTP " + responseCode + ": " + errorResponse.toString() + "\"}";

            }

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }
}
