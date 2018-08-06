package uk.gov.dvla.osg.despatchapp.controllers;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.utilities.BarcodeReader;
import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;

public class MainFormController {

    private static final ObservableList<String> SITES = FXCollections.observableArrayList("MORRISTON", "TY FELIN");
    private static final int JID_LENGTH = 10;
    private ObservableList<JobId> model = FXCollections.observableArrayList();
    private BarcodeReader barcodeReader = new BarcodeReader();
    private RemoveItemController removeItemController = new RemoveItemController(model);
    private SubmitFileController submitFileController = new SubmitFileController(model);
    
    @FXML private ChoiceBox cbSite;
    @FXML private ListView lbContent;
    @FXML private Label lblError;
    @FXML private Label lblSite;
    @FXML private Label lblItems;
    @FXML private Button btnSubmit;

    @FXML
    private void initialize() {
        cbSite.setItems(SITES);
        lbContent.setDisable(true);
        FxUtils.disableNode(btnSubmit);
        cbSite.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
            cbSite.getSelectionModel().select((int) newValue);
            lblSite.setText((String) cbSite.getSelectionModel().getSelectedItem());
            FxUtils.swapControls(cbSite, lblSite);
            lbContent.setDisable(false);
            FxUtils.enableNode(btnSubmit);
            lblSite.requestFocus();
        });
        lbContent.setItems(model);
        lbContent.setDisable(true);
        lblItems.textProperty().bind(Bindings.concat("No. of items: ", Bindings.size((lbContent.getItems())).asString()));
    }

    @FXML
    private void keyTyped(KeyEvent event) {
        // Do nothing until a site has been selected
        if (lbContent.isDisable()) {
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
                    // All good so add it to the list
                    model.add(jid);
                }
            } else {
                FxUtils.displayErrorMessage(lblError, "Whoops that wasn't a Job ID!");
            }
        }
    }

    /**
     * Remove item from listbox and reprint list when right mouse button is clicked.
     * Left mouse button selects list item.
     * @param e mouse click
     */
    @FXML
    private void mousePressed(MouseEvent e) {
        // Check right mouse button clicked
        if (e.getButton() == MouseButton.SECONDARY && !model.isEmpty()) {
            removeItemController.remove(lbContent);
        }
    }
    
    @FXML
    private void submit() {
        String successMsg = model.size() == 1 ? "1 item sent to RPD" : model.size() + " items sent to RPD!";
        boolean success = submitFileController.submit();
        if (success) {
            FxUtils.displaySuccessMessage(lblError, successMsg);
        }
    }
    
    /**
     * Validates if the entered barcode is a valid ten-digit RPD job id.
     * @param barcode the barcode to validate
     * @return true, if barcode is a valid ten-digit Job ID
     */
    private boolean barcodeIsJobId(String barcode) {
        return StringUtils.isNumeric(barcode) && barcode.length() == JID_LENGTH;
    }
    
}
