package software.amazon.transfer.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.DEFAULT_ENDPOINT_TYPE;

import java.util.Collection;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.CreateServerRequest;
import software.amazon.awssdk.services.transfer.model.CreateServerResponse;
import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.DescribeServerResponse;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.EndpointDetails;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private CreateHandler handler;

    @BeforeEach
    public void setupTestData() {
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccessWithPublicServer() {
        assertSuccessWithSimpleServer(EndpointType.PUBLIC.name());
    }

    @Test
    public void handleRequest_SimpleSuccessWithVpcServer() {
        assertSuccessWithSimpleServer(EndpointType.VPC.name());
    }

    private void assertSuccessWithSimpleServer(String endpointType) {
        final ResourceModel model = setupSimpleServerModel(endpointType);

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        if (EndpointType.VPC.name().equals(endpointType)) {
            setupVpcEndpointStates(model);
        }

        final ProgressEvent<ResourceModel, CallbackContext> response =
                createServerAndAssertStatus(model, request, "ONLINE", OperationStatus.SUCCESS);

        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).createServer(any(CreateServerRequest.class));
        verify(sdkClient, atLeastOnce()).describeServer(any(DescribeServerRequest.class));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createServerAndAssertStatus(
            ResourceModel model,
            ResourceHandlerRequest<ResourceModel> request,
            String postCreateState,
            OperationStatus operationStatus) {
        setupCreateServerResponse();

        DescribeServerResponse describeServerResponse = describeServerFromModel("testServerId", postCreateState, model);

        doReturn(describeServerResponse).when(sdkClient).describeServer(any(DescribeServerRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(operationStatus);

        return response;
    }

    @Test
    public void stabilizationFailures() {

        ResourceModel model = setupSimpleServerModel(DEFAULT_ENDPOINT_TYPE);

        ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        softly.assertThatThrownBy(
                        () -> createServerAndAssertStatus(model, request, "START_FAILED", OperationStatus.FAILED))
                .isInstanceOf(CfnNotStabilizedException.class);

        softly.assertThatThrownBy(
                        () -> createServerAndAssertStatus(model, request, "STOP_FAILED", OperationStatus.FAILED))
                .isInstanceOf(CfnNotStabilizedException.class);

        verify(sdkClient, atLeastOnce()).createServer(any(CreateServerRequest.class));
        verify(sdkClient, atLeastOnce()).describeServer(any(DescribeServerRequest.class));
    }

    @Test
    public void fullyLoadedServerTest() {

        final String serverId = "testServerId";
        final ResourceModel model = fullyLoadedServerModel();
        model.setServerId(serverId);

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        setupCreateServerResponse();
        setupVpcEndpointStates(model);
        setupPrivateIpsStates();

        DescribedServer initialServerState =
                describeServerFromModel(model.getServerId(), "ONLINE", model).server();

        EndpointDetails noAddrAllocIds = initialServerState.endpointDetails().toBuilder()
                .addressAllocationIds((Collection<String>) null)
                .build();

        initialServerState =
                initialServerState.toBuilder().endpointDetails(noAddrAllocIds).build();

        DescribeServerResponse initialState =
                DescribeServerResponse.builder().server(initialServerState).build();
        DescribeServerResponse finalStateResponse = describeServerFromModel(model.getServerId(), "ONLINE", model);

        DescribeServerResponse stoppingServerResponse = newStateResponse(initialState, "STOPPING");
        DescribeServerResponse offlineServerResponse = newStateResponse(initialState, "OFFLINE");
        DescribeServerResponse updatedServerResponse = newStateResponse(finalStateResponse, "OFFLINE");
        DescribeServerResponse startingServerResponse = newStateResponse(finalStateResponse, "STARTING");

        doReturn(
                        initialState,
                        initialState,
                        stoppingServerResponse,
                        offlineServerResponse,
                        updatedServerResponse,
                        startingServerResponse,
                        finalStateResponse)
                .when(sdkClient)
                .describeServer(any(DescribeServerRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).createServer(any(CreateServerRequest.class));
        verify(sdkClient, atLeastOnce()).describeServer(any(DescribeServerRequest.class));
    }

    @Test
    public void errorPathsTest() {
        final ResourceModel model = setupSimpleServerModel(DEFAULT_ENDPOINT_TYPE);

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        setupCreateServerResponse();

        Exception ex1 = ThrottlingException.builder().build();
        Exception ex2 = InvalidRequestException.builder().build();

        doThrow(ex1).doThrow(ex2).when(sdkClient).describeServer(any(DescribeServerRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        // We expect the throttling exception to be retried, the result should be IN_PROGRESS
        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);

        response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        // Then the InvalidRequest happens
        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    private void setupCreateServerResponse() {
        CreateServerResponse createServerResponse =
                CreateServerResponse.builder().serverId("testServerId").build();
        doReturn(createServerResponse).when(sdkClient).createServer(any(CreateServerRequest.class));
    }
}
