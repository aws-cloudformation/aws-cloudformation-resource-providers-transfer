package software.amazon.transfer.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.DescribeProfileRequest;
import software.amazon.awssdk.services.transfer.model.DescribeProfileResponse;
import software.amazon.awssdk.services.transfer.model.DescribedProfile;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder().profileId("testId").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        DescribeProfileResponse describeProfileResponse = DescribeProfileResponse.builder()
                .profile(DescribedProfile.builder()
                        .profileId("testId")
                        .arn("testArn")
                        .build())
                .build();

        doReturn(describeProfileResponse).when(client).describeProfile(any(DescribeProfileRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);
        ResourceModel testModel = response.getResourceModel();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).hasFieldOrPropertyWithValue("profileId", "testId");
        assertThat(testModel).hasFieldOrPropertyWithValue("arn", "testArn");
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).describeProfile(any(DescribeProfileRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).describeProfile(any(DescribeProfileRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceNotFoundExceptionFailed() {
        doThrow(ResourceNotFoundException.class).when(client).describeProfile(any(DescribeProfileRequest.class));

        ResourceModel model = ResourceModel.builder().profileId("testId").build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).describeProfile(any(DescribeProfileRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
