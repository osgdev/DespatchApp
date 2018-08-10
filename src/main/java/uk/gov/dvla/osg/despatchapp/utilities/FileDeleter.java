package uk.gov.dvla.osg.despatchapp.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.rpd.web.config.Session;


public class FileDeleter {

	static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Files that are not sucessfully sent to RPD remain in the working directory.
	 * Files that are not manually moved or deleted by Dev team are automatically
	 * deleted after the given number of days.
	 * @param workingDir directory files are saved to before sending to RPD
	 * @param daysBack
	 */
	public static void deleteFilesOlderThanNdays(String workingDir, int daysBack) {

		int dayInMilis = 24 * 60 * 60 * 1000;

		long purgeTime = System.currentTimeMillis() - (daysBack * dayInMilis);

		Path directory = Paths.get(workingDir);
        boolean isDirectory = Files.isDirectory(directory);
        boolean isWritable = Files.isWritable(directory);
        
		if (!isDirectory) {
		    LOGGER.fatal("Directory [{}] does not exist or is not accessible. Please check config file.", workingDir);
		} else if (!isWritable) {
		    LOGGER.fatal("User {} does not have permission to delete from {}", Session.getInstance().getUserName(), workingDir);
		} else {
    		try {
    			Files.list(Paths.get(workingDir))
    			.filter(p -> p.toFile().lastModified() < purgeTime)
    			.forEach(p -> {
    				if (StringUtils.endsWithAny(p.toString().toUpperCase(), new String[] {"DAT", "EOT", "PDF"})) {
    					try {
    						Files.deleteIfExists(p);
    					} catch (IOException e) {
    						LOGGER.fatal("Unable to delte file:- {}" + p.getFileName());
    					}
    				}
    			});
    		} catch (IOException e) {
    			LOGGER.fatal(e.getMessage());
    		}
		}
	}
}
