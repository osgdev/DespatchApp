package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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
import uk.gov.dvla.osg.vault.enums.Site;

public class MainFormController {

    private static final ObservableList<String> SITES = FXCollections.observableArrayList("MORRISTON", "TY FELIN");
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
            Site site = siteString.equals("MORRISTON") ? Site.M : Site.F;
            fileManager = new FileManager(site);
            removeItemController = new RemoveItemController(fileManager);
            submitFileController = new SubmitFileController(site, fileManager);
            try {
                List<String> fileLines = fileManager.read();
                for (String line : fileLines) {
                    model.add(JobId.fromString(line));
                }
            } catch (IOException ex) {
                ErrMsgDialog.builder("Unable to read from data file", ex.getMessage()).display();
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
                JobId jid = JobId.newInstance(input, DateUtils.getTimeStamp("dd/MM/yy hh:mm:ss"));
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
                        ErrMsgDialog.builder("Unable to write to file", ex.getMessage()).display();
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
        if (e.getButton() == MouseButton.SECONDARY && !model.isEmpty()) {
            removeItemController.remove(lvContent);
        }
    }

    @FXML
    private void submit() {
        if (model.size() > 0) {
            String successMsg = model.size() == 1 ? "1 item sent to RPD" : model.size() + " items sent to RPD!";
            // Convert model to list of strings
            List<String> jobIdList = model.stream().map(jid -> jid.getJobId()).collect(Collectors.toList());
            boolean success = submitFileController.submit(jobIdList);
            if (success) {
                model.clear();
                FxUtils.displaySuccessMessage(lblError, successMsg);
            }
        } else {
            FxUtils.displayErrorMessage(lblError, "No items to send.");
        }
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
