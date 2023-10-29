package software.amazon.transfer.user.translators;

import com.amazonaws.arn.Arn;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

/** Arn representing an SFTP user. */
public class UserArn extends BaseArn {
    /** Number of parts delimited by {@link #RESOURCE_DELIMITER} in relativeId of ARN. */
    private static final int RELATIVE_ID_PARTS = 3;

    /** User resource type. */
    static final String USER = "user";

    private final String serverId;

    private final String userName;

    public UserArn(Region region, String accountId, String serverId, String userName) {
        super(region, accountId);
        this.serverId = serverId;
        this.userName = userName;
    }

    public String getResourceType() {
        return USER;
    }

    public String getResourceId() {
        return String.join(RESOURCE_DELIMITER, serverId, userName);
    }

    public String getUserName() {
        return userName;
    }

    public String getServerId() {
        return serverId;
    }

    public static UserArn fromString(String arn) {
        return fromArn(Arn.fromString(arn));
    }

    static UserArn fromArn(Arn arn) {
        String[] relativeIdParts = getRelativeIdParts(arn);
        if (relativeIdParts.length != RELATIVE_ID_PARTS || !relativeIdParts[0].equals(USER)) {
            throw new IllegalArgumentException("Invalid User ARN: " + arn.toString());
        }

        Region r = RegionUtils.getRegion(arn.getRegion());
        return new UserArn(r, arn.getAccountId(), relativeIdParts[1], relativeIdParts[2]);
    }
}
