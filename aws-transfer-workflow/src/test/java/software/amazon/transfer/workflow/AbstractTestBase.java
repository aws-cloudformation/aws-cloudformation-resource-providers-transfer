package software.amazon.transfer.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import software.amazon.awssdk.services.transfer.model.OverwriteExisting;
import software.amazon.awssdk.services.transfer.model.WorkflowStepType;

public class AbstractTestBase {
  static String TEST_DESCRIPTION = "unit test";
  static Map<String, String> RESOURCE_TAG_MAP = Collections.singletonMap("key", "value");
  static Map<String, String> SYSTEM_TAG_MAP = Collections.singletonMap(
          "aws:cloudformation:stack-name", "StackName");
  static Map<String, String> TEST_TAG_MAP = ImmutableMap.of("key", "value", "aws:cloudformation:stack-name", "StackName");
  static Set<Tag> MODEL_TAGS = ImmutableSet.of(
          Tag.builder()
                  .key("key")
                  .value("value")
                  .build());
  static software.amazon.awssdk.services.transfer.model.Tag SDK_MODEL_TAG =
          software.amazon.awssdk.services.transfer.model.Tag.builder()
                  .key("key")
                  .value("value")
                  .build();
  static software.amazon.awssdk.services.transfer.model.Tag SDK_SYSTEM_TAG =
          software.amazon.awssdk.services.transfer.model.Tag.builder()
                  .key("aws:cloudformation:stack-name")
                  .value("StackName")
                  .build();

  public List<WorkflowStep> getModelCopyWorkflowSteps() {
    WorkflowStep step = WorkflowStep.builder()
            .type(WorkflowStepType.COPY.toString())
            .copyStepDetails(CopyStepDetails.builder()
                    .name("COPY")
                    .overwriteExisting(OverwriteExisting.TRUE.toString())
                    .destinationFileLocation(InputFileLocation.builder()
                            .s3FileLocation(S3InputFileLocation.builder()
                                    .bucket("bucket")
                                    .key("key")
                                    .build())
                            .build())
                    .build())
            .build();
    return Collections.singletonList(step);
  }
}
