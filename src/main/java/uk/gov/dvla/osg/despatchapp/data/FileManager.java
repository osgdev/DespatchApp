package uk.gov.dvla.osg.despatchapp.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import uk.gov.dvla.osg.despatchapp.main.AppConfig;
import uk.gov.dvla.osg.vault.enums.Site;

public class FileManager {
    
    final static Charset ENCODING = StandardCharsets.UTF_8;

    File datFile, eotFile, tempFile, reportFile;
    
    public FileManager(Site site) {
        AppConfig config = AppConfig.getInstance();
        
        switch (site) {
        case F:
            datFile = new File(config.getfDatFile());
            eotFile = new File(config.getfEotFile());
            tempFile = new File(config.getfTempFile());
            reportFile = new File(config.getfReportFile());
            break;
        case M:
            datFile = new File(config.getmDatFile());
            eotFile = new File(config.getmEotFile());
            tempFile = new File(config.getmTempFile());
            reportFile = new File(config.getmReportFile());
            break;
        }
    }
    
    public List<String> read() throws IOException {
        if (tempFile.exists()) {
            return FileUtils.readLines(tempFile, ENCODING);
        }
        return new ArrayList<String>();
    }
    
    public void append(String jid) throws IOException {
        FileUtils.writeStringToFile(tempFile, jid, ENCODING, true);
    }
    
    public void remove(String searchString) throws IOException {
        // Read all lines from file
        List<String> lines = FileUtils.readLines(tempFile, ENCODING);
        // Ignore search string while creating new list
        List<String> updatedLines = lines.stream().filter(s -> !s.equals(searchString)).collect(Collectors.toList());
        // Overwrite the file contents with the new list
        FileUtils.writeLines(tempFile, updatedLines, false);
    }
    
    public void sendDataFiles() {
        //TODO:
        // Create DAT file in temp folder
        // Create matching EOT file
        // Send both files to RPD via web client
    }
}
