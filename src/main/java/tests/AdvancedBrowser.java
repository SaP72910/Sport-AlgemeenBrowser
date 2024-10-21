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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AdvancedBrowser extends Application {

    private boolean isLoggedIn = false;  // Track login state
    private String username = "";  // Store the logged-in user's name
    private String homeUrl = "https://www.sportalgemeen.com";  // Default homepage

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(homeUrl);  // Load the homepage

        // Navigation buttons
        Button backButton = new Button("Back");
        Button forwardButton = new Button("Forward");
        Button reloadButton = new Button("Reload");
        Button homeButton = new Button("Home");

        TextField urlField = new TextField(homeUrl);
        urlField.setPrefWidth(600);

        // Navigation functionality
        backButton.setOnAction(e -> {
            if (webEngine.getHistory().getCurrentIndex() > 0) {
                webEngine.getHistory().go(-1);
                urlField.setText(webEngine.getLocation());
            }
        });

        forwardButton.setOnAction(e -> {
            if (webEngine.getHistory().getCurrentIndex() < webEngine.getHistory().getEntries().size() - 1) {
                webEngine.getHistory().go(1);
                urlField.setText(webEngine.getLocation());
            }
        });

        reloadButton.setOnAction(e -> webEngine.reload());

        homeButton.setOnAction(e -> {
            webEngine.load(homeUrl);  // Load the homepage
            urlField.setText(homeUrl);  // Update the URL field
        });

        urlField.setOnAction(e -> webEngine.load(urlField.getText()));  // Load URL from text field

        // Title banner
        Text titleBanner = new Text("Sport Algemeen");
        titleBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Navigation bar layout
        HBox navigationBar = new HBox(10, backButton, forwardButton, reloadButton, homeButton, urlField);
        navigationBar.setPadding(new Insets(10));

        BorderPane header = new BorderPane();
        header.setLeft(navigationBar);
        header.setCenter(titleBanner);
        header.setPadding(new Insets(10, 20, 10, 20));

        // Login/logout buttons and fields
        Button loginButton = new Button("Login");
        Button logoutButton = new Button("Logout");
        logoutButton.setVisible(false);  // Initially hidden
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        // Login layout
        VBox loginLayout = new VBox(10, new Label("Username"), usernameField, new Label("Password"), passwordField, loginButton);
        loginLayout.setPadding(new Insets(10));

        // Handle login
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();

            if (!user.isEmpty() && !pass.isEmpty()) {
                isLoggedIn = true;
                username = user;
                loginLayout.setVisible(false);  // Hide login form
                header.setRight(new Label("Logged in as: " + username));  // Display username
                logoutButton.setVisible(true);  // Show logout button
            } else {
                System.out.println("Invalid credentials.");
            }
        });

        // Handle logout
        logoutButton.setOnAction(e -> {
            isLoggedIn = false;
            header.setRight(null);  // Clear login info
            loginLayout.setVisible(true);  // Show login form
            logoutButton.setVisible(false);  // Hide logout button
        });

        // Settings menu
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu("Settings");
        MenuItem setHomepage = new MenuItem("Set Homepage");

        settingsMenu.getItems().add(setHomepage);
        menuBar.getMenus().add(settingsMenu);

        // Handle setting the homepage
        setHomepage.setOnAction(e -> {
            TextInputDialog homepageDialog = new TextInputDialog(homeUrl);
            homepageDialog.setTitle("Set Homepage");
            homepageDialog.setHeaderText(null);
            homepageDialog.setContentText("Enter new homepage URL:");
            homepageDialog.showAndWait().ifPresent(url -> {
                homeUrl = url;
                webEngine.load(homeUrl);  // Load the new homepage
                urlField.setText(homeUrl);  // Update the URL field
            });
        });

        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(new VBox(menuBar, header));
        mainLayout.setLeft(loginLayout);
        mainLayout.setCenter(webView);

        // Error handling for the WebView
        webEngine.setOnError((WebErrorEvent event) -> {
            System.err.println("Error loading web page: " + event.getMessage());
            urlField.setText("Error loading page");
        });

        // Update the URL bar as pages load
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> urlField.setText(newLocation));

        // Window close event handling
        primaryStage.setOnCloseRequest((WindowEvent we) -> {
            System.out.println("Browser window closed.");
        });

        // Set scene and show the browser
        Scene scene = new Scene(mainLayout, 1024, 768);
        primaryStage.setTitle("Sport Algemeen");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
