package uk.gov.dvla.osg.despatchapp.controllers;

import java.lang.management.ManagementFactory;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.client.LoginClient;
import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class LogInController {

    static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    /**
     * Submits login request to RPD webservice. If token is retrieved then the user
     * is authenticated, else the RPD error message is displayed.
     */
    public void btnLoginClicked() {

        Session.getInstance().setUserName(nameField.getText().trim());
        Session.getInstance().setPassword(passwordField.getText().trim());

        // disable buttons while RPD is contacted
        lblMessage.setText("Please wait...");
        btnLogin.setDisable(true);
        nameField.setDisable(true);
        passwordField.setDisable(true);

        final LoginClient login = LoginClient.getInstance(NetworkConfig.getInstance());

        // Login performed on background thread to prevent GUI freezing
        new Thread(() -> {
            
            // bypass login while testing
            if (DEBUG_MODE) {
                return;
            }
           
            Optional<String> token = login.getSessionToken(Session.getInstance().getUserName(), Session.getInstance().getPassword());
     
            // if token wasn't retrieved & not in debug mode, display error dialog
            if (!token.isPresent()) {
                Platform.runLater(() -> {
                    RpdErrorResponse error = login.getErrorResponse();
                    ErrMsgDialog.builder(error.getCode(), error.getMessage())
                        .action(error.getAction()).display();
                    // cleanup fields
                    lblMessage.setText("");
                    passwordField.setText("");
                    nameField.setDisable(false);
                    passwordField.setDisable(false);
                    passwordField.requestFocus();
                });
            } else {
                Session.getInstance().setToken(token.get());
                Platform.runLater(() -> ((Stage) btnLogin.getScene().getWindow()).close());
            }
        }).start();
    }

    /**
     * Method responds to keypress events on the user and password fields. Login
     * button is only enabled when both fields contain text.
     */
    public void txtChanged() {
        if (nameField.getText().trim().equals("") || passwordField.getText().trim().equals("")) {
            btnLogin.setDisable(true);
        } else {
            btnLogin.setDisable(false);
        }
    }

}
