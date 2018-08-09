package uk.gov.dvla.osg.despatchapp.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Class NetworkConfig holds the RPD Rest API URL's.
 * It is loaded from a network configuration properties file which is stored
 * in the local file system. 
 */
public class AppConfig {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    /******************************************************************************************
    *              SINGLETON PATTERN
    ******************************************************************************************/
   private static String filename;

   private static class SingletonHelper {
       private static final AppConfig INSTANCE = new AppConfig();
   }


   /**
    * Gets the single instance of NetworkConfig.
    *
    * @return single instance of NetworkConfig
    * @throws RuntimeException if the method is called before initialising with the network configuration file
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
    * @throws RuntimeException if the configuration file does not exist or if NetworkConfig has already been initialised.
    */
   public static void init(String file) throws RuntimeException {
       if (StringUtils.isBlank(filename)) {
           if (new File(file).isFile()) {
               filename = file;
           } else {
               throw new RuntimeException("Application Configuration File " + filename + " does not exist on filepath.");
           }
       } else {
           throw new RuntimeException("Application Configuration has already been initialised");
       }
   }
   /*****************************************************************************************/

    private String mDatFile;
    private String mEotFile;
    private String mTempFile;
    private String fDatFile;
    private String fEotFile;
    private String fTempFile;
    private String mReportFile;
    private String fReportFile;
    
    /**
     * Instantiates a new network config from the fields in the property file.
     */
    private AppConfig() {
        // PropertyLoader loads the properties from the configuration file and validates each entry
        try {
            PropertyLoader loader = new PropertyLoader(filename);
            mDatFile = loader.getProperty("mDatFile");
            mEotFile = loader.getProperty("mEotFile");
            mTempFile = loader.getProperty("mTempFile");
            mReportFile = loader.getProperty("mReportFile");
            fDatFile = loader.getProperty("fDatFile");
            fEotFile = loader.getProperty("fEotFile");
            fTempFile = loader.getProperty("fTempFile");
            fReportFile = loader.getProperty("fReportFile");
        } catch (IOException ex) {
            LOGGER.fatal("Unable to load properties from {}", filename);
            System.exit(1);
        } catch (RuntimeException ex) {
            // Property value is missing from the file
            LOGGER.fatal("Unable to load properties from {}", filename);
            LOGGER.fatal(ex.getMessage());
            System.exit(1);
        }
    }

    public String getmDatFile() {
        return mDatFile;
    }

    public String getmEotFile() {
        return mEotFile;
    }

    public String getmTempFile() {
        return mTempFile;
    }

    public String getfDatFile() {
        return fDatFile;
    }

    public String getfEotFile() {
        return fEotFile;
    }

    public String getfTempFile() {
        return fTempFile;
    }

    public String getfReportFile() {
        return fReportFile;
    }

    public String getmReportFile() {
        return mReportFile;
    }
    
}
