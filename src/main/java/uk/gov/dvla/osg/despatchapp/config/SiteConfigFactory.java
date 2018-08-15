package uk.gov.dvla.osg.despatchapp.config;

public class SiteConfigFactory {

    public static SiteConfig get(String site) {
        AppConfig config = AppConfig.getInstance();
        switch (site) {
        case "MORRISTON":
            return config.morriston();
        case "TY FELIN":
            return config.tyFelin();
        case "BRP":
            return config.brp();
        default:
            return null;
        }
    }
}
