package uk.gov.dvla.osg.despatchapp.views;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class ErrMsgDialog {

    private static final Alert DIALOG = new Alert(AlertType.ERROR);

    static {
        DIALOG.setAlertType(AlertType.ERROR);
        // show above form
        DIALOG.initModality(Modality.APPLICATION_MODAL);
        // Dialog can grow down but not across
        DIALOG.getDialogPane().setMaxWidth(500); 
    }
    
    public static void show(String code, String message) {
        show(code,message,"Please contact Dev Team if problem persists.");
    }
    
    public static void show(String code, String message, String action) {
        DIALOG.setTitle(code);
        DIALOG.setHeaderText(message);
        DIALOG.setContentText(action);
        DIALOG.showAndWait();
    }

}
