package software.amazon.transfer.server.translators;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import software.amazon.transfer.server.WorkflowDetail;
import software.amazon.transfer.server.WorkflowDetails;

public final class WorkflowDetailsTranslator {
    private WorkflowDetailsTranslator() {}

    public static software.amazon.awssdk.services.transfer.model.WorkflowDetails toSdk(
            WorkflowDetails workflowDetails, boolean forUpdate) {
        software.amazon.awssdk.services.transfer.model.WorkflowDetails.Builder builder =
                software.amazon.awssdk.services.transfer.model.WorkflowDetails.builder();

        if (workflowDetails != null) {
            builder.onUpload(toSdk(workflowDetails.getOnUpload()))
                    .onPartialUpload(toSdk(workflowDetails.getOnPartialUpload()));
        } else if (forUpdate) {
            // As the server may already have workflows details, we need to make sure
            // we remove the old onUpload or onPartialUpload workflows if they are not
            // set in the update.

            builder.onUpload(Collections.emptyList()).onPartialUpload(Collections.emptyList());
        } else {
            return null;
        }
        return builder.build();
    }

    private static List<software.amazon.awssdk.services.transfer.model.WorkflowDetail> toSdk(
            List<WorkflowDetail> details) {
        if (details == null) {
            return null;
        }
        return details.stream()
                .map(detail -> software.amazon.awssdk.services.transfer.model.WorkflowDetail.builder()
                        .workflowId(detail.getWorkflowId())
                        .executionRole(detail.getExecutionRole())
                        .build())
                .collect(Collectors.toList());
    }

    public static WorkflowDetails fromSdk(
            software.amazon.awssdk.services.transfer.model.WorkflowDetails workflowDetails) {
        if (workflowDetails == null) {
            return null;
        }

        if (workflowDetails.hasOnUpload() || workflowDetails.hasOnPartialUpload()) {
            return WorkflowDetails.builder()
                    .onUpload(fromSdk(workflowDetails.onUpload()))
                    .onPartialUpload(fromSdk(workflowDetails.onPartialUpload()))
                    .build();
        }

        return null;
    }

    private static List<WorkflowDetail> fromSdk(
            List<software.amazon.awssdk.services.transfer.model.WorkflowDetail> details) {
        if (details == null) {
            return null;
        }
        return details.stream()
                .map(detail -> WorkflowDetail.builder()
                        .workflowId(detail.workflowId())
                        .executionRole(detail.executionRole())
                        .build())
                .collect(Collectors.toList());
    }
}
