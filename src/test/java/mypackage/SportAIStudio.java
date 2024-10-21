package mypackage;  // Make sure this matches the folder structure

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;  // Correct stage import for JavaFX
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SportAIStudio extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Creating UI components
        Label label = new Label("Enter the sport data file (CSV):");
        TextField fileInput = new TextField();
        Button analyzeButton = new Button("Analyze Data");

        // Button action to analyze data
        analyzeButton.setOnAction(e -> {
            String filePath = fileInput.getText();
            // Logic to analyze sport data will be here
            analyzeSportData(filePath);
        });

        // Layout for UI
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, fileInput, analyzeButton);

        // Scene and Stage setup
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sport AI Studio");
        primaryStage.show();
    }

    // Function to analyze the sport data
    private void analyzeSportData(String filePath) {
        System.out.println("Analyzing data from: " + filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int totalScore = 0;
            int count = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int score = Integer.parseInt(values[1]); // Assuming second column is score
                totalScore += score;
                count++;
            }

            int averageScore = totalScore / count;
            System.out.println("Average Score: " + averageScore);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
