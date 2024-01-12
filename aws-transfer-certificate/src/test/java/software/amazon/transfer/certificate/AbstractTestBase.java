package software.amazon.transfer.certificate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
