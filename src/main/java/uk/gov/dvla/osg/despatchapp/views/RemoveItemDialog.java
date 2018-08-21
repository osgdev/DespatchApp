package uk.gov.dvla.osg.despatchapp.views;

import java.text.MessageFormat;
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
        dialogPane.setContentText(MessageFormat.format("Do you want to remove '{0}'?", str));
    }
    
    /**
     * Shows the dialog and waits for the user response (in other words, brings up a blocking dialog, with the returned value the users input).
     *
     * @return An Optional that contains the result.
     */
    public Optional<ButtonType> displayAndWait() {
        return dialog.showAndWait();
    }
    
}
