package uk.gov.dvla.osg.despatchapp.controllers;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import uk.gov.dvla.osg.despatchapp.config.SiteConfig;
import uk.gov.dvla.osg.despatchapp.config.SiteConfigFactory;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.models.JobId;
import uk.gov.dvla.osg.despatchapp.models.PrintSite;
import uk.gov.dvla.osg.despatchapp.utilities.FxUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;

public final class SiteListenerImplementation implements ChangeListener {
    
    static final Logger LOGGER = LogManager.getLogger();
    
    MainFormController controller;
    
    public SiteListenerImplementation(MainFormController controller) {
        this.controller = controller;
    }
    
    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        controller.cbSite.getSelectionModel().select((int) newValue);
        PrintSite chosenSite = (PrintSite) controller.cbSite.getSelectionModel().getSelectedItem();
        SiteConfig config = SiteConfigFactory.get(chosenSite);
        controller.lblSite.setText(chosenSite.toString());
        FxUtils.swapControls(controller.cbSite, controller.lblSite);
        controller.lvContent.setDisable(false);
        FxUtils.enableNode(controller.btnSubmit);
        controller.lblSite.requestFocus();
        FileManager fileManager = new FileManager(config);
        controller.fileManager = fileManager;
        controller.removeItemController = new RemoveItemController(fileManager);
        controller.submitFileController = new SubmitFileController(config, fileManager);
        
        if (!fileManager.userHasRepoAccess()) {
            LOGGER.error("Unable to write to the repository directory");
            String repoDir = config.repository();
            ErrMsgDialog.builder("Folder Permissions", "Please check you have read,write access to " + repoDir).display();
            Platform.exit();
        }
        
        try {
            fileManager.read().forEach(line -> controller.model.add(JobId.fromString(line)));
        } catch (IOException ex) {
            LOGGER.error("Unable to read from temp data file {}", ex.getMessage());
            String tempDir = new File(config.tempFile()).getParent();
            ErrMsgDialog.builder("Folder Permissions", "Please check you have read,write access to " + tempDir).display();
            Platform.exit();
        } catch (RuntimeException ex) {
            ErrMsgDialog.builder("Application Start", "Application is already in use at this site - " + config.site())
                        .action("Please close the open application before continuing.").display();
            Platform.exit();
        }
    }
}