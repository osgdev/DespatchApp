package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.utilities.BarcodeReader;
import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;

public class MainFormController {
    
    static final Logger LOGGER = LogManager.getLogger();
    
    private static final String EMPTY = "";
    private static final ObservableList<String> SITES = FXCollections.observableArrayList("MORRISTON", "TY FELIN", "BRP");
    private static final int JID_LENGTH = 10;
    
    @FXML private ChoiceBox cbSite;
    @FXML private ListView lvContent;
    @FXML private Label lblError;
    @FXML private Label lblSite;
    @FXML private Label lblItems;
    @FXML private Button btnSubmit;

    private ObservableList<JobId> model = FXCollections.observableArrayList();
    private BarcodeReader barcodeReader = new BarcodeReader();
    private RemoveItemController removeItemController;
    private SubmitFileController submitFileController;
    private FileManager fileManager;
    
    @FXML
    private void initialize() {
        cbSite.setItems(SITES);
        lvContent.setDisable(true);
        FxUtils.disableNode(btnSubmit);
        cbSite.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
            cbSite.getSelectionModel().select((int) newValue);
            String siteString = (String) cbSite.getSelectionModel().getSelectedItem();
            lblSite.setText(siteString);
            FxUtils.swapControls(cbSite, lblSite);
            lvContent.setDisable(false);
            FxUtils.enableNode(btnSubmit);
            lblSite.requestFocus();
            fileManager = new FileManager(siteString);
            removeItemController = new RemoveItemController(fileManager);
            submitFileController = new SubmitFileController(siteString, fileManager);
            try {
                List<String> fileLines = fileManager.read();
                for (String line : fileLines) {
                    model.add(JobId.fromString(line));
                }
            } catch (IOException ex) {
                LOGGER.fatal("Unable to read from temp data file", ex);
                ErrMsgDialog.builder("File read error", "Unable to read from data file.").display();
            }
        });
        
        lvContent.setItems(model);
        lvContent.setDisable(true);
        lblItems.textProperty().bind(Bindings.concat("No. of items: ", Bindings.size((lvContent.getItems())).asString()));
    }

    @FXML
    private void keyTyped(KeyEvent event) {
        // Do nothing until a site has been selected
        if (lvContent.isDisable()) {
            event.consume();
        } else if (barcodeReader.handle(event)) {
            // Barcode Reader has returned an enter key, check if input is a valid Job ID
            String input = barcodeReader.getBarcode();
            if (barcodeIsJobId(input)) {
                String timeStamp = DateUtils.timeStamp("dd/MM/yy hh:mm:ss");
                JobId jid = JobId.newInstance(input, timeStamp);
                // Check if ID has already been entered
                if (model.contains(jid)) {
                    FxUtils.displayErrorMessage(lblError, "Job ID already entered!");
                } else {
                    // Add it to the file
                    try {
                        fileManager.append(jid.toString());
                        // All good so add it to the list
                        model.add(jid);
                    } catch (IOException ex) {
                        LOGGER.fatal("Unable to write to file", ex);
                        ErrMsgDialog.builder("File write error", "Unable to write to file").display();
                    }
                }
            } else {
                FxUtils.displayErrorMessage(lblError, "Whoops that wasn't a Job ID!");
            }
        }
    }

    @FXML
    private void lvKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
            // Only remove if an item was selected
            int index = lvContent.getSelectionModel().getSelectedIndex();
            if (index != -1) {
                removeItemController.remove(lvContent);
            }   
        }
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
        boolean success = submitFileController.submit(jobIdList);
        if (success) {
            String successMsg = jobIdList.size() == 1 ? "1 item sent to RPD" : jobIdList.size() + " items sent to RPD!";
            model.clear();
            FxUtils.displaySuccessMessage(lblError, successMsg);
        }
        // Reset button
        Platform.runLater(() -> {
            btnSubmit.setText("Submit");
            btnSubmit.setGraphic(null);
        });
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
