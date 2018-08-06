package uk.gov.dvla.osg.despatchapp.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    
    /**
     * Gets the time stamp in the specified format.
     * @param format the format of the time stamp
     * @return the time stamp
     */
    public static String getTimeStamp(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
