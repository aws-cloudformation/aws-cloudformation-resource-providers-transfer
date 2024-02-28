package software.amazon.transfer.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateWorkflowRequest;
import software.amazon.awssdk.services.transfer.model.CreateWorkflowResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.util.CollectionUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public CreateHandler(TransferClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        ResourceModel model = request.getDesiredResourceState();

        Map<String, String> allTags = new HashMap<>();

        if (request.getDesiredResourceTags() != null) {
            allTags.putAll(request.getDesiredResourceTags());
        }
        if (request.getSystemTags() != null) {
            allTags.putAll(request.getSystemTags());
        }

        model.setTags(Converter.TagConverter.translateTagfromMap(allTags));
        CreateWorkflowRequest createWorkflowRequest = CreateWorkflowRequest.builder()
                .description(model.getDescription())
                .onExceptionSteps(
                        (CollectionUtils.isNullOrEmpty(model.getOnExceptionSteps()))
                                ? null
                                : model.getOnExceptionSteps().stream()
                                        .map(Converter.WorkflowStepConverter::toSdk)
                                        .collect(Collectors.toList()))
                .steps(
                        (CollectionUtils.isNullOrEmpty(model.getSteps()))
                                ? null
                                : model.getSteps().stream()
                                        .map(Converter.WorkflowStepConverter::toSdk)
                                        .collect(Collectors.toList()))
                .tags(
                        (CollectionUtils.isNullOrEmpty(model.getTags()))
                                ? null
                                : model.getTags().stream()
                                        .map(Converter.TagConverter::toSdk)
                                        .collect(Collectors.toList()))
                .build();
        try {
            CreateWorkflowResponse response =
                    proxy.injectCredentialsAndInvokeV2(createWorkflowRequest, client::createWorkflow);
            model.setWorkflowId(response.workflowId());
            logger.log(String.format("%s created successfully", ResourceModel.TYPE_NAME));
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + createWorkflowRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("createWorkflow", e);
        } catch (ResourceExistsException e) {
            throw new CfnAlreadyExistsException(
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (ThrottlingException e) {
            throw new CfnThrottlingException("createWorkflow", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
