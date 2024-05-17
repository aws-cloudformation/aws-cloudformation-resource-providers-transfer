package software.amazon.transfer.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.CreateAgreementRequest;
import software.amazon.awssdk.services.transfer.model.CreateAgreementResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .accessRole(TEST_ACCESS_ROLE)
                .baseDirectory(TEST_BASE_DIRECTORY)
                .description(TEST_DESCRIPTION)
                .localProfileId(TEST_LOCAL_PROFILE)
                .partnerProfileId(TEST_PARTNER_PROFILE)
                .serverId(TEST_SERVER_ID)
                .status(TEST_STATUS)
                .tags(MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        CreateAgreementResponse createAgreementResponse =
                CreateAgreementResponse.builder().agreementId(TEST_AGREEMENT_ID).build();
        doReturn(createAgreementResponse).when(client).createAgreement(any(CreateAgreementRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        ResourceModel testModel = response.getResourceModel();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(testModel).hasFieldOrPropertyWithValue("accessRole", TEST_ACCESS_ROLE);
        assertThat(testModel).hasFieldOrPropertyWithValue("baseDirectory", TEST_BASE_DIRECTORY);
        assertThat(testModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testModel).hasFieldOrPropertyWithValue("localProfileId", TEST_LOCAL_PROFILE);
        assertThat(testModel).hasFieldOrPropertyWithValue("partnerProfileId", TEST_PARTNER_PROFILE);
        assertThat(testModel).hasFieldOrPropertyWithValue("serverId", TEST_SERVER_ID);
        assertThat(testModel).hasFieldOrPropertyWithValue("status", TEST_STATUS);

        ArgumentCaptor<CreateAgreementRequest> requestCaptor = ArgumentCaptor.forClass(CreateAgreementRequest.class);
        verify(client).createAgreement(requestCaptor.capture());
        CreateAgreementRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.tags()).containsExactlyInAnyOrder(SDK_MODEL_TAG, SDK_SYSTEM_TAG);
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).createAgreement(any(CreateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).createAgreement(any(CreateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceExistsExceptionFailed() {
        doThrow(ResourceExistsException.class).when(client).createAgreement(any(CreateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder()
                .agreementId(TEST_AGREEMENT_ID)
                .serverId(TEST_SERVER_ID)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ThrottlingExceptionFailed() {
        doThrow(ThrottlingException.class).when(client).createAgreement(any(CreateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnThrottlingException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).createAgreement(any(CreateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
