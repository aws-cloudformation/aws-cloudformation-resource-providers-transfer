package software.amazon.transfer.profile;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class AbstractTestBase {

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
