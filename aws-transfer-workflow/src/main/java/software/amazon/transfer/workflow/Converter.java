package software.amazon.transfer.workflow;

import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.model.CopyStepDetails;
import software.amazon.awssdk.services.transfer.model.CustomStepDetails;
import software.amazon.awssdk.services.transfer.model.DeleteStepDetails;
import software.amazon.awssdk.services.transfer.model.InputFileLocation;
import software.amazon.awssdk.services.transfer.model.S3Tag;
import software.amazon.awssdk.services.transfer.model.S3InputFileLocation;
import software.amazon.awssdk.services.transfer.model.Tag;
import software.amazon.awssdk.services.transfer.model.TagStepDetails;
import software.amazon.awssdk.services.transfer.model.WorkflowStep;
import software.amazon.awssdk.services.transfer.model.WorkflowStepType;

public class Converter {
    static class TagConverter {
        static Tag toSdk(
                software.amazon.transfer.workflow.Tag tag) {
            if (tag == null) {
                return null;
            }
            return Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
        }

        static software.amazon.transfer.workflow.Tag toModel(Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.transfer.workflow.Tag.builder()
                    .key(tag.key())
                    .value(tag.value())
                    .build();
        }
    }

    static class WorkflowStepConverter {
        static WorkflowStep toSdk(
                software.amazon.transfer.workflow.WorkflowStep workflowStep) {
            if (workflowStep == null) {
                return null;
            }
            WorkflowStep.Builder sdkWorkflowStep = WorkflowStep.builder();

            String type = workflowStep.getType();
            sdkWorkflowStep.type(type);

            if (WorkflowStepType.COPY.toString().equals(type) && workflowStep.getCopyStepDetails() != null) {
                sdkWorkflowStep.copyStepDetails(CopyStepDetails.builder()
                        .name(workflowStep.getCopyStepDetails().getName())
                        .destinationFileLocation(
                                (workflowStep.getCopyStepDetails().getDestinationFileLocation() == null) ?
                                        null : InputFileLocation.builder()
                                        .s3FileLocation((workflowStep.getCopyStepDetails()
                                                .getDestinationFileLocation().getS3FileLocation() == null) ?
                                                null : S3InputFileLocation.builder()
                                                .bucket(workflowStep.getCopyStepDetails().getDestinationFileLocation()
                                                        .getS3FileLocation().getBucket())
                                                .key(workflowStep.getCopyStepDetails().getDestinationFileLocation()
                                                        .getS3FileLocation().getKey())
                                                .build())
                                        .build())
                        .overwriteExisting(workflowStep.getCopyStepDetails().getOverwriteExisting())
                        .build());
            }
            if (WorkflowStepType.CUSTOM.toString().equals(type) && workflowStep.getCustomStepDetails() !=null) {
                sdkWorkflowStep.customStepDetails(CustomStepDetails.builder()
                        .name(workflowStep.getCustomStepDetails().getName())
                        .target(workflowStep.getCustomStepDetails().getTarget())
                        .timeoutSeconds(workflowStep.getCustomStepDetails().getTimeoutSeconds())
                        .build());
            }
            if (WorkflowStepType.DELETE.toString().equals(type) && workflowStep.getDeleteStepDetails() != null) {
                sdkWorkflowStep.deleteStepDetails(DeleteStepDetails.builder()
                        .name(workflowStep.getDeleteStepDetails().getName())
                        .build());
            }
            if (WorkflowStepType.TAG.toString().equals(type) && workflowStep.getTagStepDetails() != null) {
                sdkWorkflowStep.tagStepDetails(TagStepDetails.builder()
                        .name(workflowStep.getTagStepDetails().getName())
                        .tags((workflowStep.getTagStepDetails().getTags() == null) ?
                                null : workflowStep.getTagStepDetails().getTags()
                                .stream()
                                .map(S3TagConverter::toSdk)
                                .collect(Collectors.toList()))
                        .build());
            }
            return sdkWorkflowStep.build();
        }

        static software.amazon.transfer.workflow.WorkflowStep toModel(WorkflowStep workflowStep) {
            if (workflowStep == null) {
                return null;
            }
            software.amazon.transfer.workflow.WorkflowStep modelWorkflowStep =
                    new software.amazon.transfer.workflow.WorkflowStep();

            WorkflowStepType type = workflowStep.type();
            if (type != null) {
                modelWorkflowStep.setType(type.toString());
            }

            if (workflowStep.copyStepDetails() != null) {
                software.amazon.transfer.workflow.CopyStepDetails copyStepDetails =
                        software.amazon.transfer.workflow.CopyStepDetails.builder()
                                .name(workflowStep.copyStepDetails().name())
                                .overwriteExisting(workflowStep.copyStepDetails().overwriteExisting().toString())
                                .build();
                if (workflowStep.copyStepDetails().destinationFileLocation() != null) {
                    software.amazon.transfer.workflow.InputFileLocation inputFileLocation =
                            software.amazon.transfer.workflow.InputFileLocation.builder().build();
                    if (workflowStep.copyStepDetails().destinationFileLocation().s3FileLocation() != null) {
                        inputFileLocation.setS3FileLocation(
                                software.amazon.transfer.workflow.S3InputFileLocation.builder()
                                        .bucket(workflowStep.copyStepDetails().destinationFileLocation()
                                                .s3FileLocation().bucket())
                                        .key(workflowStep.copyStepDetails().destinationFileLocation()
                                                .s3FileLocation().key())
                                        .build());
                    }
                    copyStepDetails.setDestinationFileLocation(inputFileLocation);
                }
                modelWorkflowStep.setCopyStepDetails(copyStepDetails);
            }
            if (workflowStep.customStepDetails() !=null) {
                modelWorkflowStep.setCustomStepDetails(software.amazon.transfer.workflow.CustomStepDetails.builder()
                        .name(workflowStep.customStepDetails().name())
                        .target(workflowStep.customStepDetails().target())
                        .timeoutSeconds(workflowStep.customStepDetails().timeoutSeconds())
                        .build());
            }
            if (workflowStep.deleteStepDetails() != null) {
                modelWorkflowStep.setDeleteStepDetails(software.amazon.transfer.workflow.DeleteStepDetails.builder()
                        .name(workflowStep.deleteStepDetails().name())
                        .build());
            }
            if (workflowStep.tagStepDetails() != null) {
                modelWorkflowStep.setTagStepDetails(software.amazon.transfer.workflow.TagStepDetails.builder()
                        .name(workflowStep.tagStepDetails().name())
                        .tags(workflowStep.tagStepDetails().tags()
                                .stream()
                                .map(S3TagConverter::toModel)
                                .collect(Collectors.toSet()))
                        .build());
            }
            return modelWorkflowStep;
        }
    }

    private static class S3TagConverter {
        static S3Tag toSdk(
                software.amazon.transfer.workflow.S3Tag s3tag) {
            if (s3tag == null) {
                return null;
            }
            return S3Tag.builder()
                    .key(s3tag.getKey())
                    .value(s3tag.getValue())
                    .build();
        }

        static software.amazon.transfer.workflow.S3Tag toModel(S3Tag s3tag) {
            if (s3tag == null) {
                return null;
            }
            return software.amazon.transfer.workflow.S3Tag.builder()
                    .key(s3tag.key())
                    .value(s3tag.value())
                    .build();
        }
    }
}
