package software.amazon.transfer.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static software.amazon.transfer.certificate.AbstractTestBase.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListCertificatesResponse;
import software.amazon.awssdk.services.transfer.model.ListedCertificate;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private TransferClient client;

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler(client);

        ListedCertificate listedCertificate = ListedCertificate.builder()
                .description(TEST_DESCRIPTION)
                .arn(TEST_ARN)
                .certificateId(TEST_CERTIFICATE_ID)
                .build();

        final ResourceModel model =
                ResourceModel.builder().certificateId(TEST_CERTIFICATE_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListCertificatesResponse listCertificatesResponse = ListCertificatesResponse.builder()
                .certificates(listedCertificate)
                .build();
        doReturn(listCertificatesResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        List<ResourceModel> testModels = response.getResourceModels();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();

        ResourceModel testListedModel = testModels.get(0);

        assertThat(testListedModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("arn", TEST_ARN);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("certificateId", TEST_CERTIFICATE_ID);

        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(InvalidRequestException.class).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(InternalServiceErrorException.class).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        ListHandler handler = new ListHandler(client);

        doThrow(TransferException.class).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
