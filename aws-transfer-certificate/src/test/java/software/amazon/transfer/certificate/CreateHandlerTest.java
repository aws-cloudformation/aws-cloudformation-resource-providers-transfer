package software.amazon.transfer.certificate;

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

import software.amazon.awssdk.services.transfer.model.ImportCertificateRequest;
import software.amazon.awssdk.services.transfer.model.ImportCertificateResponse;
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
                .description(TEST_DESCRIPTION)
                .usage(TEST_USAGE)
                .certificate(TEST_CERTIFICATE)
                .certificateChain(TEST_CERTIFICATE_CHAIN)
                .privateKey(TEST_PRIVATE_KEY)
                .activeDate(TEST_ACTIVE_DATE)
                .inactiveDate(TEST_INACTIVE_DATE)
                .tags(MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        ImportCertificateResponse importCertificateResponse = ImportCertificateResponse.builder()
                .certificateId(TEST_CERTIFICATE_ID)
                .build();
        doReturn(importCertificateResponse).when(client).importCertificate(any(ImportCertificateRequest.class));

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
        assertThat(testModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testModel).hasFieldOrPropertyWithValue("usage", TEST_USAGE);
        assertThat(testModel).hasFieldOrPropertyWithValue("certificate", TEST_CERTIFICATE);
        assertThat(testModel).hasFieldOrPropertyWithValue("certificateChain", TEST_CERTIFICATE_CHAIN);
        assertThat(testModel).hasFieldOrPropertyWithValue("privateKey", TEST_PRIVATE_KEY);
        assertThat(testModel).hasFieldOrPropertyWithValue("activeDate", TEST_ACTIVE_DATE);
        assertThat(testModel).hasFieldOrPropertyWithValue("inactiveDate", TEST_INACTIVE_DATE);

        ArgumentCaptor<ImportCertificateRequest> requestCaptor =
                ArgumentCaptor.forClass(ImportCertificateRequest.class);
        verify(client).importCertificate(requestCaptor.capture());
        ImportCertificateRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.tags()).containsExactlyInAnyOrder(SDK_MODEL_TAG, SDK_SYSTEM_TAG);
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).importCertificate(any(ImportCertificateRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class)
                .when(client)
                .importCertificate(any(ImportCertificateRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceExistsExceptionFailed() {
        doThrow(ResourceExistsException.class).when(client).importCertificate(any(ImportCertificateRequest.class));

        ResourceModel model = ResourceModel.builder().certificateId("testId").build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ThrottlingExceptionFailed() {
        doThrow(ThrottlingException.class).when(client).importCertificate(any(ImportCertificateRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnThrottlingException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).importCertificate(any(ImportCertificateRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
