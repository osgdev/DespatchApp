package uk.gov.dvla.osg.despatchapp.models;

public class JobId {
    
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
    public static JobId newInstance(String jId, String timeStamp) {
        return new JobId(jId, timeStamp);
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

    /* Objects are equal if they have the same Job ID, ignoring the timestamp.
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