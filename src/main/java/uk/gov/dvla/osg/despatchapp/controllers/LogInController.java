package uk.gov.dvla.osg.despatchapp.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import uk.gov.dvla.osg.rpd.web.config.Session;

public class LogInController {

    static final Logger LOGGER = LogManager.getLogger();

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    /**
     * Submits login request to RPD webservice. 
     * If token is retrieved then the user is authenticated, else the RPD error message dialog is displayed.
     */
    public void btnLoginClicked() {

        Session.getInstance().setUserName(nameField.getText().trim());
        Session.getInstance().setPassword(passwordField.getText().trim());

        // disable buttons while RPD is contacted
        lblMessage.setText("Please wait...");
        btnLogin.setDisable(true);
        nameField.setDisable(true);
        passwordField.setDisable(true);

        // Login performed on background thread to prevent GUI freezing
        new Thread(new LoginMethod(nameField, passwordField, btnLogin, lblMessage)).start();
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
