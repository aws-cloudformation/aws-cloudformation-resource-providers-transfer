package software.amazon.transfer.workflow;

import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeWorkflowRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWorkflowResponse;
import software.amazon.awssdk.services.transfer.model.DescribedWorkflow;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.util.CollectionUtils;

@NoArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public ReadHandler(TransferClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        if (this.client == null){
            this.client = ClientBuilder.getClient();
        }

        ResourceModel model = request.getDesiredResourceState();
        DescribeWorkflowRequest describeWorkflowRequest = DescribeWorkflowRequest.builder()
                .workflowId(model.getWorkflowId())
                .build();
        try {
            DescribeWorkflowResponse response = proxy
                    .injectCredentialsAndInvokeV2(describeWorkflowRequest, client::describeWorkflow);
            logger.log(String.format("%s %s described successfully",
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
            DescribedWorkflow describedWorkflow = response.workflow();

            ResourceModel resourceModel = ResourceModel.builder()
                    .arn(describedWorkflow.arn())
                    .description(describedWorkflow.description())
                    .onExceptionSteps((CollectionUtils.isNullOrEmpty(describedWorkflow.onExceptionSteps())) ?
                            null : describedWorkflow.onExceptionSteps()
                            .stream()
                            .map(Converter.WorkflowStepConverter::fromSdk)
                            .collect(Collectors.toList()))
                    .steps((CollectionUtils.isNullOrEmpty(describedWorkflow.steps())) ?
                            null : describedWorkflow.steps()
                            .stream()
                            .map(Converter.WorkflowStepConverter::fromSdk)
                            .collect(Collectors.toList()))
                    .tags((CollectionUtils.isNullOrEmpty(describedWorkflow.tags())) ?
                            null : describedWorkflow.tags()
                            .stream()
                            .map(Converter.TagConverter::fromSdk)
                            .collect(Collectors.toSet()))
                    .workflowId(model.getWorkflowId())
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }  catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(describeWorkflowRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("describeWorkflow", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
