package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.IOException;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.despatchapp.views.RemoveItemDialog;

public class RemoveItemController {

    private FileManager fileManager;
    
    public RemoveItemController(FileManager manager) {
        this.fileManager = manager;
    }
    
    public void remove(ListView listView) {
        JobId selectedItem = (JobId) listView.getSelectionModel().getSelectedItem();
        String id = selectedItem.getJobId();
        // display dialog and wait for a button to be clicked
        Optional<ButtonType> result = RemoveItemDialog.newInstance(id).display();
        // logout if user clicks the OK button
        if (result.isPresent() && result.get() == ButtonType.YES) {
            int index = listView.getSelectionModel().getSelectedIndex();
            listView.getItems().remove(index);
            //model.remove(index);
            try {
                fileManager.remove(selectedItem.toString());
            } catch (IOException ex) {
                ErrMsgDialog.builder("Unable to remove the selected item from data file.", ex.getMessage()).display();
            }
        }
    }

}
