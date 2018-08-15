package uk.gov.dvla.osg.despatchapp.config;

public class SiteConfig {

    private String tempFile;
    private String eotFile;
    private String datFile;
    private String report;
    private String repository;
    private int retentionPeriod;
    private String site;


    private SiteConfig(Builder builder) {
        this.site = builder.innerSite;
        this.tempFile = builder.innerTempFile;
        this.eotFile = builder.innerEotFile;
        this.datFile = builder.innerDatFile;
        this.report = builder.innerReport;
        this.repository = builder.innerRepository;
        this.retentionPeriod = builder.innerRetentionPeriod;
    }

    public String report() {
        return this.report;
    }

    public String datFile() {
        return this.datFile;
    }

    public String eotFile() {
        return this.eotFile;
    }

    public String tempFile() {
        return this.tempFile;
    }

    public String repository() {
        return this.repository;
    }

    public int retentionPeriod() {
        return this.retentionPeriod;
    }
    
    public String site() {
        return this.site;
    }

    /**
     * Creates builder to build {@link SiteConfig}.
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link SiteConfig}.
     */
    public static final class Builder {
        private String innerSite;
        private String innerTempFile;
        private String innerEotFile;
        private String innerDatFile;
        private String innerReport;
        private String innerRepository;
        private int innerRetentionPeriod;

        private Builder() {
        }
        
        public Builder SiteName(String site) {
            this.innerSite = site;
            return this;
        }
        
        public Builder TempFile(String tempFile) {
            this.innerTempFile = tempFile;
            return this;
        }

        public Builder EotFile(String eotFile) {
            this.innerEotFile = eotFile;
            return this;
        }

        public Builder DatFile(String datFile) {
            this.innerDatFile = datFile;
            return this;
        }

        public Builder Report(String report) {
            this.innerReport = report;
            return this;
        }

        public Builder Repository(String repository) {
            this.innerRepository = repository;
            return this;
        }

        public Builder RetentionPeriod(int retentionPeriod) {
            this.innerRetentionPeriod = retentionPeriod;
            return this;
        }

        public SiteConfig build() {
            return new SiteConfig(this);
        }
    }

}
