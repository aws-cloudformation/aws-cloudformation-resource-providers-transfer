package software.amazon.transfer.connector;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListConnectorsRequest;
import software.amazon.awssdk.services.transfer.model.ListConnectorsResponse;
import software.amazon.awssdk.services.transfer.model.ListedConnector;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

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
        client = mock(TransferClient.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler(client);

        ListedConnector listedConnector = ListedConnector.builder()
                .arn(TEST_ARN)
                .connectorId(TEST_CONNECTOR_ID)
                .url(TEST_URL)
                .build();

        final ResourceModel model = ResourceModel.builder()
                .connectorId(TEST_CONNECTOR_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListConnectorsResponse listConnectorsResponse = ListConnectorsResponse.builder().connectors(listedConnector)
                .build();
        doReturn(listConnectorsResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null,
                logger);

        List<ResourceModel> testModels = response.getResourceModels();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(testModels).isNotNull();

        ResourceModel testListedModel = testModels.get(0);

        assertThat(testListedModel).hasFieldOrPropertyWithValue("arn", TEST_ARN);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("connectorId", TEST_CONNECTOR_ID);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("url", TEST_URL);

        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(InvalidRequestException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListConnectorsRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(InternalServiceErrorException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListConnectorsRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(TransferException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ListConnectorsRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }
}
