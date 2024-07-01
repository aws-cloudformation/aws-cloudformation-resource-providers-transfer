package software.amazon.transfer.server.translators;

import static software.amazon.transfer.server.translators.Translator.streamOfOrEmpty;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.transfer.server.WorkflowDetail;
import software.amazon.transfer.server.WorkflowDetails;

public final class WorkflowDetailsTranslator {
    private WorkflowDetailsTranslator() {}

    public static software.amazon.awssdk.services.transfer.model.WorkflowDetails toSdk(
            WorkflowDetails workflowDetails, boolean forUpdate) {
        // As the server may already have workflows details, we need to make sure
        // we remove the old onUpload or onPartialUpload workflows if they are not
        // set in the update. The unit test proves this is done correctly.
        var builder = software.amazon.awssdk.services.transfer.model.WorkflowDetails.builder();
        List<software.amazon.awssdk.services.transfer.model.WorkflowDetail> onUpload;
        List<software.amazon.awssdk.services.transfer.model.WorkflowDetail> onPartialUpload;
        if (workflowDetails == null) {
            onUpload = List.of();
            onPartialUpload = List.of();
        } else {
            onUpload = toSdk(workflowDetails.getOnUpload());
            onPartialUpload = toSdk(workflowDetails.getOnPartialUpload());
        }

        if (!forUpdate) {
            if (!onUpload.isEmpty()) {
                if (onPartialUpload.isEmpty()) {
                    onPartialUpload = null;
                }
            } else {
                if (onPartialUpload.isEmpty()) {
                    // Both null then Create needs to return null
                    return null;
                }
                onUpload = null;
            }
        }

        return builder.onUpload(onUpload).onPartialUpload(onPartialUpload).build();
    }

    private static List<software.amazon.awssdk.services.transfer.model.WorkflowDetail> toSdk(
            List<WorkflowDetail> details) {
        return streamOfOrEmpty(details)
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
