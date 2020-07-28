package uk.gov.dvla.osg.despatchapp.views;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginGui {
    
    static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * New instance.
     *
     * @return the Login GUI
     */
    public static void newInstance() {
        new LoginGui();
    }
    
    /**
     * Required as getClass() needs to be an instantiated object to work
     */
    private LoginGui() {
        try (InputStream asStream = getClass().getResourceAsStream("/Images/logo.jpg")) {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/LoginGui.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("RPD Log In");
            loginStage.getIcons().add(new Image(asStream));
            loginStage.setResizable(false);
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setScene(new Scene(root, 300, 250));
            loginStage.showAndWait();
        } catch (IOException ex) {
            LOGGER.fatal("Unable to load Login Gui {}", ex.getMessage());
        }
    }

}
