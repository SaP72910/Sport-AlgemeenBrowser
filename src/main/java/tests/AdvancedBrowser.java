package tests;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdvancedBrowser extends Application {

    private boolean isLoggedIn = false;  // Track login state
    private String username = "";  // Store the logged-in user's name
    private String homeUrl = "https://www.sportalgemeen.com";  // Default homepage
    private Map<String, String> userDatabase = new HashMap<>();  // In-memory storage for registered users
    private TabPane tabPane;  // TabPane to hold multiple tabs
    private ArrayList<String> searchHistory = new ArrayList<>();  // Store search history

    @Override
    public void start(Stage primaryStage) {
        // Create the main browser scene
        Scene browserScene = createBrowserScene(primaryStage);

        // Set the initial scene to the browser
        primaryStage.setScene(browserScene);
        primaryStage.setTitle("Sport Algemeen");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private Scene createBrowserScene(Stage primaryStage) {
        tabPane = new TabPane();  // Create a new TabPane

        // Create the initial tab with the homepage
        Tab initialTab = createTab("Home", homeUrl);
        tabPane.getTabs().add(initialTab);

        // Navigation buttons
        Button newTabButton = new Button("New Tab");
        newTabButton.setOnAction(e -> tabPane.getTabs().add(createTab("New Tab", homeUrl)));

        Button backButton = new Button("Back");
        Button forwardButton = new Button("Forward");
        Button reloadButton = new Button("Reload");
        Button homeButton = new Button("Home");

        TextField urlField = new TextField(homeUrl);
        urlField.setPrefWidth(600);

        // Navigation functionality
        backButton.setOnAction(e -> navigateBack(urlField));
        forwardButton.setOnAction(e -> navigateForward(urlField));
        reloadButton.setOnAction(e -> reloadCurrentTab());
        homeButton.setOnAction(e -> loadHomePage(urlField));
        urlField.setOnAction(e -> loadUrlFromField(urlField));

        // Title banner
        Text titleBanner = new Text("Sport Algemeen");
        titleBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Navigation bar layout
        HBox navigationBar = new HBox(10, newTabButton, backButton, forwardButton, reloadButton, homeButton, urlField);
        navigationBar.setPadding(new Insets(10));

        BorderPane header = new BorderPane();
        header.setLeft(navigationBar);
        header.setCenter(titleBanner);
        header.setPadding(new Insets(10, 20, 10, 20));

        // Login/logout buttons and fields
        Button signInButton = new Button("Sign In");
        Button loginButton = new Button("Login");
        Button logoutButton = new Button("Logout");
        logoutButton.setVisible(false);  // Initially hidden

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        // Login layout
        VBox loginLayout = new VBox(10, new Label("Username"), usernameField, new Label("Password"), passwordField, loginButton, signInButton);
        loginLayout.setPadding(new Insets(10));

        // Handle Login
        loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, header, loginLayout, logoutButton));

        // Handle Logout
        logoutButton.setOnAction(e -> handleLogout(header, loginLayout, logoutButton));

        // Handle clicking Sign In to switch to the sign-in scene
        signInButton.setOnAction(e -> primaryStage.setScene(createSignInScene(primaryStage, primaryStage.getScene())));

        // Settings menu
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu("Settings");
        MenuItem setHomepage = new MenuItem("Set Homepage");

        MenuItem viewHistory = new MenuItem("View Search History");
        settingsMenu.getItems().addAll(setHomepage, viewHistory);
        menuBar.getMenus().add(settingsMenu);

        // Handle setting the homepage
        setHomepage.setOnAction(e -> handleSetHomepage());

        // Handle viewing search history
        viewHistory.setOnAction(e -> handleViewSearchHistory());

        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(new VBox(menuBar, header));
        mainLayout.setCenter(tabPane);  // Set the tab pane in the center

        return new Scene(mainLayout, 1024, 768);
    }

    // Method to create a new tab with a WebView
    private Tab createTab(String title, String url) {
        Tab tab = new Tab(title);
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(url);  // Load the URL

        // Error handling for web loading
        webEngine.setOnError((WebErrorEvent event) -> {
            System.err.println("Error loading web page: " + event.getMessage());
        });

        // Update the search history when a new page is loaded
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            searchHistory.add(newLocation);  // Add to search history
            if (isDownloadableFile(newLocation)) {
                promptDownload(newLocation);
            }
        });

        tab.setContent(webView);
        return tab;
    }

    private void handleViewSearchHistory() {
        // Display the search history in a pop-up dialog
        StringBuilder historyString = new StringBuilder("Search History:\n");
        for (String history : searchHistory) {
            historyString.append(history).append("\n");
        }

        Alert historyAlert = new Alert(Alert.AlertType.INFORMATION);
        historyAlert.setTitle("Search History");
        historyAlert.setHeaderText("Your Search History");
        historyAlert.setContentText(historyString.toString());
        historyAlert.showAndWait();
    }

    private void handleSetHomepage() {
        // Set a new homepage
        TextInputDialog dialog = new TextInputDialog(homeUrl);
        dialog.setTitle("Set Homepage");
        dialog.setHeaderText("Set a new homepage");
        dialog.setContentText("Please enter the new homepage URL:");
        dialog.showAndWait().ifPresent(newUrl -> homeUrl = newUrl);
    }

    private void navigateBack(TextField urlField) {
        WebView currentWebView = getCurrentWebView();
        WebEngine webEngine = currentWebView.getEngine();
        webEngine.getHistory().go(-1);  // Go back in the web history
        urlField.setText(webEngine.getLocation());  // Update URL field
    }

    private void navigateForward(TextField urlField) {
        WebView currentWebView = getCurrentWebView();
        WebEngine webEngine = currentWebView.getEngine();
        webEngine.getHistory().go(1);  // Go forward in the web history
        urlField.setText(webEngine.getLocation());  // Update URL field
    }

    private void reloadCurrentTab() {
        WebView currentWebView = getCurrentWebView();
        currentWebView.getEngine().reload();  // Reload the current web page
    }

    private void loadHomePage(TextField urlField) {
        WebView currentWebView = getCurrentWebView();
        currentWebView.getEngine().load(homeUrl);  // Load the homepage
        urlField.setText(homeUrl);  // Update URL field
    }

    private void loadUrlFromField(TextField urlField) {
        String url = urlField.getText();
        WebView currentWebView = getCurrentWebView();
        currentWebView.getEngine().load(url);  // Load the URL from the text field
    }

    private WebView getCurrentWebView() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        return (WebView) currentTab.getContent();  // Return the WebView of the current tab
    }

    // Downloading system methods
    private boolean isDownloadableFile(String url) {
        // Check for common downloadable file extensions
        return url.endsWith(".pdf") || url.endsWith(".zip") || url.endsWith(".jpg") ||
                url.endsWith(".png") || url.endsWith(".mp3") || url.endsWith(".mp4");
    }

    private void promptDownload(String fileUrl) {
        try {
            // Extract file name from the URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

            // Set up a file chooser to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName(fileName);

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                // Start the download in a separate thread
                new Thread(() -> downloadFile(fileUrl, file)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String fileUrl, File destinationFile) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded: " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dummy login method for testing
    private void handleLogin(TextField usernameField, PasswordField passwordField, BorderPane header, VBox loginLayout, Button logoutButton) {
        if (usernameField.getText().equals("admin") && passwordField.getText().equals("admin")) {
            isLoggedIn = true;
            username = usernameField.getText();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText(null);
            alert.setContentText("Welcome, " + username + "!");
            alert.showAndWait();

            logoutButton.setVisible(true);  // Show logout button
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid credentials!");
            alert.showAndWait();
        }
    }

    private void handleLogout(BorderPane header, VBox loginLayout, Button logoutButton) {
        isLoggedIn = false;
        username = "";
        logoutButton.setVisible(false);  // Hide logout button
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout Successful");
        alert.setHeaderText(null);
        alert.setContentText("You have been logged out.");
        alert.showAndWait();
    }

    private Scene createSignInScene(Stage primaryStage, Scene browserScene) {
        BorderPane layout = new BorderPane();

        // Create UI components
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button signInButton = new Button("Sign In");

        VBox formLayout = new VBox(10, new Label("Username"), usernameField, new Label("Password"), passwordField, signInButton);
        formLayout.setPadding(new Insets(20));

        layout.setCenter(formLayout);

        // Handle Sign In
        signInButton.setOnAction(e -> {
            if (usernameField.getText().equals("admin") && passwordField.getText().equals("admin")) {
                isLoggedIn = true;
                username = usernameField.getText();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Successful");
                alert.setHeaderText(null);
                alert.setContentText("Welcome, " + username + "!");
                alert.showAndWait();

                primaryStage.setScene(browserScene);  // Return to browser scene after login
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid username or password.");
                alert.showAndWait();
            }
        });

        return new Scene(layout, 400, 300);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
