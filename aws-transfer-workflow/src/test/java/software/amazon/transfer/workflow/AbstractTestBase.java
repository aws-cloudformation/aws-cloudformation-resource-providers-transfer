package software.amazon.transfer.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.transfer.model.OverwriteExisting;
import software.amazon.awssdk.services.transfer.model.WorkflowStepType;

public class AbstractTestBase {
  static String TEST_DESCRIPTION = "unit test";
  static Map<String, String> TEST_TAG_MAP = Collections.singletonMap("key", "value");

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
