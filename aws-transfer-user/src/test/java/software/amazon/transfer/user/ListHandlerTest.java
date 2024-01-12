package software.amazon.transfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static software.amazon.transfer.user.BaseHandlerStd.THROTTLE_CALLBACK_DELAY_SECONDS;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.HomeDirectoryType;
import software.amazon.awssdk.services.transfer.model.ListUsersRequest;
import software.amazon.awssdk.services.transfer.model.ListUsersResponse;
import software.amazon.awssdk.services.transfer.model.ListedUser;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final ListHandler handler = new ListHandler();

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = simpleUserModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        ListUsersResponse listUsersResponse = ListUsersResponse.builder()
                .serverId(model.getServerId())
                .users(ListedUser.builder()
                        .arn(model.getArn())
                        .userName(model.getUserName())
                        .homeDirectoryType(HomeDirectoryType.PATH)
                        .homeDirectory("/")
                        .sshPublicKeyCount(1)
                        .role("role")
                        .build())
                .build();
        doThrow(ThrottlingException.builder().build())
                .doReturn(listUsersResponse)
                .when(sdkClient)
                .listUsers(any(ListUsersRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(THROTTLE_CALLBACK_DELAY_SECONDS);

        // Call again in response to ThrottleException
        response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModels()).size().isEqualTo(1);
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackContext()).isNull();
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        ResourceModel result = response.getResourceModels().get(0);
        softly.assertThat(result.getArn()).isEqualTo(model.getArn());
        softly.assertThat(result.getServerId()).isEqualTo(model.getServerId());
        softly.assertThat(result.getUserName()).isEqualTo(model.getUserName());
    }
}
