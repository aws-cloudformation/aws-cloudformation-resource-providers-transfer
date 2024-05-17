package software.amazon.transfer.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListProfilesRequest;
import software.amazon.awssdk.services.transfer.model.ListProfilesResponse;
import software.amazon.awssdk.services.transfer.model.ListedProfile;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new ListHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        ListedProfile listedProfile = ListedProfile.builder()
                .profileId("testid")
                .arn("testarn")
                .profileType("testprofile")
                .as2Id("testas2id")
                .build();

        final ResourceModel model = ResourceModel.builder().profileId("testid").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListProfilesResponse listProfilesResponse =
                ListProfilesResponse.builder().profiles(listedProfile).build();

        doReturn(listProfilesResponse).when(client).listProfiles(any(ListProfilesRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        List<ResourceModel> testModels = response.getResourceModels();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(testModels).isNotNull();

        ResourceModel testListedModel = testModels.get(0);

        assertThat(testListedModel).hasFieldOrPropertyWithValue("profileId", "testid");
        assertThat(testListedModel).hasFieldOrPropertyWithValue("profileType", "testprofile");
        assertThat(testListedModel).hasFieldOrPropertyWithValue("as2Id", "testas2id");
        assertThat(testListedModel).hasFieldOrPropertyWithValue("arn", "testarn");

        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).listProfiles(any(ListProfilesRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).listProfiles(any(ListProfilesRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).listProfiles(any(ListProfilesRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceNotFoundExceptionFailed() {
        doThrow(ResourceNotFoundException.class).when(client).listProfiles(any(ListProfilesRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> callHandler(request));
    }
}
