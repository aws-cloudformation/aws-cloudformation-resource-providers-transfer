package software.amazon.transfer.agreement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
}
