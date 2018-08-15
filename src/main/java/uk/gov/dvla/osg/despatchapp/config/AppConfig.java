package uk.gov.dvla.osg.despatchapp.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        // PropertyLoader loads the properties from the configuration file and validates
        // each entry. Temp files include the full path name, other files are stored together in a
        // single repo folder
        try {
            PropertyLoader loader = new PropertyLoader(filename);
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
        } catch (IOException ex) {
            LOGGER.fatal("Unable to load properties from {}", filename);
            System.exit(1);
        } catch (RuntimeException ex) {
            // Property value is missing from the file
            LOGGER.fatal("Unable to load properties from {}", filename);
            System.exit(1);
        }
    }

    public SiteConfig morriston() {
        return SiteConfig.builder()
                         .SiteName("Morriston")
                         .Repository(repoDir)
                         .RetentionPeriod(retentionPeriod)
                         .DatFile(mDatFile)
                         .EotFile(mEotFile)
                         .Report(mReportFile)
                         .TempFile(mTempFile)
                         .build();
    }

    public SiteConfig tyFelin() {
        return SiteConfig.builder()
                         .SiteName("Ty Felin")
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
                         .SiteName("BRP")
                         .Repository(repoDir)
                         .RetentionPeriod(retentionPeriod)
                         .DatFile(brpDatFile)
                         .EotFile(brpEotFile)
                         .Report(brpReportFile)
                         .TempFile(brpTempFile)
                         .build();
    }
}
