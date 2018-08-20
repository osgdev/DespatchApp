package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.utilities.BarcodeReader;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;

public class MainFormController {

    static final Logger LOGGER = LogManager.getLogger();

    private static final String EMPTY = "";
    private static final ObservableList<String> SITES = FXCollections.observableArrayList("MORRISTON", "TY FELIN");
    private static final int JID_LENGTH = 10;

    @FXML ChoiceBox cbSite;
    @FXML ListView lvContent;
    @FXML private Label lblError;
    @FXML Label lblSite;
    @FXML private Label lblItems;
    @FXML Button btnSubmit;

    ObservableList<JobId> model = FXCollections.observableArrayList();
    private BarcodeReader barcodeReader = new BarcodeReader();
    RemoveItemController removeItemController;
    SubmitFileController submitFileController;
    FileManager fileManager;

    @FXML
    private void initialize() {
        cbSite.setItems(SITES);
        lvContent.setDisable(true);
        FxUtils.disableNode(btnSubmit);
        cbSite.getSelectionModel().selectedIndexProperty().addListener(new SiteListenerImplementation(this));

        lvContent.setItems(model);
        lvContent.setDisable(true);
        lblItems.textProperty().bind(Bindings.concat("No. of items: ", Bindings.size((lvContent.getItems())).asString()));
    }

    @FXML
    private void keyTyped(KeyEvent event) {
        // Do nothing until a site has been selected
        if (lvContent.isDisable()) {
            event.consume();
            return;
        }
        // Do nothing until full barcode has been read
        if (!barcodeReader.handle(event)) {
            return;
        }
        // Check if input is a valid Job ID
        String input = barcodeReader.getBarcode();
        if (!barcodeIsJobId(input)) {
            FxUtils.displayErrorMessage(lblError, "Whoops that wasn't a Job ID!");
            return;
        }
        // Create JobId from input
        JobId jid = JobId.newInstance(input);
        // Check if ID has already been entered
        if (model.contains(jid)) {
            FxUtils.displayErrorMessage(lblError, "Job ID already entered!");
            return;
        }
        // Add it to the file
        try {
            fileManager.append(jid.toString());
        } catch (IOException ex) {
            LOGGER.error(ex);
            ErrMsgDialog.builder("File write error", "Unable to write to file").display();
            return;
        }
        // All good so add it to the list
        model.add(jid);
    }

    @FXML
    private void lvKeyPressed(KeyEvent event) {
        if (!FxUtils.deleteKeyPressed(event) || FxUtils.listViewIsEmpty(lvContent)) {
            return;
        }
        
        removeItemController.remove(lvContent);
    }

    /**
     * Remove item from listbox and reprint list when right mouse button is clicked.
     * Left mouse button selects list item.
     * 
     * @param e mouse click
     */
    @FXML
    private void mousePressed(MouseEvent e) {
        // Check right mouse button clicked
        if (!model.isEmpty() && e.getButton() == MouseButton.SECONDARY) {
            removeItemController.remove(lvContent);
        }
    }

    @FXML
    private void submit() {
        if (model.size() <= 0) {
            FxUtils.displayErrorMessage(lblError, "No items to send.");
            return;
        }
        // Add progress indicator to button
        Platform.runLater(() -> {
            btnSubmit.setText(EMPTY);
            btnSubmit.setGraphic(new ProgressIndicator());
        });
        // Convert model to list of strings
        List<String> jobIdList = model.stream().map(jid -> jid.getJobId()).collect(Collectors.toList());
        // Send the data to RPD
        if (!submitFileController.login().trySubmit(jobIdList)) {
            return;
        }
        String successMsg = jobIdList.size() == 1 ? "1 item sent to RPD" : jobIdList.size() + " items sent to RPD!";
        FxUtils.displaySuccessMessage(lblError, successMsg);
        model.clear();
        // Reset button
        Platform.runLater(() -> {
            btnSubmit.setText("Submit");
            btnSubmit.setGraphic(null);
        });
    }

    /**
     * Release lock on application when application terminates.
     * Called from the OnClose event set in the Main class.
     */
    public void shutdown() {
        fileManager.unlockTempFile();
    }

    /**
     * Validates if the entered barcode is a valid ten-digit RPD job id.
     * 
     * @param barcode the barcode to validate
     * @return true, if barcode is a valid ten-digit Job ID
     */
    private boolean barcodeIsJobId(String barcode) {
        return StringUtils.isNumeric(barcode) && barcode.length() == JID_LENGTH;
    }

}
