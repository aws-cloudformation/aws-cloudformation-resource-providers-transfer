package software.amazon.transfer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.user.BaseHandlerStd.THROTTLE_CALLBACK_DELAY_SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.DescribeUserRequest;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.TagResourceResponse;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UntagResourceResponse;
import software.amazon.awssdk.services.transfer.model.UpdateUserRequest;
import software.amazon.awssdk.services.transfer.model.UpdateUserResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    final UpdateHandler handler = new UpdateHandler();

    @Test
    public void handleRequest_SimpleSuccess() {
        ResourceModel model = simpleUserModel();
        updateAndAssertSuccess(model, model);
    }

    @Test
    public void handlerRequest_VerifyAddingAndRemovingKeys() {
        String key1 = "ssh-rsa abcd";
        String key2 = "ssh-rsa efgh";
        String key3 = "ssh-rsa ijklm";

        ResourceModel current = simpleUserModel();
        current.setSshPublicKeys(Collections.singletonList(key1));

        ResourceModel desired = simpleUserModel();
        desired.setSshPublicKeys(Arrays.asList(key1, key2, key3));

        updateAndAssertSuccess(current, desired);

        current.setSshPublicKeys(Arrays.asList(key1, key2, key3));
        desired.setSshPublicKeys(Collections.singletonList(key1));

        updateAndAssertSuccess(current, desired);
    }

    @Test
    public void handleRequest_VerifyAddingAndRemovingTags() {
        Tag tag1 = Tag.builder().key("key1").value("value1").build();
        Tag tag2 = Tag.builder().key("key2").value("value2").build();
        Tag tag3 = Tag.builder().key("key3").value("value3").build();

        ResourceModel current = simpleUserModel();
        ResourceModel desired = simpleUserModel();

        // No changes
        updateAndAssertSuccess(current, desired);

        desired.setTags(Arrays.asList(tag1, tag2, tag3));

        // adding three
        updateAndAssertSuccess(current, desired);

        current.setTags(Arrays.asList(tag1, tag2, tag3));
        desired.setTags(null);

        // removing three
        updateAndAssertSuccess(current, desired);

        current.setTags(Arrays.asList(tag1, tag3));
        desired.setTags(Arrays.asList(tag1, tag2, tag3));

        // adding one
        updateAndAssertSuccess(current, desired);

        current.setTags(Arrays.asList(tag1, tag2, tag3));
        desired.setTags(Arrays.asList(tag1, tag3));

        // removing one
        updateAndAssertSuccess(current, desired);
    }

    private void updateAndAssertSuccess(ResourceModel current, ResourceModel desired) {
        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(current)
                .desiredResourceState(desired)
                .build();

        setupUserUpdateResponse();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(desired);
        doReturn(describeUserResponse).when(sdkClient).describeUser(any(DescribeUserRequest.class));

        callAndAssertInProgress(request);
        callAndAssertSuccess(request);

        verify(sdkClient, atLeastOnce()).updateUser(any(UpdateUserRequest.class));
        verify(sdkClient, atLeastOnce()).describeUser(any(DescribeUserRequest.class));
    }

    @Test
    public void handleRequest_ThrottlingHandling() {
        Exception ex = ThrottlingException.builder().build();

        final ResourceModel currentState = simpleUserModel();
        final ResourceModel desiredState = simpleUserModel();
        desiredState.setTags(TEST_TAG_MAP.entrySet().stream()
                .map(entry -> Tag.builder()
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList()));

        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(currentState)
                .desiredResourceState(desiredState)
                .previousResourceTags(RESOURCE_TAG_MAP)
                .desiredResourceTags(TEST_TAG_MAP)
                .build();

        setupUserUpdateResponse();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(desiredState);
        doReturn(describeUserResponse).when(sdkClient).describeUser(any(DescribeUserRequest.class));

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

        verify(sdkClient, atLeastOnce()).updateUser(any(UpdateUserRequest.class));
        verify(sdkClient, atLeastOnce()).describeUser(any(DescribeUserRequest.class));
    }

    private void callAndAssertSuccess(ResourceHandlerRequest<ResourceModel> request) {
        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();
    }

    private void callAndAssertInProgress(ResourceHandlerRequest<ResourceModel> request) {
        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(THROTTLE_CALLBACK_DELAY_SECONDS);
    }

    @Test
    public void fullyLoadedUserUpdate() {
        ResourceModel currentState = simpleUserModel();
        currentState.setTags(Collections.singletonList(
                Tag.builder().key("someTagToRemove").value("someValue").build()));
        ResourceModel desiredState = fullyLoadedUserModel();
        desiredState.setTags(Collections.singletonList(
                Tag.builder().key("someTagToAdd").value("someValue").build()));

        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(currentState)
                .desiredResourceState(desiredState)
                .previousResourceTags(RESOURCE_TAG_MAP)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .previousSystemTags(SYSTEM_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        setupUserUpdateResponse();

        DescribeUserResponse describeUserResponse = describeUserResponseFromModel(desiredState);
        doReturn(describeUserResponse).when(sdkClient).describeUser(any(DescribeUserRequest.class));

        callAndAssertInProgress(request);
        ProgressEvent<ResourceModel, CallbackContext> response;

        // Call again in response to ThrottleException
        response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    private void setupUserUpdateResponse() {
        UpdateUserResponse updateUserResponse = UpdateUserResponse.builder()
                .serverId("testServerId")
                .userName("testUserId")
                .build();

        // Add some error coverage
        doThrow(ThrottlingException.builder().build())
                .doReturn(updateUserResponse)
                .when(sdkClient)
                .updateUser(any(UpdateUserRequest.class));
    }
}
