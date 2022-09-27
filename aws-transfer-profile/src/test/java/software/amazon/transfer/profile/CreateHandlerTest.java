package software.amazon.transfer.profile;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateProfileRequest;
import software.amazon.awssdk.services.transfer.model.CreateProfileResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static software.amazon.transfer.profile.AbstractTestBase.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private AmazonWebServicesClientProxy proxy;
    private Logger logger;
    private TransferClient client;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        client = mock(TransferClient.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler(client);

        final ResourceModel model = ResourceModel.builder()
                .as2Id("testid")
                .profileType("PARTNER")
                .tags(MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .desiredResourceTags(RESOURCE_TAG_MAP)
            .systemTags(SYSTEM_TAG_MAP)
            .build();

        CreateProfileResponse createProfileResponse = CreateProfileResponse.builder().profileId("p-123456").build();

        doReturn(createProfileResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        ResourceModel testModel = response.getResourceModel();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(testModel).hasFieldOrPropertyWithValue("as2Id", "testid");
        assertThat(testModel).hasFieldOrPropertyWithValue("profileType", "PARTNER");
        assertThat(testModel).hasFieldOrPropertyWithValue("profileId", "p-123456");

        ArgumentCaptor<CreateProfileRequest> requestCaptor = ArgumentCaptor.forClass(CreateProfileRequest.class);
        verify(proxy).injectCredentialsAndInvokeV2(requestCaptor.capture(), any());
        CreateProfileRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.tags()).containsExactlyInAnyOrder(SDK_MODEL_TAG, SDK_SYSTEM_TAG); 
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(InvalidRequestException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateProfileRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(InternalServiceErrorException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateProfileRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(TransferException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateProfileRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_ResourceNotFoundExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(ResourceNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(CreateProfileRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }
}
