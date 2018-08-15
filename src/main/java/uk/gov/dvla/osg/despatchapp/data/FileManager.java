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
    final static String NEWLINE = "\n";

    File datFile, eotFile, tempFile;
    String site;

    public FileManager(SiteConfig config) {
        tempFile = new File(config.tempFile());
        site = config.site();

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
        // Create new file if it does not already exist on the file system
        if (!tempFile.exists()) {
            // Create file
            FileUtils.touch(tempFile);
            // Apply lock on file
            lockTempFile();
            // Return empty list
            return new ArrayList<String>();
        }

        // Check if another user has the application open
        if (!tempFile.canWrite()) {
            throw new RuntimeException();
        }

        // Read file contents
        List<String> lines = FileUtils.readLines(tempFile, ENCODING);
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
        // Temporarily unlock the file
        unlockTempFile();
        // Save JID to file
        FileUtils.writeStringToFile(tempFile, jid + NEWLINE, ENCODING, true);
        // Re-apply lock on file
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
        try {
            FileUtils.writeLines(datFile, list, false);
        } catch (IOException ex) {
            LOGGER.error("Unable to save DAT file {}, {}", datFile, ex);
            ErrMsgDialog.builder("Save file error", "Unable to save DAT file.").display();
            return false;
        }
        // Send DAT files to RPD via web client
        if (!sendToRpd(datFile)) {
            return false;
        }
        
        // Create matching EOT file
        List<String> eotContent = Arrays.asList("RUNVOL=" + list.size(), "USER=" + Session.getInstance().getUserName());
        
        try {
            FileUtils.writeLines(eotFile, eotContent, false);
        } catch (IOException ex) {
            LOGGER.debug("Unable to save EOT file", ex);
            ErrMsgDialog.builder("File save error", "Unable to save EOT file").display();
            return false;
        }
        // Send EOT files to RPD via web client
        if (!sendToRpd(eotFile)) {
            return false;
        }

        try {
            // Temporarily unlock the file
            unlockTempFile();
            // Remove contents from the temp file
            FileUtils.writeStringToFile(tempFile, "", ENCODING);
            // Re-apply lock on file
            lockTempFile();
        } catch (IOException ex) {
            LOGGER.error("Unable to delete contents of the site temp file {}", tempFile);
            ErrMsgDialog.builder("Send Data Files", "Unable to clear the site Temp file").display();
            return false;
        }

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
        if (!sjc.trySubmit(file)) {
            RpdErrorResponse rpdError = sjc.getErrorResponse();
            LOGGER.error(sjc.toString());
            ErrMsgDialog.builder(rpdError.getCode(), rpdError.getMessage()).action(rpdError.getAction()).display();
            return false;
        }
        return true;
    }

    private void lockTempFile() {
        tempFile.setReadOnly();
    }

    public void unlockTempFile() {
        tempFile.setWritable(true);
    }

    public boolean hasRepoAccess() {
        String repo = datFile.getParent() + "\\";
        String timeStamp = DateUtils.timeStamp("ddMMyyHHmmss");
        File testFile = new File(repo + timeStamp + ".tmp");
        
        try {
            FileUtils.writeStringToFile(testFile, "", ENCODING);
            FileUtils.deleteQuietly(testFile);
        } catch (IOException ex) {
            LOGGER.debug("Unable to write to repository directory");
            return false;
        }
        
        return true;
    }
}
