package software.amazon.transfer.connector;

import software.amazon.awssdk.services.transfer.TransferClient;
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
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static software.amazon.transfer.connector.AbstractTestBase.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private TransferClient client;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler(client);

        final ResourceModel model = ResourceModel.builder()
                .accessRole(TEST_ACCESS_ROLE)
                .as2Config(getAs2Config())
                .loggingRole(TEST_LOGGING_ROLE)
                .url(TEST_URL)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        CreateConnectorResponse createConnectorResponse = CreateConnectorResponse.builder()
                .connectorId(TEST_CONNECTOR_ID)
                .build();

        doReturn(createConnectorResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null,
                logger);

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
        assertThat(testModel).hasFieldOrPropertyWithValue("loggingRole", TEST_LOGGING_ROLE);
        assertThat(testModel).hasFieldOrPropertyWithValue("url", TEST_URL);
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(InvalidRequestException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateConnectorRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(InternalServiceErrorException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateConnectorRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_ResourceExistsExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(ResourceExistsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateConnectorRequest.class), any());

        ResourceModel model = ResourceModel.builder()
                .connectorId(TEST_CONNECTOR_ID)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_ThrottlingExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(ThrottlingException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateConnectorRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(TransferException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateConnectorRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
