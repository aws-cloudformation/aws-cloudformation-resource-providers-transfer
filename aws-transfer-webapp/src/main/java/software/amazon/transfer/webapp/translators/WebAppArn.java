package software.amazon.transfer.webapp.translators;

import com.amazonaws.arn.Arn;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

/** ARN representing a web app. */
public class WebAppArn extends BaseArn {
    /** Number of parts delimited by {@link #RESOURCE_DELIMITER} in relativeId of ARN. */
    private static final int RELATIVE_ID_PARTS = 2;
    /** Web app resource type. */
    static final String WEB_APP = "webapp";

    private final String webAppId;

    public WebAppArn(Region region, String accountId, String webAppId) {
        super(region, accountId);
        this.webAppId = webAppId;
    }

    public String getResourceType() {
        return WEB_APP;
    }

    public String getResourceId() {
        return webAppId;
    }

    public String getWebAppId() {
        return webAppId;
    }

    public static WebAppArn fromString(String arn) {
        return fromArn(Arn.fromString(arn));
    }

    public static WebAppArn fromArn(Arn arn) {
        String[] relativeIdParts = getRelativeIdParts(arn);
        if (relativeIdParts.length != RELATIVE_ID_PARTS || !relativeIdParts[0].equals(WEB_APP)) {
            throw new IllegalArgumentException("Invalid Web App ARN: " + arn);
        }

        Region r = RegionUtils.getRegion(arn.getRegion());
        return new WebAppArn(r, arn.getAccountId(), relativeIdParts[1]);
    }
}
