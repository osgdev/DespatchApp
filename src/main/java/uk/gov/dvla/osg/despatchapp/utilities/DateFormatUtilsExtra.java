package uk.gov.dvla.osg.despatchapp.utilities;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

public class DateFormatUtilsExtra extends DateFormatUtils {

    public static String timeStamp(String format) {
        return format(new Date(), format);
    }
}
