package uk.gov.dvla.osg.despatchapp.models;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import uk.gov.dvla.osg.despatchapp.utilities.DateUtils;

public class JobId {
    
    private static final int JID_LENGTH = 10;
    
    // Ten Digit RPD Job ID
    private String jId;
    // Time that the barcode was scanned, format is DD/MM/YY HH:MM:SS
    private String timeStamp;

    /**
     * New instance.
     * @param jId the job id
     * @param timeStamp the time stamp
     * @return the JobId
     */
    public static JobId newInstance(String jId) {
        if (!isValid(jId)) {
            throw new IllegalArgumentException(MessageFormat.format("Job ID is not valid - [{0}]", jId));
        }
        String timeStamp = DateUtils.timeStamp("dd/MM/yy HH:mm:ss");
        return new JobId(jId, timeStamp);
    }

    /**
     * New instance of a JobId parsed from an input string.
     * @param input the input
     * @return the job id
     */
    public static JobId fromString(String input) {
        List<String> array = Arrays.asList(input.split("\t"));
        String jid = array.get(0);
        String time = array.get(1);
        return new JobId(jid, time);
    }
    
    /**
     * Instantiates a new job id.
     * @param jId the job id
     * @param timeStamp the time stamp
     */
    private JobId(String jId, String timeStamp) {
        this.jId = jId;
        this.timeStamp = timeStamp;
    }

    public String getJobId() {
        return jId;
    }
    
    /**
     * Validates if the entered barcode is a valid ten-digit RPD job id.
     * 
     * @param barcode the barcode to validate
     * @return true, if barcode is a valid ten-digit Job ID
     */
    public static boolean isValid(String barcode) {
        return StringUtils.isNumeric(barcode) && barcode.length() == JID_LENGTH;
    }
    
    /* 
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.join("\t", jId, timeStamp);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jId == null) ? 0 : jId.hashCode());
        return result;
    }

    /* 
     * Objects are equal if they have the same Job ID, ignoring the timestamp.
     * Used to avoid duplicate ID's being created.
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobId other = (JobId) obj;
        if (jId == null) {
            if (other.jId != null) {
                return false;
            }
        } else if (!jId.equals(other.jId)) {
            return false;
        }
        return true;
    }

}
