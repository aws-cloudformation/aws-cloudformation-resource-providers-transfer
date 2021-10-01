package software.amazon.transfer.workflow;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListWorkflowsRequest;
import software.amazon.awssdk.services.transfer.model.ListWorkflowsResponse;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ListHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public ListHandler(TransferClient client) {
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

        final List<ResourceModel> models = new ArrayList<>();

        ListWorkflowsRequest listWorkflowsRequest = ListWorkflowsRequest.builder()
                .maxResults(10)
                .nextToken(request.getNextToken())
                .build();

        try {
            ListWorkflowsResponse response =
                    proxy.injectCredentialsAndInvokeV2(listWorkflowsRequest, client::listWorkflows);

            response.workflows().forEach(listedWorkflow -> {
                ResourceModel model = ResourceModel.builder()
                        .arn(listedWorkflow.arn())
                        .description(listedWorkflow.description())
                        .workflowId(listedWorkflow.workflowId())
                        .build();
                models.add(model);
            });

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .nextToken(response.nextToken())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(listWorkflowsRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("listWorkflow", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
