package uk.gov.dvla.osg.despatchapp.views;

import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class RemoveItemDialog {

    public static Optional<ButtonType> show(String str) {
        Dialog<ButtonType> dialog = new Dialog<>();
        // create dialog
        dialog.setTitle("Delete Selected Item");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        dialogPane.setContentText(String.format("Do you want to remove '%s'?", str));
        return dialog.showAndWait();
    }
    
    
}
