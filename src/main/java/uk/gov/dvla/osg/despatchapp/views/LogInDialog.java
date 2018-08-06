package uk.gov.dvla.osg.despatchapp.views;

import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import uk.gov.dvla.osg.rpd.web.client.LoginClient;
import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class LogInDialog {
    
    private static final boolean DEBUG_MODE = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    private ErrMsgDialog errMsg = new ErrMsgDialog();
    private boolean loggedIn = false;
    
    public static LogInDialog newInstance() {
        return new LogInDialog();
    }
    
    private LogInDialog() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        // Set the icon (must be included in the project).
        dialog.setGraphic(new ImageView(this.getClass().getResource("/Images/login.png").toString()));
        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> usernameField.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            Session.getInstance().setUserName(usernamePassword.getKey());
            Session.getInstance().setPassword(usernamePassword.getValue());

            // disable buttons while RPD is contacted
            final LoginClient login = LoginClient.getInstance(NetworkConfig.getInstance());

            // Login performed on background thread to prevent GUI freezing
            new Thread(() -> {
                // bypass login while testing
                Optional<String> token = Optional.empty();
                if (!DEBUG_MODE) {
                    token = login.getSessionToken(Session.getInstance().getUserName(), Session.getInstance().getPassword());
                }
                // if token wasn't retrieved & not in debug mode, display error dialog
                if (!token.isPresent() && !DEBUG_MODE) {
                    Platform.runLater(() -> {
                        RpdErrorResponse error = login.getErrorResponse();
                        errMsg.display(error.getCode(), error.getMessage(), error.getAction());
                        // cleanup fields
                        passwordField.setText("");
                        usernameField.setDisable(false);
                        passwordField.setDisable(false);
                        passwordField.requestFocus();
                    });
                } else {
                    Session.getInstance().setToken(token.get());
                    loggedIn = true;
                    dialog.close();
                }
            }).start();
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
        });
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
}
