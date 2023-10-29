package software.amazon.transfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.user.BaseHandlerStd.THROTTLE_CALLBACK_DELAY_SECONDS;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.transfer.model.DeleteUserRequest;
import software.amazon.awssdk.services.transfer.model.DeleteUserResponse;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.UserArn;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions private SoftAssertions softly;

    private final DeleteHandler handler = new DeleteHandler();

    @Test
    public void handleRequest_SimpleSuccess() {

        Region region = Region.getRegion(Regions.US_EAST_1);
        final UserArn userArn = new UserArn(region, "123456789012", "testServerId", "testUserName");
        final ResourceModel model = ResourceModel.builder().arn(userArn.getArn()).build();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        DeleteUserResponse deleteUserResponse = DeleteUserResponse.builder().build();
        // Cover error paths
        doThrow(ThrottlingException.builder().build())
                .doReturn(deleteUserResponse)
                .when(sdkClient)
                .deleteUser(any(DeleteUserRequest.class));

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
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).deleteUser(any(DeleteUserRequest.class));
    }
}
