package uk.gov.dvla.osg.despatchapp.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.views.LoginGui;
import uk.gov.dvla.osg.rpd.web.config.Session;

public class SubmitFileController {

    static final Logger LOGGER = LogManager.getLogger();

    private ObservableList<JobId> model;

    SubmitFileController(ObservableList<JobId> model) {
        this.model = model;
    }

    public boolean submit() {
        if (model.size() > 0) {
            LoginGui.newInstance().load();
            if (Session.getInstance().isLoggedIn()) {
                model.clear();
                return true;
            }
        }
        return false;
    }
}
