package uk.gov.dvla.osg.despatchapp.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
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
        launch(args);
    }

}
