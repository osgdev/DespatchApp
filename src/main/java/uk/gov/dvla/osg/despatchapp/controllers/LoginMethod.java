package uk.gov.dvla.osg.despatchapp.controllers;

import java.lang.management.ManagementFactory;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.client.LoginClient;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class LoginMethod implements Runnable {

    private static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

    private TextField nameField;
    private PasswordField passwordField;
    private Button btnLogin;
    private Label lblMessage;
    private LoginClient login;
    private Session session;

    public LoginMethod(TextField nameField, PasswordField passwordField, Button btnLogin, Label lblMessage) {
        this.nameField = nameField;
        this.passwordField = passwordField;
        this.btnLogin = btnLogin;
        this.lblMessage = lblMessage;
        this.login = LoginClient.getInstance();
        this.session = Session.getInstance();
    }

    @Override
    public void run() {
        // bypass login while testing
        if (DEBUG_MODE) {
            session.setToken("TEST123");
            Platform.runLater(closeLoginWindow());
            return;
        }

        Optional<String> token = login.getSessionToken(session.getUserName(), session.getPassword());

        // if token wasn't retrieved & not in debug mode, display error dialog
        if (token.isPresent()) {
            session.setToken(token.get());
            Platform.runLater(closeLoginWindow());
            return;
        }
        
        Platform.runLater(displayError());

    }

    private Runnable closeLoginWindow() {
        return () -> ((Stage) btnLogin.getScene().getWindow()).close();
    }

    private Runnable displayError() {
        return () -> {
            RpdErrorResponse error = login.getErrorResponse();
            ErrMsgDialog.show(error.getCode(), error.getMessage(), error.getAction());
            // cleanup fields
            lblMessage.setText("");
            passwordField.setText("");
            nameField.setDisable(false);
            passwordField.setDisable(false);
            passwordField.requestFocus();
        };
    }

}
