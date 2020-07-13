package uk.gov.dvla.osg.despatchapp.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import uk.gov.dvla.osg.despatchapp.config.AppConfig;
import uk.gov.dvla.osg.despatchapp.controllers.MainFormController;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;

/**************** REVISION HISTORY ***************
  1.00 Initial Version
  1.01 Added full RPD Web Client functionality
  1.02 Fixed issue with FileDeleter retention period
  1.03 Added application credentials to submit method in web client
  1.04 Fixed issue with time being displayed as 12hr
  1.05 Added file locking to ensure only one open application per site
  1.06 Added checking for read/write access to Temp and Repo directories, plus BRP site removed
  1.07 Added RunDate to EOT
  1.08 Fixed issue with RPD Login dialog stalling
  1.09 Fixed issue with inability to close form on first load - shutdown method
  1.10 Added additonal logging when loading resources
 ************************************************/
public class Main extends Application {
    
    private static final Logger LOG = LogManager.getLogger();
    private Image logo;
    private URL fxml;
    
    @Override
    public void init() {
        try (InputStream asStream = getClass().getResourceAsStream("/Images/dispatch.png")) {
            logo = new Image(asStream);
        } catch (IOException ex) {
            String msg = String.format("Unable to load logo: '%s'", ex.getMessage());
            LOG.error(msg);
            showErrorMessage(msg, "Contact Dev team.");
            System.exit(999);;
        }
        
        try {
            fxml = getClass().getResource("/FXML/MainScreen.fxml");
        } catch (Exception ex) {
            String msg = String.format("Unable to load FXML: '%s'", ex.getMessage());
            LOG.error(msg);
            showErrorMessage(msg, "Contact Dev team.");
            System.exit(999);;
        }
    }
    
    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // load root scene
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();
            // unlock files when scene closes
            MainFormController controller = loader.getController();
            primaryStage.setOnCloseRequest(e -> controller.shutdown());
            // apply window settings
            primaryStage.setTitle("Despatch App v1.09");
            primaryStage.getIcons().add(logo);
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            // display the GUI
            primaryStage.show();
        } catch (IOException ex) {
            String msg = String.format("Unable to load window: '%s'", ex.getMessage());
            LOG.error(msg);
            showErrorMessage(msg, "Contact Dev team.");
        }
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
            LOG.error(msg);
            showErrorMessage(msg, "Usage: DespatchApp.jar {networkConfig} {appConfig}");
            return;
        }
        
        // Check network config file path is correct
        String networkConfigFile = args[0];
        if (!new File(networkConfigFile).exists()) {
            String msg = MessageFormat.format("Properties File '{0}' doesn't exist", networkConfigFile);
            LOG.error(msg);
            showErrorMessage(msg, "Check the filename in the shortcut.");
            return;
        }
        
        try {
            // Initialise the network configuration from the file
            NetworkConfig.init(networkConfigFile);
        } catch (RuntimeException ex) {
            LOG.error(ex.getMessage());
            showErrorMessage(ex.getMessage(), "Check Netowrk Configuration file.");
            return;
        }
        
        // Check app config file path is correct
        String appConfigFile = args[1];
        if (!new File(appConfigFile).exists()) {
            String msg = MessageFormat.format("Properties File '{0}' doesn't exist", appConfigFile);
            LOG.error(msg);
            showErrorMessage(msg, "Check the filename in the shortcut.");
            return;
        }

        try {
            // Initialise the application configuration from the file
            AppConfig.init(appConfigFile);
            // Checks that the filepaths can be read correctly
            AppConfig.getInstance();
        } catch (RuntimeException ex) {
            LOG.error(ex.getMessage());
            showErrorMessage(ex.getMessage(), "Check Application Configuration file.");
            return;
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
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
