package software.amazon.transfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.user.BaseHandlerStd.THROTTLE_CALLBACK_DELAY_SECONDS;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.transfer.model.DescribeUserRequest;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions private SoftAssertions softly;

    private final ReadHandler handler = new ReadHandler();

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = simpleUserModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(model);
        doThrow(ThrottlingException.builder().build())
                .doReturn(describeUserResponse)
                .when(sdkClient)
                .describeUser(any(DescribeUserRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        softly.assertThat(response.getCallbackDelaySeconds())
                .isEqualTo(THROTTLE_CALLBACK_DELAY_SECONDS);

        // Call again in response to ThrottleException
        response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).describeUser(any(DescribeUserRequest.class));
    }
}
