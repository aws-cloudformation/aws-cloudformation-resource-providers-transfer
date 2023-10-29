package software.amazon.transfer.server.translators;

import com.amazonaws.arn.Arn;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

/** ARN representing an SFTP server. */
public class ServerArn extends BaseArn {
    /** Number of parts delimited by {@link #RESOURCE_DELIMITER} in relativeId of ARN. */
    private static final int RELATIVE_ID_PARTS = 2;
    /** Server resource type. */
    static final String SERVER = "server";

    private final String serverId;

    public ServerArn(Region region, String accountId, String serverId) {
        super(region, accountId);
        this.serverId = serverId;
    }

    public String getResourceType() {
        return SERVER;
    }

    public String getResourceId() {
        return serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public static ServerArn fromString(String arn) {
        return fromArn(Arn.fromString(arn));
    }

    public static ServerArn fromArn(Arn arn) {
        String[] relativeIdParts = getRelativeIdParts(arn);
        if (relativeIdParts.length != RELATIVE_ID_PARTS || !relativeIdParts[0].equals(SERVER)) {
            throw new IllegalArgumentException("Invalid Server ARN: " + arn);
        }

        Region r = RegionUtils.getRegion(arn.getRegion());
        return new ServerArn(r, arn.getAccountId(), relativeIdParts[1]);
    }
}
