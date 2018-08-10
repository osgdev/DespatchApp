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

import uk.gov.dvla.osg.despatchapp.main.AppConfig;
import uk.gov.dvla.osg.despatchapp.utilities.DateFormatUtilsExtra;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.client.SubmitJobClient;
import uk.gov.dvla.osg.rpd.web.config.Session;
import uk.gov.dvla.osg.rpd.web.error.RpdErrorResponse;

public class FileManager {
    
    final static Charset ENCODING = StandardCharsets.UTF_8;

    File datFile, eotFile, tempFile;
    String site;
    
    public FileManager(String site) {
        this.site = site.toString().toLowerCase();
        
        AppConfig config = AppConfig.getInstance();
        String timeStamp = DateFormatUtilsExtra.timeStamp("ddMMyyyy_HHmmss");
        
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
        if (tempFile.exists()) {
            return FileUtils.readLines(tempFile, ENCODING);
        }
        return new ArrayList<String>();
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
    public boolean sendDataFiles(List<String> list) {
        // Create DAT file in temp folder
        try {
            FileUtils.writeLines(datFile, list, false);
        } catch (IOException ex) {
            ErrMsgDialog.builder("Unable to save DAT file.", ex.getMessage()).display();
        }
        // Send DAT files to RPD via web client
        if (!sendToRpd(datFile)) {
            return false;
        }
        // Create matching EOT file
        List<String> eotContent = Arrays.asList("RUNVOL=" + list.size(),"LOCATION=" + site, "USER=" + Session.getInstance().getUserName());
        try {
            FileUtils.writeLines(eotFile, eotContent, false);
        } catch (IOException ex) {
            ErrMsgDialog.builder("Unable to save EOT file", ex.getMessage()).display();
        }
        // Send EOT files to RPD via web client
        if (!sendToRpd(eotFile)) {
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
    private boolean sendToRpd(File file) {
        SubmitJobClient sjc = SubmitJobClient.getInstance();
        boolean success = sjc.submit(file);
        if (!success) {
            RpdErrorResponse rpdError = sjc.getErrorResponse();
            ErrMsgDialog.builder(rpdError.getCode(), rpdError.getMessage()).action(rpdError.getAction()).display();
        }
        return success;
    }
}
