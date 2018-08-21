package uk.gov.dvla.osg.despatchapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class PropertyLoader loads the properties from the configuration file.
 */
public class PropertyLoader {
    
    static final Logger LOGGER = LogManager.getLogger();
    private static final Properties properties = new Properties();
    private String filename;
    
    /**
     * PropertyLoader loads the properties from the configuration file and validates
       each entry.
     *
     * @param filename the configuration file holding the properties.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PropertyLoader(String filename) throws IOException {
        this.filename = filename;
        try (FileInputStream input = new FileInputStream(new File(filename))) {
            properties.load(input);
        }
    }
    
    /**
     * Gets the string property matching the provided key.
     *
     * @param key the key to match
     * @return the property for the key
     * @throws RuntimeException if the key is not present in the configuration file
     */
    public String getProperty(String key) throws RuntimeException {
        if (!properties.containsKey(key)) {
            throw new RuntimeException(MessageFormat.format("Unable to load property [{0}] from file [{1}].", key, filename)) ;
        }
        
        return properties.getProperty(key);
    }
    
    /**
     * Gets the int property matching the provided key.
     *
     * @param key the key to match
     * @return the property for the key
     * @throws RuntimeException if the key is not present in the configuration file or is not a valid integer
     */
    public int getPropertyInt(String key) throws RuntimeException {
        if (!properties.containsKey(key)) {
            throw new RuntimeException(MessageFormat.format("Unable to load property [{0}] from file [{1}]", key, filename));
        }
        
        String value = properties.getProperty(key);
        if (!StringUtils.isNumeric(value)) {
            throw new RuntimeException(MessageFormat.format("Value [{0}] is not valid for the property [{1}] in file [{2}]", value, key, filename));
        }
        
        return Integer.parseInt(value);
    }
}
