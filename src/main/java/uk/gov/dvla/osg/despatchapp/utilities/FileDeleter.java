package uk.gov.dvla.osg.despatchapp.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.rpd.web.config.Session;

public class FileDeleter {

    static final Logger LOGGER = LogManager.getLogger();

    private static final String[] EXTENSIONS = new String[] { "DAT", "EOT", "PDF" };
    private static final int DAY_IN_MILIS = 24 * 60 * 60 * 1000;

    /**
     * Files that are not sucessfully sent to RPD remain in the working directory.
     * Files that are not manually moved or deleted by Dev team are automatically
     * deleted after the given number of days.
     * 
     * @param workingDir directory that files are saved to before sending to RPD
     * @param daysBack file retention period in days
     */
    public static void deleteFilesOlderThanNdays(String workingDir, int daysBack) {
        Path directory = Paths.get(workingDir);

        if (!Files.isDirectory(directory)) {
            LOGGER.fatal("Directory [{}] does not exist or is not accessible. Please check config file.", workingDir);
            return;
        }
        if (!Files.isWritable(directory)) {
            LOGGER.fatal("User [{}] does not have permission to delete from {}", Session.getInstance().getUserName(), workingDir);
            return;
        }
        
        long purgeTime = System.currentTimeMillis() - ((long)daysBack * DAY_IN_MILIS);
                
        try {
            Files.list(Paths.get(workingDir)).filter(p -> getFileCreationDate(p) < purgeTime)
              .forEach(p -> {
                if (StringUtils.endsWithAny(p.toString().toUpperCase(), EXTENSIONS)) {
                    LOGGER.trace("Deleteing file from repository after {} days: {}", daysBack, p.toString());
                    deleteFile(p);
                }
            });
        } catch (IOException e) {
            LOGGER.fatal("Error deleting file.", e);
        }
    }

    /**
     * Gets the file creation date.
     *
     * @param p the file
     * @return the file creation date if successful, else MAX_VALUE
     */
    private static long getFileCreationDate(Path p) {
        try {
            return Files.readAttributes(p, BasicFileAttributes.class).creationTime().toMillis();
        } catch (Exception ex) {
           return Long.MAX_VALUE;
        }
    }
    
    /**
     * Try to delete file and log filename if unsuccessful.
     * 
     * @param p the file to delete
     */
    private static void deleteFile(Path p) {
        if (!FileUtils.deleteQuietly(p.toFile())) {
            LOGGER.fatal("Unable to delete file:- {}" + p.getFileName());
        }
    }

}
