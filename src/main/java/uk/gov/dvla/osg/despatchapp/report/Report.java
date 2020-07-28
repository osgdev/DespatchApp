package uk.gov.dvla.osg.despatchapp.report;

import java.awt.Desktop;
import java.io.*;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;
import uk.gov.dvla.osg.despatchapp.views.ErrMsgDialog;
import uk.gov.dvla.osg.rpd.web.config.Session;

/**
 * Generate PDF report for stats team.
 *
 */
public class Report {

	/**
	 * Report contains a single header followed by a summary of all reprints. The reportContent
	 * Strings are taken from the ToString() method of each report type.
	 * @param reportContent
	 */
	public static void writePDFreport(List<String> reportContent, String fileName) {
		try {
			String fName = getFileName(fileName);
			validateFile(fName);
			writeFileContents(reportContent, fName);	
			display(fName);
		} catch (DocumentException e) {
		    ErrMsgDialog.show(e.getClass().getSimpleName(), e.getMessage());
		} catch (IOException e) {
		    ErrMsgDialog.show(e.getClass().getSimpleName(), "The report pdf is already open or unavailable!");
		}
	}

    /**
     * Write file contents.
     *
     * @param reportContent the report content
     * @param fName the f name
     * @throws DocumentException the document exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    private static void writeFileContents(List<String> reportContent, String fName) throws DocumentException, IOException, FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(fName)) {
            // pdf document object to write to file
            Document pdfDoc = new Document();
        	// pdf writer to write to the document
        	PdfWriter.getInstance(pdfDoc, fos);
        	// all text is appended to a single paragraph
        	Paragraph p = new Paragraph();
        	// generate timestamp
        	String timeStamp = DateUtils.timeStamp("dd/MM/yyyy @ HH:mm:ss");
        	// add the report headng
        	p.add("Despatch Report");
        	p.add("\n\nSubmitted on " + timeStamp + " by " + Session.getInstance().getUserName() + ":\n\n");
        	// add the report content
        	 for (String str : reportContent) {
        		p.add("\n     " + str);
        	}
        	// write the content to the pdf file
        	pdfDoc.open();
        	pdfDoc.add(p);
        	pdfDoc.close();
        }
    }

    /**
     * Show report.
     * @param fName the f name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void display(String fName) throws IOException {
        if (Desktop.isDesktopSupported()) {
        	File pdfFile = new File(fName);
        	Desktop.getDesktop().open(pdfFile);
        }
    }

    /**
     * Delete report file if it aready exists
     * @param fName the file name
     */
    private static void validateFile(String fName) {
        File checkFile = new File(fName);
        if (checkFile.exists()) {
        	checkFile.delete();
        }
    }

    /**
     * filename -> {workingDir}\{filePrefix}.{user}.{timestamp}.pdf
     * @param fileName the file name
     * @return the file name
     */
    private static String getFileName(String fileName) {
        String timeStamp = DateUtils.timeStamp("ddMMyyyy_HHmmss");
        return fileName + Session.getInstance().getUserName() + "." + timeStamp + ".pdf";
    }
}
