package uk.gov.dvla.osg.despatchapp.main;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.gov.dvla.osg.rpd.web.config.NetworkConfig;

public class Main extends Application {
    static final Logger LOGGER = LogManager.getLogger();
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/MainScreen.fxml"));
        primaryStage.setTitle("Despatch App v1.0");
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logo.jpg")));
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // Verify correct number of args
        if (args.length != 2) {
            LOGGER.fatal("Incorrect number of arguments supplied!");
            System.exit(1);
        }
        
        // Check network config file path is correct
        String networkConfigFile = args[0];
        boolean propsFileExists = new File(networkConfigFile).exists();
        if (!propsFileExists) {
            LOGGER.fatal("Properties File '{}' doesn't exist", networkConfigFile);
            System.exit(1);
        }
        
        // Check app config file path is correct
        String appConfigFile = args[1];
        propsFileExists = new File(appConfigFile).exists();
        if (!propsFileExists) {
            LOGGER.fatal("Properties File '{}' doesn't exist", appConfigFile);
            System.exit(1);
        }
        // Initialise the network configuration from the file
        NetworkConfig.init(networkConfigFile);
        // Initialise the network configuration from the file
        AppConfig.init(appConfigFile);
        // Checks that the filepaths can be read correctly
        AppConfig.getInstance();
        // Launch the main screen
        launch(args);
    }

}
