package uk.gov.dvla.osg.despatchapp.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.despatchapp.main.AppConfig;
import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.client.SubmitJobClient;
import uk.gov.dvla.osg.rpd.web.config.Session;

public class FileManager {
    
    static final Logger LOGGER = LogManager.getLogger();
    
    final static Charset ENCODING = StandardCharsets.UTF_8;

    File datFile, eotFile, tempFile;
    String site;
    
    public FileManager(String site) {
        this.site = site.toString().toLowerCase();
        
        AppConfig config = AppConfig.getInstance();
        String timeStamp = DateUtils.timeStamp("ddMMyyyy_HHmmss");
        
        switch (site) {
        case "TY FELIN":
            datFile = new File(config.getfDatFile() + timeStamp + ".DAT");
            eotFile = new File(config.getfEotFile() + timeStamp + ".EOT");
            tempFile = new File(config.getfTempFile());
            break;
        case "MORRISTON":
            datFile = new File(config.getmDatFile() + timeStamp + ".DAT");
            eotFile = new File(config.getmEotFile() + timeStamp + ".EOT");
            tempFile = new File(config.getmTempFile());
            break;
        case "BRP":
            datFile = new File(config.getBrpDatFile() + timeStamp + ".DAT");
            eotFile = new File(config.getBrpEotFile() + timeStamp + ".EOT");
            tempFile = new File(config.getBrpTempFile());
            break;
        }
    }
    
    /**
     * Reads data from the temp file when the site is chosen.
     *
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<String> read() throws IOException {
        if (!tempFile.exists()) {
            return new ArrayList<String>();
        }
        
        return FileUtils.readLines(tempFile, ENCODING);
    }
    
    /**
     * Each Job ID is written to the temp file to keep it synchronised with the
     * ListView and to persist data in case of PC failure.
     * 
     * @param jid the jid
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void append(String jid) throws IOException {
        FileUtils.writeStringToFile(tempFile, jid, ENCODING, true);
    }
    
    /**
     * Removes the search string line from the file.
     *
     * @param searchString the search string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void remove(String searchString) throws IOException {
        // Read all lines from file
        List<String> lines = FileUtils.readLines(tempFile, ENCODING);
        // Ignore search string while creating new list
        List<String> updatedLines = lines.stream().filter(s -> !s.equals(searchString)).collect(Collectors.toList());
        // Overwrite the file contents with the new list
        FileUtils.writeLines(tempFile, updatedLines, false);
    }
    
    /**
     * Creates DAT and EOT files and then sends these over to the RPD hotfolder.
     *
     * @param list the list of Job IDs
     */
    public boolean trySendFiles(List<String> list) {
        // Create DAT file in temp folder
        try {
            FileUtils.writeLines(datFile, list, false);
        } catch (IOException ex) {
            LOGGER.fatal("Unable to save DAT file.", ex);
            ErrMsgDialog.builder("File save error.", "Unable to save DAT file")
                .action(MessageFormat.format("Please check you have write access to {}",  AppConfig.getInstance().getRepoDirectory()))
                .display();
            return false;
        }
        // Send DAT files to RPD via web client
        if (!trySendToRpd(datFile)) {
            return false;
        }
        // Create matching EOT file
        List<String> eotContent = Arrays.asList("RUNVOL=" + list.size(),"USER=" + Session.getInstance().getUserName());
        // Write EOT file to temp directory
        try {
            FileUtils.writeLines(eotFile, eotContent, false);
        } catch (IOException ex) {
            LOGGER.fatal("Unable to save EOT file.", ex);
            ErrMsgDialog.builder("File save error.", "Unable to save EOT file").display();
            return false;
        }
        // Send EOT files to RPD via web client
        if (!trySendToRpd(eotFile)) {
            return false;
        }
        // Remove the temp file
        return FileUtils.deleteQuietly(tempFile);
    }

    /**
     * Send to rpd.
     *
     * @param file the file
     * @return true, if successful
     */
    private boolean trySendToRpd(File file) {
        SubmitJobClient sjc = SubmitJobClient.getInstance();
        boolean success = sjc.trySubmit(file);
        if (!success) {
            LOGGER.fatal(sjc.getErrorResponse().toString());
            ErrMsgDialog.builder("File submission error", "Your data was not sent to RPD. Please try again.").display();
        }
        return success;
    }
}
