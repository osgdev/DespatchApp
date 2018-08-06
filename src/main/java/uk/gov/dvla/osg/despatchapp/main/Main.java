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
        primaryStage.setTitle("Despatch App Mock v1.0");
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logo.jpg")));
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // Verify correct number of args
        if (args.length != 1) {
            LOGGER.fatal("Incorrect number of arguments supplied!");
            System.exit(1);
        }
        
        // Check config file path is correct
        String configFile = args[0];
        boolean propsFileExists = new File(configFile).exists();
        if (!propsFileExists) {
            LOGGER.fatal("Properties File '{}' doesn't exist", configFile);
            System.exit(1);
        }
        
        // Initialise the network configuration from the file
        NetworkConfig.init(configFile);
        launch(args);
    }

}
