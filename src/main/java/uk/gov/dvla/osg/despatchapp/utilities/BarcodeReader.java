package uk.gov.dvla.osg.despatchapp.utilities;

import java.time.Instant;

import javafx.scene.input.KeyEvent;

public class BarcodeReader {

    private static final long THRESHOLD = 100;
    private static final int MIN_BARCODE_LENGTH = 10;

    private final StringBuffer barcode = new StringBuffer();
    private long lastEventTimeStamp = 0L;

    /**
     * Handles input and ensures it came from the Barcode Reader. Method is run sequentially for
     * every character read by the barcode reader, until the enter key is pressed. If keyboard 
     * input is used, the gap between key-presses will be larger than the THRESHOLD, so the
     * StringBuffer is cleared. Additionally, if 'Enter' is received as an input before the 
     * minimum barcode length is reached, then input is discarded.
     * @param event the event containing the input character
     * @return true, when a full barcode has been scanned, false for every character prior to the 'Enter' key,
     * or when keyboard has been used as input.
     */
    public boolean handle(KeyEvent event) {
        long now = Instant.now().toEpochMilli();
        long elapsedTime = now - lastEventTimeStamp;
        // events must come fast enough to separate from manual input
        if (elapsedTime > THRESHOLD) {
            barcode.delete(0, barcode.length());
        }
        lastEventTimeStamp = now;

        // ENTER comes as 0x000d
        if (event.getCharacter().charAt(0) == (char) 0x000d) {
            if (barcode.length() >= MIN_BARCODE_LENGTH) {
                return true;
            }
            barcode.delete(0, barcode.length());
        } else {
            barcode.append(event.getCharacter());
        }
        event.consume();
        return false;
    }
    
    /**
     * Call when the handle event returns true, to retrieve the scanned barcode.
     * @return the barcode
     */
    public String getBarcode() {
        return barcode.toString();
    }
}
