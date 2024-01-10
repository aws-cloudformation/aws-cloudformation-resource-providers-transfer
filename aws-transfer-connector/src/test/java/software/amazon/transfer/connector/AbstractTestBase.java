package software.amazon.transfer.connector;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class AbstractTestBase {
    public static String TEST_ARN = "arn:test-arn";
    public static String TEST_ACCESS_ROLE = "access-role";
    public static String TEST_LOGGING_ROLE = "logging-role";
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
    public static String TEST_BASIC_AUTH_SECRET = "basic-auth-secret";
    public static String TEST_USER_SECRET_ID = "arn:aws:secretsmanager:us-east-1:123456789:secret:testest";
    public static Map<String, String> RESOURCE_TAG_MAP = Collections.singletonMap("key", "value");
    public static Map<String, String> SYSTEM_TAG_MAP =
            Collections.singletonMap("aws:cloudformation:stack-name", "StackName");
    public static Map<String, String> TEST_TAG_MAP =
            ImmutableMap.of("key", "value", "aws:cloudformation:stack-name", "StackName");
    public static Set<Tag> MODEL_TAGS =
            ImmutableSet.of(Tag.builder().key("key").value("value").build());
    public static software.amazon.awssdk.services.transfer.model.Tag SDK_MODEL_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("key")
                    .value("value")
                    .build();
    public static software.amazon.awssdk.services.transfer.model.Tag SDK_SYSTEM_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("aws:cloudformation:stack-name")
                    .value("StackName")
                    .build();

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
                .basicAuthSecretId(TEST_BASIC_AUTH_SECRET)
                .build();
    }

    public static SftpConfig getSftpConfig() {
        return SftpConfig.builder()
                .userSecretId(TEST_USER_SECRET_ID)
                .trustedHostKeys(Collections.emptyList())
                .build();
    }
}
