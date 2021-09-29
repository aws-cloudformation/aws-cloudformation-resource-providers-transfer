package software.amazon.transfer.workflow;

import java.util.Arrays;
import java.util.List;

import software.amazon.awssdk.services.transfer.model.OverwriteExisting;
import software.amazon.awssdk.services.transfer.model.WorkflowStepType;

public class AbstractTestBase {
  static String TEST_DESCRIPTION = "uluru unit test";

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

    return Arrays.asList(step);
  }
}
