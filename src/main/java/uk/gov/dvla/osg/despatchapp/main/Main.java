package uk.gov.dvla.osg.despatchapp.main;

import java.io.File;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.gov.dvla.osg.despatchapp.config.AppConfig;
import uk.gov.dvla.osg.despatchapp.controllers.MainFormController;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;

/**
 *************** REVISION HISTORY ***************
 *1.00 Initial Version
 *1.01 Added full RPD Web Client functionality
 *1.02 Fixed issue with FileDeleter retention period
 *1.03 Added application credentials to submit method in web client
 *1.04 Fixed issue with time being displayed as 12hr
 *1.05 Added file locking to ensure only one open application per site
 ************************************************
 */
public class Main extends Application {
    
    /** The Constant LOGGER. */
    static final Logger LOGGER = LogManager.getLogger();
    
    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainScreen.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Despatch App v1.05");
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logo.jpg")));
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        MainFormController controller = loader.getController();
        primaryStage.setOnCloseRequest(e -> controller.shutdown());
        primaryStage.show();
    }
    
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        // Verify correct number of args
        if (args.length != 2) {
            String msg = "Incorrect number of arguments supplied!";
            LOGGER.fatal(msg);
            showErrorMessage(msg, "Usage: DespatchApp.jar {networkConfig} {appConfig}");
            return;
        }
        
        // Check network config file path is correct
        String networkConfigFile = args[0];
        if (!new File(networkConfigFile).exists()) {
            String msg = MessageFormat.format("Properties File '{0}' doesn't exist", networkConfigFile);
            LOGGER.fatal(msg);
            showErrorMessage(msg, "Check the filename in the shortcut.");
            return;
        }
        
        try {
            // Initialise the network configuration from the file
            NetworkConfig.init(networkConfigFile);
        } catch (RuntimeException ex) {
            LOGGER.fatal(ex.getMessage());
            showErrorMessage(ex.getMessage(), "Check Netowrk Configuration file.");
            return;
        }
        
        // Check app config file path is correct
        String appConfigFile = args[1];
        if (!new File(appConfigFile).exists()) {
            String msg = MessageFormat.format("Properties File '{0}' doesn't exist", appConfigFile);
            LOGGER.fatal(msg);
            showErrorMessage(msg, "Check the filename in the shortcut.");
            return;
        }

        try {
            // Initialise the application configuration from the file
            AppConfig.init(appConfigFile);
            // Checks that the filepaths can be read correctly
            AppConfig.getInstance();
        } catch (RuntimeException ex) {
            LOGGER.fatal(ex.getMessage());
            showErrorMessage(ex.getMessage(), "Check Application Configuration file.");
            return;
        } catch (Exception ex) {
            LOGGER.fatal(ex.getMessage());
            showErrorMessage(ex.getMessage(), "Check Application Configuration file.");
            return;
        }
        // Launch the main screen
        launch(args);
    }

    /**
     * Show error message to the user and wait for the dialog to close before continuing.
     * @param msg the error message
     * @param action the error action
     */
    public static void showErrorMessage(String msg, String action) {
        FxUtils.runAndWait(() -> ErrMsgDialog.builder("Application Start", msg).action(action).display());
    }

}
