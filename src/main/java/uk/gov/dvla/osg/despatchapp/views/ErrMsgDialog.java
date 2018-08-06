package uk.gov.dvla.osg.despatchapp.views;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class ErrMsgDialog {

    public void display(String code, String message, String action) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(code);
        alert.setHeaderText(message);
        alert.setContentText(action);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public void display(String code, String message) {
        String action = "Please contact Dev Team if problem persists.";
        display(code, message, action );
    }

}
