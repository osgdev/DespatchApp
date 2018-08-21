package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.despatchapp.views.RemoveItemDialog;

public class RemoveItemController {
    
    static final Logger LOGGER = LogManager.getLogger();
    

    private FileManager fileManager;

    public RemoveItemController(FileManager manager) {
        this.fileManager = manager;
    }

    /**
     * Prompts the user to confirm removal of item from ListView. If confirmed
     * the item is removed from both the ListView and the temp file.
     *
     * @param listView the list view
     */
    public void remove(ListView listView) {
        JobId selectedItem = (JobId) listView.getSelectionModel().getSelectedItem();
        String id = selectedItem.getJobId();
        // display dialog and wait for a button to be clicked
        Optional<ButtonType> result = RemoveItemDialog.newInstance(id).displayAndWait();
        // Exit if user cancelled
        if (!result.isPresent() || result.get() != ButtonType.YES) {
            return;
        }
        // remove line from temp file
        try {
            fileManager.remove(selectedItem.toString());
        } catch (IOException ex) {
            LOGGER.fatal("Unable to remove the selected item from data file.", ex);
            ErrMsgDialog.builder("Remove JobId", "Unable to remove the selected item from data file.").display();
            return;
        }
        // remove item from list
        int index = listView.getSelectionModel().getSelectedIndex();
        listView.getItems().remove(index);
    }

}
