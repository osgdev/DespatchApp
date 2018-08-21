package uk.gov.dvla.osg.despatchapp.views;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class ErrMsgDialog {

    final String code, message, action;

    private ErrMsgDialog(Builder builder) {
        this.code = builder.innerCode;
        this.message = builder.innerMessage;
        this.action = builder.innerAction;
    }
    
    private void display() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setMaxWidth(500); // Dialog can grow down but not across
        alert.setTitle(code);
        alert.setHeaderText(message);
        alert.setContentText(action);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Creates builder to build {@link ErrMsgDialog}.
     * @return created builder
     */
    public static Builder builder(String code, String message) {
        return new Builder(code, message);
    }

    /**
     * Builder to build {@link ErrMsgDialog}.
     */
    public static final class Builder {
        private String innerCode;
        private String innerMessage;
        private String innerAction = "Please contact Dev Team if problem persists."; // Default msg can be overridden if required

        private Builder(String code, String message) {
            this.innerCode = code;
            this.innerMessage = message;
        }

        public Builder action(String action) {
            this.innerAction = action;
            return this;
        }

        public void display() {
            new ErrMsgDialog(this).display();;
        }
    }
}
