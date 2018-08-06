package uk.gov.dvla.osg.despatchapp.controllers;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.views.RemoveItemDialog;

public class RemoveItemController {

    private ObservableList<JobId> model;

    public RemoveItemController(ObservableList<JobId> model) {
        this.model = model;
    }
    
    public void remove(ListView lbContent) {
        JobId selectedItem = (JobId) lbContent.getSelectionModel().getSelectedItem();
        String id = selectedItem.getJobId();
        // display dialog and wait for a button to be clicked
        Optional<ButtonType> result = RemoveItemDialog.newInstance(id).display();
        // logout if user clicks the OK button
        if (result.isPresent() && result.get() == ButtonType.YES) {
            int index = lbContent.getSelectionModel().getSelectedIndex();
            if (index != -1) {
                model.remove(index);
            }
        }
    }
}
