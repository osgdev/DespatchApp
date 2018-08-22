package uk.gov.dvla.osg.despatchapp.config;

import uk.gov.dvla.osg.despatchapp.models.PrintSite;

public class SiteConfigFactory {

    public static SiteConfig get(PrintSite site) {
        switch (site) {
        case MORRISTON:
            return AppConfig.getInstance().morriston();
        case TYFELIN:
            return AppConfig.getInstance().tyFelin();
        default:
            return null;
        }
    }
}
