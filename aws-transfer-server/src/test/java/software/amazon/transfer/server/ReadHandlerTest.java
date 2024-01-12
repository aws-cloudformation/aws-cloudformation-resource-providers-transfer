package software.amazon.transfer.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.State;
import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.DescribeServerResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        ResourceModel model = fullyLoadedServerModel();
        model.setArn(getTestServerArn("testServer"));
        model.setServerId("testServer");

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        DescribeServerResponse describeServerResponse = describeServerFromModel("testServer", "OFFLINE", model);

        doReturn(describeServerResponse).when(sdkClient).describeServer(any(DescribeServerRequest.class));

        DescribeVpcEndpointsResponse describeVpcEndpointsResponse = vpcEndpointResponse(model, State.AVAILABLE);

        doReturn(describeVpcEndpointsResponse)
                .when(sdkEc2Client)
                .describeVpcEndpoints(any(DescribeVpcEndpointsRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();
    }
}
