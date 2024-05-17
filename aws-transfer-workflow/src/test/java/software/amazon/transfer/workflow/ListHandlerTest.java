package software.amazon.transfer.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListWorkflowsRequest;
import software.amazon.awssdk.services.transfer.model.ListWorkflowsResponse;
import software.amazon.awssdk.services.transfer.model.ListedWorkflow;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new ListHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        ListedWorkflow listedWorkflow = ListedWorkflow.builder()
                .description(TEST_DESCRIPTION)
                .arn("testarn")
                .workflowId("testid")
                .build();

        ResourceModel model = ResourceModel.builder().workflowId("testid").build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListWorkflowsResponse listWorkflowsResponse =
                ListWorkflowsResponse.builder().workflows(listedWorkflow).build();
        doReturn(listWorkflowsResponse).when(client).listWorkflows(any(ListWorkflowsRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        List<ResourceModel> testModels = response.getResourceModels();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(testModels).isNotNull();

        ResourceModel testListedModel = testModels.get(0);

        assertThat(testListedModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("arn", "testarn");
        assertThat(testListedModel).hasFieldOrPropertyWithValue("workflowId", "testid");

        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).listWorkflows(any(ListWorkflowsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).listWorkflows(any(ListWorkflowsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).listWorkflows(any(ListWorkflowsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
