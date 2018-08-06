package uk.gov.dvla.osg.despatchapp.controllers;

import javafx.collections.ObservableList;
import uk.gov.dvla.osg.despatchapp.models.JobId;

class SubmitFileController {

    private ObservableList<JobId> model;

    SubmitFileController(ObservableList<JobId> model) {
        this.model = model;
    }
    
    boolean submit() {
        if (model.size() > 0) {
            model.clear();
            return true;
        }
        return false;
    }
}
