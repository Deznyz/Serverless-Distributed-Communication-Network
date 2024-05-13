package com.example.serverlessdistributedcommunicationnetwork;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DataTransfer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Collect user input (the text message)
        System.out.print("Enter your text message: ");
        String message = scanner.nextLine();

        // Construct the JSON string
        String jsonContent = "{\"message\": \"" + escapeSpecialCharacters(message) + "\"}";

        // Write the JSON string to a file
        try (FileWriter fileWriter = new FileWriter("message_to_friend.json")) {
            fileWriter.write(jsonContent);
            System.out.println("Message saved to message_to_friend.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to escape special characters in the message
    private static String escapeSpecialCharacters(String input) {
        return input.replace("\"", "\\\"");
    }
}
