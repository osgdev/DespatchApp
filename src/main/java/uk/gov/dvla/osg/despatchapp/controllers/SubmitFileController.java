package uk.gov.dvla.osg.despatchapp.controllers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.data.FileManager;
import uk.gov.dvla.osg.despatchapp.main.AppConfig;
import uk.gov.dvla.osg.despatchapp.report.Report;
import uk.gov.dvla.osg.despatchapp.views.LoginGui;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.vault.enums.Site;

public class SubmitFileController {

    static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG_MODE = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    
    @SuppressWarnings("unused")
    private FileManager manager;
    private String reportFile;

    public SubmitFileController(Site site, FileManager fileManager) {
        this.manager = fileManager;
        AppConfig config = AppConfig.getInstance();
        switch (site) {
        case F:
            reportFile = config.getfReportFile();
            break;
        case M:
            reportFile = config.getmReportFile();
            break;
        }
    }
    
    public boolean submit(List<String> list) {
        LoginGui.newInstance().load();
        if (Session.getInstance().isLoggedIn() || DEBUG_MODE) {
            //manager.sendDataFiles();
            Report.writePDFreport(list, reportFile);
            return true;
        }
        return false;
    }
    
}
