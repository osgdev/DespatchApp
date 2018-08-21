package uk.gov.dvla.osg.despatchapp.config;

public class SiteConfigFactory {

    public static SiteConfig get(String site) {
        switch (site) {
        case "MORRISTON":
            return AppConfig.getInstance().morriston();
        case "TY FELIN":
            return AppConfig.getInstance().tyFelin();
        case "BRP":
            return AppConfig.getInstance().brp();
        default:
            return null;
        }
    }
}
