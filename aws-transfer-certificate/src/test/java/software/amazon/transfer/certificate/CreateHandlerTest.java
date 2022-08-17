package software.amazon.transfer.certificate;

import software.amazon.awssdk.services.transfer.TransferClient;
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
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static software.amazon.transfer.certificate.AbstractTestBase.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private TransferClient client;

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler(client);

        final ResourceModel model = ResourceModel.builder()
                .description(TEST_DESCRIPTION)
                .usage(TEST_USAGE)
                .certificate(TEST_CERTIFICATE)
                .certificateChain(TEST_CERTIFICATE_CHAIN)
                .privateKey(TEST_PRIVATE_KEY)
                .activeDate(TEST_ACTIVE_DATE)
                .inactiveDate(TEST_INACTIVE_DATE)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        ImportCertificateResponse importCertificateResponse = ImportCertificateResponse.builder()
                .certificateId(TEST_CERTIFICATE_ID)
                .build();
        doReturn(importCertificateResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

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
        assertThat(testModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testModel).hasFieldOrPropertyWithValue("usage", TEST_USAGE);
        assertThat(testModel).hasFieldOrPropertyWithValue("certificate", TEST_CERTIFICATE);
        assertThat(testModel).hasFieldOrPropertyWithValue("certificateChain", TEST_CERTIFICATE_CHAIN);
        assertThat(testModel).hasFieldOrPropertyWithValue("privateKey", TEST_PRIVATE_KEY);
        assertThat(testModel).hasFieldOrPropertyWithValue("activeDate", TEST_ACTIVE_DATE);
        assertThat(testModel).hasFieldOrPropertyWithValue("inactiveDate", TEST_INACTIVE_DATE);
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(InvalidRequestException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(), any());

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
                .injectCredentialsAndInvokeV2(any(ImportCertificateRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_ResourceExistsExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(ResourceExistsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ImportCertificateRequest.class), any());

        ResourceModel model = ResourceModel.builder()
                .certificateId("testId")
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_ThrottlingExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(ThrottlingException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ImportCertificateRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnThrottlingException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        CreateHandler handler = new CreateHandler(client);

        doThrow(TransferException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(any(ImportCertificateRequest.class), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        } );
    }
}
