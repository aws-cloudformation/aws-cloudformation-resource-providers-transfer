package software.amazon.transfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.user.BaseHandlerStd.THROTTLE_CALLBACK_DELAY_SECONDS;
import static software.amazon.transfer.user.translators.Translator.generateUserArn;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.transfer.model.CreateUserRequest;
import software.amazon.awssdk.services.transfer.model.CreateUserResponse;
import software.amazon.awssdk.services.transfer.model.DescribeUserRequest;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions private SoftAssertions softly;

    private final CreateHandler handler = new CreateHandler();

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = simpleUserModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        model.setArn(generateUserArn(request));
        setupCreateUserResponse();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(model);
        doReturn(describeUserResponse).when(sdkClient).describeUser(any(DescribeUserRequest.class));

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
        softly.assertThat(response.getResourceModel()).isEqualTo(model);
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).createUser(any(CreateUserRequest.class));
        verify(sdkClient, atLeastOnce()).describeUser(any(DescribeUserRequest.class));
    }

    @Test
    public void fullyLoadedUserTest() {
        final ResourceModel model = fullyLoadedUserModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder()
                        .desiredResourceState(model)
                        .desiredResourceTags(RESOURCE_TAG_MAP)
                        .systemTags(SYSTEM_TAG_MAP)
                        .build();

        model.setArn(generateUserArn(request));
        setupCreateUserResponse();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(model);
        doReturn(describeUserResponse).when(sdkClient).describeUser(any(DescribeUserRequest.class));

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
        softly.assertThat(response.getResourceModel()).isEqualTo(model);
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).createUser(any(CreateUserRequest.class));
        verify(sdkClient, atLeastOnce()).describeUser(any(DescribeUserRequest.class));
    }

    private void setupCreateUserResponse() {
        CreateUserResponse createUserResponse =
                CreateUserResponse.builder().serverId("testServerId").userName("userName").build();

        // Add some error coverage
        doThrow(ThrottlingException.builder().build())
                .doReturn(createUserResponse)
                .when(sdkClient)
                .createUser(any(CreateUserRequest.class));
    }
}
