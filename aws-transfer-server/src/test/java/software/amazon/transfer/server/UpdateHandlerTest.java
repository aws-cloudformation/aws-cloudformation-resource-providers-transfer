package software.amazon.transfer.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.DEFAULT_ENDPOINT_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Stubber;

import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.DescribeServerResponse;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.TagResourceResponse;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UntagResourceResponse;
import software.amazon.awssdk.services.transfer.model.UpdateServerRequest;
import software.amazon.awssdk.services.transfer.model.UpdateServerResponse;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.ServerArn;
import software.amazon.transfer.server.translators.Translator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final UpdateHandler handler = new UpdateHandler();

    @Test
    public void handleRequest_SimpleUpdate() {
        ResourceModel model = setupSimpleServerModel(DEFAULT_ENDPOINT_TYPE);
        setServerId(model, "testServer");

        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequestBuilder()
                .previousResourceState(model)
                .desiredResourceState(model)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response =
                updateServerAndAssertStatus(request, "ONLINE", OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isEqualTo(model);

        verify(sdkClient, atLeastOnce()).updateServer(any(UpdateServerRequest.class));
    }

    private static void setServerId(ResourceModel model, String serverId) {
        Region region = Region.getRegion(Regions.US_EAST_1);
        ServerArn serverArn = new ServerArn(region, "123456789012", serverId);
        model.setArn(serverArn.getArn());
        model.setServerId(serverId);
    }

    @Test
    public void handleRequest_ThrottlingHandling() {
        Exception ex = ThrottlingException.builder().build();
        ResourceModel currentState = setupSimpleServerModel(DEFAULT_ENDPOINT_TYPE);
        setServerId(currentState, "testServerId");
        ResourceModel postUpdateState = setupSimpleServerModel(DEFAULT_ENDPOINT_TYPE);
        setServerId(postUpdateState, "testServerId");
        postUpdateState.setTags(Translator.translateTagMapToTagList(EXTRA_MODEL_TAGS));

        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequestBuilder()
                .previousResourceState(currentState)
                .desiredResourceState(postUpdateState)
                .previousResourceTags(Translator.translateTagListToTagMap(MODEL_TAGS))
                .desiredResourceTags(EXTRA_MODEL_TAGS)
                .build();
        setupUpdateServerResponse(ex);

        DescribeServerResponse stabilizeFirstResponse =
                describeServerFromModel(currentState.getServerId(), "ONLINE", postUpdateState);

        doReturn(stabilizeFirstResponse).when(sdkClient).describeServer(any(DescribeServerRequest.class));

        doThrow(ex)
                .doReturn(TagResourceResponse.builder().build())
                .when(sdkClient)
                .tagResource(any(TagResourceRequest.class));

        doThrow(ex)
                .doReturn(UntagResourceResponse.builder().build())
                .when(sdkClient)
                .untagResource(any(UntagResourceRequest.class));

        callAndAssertInProgress(request);
        callAndAssertInProgress(request);
        callAndAssertInProgress(request);
        callAndAssertSuccess(request);

        verify(sdkClient, atLeastOnce()).updateServer(any(UpdateServerRequest.class));
    }

    private void callAndAssertInProgress(ResourceHandlerRequest<ResourceModel> request) {
        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
    }

    private static Stream<Arguments> provideEndpointTestParameters() {
        final String pub = EndpointType.PUBLIC.toString();
        final String vpc = EndpointType.VPC.toString();
        final List<String> sg1 = ImmutableList.of("sec1", "sec2", "sec3");
        final List<String> sg2 = ImmutableList.of("sg1", "sg2");
        final List<String> none = Collections.emptyList();
        return Stream.of(
                Arguments.of(pub, none, pub, none),
                Arguments.of(pub, none, vpc, sg1),
                Arguments.of(vpc, sg2, pub, none),
                Arguments.of(vpc, none, vpc, none),
                Arguments.of(vpc, none, vpc, sg1),
                Arguments.of(vpc, sg1, vpc, sg2),
                Arguments.of(vpc, sg2, vpc, none));
    }

    @ParameterizedTest
    @MethodSource({"provideEndpointTestParameters"})
    public void handleRequest_VerifySecurityGroupAssignments(
            String prevType, List<String> prevSg, String desiredType, List<String> desiredSg) {
        ResourceHandlerRequest<ResourceModel> request =
                vpcSecurityGroupsChangeRequest(prevType, prevSg, desiredType, desiredSg);

        ProgressEvent<ResourceModel, CallbackContext> response =
                updateServerAndAssertStatus(request, "ONLINE", OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());

        verify(sdkClient, atLeastOnce()).updateServer(any(UpdateServerRequest.class));
    }

    private static ResourceHandlerRequest<ResourceModel> vpcSecurityGroupsChangeRequest(
            String prevType, List<String> prevSg, String desiredType, List<String> desiredSg) {
        ResourceModel previousState = setupSimpleServerModel(prevType);
        setupEndpointDetails(prevSg, previousState);
        ResourceModel desiredState = setupSimpleServerModel(desiredType);
        setupEndpointDetails(desiredSg, desiredState);

        return getResourceHandlerRequestBuilder()
                .previousResourceState(previousState)
                .desiredResourceState(desiredState)
                .build();
    }

    private static void setupEndpointDetails(List<String> sg, ResourceModel model) {
        setServerId(model, "testServerId");
        if (model.getEndpointDetails() != null) {
            model.getEndpointDetails().setSecurityGroupIds(sg);
        }
    }

    @Test
    public void stabilizationFailures() {
        ResourceModel currentState = setupSimpleServerModel(EndpointType.VPC.name());
        setServerId(currentState, "testServerId");

        ResourceModel desiredState = currentState.toBuilder()
                .endpointDetails(EndpointDetails.builder()
                        .addressAllocationIds(Arrays.asList("addr1", "addr2"))
                        .build())
                .build();

        ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequestBuilder()
                .previousResourceState(currentState)
                .desiredResourceState(desiredState)
                .build();

        softly.assertThatThrownBy(() -> updateServerAndAssertStatus(request, "START_FAILED", OperationStatus.FAILED))
                .isInstanceOf(CfnNotStabilizedException.class);

        softly.assertThatThrownBy(() -> updateServerAndAssertStatus(request, "STOP_FAILED", OperationStatus.FAILED))
                .isInstanceOf(CfnNotStabilizedException.class);

        verify(sdkClient, atLeastOnce()).updateServer(any(UpdateServerRequest.class));
        verify(sdkClient, atLeastOnce()).describeServer(any(DescribeServerRequest.class));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateServerAndAssertStatus(
            ResourceHandlerRequest<ResourceModel> request, String postUpdateState, OperationStatus operationStatus) {
        // Create a copy to avoid the update handler mutating the original.
        request = request.toBuilder()
                .previousResourceState(
                        request.getPreviousResourceState().toBuilder().build())
                .desiredResourceState(
                        request.getDesiredResourceState().toBuilder().build())
                .build();

        ResourceModel currentState = request.getPreviousResourceState();
        ResourceModel desiredState = request.getDesiredResourceState();

        setupUpdateServerResponse(null);
        setupVpcEndpointStates(currentState);
        setupVpcEndpointStates(desiredState);

        DescribeServerResponse stabilizeFirstResponse =
                describeServerFromModel(currentState.getServerId(), postUpdateState, currentState);
        DescribeServerResponse finalResponse =
                describeServerFromModel(desiredState.getServerId(), postUpdateState, desiredState);
        doReturn(stabilizeFirstResponse, finalResponse)
                .when(sdkClient)
                .describeServer(any(DescribeServerRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(operationStatus);

        return response;
    }

    @Test
    public void handleRequest_FullServerUpdateTest() {

        ResourceModel model = fullyLoadedServerModel();
        setServerId(model, "testServerId");

        ResourceModel currentState = fullyLoadedServerModel();
        setServerId(currentState, "testServerId");
        currentState.setTags(Collections.singletonList(
                Tag.builder().key("newTag").value("newValue").build())); // trigger tagging
        currentState.getEndpointDetails().setSubnetIds((List<String>) null);
        currentState.getEndpointDetails().setSecurityGroupIds((List<String>) null);
        currentState.getEndpointDetails().setAddressAllocationIds((List<String>) null);

        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequestBuilder()
                .previousResourceState(currentState)
                .desiredResourceState(model)
                .build();

        setupUpdateServerResponse(null);

        // What is happening here?
        // This simulates a multiple step update of the server.
        // 1. We start with the current state that has no allocation ids,
        //    no security groups and different tags.
        // 2. The server is STOPPING.
        // 3. The server is now stopped/offline ready to update.
        // 4. The server is STARTING.
        // 5. The server reached the final state and can be finalized
        //    by applying the new security groups and tags.
        DescribeServerResponse initialState = describeServerFromModel(model.getServerId(), "ONLINE", currentState);
        DescribeServerResponse finalStateResponse = describeServerFromModel(model.getServerId(), "ONLINE", model);

        DescribeServerResponse stoppingServerResponse = newStateResponse(initialState, "STOPPING");
        DescribeServerResponse offlineServerResponse = newStateResponse(initialState, "OFFLINE");

        // Adding the expected subnets
        DescribeServerResponse offlineWithSubnets = DescribeServerResponse.builder()
                .server(offlineServerResponse.server().toBuilder()
                        .endpointDetails(offlineServerResponse.server().endpointDetails().toBuilder()
                                .subnetIds(model.getEndpointDetails().getSubnetIds())
                                .build())
                        .build())
                .build();

        DescribeServerResponse offlineCompleted = newStateResponse(finalStateResponse, "OFFLINE");
        DescribeServerResponse startingServerResponse = newStateResponse(finalStateResponse, "STARTING");

        doReturn(
                        initialState,
                        initialState,
                        stoppingServerResponse,
                        offlineServerResponse,
                        offlineWithSubnets,
                        offlineCompleted,
                        startingServerResponse,
                        finalStateResponse)
                .when(sdkClient)
                .describeServer(any(DescribeServerRequest.class));

        setupVpcEndpointStates(model);
        setupPrivateIpsStates();

        callAndAssertSuccess(request);

        verify(sdkClient, atLeastOnce()).updateServer(any(UpdateServerRequest.class));
    }

    private void callAndAssertSuccess(ResourceHandlerRequest<ResourceModel> request) {
        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();
    }

    private void setupUpdateServerResponse(Exception ex) {
        UpdateServerResponse response =
                UpdateServerResponse.builder().serverId("testServerId").build();

        Stubber stubber;
        if (ex != null) {
            stubber = doThrow(ex).doReturn(response);
        } else {
            stubber = doReturn(response);
        }

        stubber.when(sdkClient).updateServer(any(UpdateServerRequest.class));
    }
}
