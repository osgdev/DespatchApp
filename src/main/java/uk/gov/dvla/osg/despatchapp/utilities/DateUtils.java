package uk.gov.dvla.osg.despatchapp.utilities;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

public class DateUtils {

    public static String timeStamp(String format) {
        return DateFormatUtils.format(new Date(), format);
    }
}
