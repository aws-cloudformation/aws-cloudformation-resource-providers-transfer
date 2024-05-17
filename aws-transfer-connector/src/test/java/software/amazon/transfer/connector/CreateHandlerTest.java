package software.amazon.transfer.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.connector.AbstractTestBase.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.CreateConnectorRequest;
import software.amazon.awssdk.services.transfer.model.CreateConnectorResponse;
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
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .accessRole(TEST_ACCESS_ROLE)
                .as2Config(getAs2Config())
                .sftpConfig(getSftpConfig())
                .loggingRole(TEST_LOGGING_ROLE)
                .url(TEST_URL)
                .tags(MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        CreateConnectorResponse createConnectorResponse =
                CreateConnectorResponse.builder().connectorId(TEST_CONNECTOR_ID).build();

        doReturn(createConnectorResponse).when(client).createConnector(any(CreateConnectorRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        ResourceModel testModel = response.getResourceModel();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(testModel).hasFieldOrPropertyWithValue("accessRole", TEST_ACCESS_ROLE);
        assertThat(testModel).hasFieldOrPropertyWithValue("as2Config", getAs2Config());
        assertThat(testModel).hasFieldOrPropertyWithValue("sftpConfig", getSftpConfig());
        assertThat(testModel).hasFieldOrPropertyWithValue("loggingRole", TEST_LOGGING_ROLE);
        assertThat(testModel).hasFieldOrPropertyWithValue("url", TEST_URL);

        ArgumentCaptor<CreateConnectorRequest> requestCaptor = ArgumentCaptor.forClass(CreateConnectorRequest.class);
        verify(client).createConnector(requestCaptor.capture());
        CreateConnectorRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.tags()).containsExactlyInAnyOrder(SDK_MODEL_TAG, SDK_SYSTEM_TAG);
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).createConnector(any(CreateConnectorRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).createConnector(any(CreateConnectorRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceExistsExceptionFailed() {
        doThrow(ResourceExistsException.class).when(client).createConnector(any(CreateConnectorRequest.class));

        ResourceModel model =
                ResourceModel.builder().connectorId(TEST_CONNECTOR_ID).build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ThrottlingExceptionFailed() {
        doThrow(ThrottlingException.class).when(client).createConnector(any(CreateConnectorRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnThrottlingException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).createConnector(any(CreateConnectorRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
