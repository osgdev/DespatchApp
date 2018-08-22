package uk.gov.dvla.osg.despatchapp.models;

public enum PrintSite {
    MORRISTON {
        @Override
        public String toString() {
            return"MORRISTON";
        }
    },
    TYFELIN{
        @Override
        public String toString() {
            return"TY FELIN";
        }
    };
}
