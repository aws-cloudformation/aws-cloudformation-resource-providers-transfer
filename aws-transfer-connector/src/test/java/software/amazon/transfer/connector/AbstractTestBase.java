package software.amazon.transfer.connector;

import java.util.Collections;
import java.util.Map;

public class AbstractTestBase {
    public static String TEST_ARN = "arn:test-arn";
    public static String TEST_ACCESS_ROLE = "access-role";
    public static String TEST_LOGGING_ROLE = "logging-role";
    public static Map<String, String> TEST_TAG_MAP = Collections.singletonMap("key", "value");
    public static String TEST_URL = "http://test.com/";
    public static String TEST_CONNECTOR_ID = "c-123456789";

    public static String TEST_LOCAL_PROFILE = "local-profile";
    public static String TEST_PARTNER_PROFILE = "partner-profile";
    public static String TEST_MESSAGE_SUBJECT = "This is a test message subject.";
    public static String TEST_COMPRESSION = "ZLIB";
    public static String TEST_ENCRYPTION_ALGORITHM = "AES128_CBC";
    public static String TEST_SIGNING_ALGORITHM = "SHA256";
    public static String TEST_MDN_SIGNING_ALGORITHM = "SHA256";
    public static String TEST_MDN_RESPONSE = "SYNC";

    public static As2Config getAs2Config() {
        return As2Config.builder()
            .localProfileId(TEST_LOCAL_PROFILE)
        .partnerProfileId(TEST_PARTNER_PROFILE)
        .messageSubject(TEST_MESSAGE_SUBJECT)
        .compression(TEST_COMPRESSION)
        .encryptionAlgorithm(TEST_ENCRYPTION_ALGORITHM)
        .signingAlgorithm(TEST_SIGNING_ALGORITHM)
        .mdnSigningAlgorithm(TEST_MDN_SIGNING_ALGORITHM)
        .mdnResponse(TEST_MDN_RESPONSE)
        .build();
    }
}
