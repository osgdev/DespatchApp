package uk.gov.dvla.osg.despatchapp.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.models.PrintSite;

public class AppConfig {

    static final Logger LOGGER = LogManager.getLogger();

    /******************************************************************************************
     * SINGLETON PATTERN
     ******************************************************************************************/
    private static String filename;

    private static class SingletonHelper {
        private static final AppConfig INSTANCE = new AppConfig();
    }
    
    /**
     * Gets the single instance of NetworkConfig.
     *
     * @return single instance of NetworkConfig
     * @throws RuntimeException if the method is called before initialising with the
     *             network configuration file
     */
    public static AppConfig getInstance() throws RuntimeException {
        if (StringUtils.isBlank(filename)) {
            throw new RuntimeException("Application Configuration not initialised before use");
        }
        
        return SingletonHelper.INSTANCE;

    }

    /**
     * Initialises the NetworkConfig with the network configuration file.
     *
     * @param file the network configuration file
     * @throws RuntimeException if the configuration file does not exist or if
     *             NetworkConfig has already been initialised.
     */
    public static void init(String file) throws RuntimeException {
        if (!StringUtils.isBlank(filename)) {
            throw new RuntimeException("Application Configuration has already been initialised");
        }
        
        if (!new File(file).isFile()) {
            throw new RuntimeException("Application Configuration File " + filename + " does not exist on filepath.");
        }
        
        filename = file;
    }


    /*****************************************************************************************/

    private String repoDir;
    private String mDatFile;
    private String mEotFile;
    private String mTempFile;
    private String fDatFile;
    private String fEotFile;
    private String fTempFile;
    private String mReportFile;
    private String fReportFile;
    private int retentionPeriod;
    private String brpTempFile;
    private String brpDatFile;
    private String brpEotFile;
    private String brpReportFile;

    /**
     * Instantiates a new network config from the fields in the property file.
     */
    private AppConfig() {
       /* Temp files include the full path name, other files are stored 
        * together in a single repo folder.
        */
        PropertyLoader loader = null;
        
        try {
            loader = new PropertyLoader(filename);
        } catch (IOException ex) {
            // Unknown exception has occurred
            LOGGER.fatal("Unable to load properties from {}", filename);
            throw new RuntimeException(ex.getMessage());
        }
        
        try {
            // GENERAL PROPERTIES
            repoDir = loader.getProperty("repoDir");
            retentionPeriod = loader.getPropertyInt("retentionPeriod");
            // MORRISTON PROPERTIES
            mTempFile = loader.getProperty("mTempFile");
            mDatFile = repoDir + loader.getProperty("mDatFile");
            mEotFile = repoDir + loader.getProperty("mEotFile");
            mReportFile = repoDir + loader.getProperty("mReportFile");
            // TY FELIN PROPERTIES
            fTempFile = loader.getProperty("fTempFile");
            fDatFile = repoDir + loader.getProperty("fDatFile");
            fEotFile = repoDir + loader.getProperty("fEotFile");
            fReportFile = repoDir + loader.getProperty("fReportFile");
            // BRP PROPERTIES
            brpTempFile = loader.getProperty("brpTempFile");
            brpDatFile = repoDir + loader.getProperty("brpDatFile");
            brpEotFile = repoDir + loader.getProperty("brpEotFile");
            brpReportFile = repoDir + loader.getProperty("brpReportFile");
        } catch (RuntimeException ex) {
            // Property value is missing from the file
            throw ex;
        }
    }

    /**
     * Morriston site configuration.
     *
     * @return the site configuration for Morriston
     */
    public SiteConfig morriston() {
        return SiteConfig.builder()
                         .SiteName(PrintSite.MORRISTON)
                         .Repository(repoDir)
                         .RetentionPeriod(retentionPeriod)
                         .DatFile(mDatFile)
                         .EotFile(mEotFile)
                         .Report(mReportFile)
                         .TempFile(mTempFile)
                         .build();
    }

    /**
     * Ty Felin site configuration.
     *
     * @return the site configuration for Ty Felin
     */
    public SiteConfig tyFelin() {
        return SiteConfig.builder()
                         .SiteName(PrintSite.TYFELIN)
                         .Repository(repoDir)
                         .RetentionPeriod(retentionPeriod)
                         .DatFile(fDatFile)
                         .EotFile(fEotFile)
                         .Report(fReportFile)
                         .TempFile(fTempFile)
                         .build();
    }

    public SiteConfig brp() {
        return SiteConfig.builder()
                         .SiteName(PrintSite.MORRISTON)
                         .Repository(repoDir)
                         .RetentionPeriod(retentionPeriod)
                         .DatFile(brpDatFile)
                         .EotFile(brpEotFile)
                         .Report(brpReportFile)
                         .TempFile(brpTempFile)
                         .build();
    }
}
