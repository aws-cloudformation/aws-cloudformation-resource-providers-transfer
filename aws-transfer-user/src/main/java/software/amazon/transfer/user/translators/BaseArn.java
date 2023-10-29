package software.amazon.transfer.user.translators;

import com.amazonaws.arn.Arn;
import com.amazonaws.arn.ArnResource;
import com.amazonaws.regions.Region;

/** Transfer service base ARN class. */
public abstract class BaseArn {
    static final String RESOURCE_DELIMITER = "/";

    /** Public SDK name of the service. */
    static final String SERVICE_NAME = "transfer";

    private final Region region;
    private final String accountId;

    BaseArn(Region region, String accountId) {
        this.region = region;
        this.accountId = accountId;
    }

    public abstract String getResourceType();

    public abstract String getResourceId();

    public String getVendor() {
        return SERVICE_NAME;
    }

    public String getAccountId() {
        return accountId;
    }

    public Region getRegion() {
        return region;
    }

    public String getArn() {
        String resource =
                ArnResource.builder()
                        .withResourceType(getResourceType())
                        .withResource(getResourceId())
                        .build()
                        .toString()
                        .replace(":", RESOURCE_DELIMITER);

        return Arn.builder()
                .withPartition(getRegion().getPartition())
                .withService(SERVICE_NAME)
                .withRegion(getRegion().getName())
                .withAccountId(getAccountId())
                .withResource(resource)
                .build()
                .toString();
    }

    protected static String[] getRelativeIdParts(Arn arn) {
        return arn.getResourceAsString().split(RESOURCE_DELIMITER, -1);
    }
}
