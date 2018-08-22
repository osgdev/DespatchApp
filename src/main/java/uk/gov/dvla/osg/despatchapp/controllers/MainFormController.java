package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
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
import uk.gov.dvla.osg.despatchapp.models.PrintSite;
import uk.gov.dvla.osg.despatchapp.utilities.BarcodeReader;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.despatchapp.views.LoginGui;

public class MainFormController {

    static final Logger LOGGER = LogManager.getLogger();

    private static final String EMPTY = "";
    private static final ObservableList<PrintSite> SITES = FXCollections.observableArrayList();

    @FXML ChoiceBox cbSite;
    @FXML ListView lvContent;
    @FXML Label lblError;
    @FXML Label lblSite;
    @FXML Label lblItems;
    @FXML Button btnSubmit;

    ObservableList<JobId> model = FXCollections.observableArrayList();
    BarcodeReader barcodeReader = new BarcodeReader();
    RemoveItemController removeItemController;
    SubmitFileController submitFileController;
    FileManager fileManager;

    @FXML
    private void initialize() {
        EnumUtils.getEnumList(PrintSite.class).forEach(site -> SITES.add(site));
        cbSite.setItems(SITES);
        lvContent.setDisable(true);
        FxUtils.disableNode(btnSubmit);
        cbSite.getSelectionModel().selectedIndexProperty().addListener(new SiteListenerImplementation(this));

        lvContent.setItems(model);
        lvContent.setDisable(true);
        lblItems.textProperty().bind(Bindings.concat("No. of items: ", Bindings.size((lvContent.getItems())).asString()));
    }

    /**
     * Process key press events at the form level. No keyboard input is permitted. 
     * The ListView is populated from input by a barcode scanner only. All valid
     * input is persisted to a temp file when scanned.
     *
     * @param event the event
     */
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
        if (!JobId.isValid(input)) {
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
            
            ErrMsgDialog.builder("File write error", "Unable to write to file")
                        .action(MessageFormat.format("Please request read/write access to [{0}]", fileManager.getTempFileDirectory()))
                        .display();
            return;
        }
        // All good so add it to the list
        model.add(jid);
    }

    /**
     * Removes an item from the ListView if either the Delete or
     * Backspace keys are pressed.
     *
     * @param event the event
     */
    @FXML
    private void lvKeyPressed(KeyEvent event) {
        if (!FxUtils.deleteKeyPressed(event) || model.isEmpty()) {
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
        if (!model.isEmpty() && e.getButton() == MouseButton.SECONDARY) {
            removeItemController.remove(lvContent);
        }
    }

    /**
     * Click event for the Submit button.
     */
    @FXML
    private void submit() {
        // Display message if no items were added
        if (model.isEmpty()) {
            FxUtils.displayErrorMessage(lblError, "No items to send.");
            return;
        }
        // Add progress indicator to button
        Platform.runLater(() -> {
            btnSubmit.setText(EMPTY);
            btnSubmit.setGraphic(new ProgressIndicator());
        });
        
        // Queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
            try {
                // Login user
                LoginGui.newInstance().load();
                // Convert model to list of strings
                List<String> jobIdList = model.stream().map(JobId::getJobId).collect(Collectors.toList());
                // Send the data to RPD
                if (submitFileController.trySubmit(jobIdList)) {
                    String successMsg = jobIdList.size() == 1 ? "1 item sent to RPD" : jobIdList.size() + " items sent to RPD!";
                    FxUtils.displaySuccessMessage(lblError, successMsg);
                    model.clear();
                } else {
                    FxUtils.displayErrorMessage(lblError, "Unable to send files to RPD!");
                }
            } finally {
                doneLatch.countDown();
            }

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            LOGGER.fatal(e.getMessage());
        }

        // Reset button
        btnSubmit.setText("Submit");
        btnSubmit.setGraphic(null);
    }

    /**
     * Release lock on application when application terminates. Called from the
     * OnClose event set in the Main class.
     */
    public void shutdown() {
        if (fileManager == null) {
            return;
        }
        fileManager.unlockTempFile();
    }

}
