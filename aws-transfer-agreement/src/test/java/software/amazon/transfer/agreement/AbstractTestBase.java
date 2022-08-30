package software.amazon.transfer.agreement;

import java.util.Collections;
import java.util.Map;

public class AbstractTestBase {

    public static String TEST_ARN = "arn:test-arn";
    public static String TEST_DESCRIPTION = "unit test";
    public static String TEST_DESCRIPTION_2 = "another unit test";
    public static String TEST_ACCESS_ROLE = "access-role";
    public static String TEST_BASE_DIRECTORY = "/";
    public static String TEST_LOCAL_PROFILE = "local-profile";
    public static String TEST_PARTNER_PROFILE = "partner-profile";
    public static String TEST_SERVER_ID = "test-server-id";
    public static String TEST_STATUS = "ACTIVE";
    public static String TEST_AGREEMENT_ID = "id";
    public static Map<String, String> TEST_TAG_MAP = Collections.singletonMap("key", "value");
}
