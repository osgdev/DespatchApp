package uk.gov.dvla.osg.despatchapp.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.config.SiteConfig;
import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.client.SubmitJobClient;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class FileManager {

    static final Logger LOGGER = LogManager.getLogger();

    final static Charset ENCODING = StandardCharsets.UTF_8;
    final static String NEWLINE = "\n"; // Ensures NewLine characters are Unix compatible

    private File datFile, eotFile, tempFile;

    /**
     * Instantiates a new file manager.
     *
     * @param config the config for the selected site
     */
    public FileManager(SiteConfig config) {
        LOGGER.debug("Loding File Manager...");
        tempFile = new File(config.tempFile());

        String timeStamp = DateUtils.timeStamp("ddMMyyyy_HHmmss");
        datFile = new File(config.datFile() + timeStamp + ".DAT");
        eotFile = new File(config.eotFile() + timeStamp + ".EOT");
    }

    /**
     * Reads data from the temp file when the site is chosen.
     *
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RuntimeException Application is in use by another user
     */
    public List<String> read() throws IOException, RuntimeException {
        LOGGER.debug("Looking for Temp file [{}]", tempFile.getAbsolutePath());
        // Create new file if it does not already exist on the file system
        if (!tempFile.exists()) {
            LOGGER.debug("Temp file [{}] does not exist. Creating new file.", tempFile.getAbsolutePath());
            // Create file
            FileUtils.touch(tempFile);
            // Apply lock on file
            lockTempFile();
            // Return empty list
            return new ArrayList<String>();
        }
        LOGGER.debug("Checking Temp file is writable");
        // Check if another user has the application open
        if (!tempFile.canWrite()) {
            throw new RuntimeException("File already in use");
        }
        LOGGER.debug("Reading from Temp file...");
        // Read file contents
        List<String> lines = null;
        try {
            lines = FileUtils.readLines(tempFile, ENCODING);
        } catch (IOException ex) {
            LOGGER.error("Reading from temp file failed: {}", ex.getMessage());
            ErrMsgDialog.show("File read error", "Unable to read input file");
        }
        LOGGER.debug("Temp file read");
        // Apply lock on file
        lockTempFile();

        return lines;

    }

    /**
     * Each Job ID is written to the temp file to keep it synchronised with the
     * ListView and to persist data in case of PC failure.
     * 
     * @param jid the jid
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void append(String jid) throws IOException {
        LOGGER.debug("Unlocking Temp File...");
        // Temporarily unlock the file
        unlockTempFile();
        // Save JID to file
        LOGGER.debug("Appending JID to file...");
        FileUtils.writeStringToFile(tempFile, jid + NEWLINE, ENCODING, true);
        // Re-apply lock on file
        LOGGER.debug("Locking Temp file...");
        lockTempFile();
    }

    /**
     * Removes the search string line from the file.
     *
     * @param searchString the search string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void remove(String searchString) throws IOException {
        // Temporarily unlock the file
        unlockTempFile();
        // Read all lines from file
        List<String> lines = FileUtils.readLines(tempFile, ENCODING);
        // Ignore search string while creating new list
        List<String> updatedLines = lines.stream().filter(s -> !s.equals(searchString)).collect(Collectors.toList());
        // Overwrite the file contents with the new list
        FileUtils.writeLines(tempFile, updatedLines, false);
        // Re-apply lock on file
        lockTempFile();
    }

    /**
     * Creates DAT and EOT files and then sends these over to the RPD hotfolder.
     *
     * @param list the list of Job IDs
     */
    public boolean trySendToRpd(List<String> list) {
        // Create DAT file in temp folder
        LOGGER.info("Writing to DAT file {}", datFile.getAbsolutePath());

        try {
            FileUtils.writeLines(datFile, list, false);
        } catch (IOException ex) {
            LOGGER.error("Unable to save DAT file {}, {}", datFile.getAbsolutePath(), ex.getMessage());
            ErrMsgDialog.show("Save file error", "Unable to save DAT file.");
            return false;
        }

        LOGGER.info("DAT file written.");

        LOGGER.info("Sending DAT file to RPD");
        // Send DAT files to RPD via web client
        if (!sendToRpd(datFile)) {
            LOGGER.info("Unable to send DAT file");
            return false;
        }

        LOGGER.info("DAT file transmitted");

        // Create matching EOT file
        String runDate = DateUtils.timeStamp("ddMMyyyy");

        List<String> eotContent = Arrays.asList("RUNVOL=" + list.size(), "USER=" + Session.getInstance().getUserName(), "RUNDATE="
                + runDate);

        LOGGER.info("Writing data to EOT file {}", eotFile.getAbsolutePath());

        try {
            FileUtils.writeLines(eotFile, eotContent, false);
        } catch (IOException ex) {
            LOGGER.info("Unable to save EOT file", ex);
            ErrMsgDialog.show("File save error", "Unable to save EOT file");
            return false;
        }

        LOGGER.info("EOT file written to. Sending EOT to RPD.");

        // Send EOT files to RPD via web client
        if (!sendToRpd(eotFile)) {
            return false;
        }

        LOGGER.info("EOT file transmitted. Cleaning up temp files.");

        // Remove and rebuild the temp file to clear its contents
        try {
            FileUtils.deleteQuietly(tempFile);
            // Create file
            FileUtils.touch(tempFile);
            // Apply lock on file
            lockTempFile();
        } catch (IOException ex) {
            LOGGER.error("Unable to delete contents of the site temp file [{}], {}", tempFile, ex.getMessage());
            ErrMsgDialog.show("Send Data Files", "Unable to clear the site Temp file");
            return false;
        }

        LOGGER.info("File cleanup complete");

        return true;
    }

    /**
     * Send to rpd.
     *
     * @param file the data file
     * @return true, if successful
     */
    private boolean sendToRpd(File file) {
        SubmitJobClient sjc = SubmitJobClient.getInstance();

        if (sjc.trySubmit(file)) {
            return true;
        }
    
        RpdErrorResponse rpdError = sjc.getErrorResponse();
        LOGGER.error(rpdError.toString());
        ErrMsgDialog.show(rpdError.getCode(), rpdError.getMessage(), rpdError.getAction());
        return false;
    }

    /**
     * Sets the ReadOnly flag to lock the file.
     */
    private void lockTempFile() {
        LOGGER.debug("Setting lock on tmp file");
        tempFile.setReadOnly();
        LOGGER.debug("Lock set");
    }

    /**
     * Unlocks the temp file by removing the ReadOnly flag
     */
    public void unlockTempFile() {
        LOGGER.debug("Removing lock on tmp file");
        tempFile.setWritable(true);
        LOGGER.debug("Lock removed");
    }

    /**
     * Gets the temp file directory.
     *
     * @return the temp file directory
     */
    public String getTempFileDirectory() {
        return FilenameUtils.getFullPath(tempFile.getAbsolutePath());
    }

    /**
     * A new user may be denied access to the QA repository folder. The only way to
     * check that they can write to the repo folder is by attempting to write to the
     * folder and catching any exception that occurs.
     * 
     * Checking isReadOnly() on the file checks only the flag on the file and cannot
     * check permissions on the directory.
     *
     * @return true, if successful
     */
    public boolean userHasRepoAccess() {
        String repo = FilenameUtils.getFullPath(datFile.getAbsolutePath());
        LOGGER.info("Checking user has repo access to {}", repo);
        String filename = DateUtils.timeStamp("ddMMyyHHmmss") + ".tmp";
        File testFile = new File(repo, filename);
        LOGGER.info("Test File is {}", testFile.toString());

        try {
            LOGGER.info("Writing to repository {}", testFile.getAbsolutePath());
            FileUtils.writeStringToFile(testFile, "", ENCODING);
            LOGGER.info("Deleting test file");
            FileUtils.deleteQuietly(testFile);
        } catch (IOException ex) {
            LOGGER.info("Unable to write to repository directory");
            return false;
        }

        LOGGER.info("User has access to repo...");
        return true;
    }
}
