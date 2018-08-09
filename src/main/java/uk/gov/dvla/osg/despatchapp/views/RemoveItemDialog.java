package uk.gov.dvla.osg.despatchapp.views;

import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class RemoveItemDialog {
    
    Dialog<ButtonType> dialog = new Dialog<>();
    
    public static RemoveItemDialog newInstance(String str) {
        return new RemoveItemDialog(str);
    }
    
    private RemoveItemDialog(String str) {
       // create dialog
        dialog.setTitle("Delete Selected Item");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        dialogPane.setContentText("Do you want to remove " + str + "?");
    }
    
    public Optional<ButtonType> display() {
        return dialog.showAndWait();
    }
    
}
