package uk.gov.dvla.osg.despatchapp.views;

import java.io.IOException;

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
    public static LoginGui newInstance() {
        return new LoginGui();
    }
    
    private LoginGui() {}
    
    /**
     * Load and show the Login Dialog.
     */
    public void load() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/LoginGui.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("RPD Log In");
            loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logo.jpg")));
            loginStage.setResizable(false);
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setScene(new Scene(root, 300, 250));
            loginStage.showAndWait();
        } catch (IOException ex) {
            LOGGER.fatal("Unable to load Login Gui {}", ex.getMessage());
        }
    }

}
