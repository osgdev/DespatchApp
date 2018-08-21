package uk.gov.dvla.osg.despatchapp.controllers;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.config.SiteConfig;
import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.report.Report;
import uk.gov.dvla.osg.despatchapp.utilities.FileDeleter;
import uk.gov.dvla.osg.rpd.web.config.Session;

public class SubmitFileController {

    static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    
    private FileManager manager;
    private String reportFile;
    private String repository;
    private int retentionPeriod;
    
    public SubmitFileController(SiteConfig config, FileManager fileManager) {
        this.manager = fileManager;
        reportFile = config.report();
        repository = config.repository();
        retentionPeriod = config.retentionPeriod();
    }
    
    /**
     * Send DAT & EOT files to the RPD hot folder. A PDF report of the submitted data will be 
     * displayed. DAT, EOT & PDF files older than the retention period will be removed.
     *
     * @param list the list of Job ID's (without a timestamp)
     * @return true, if successfully sent to RPD
     */
    public boolean trySubmit(List<String> list) {
        if (!DEBUG_MODE) {
            // Did user log in successfully?
            if (!Session.getInstance().isLoggedIn()) {
                return false;
            }
            // Send files and check that they sent successfully
            if (!manager.trySendToRpd(list)) {
                return false;
            }
        }

        // Write report and display to screen
        Report.writePDFreport(list, reportFile);
        // Delete report files older than retention period
        FileDeleter.deleteFilesOlderThanNdays(repository, retentionPeriod);
        
        return true;
    }
    
}
