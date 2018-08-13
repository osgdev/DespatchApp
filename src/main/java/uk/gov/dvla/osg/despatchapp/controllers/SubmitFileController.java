package uk.gov.dvla.osg.despatchapp.controllers;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.main.AppConfig;
import uk.gov.dvla.osg.despatchapp.report.Report;
import uk.gov.dvla.osg.despatchapp.utilities.FileDeleter;
import uk.gov.dvla.osg.despatchapp.views.LoginGui;
import uk.gov.dvla.osg.rpd.web.config.Session;

public class SubmitFileController {

    static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    
    private FileManager manager;
    private String reportFile;
    private AppConfig appConfig = AppConfig.getInstance();
    
    public SubmitFileController(String site, FileManager fileManager) {
        this.manager = fileManager;

        switch (site) {
        case "TY FELIN":
            reportFile = appConfig.getfReportFile();
            break;
        case "MORRISTON":
            reportFile = appConfig.getmReportFile();
            break;
        case "BRP":
            reportFile = appConfig.getBrpDatFile();
            break;
        }
    }
    
    public boolean submit(List<String> list) {
        if (!DEBUG_MODE) {
            // Prompt user to log in to RPD
            LoginGui.newInstance().load();
            // Did user log in successfully?
            if (!Session.getInstance().isLoggedIn()) {
                return false;
            }
            // Send files and check that they sent successfully
            if (!manager.trySendFiles(list)) {
                return false;
            }
        }
        // Write report and display to screen
        Report.writePDFreport(list, reportFile);
        // Delete report files older than retention period
        FileDeleter.deleteFilesOlderThanNdays(appConfig.getRepoDirectory(), appConfig.getRetentionPeriod());
        
        return true;
    }
    
}
