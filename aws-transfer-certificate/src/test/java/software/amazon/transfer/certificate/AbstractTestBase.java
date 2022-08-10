package software.amazon.transfer.certificate;

import java.util.Collections;
import java.util.Map;

public class AbstractTestBase {
    private AbstractTestBase() {}

    public static final String TEST_CERTIFICATE_ID = "id";
    public static final String TEST_ARN = "arn:certificate";
    public static final String TEST_DESCRIPTION = "description";
    public static final String TEST_DESCRIPTION_2 = "description2";
    public static final String TEST_USAGE = "usage";
    public static final String TEST_CERTIFICATE = "certificate";
    public static final String TEST_CERTIFICATE_CHAIN = "certificate-chain";
    public static final String TEST_PRIVATE_KEY = "private-key";
    public static final String TEST_ACTIVE_DATE = "2022-07-17T09:59:51.312Z";
    public static final String TEST_INACTIVE_DATE = "2069-07-17T09:59:51.312Z";
    public static final Map<String, String> TEST_TAG_MAP = Collections.singletonMap("key", "value");
}
