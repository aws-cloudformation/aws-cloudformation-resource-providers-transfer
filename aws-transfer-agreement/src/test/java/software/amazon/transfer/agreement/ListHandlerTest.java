package software.amazon.transfer.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static software.amazon.transfer.agreement.AbstractTestBase.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListAgreementsRequest;
import software.amazon.awssdk.services.transfer.model.ListAgreementsResponse;
import software.amazon.awssdk.services.transfer.model.ListedAgreement;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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
        ListedAgreement listedAgreement = ListedAgreement.builder()
                .arn(TEST_ARN)
                .agreementId(TEST_AGREEMENT_ID)
                .description(TEST_DESCRIPTION)
                .localProfileId(TEST_LOCAL_PROFILE)
                .partnerProfileId(TEST_PARTNER_PROFILE)
                .serverId(TEST_SERVER_ID)
                .status(TEST_STATUS)
                .build();

        ResourceModel model =
                ResourceModel.builder().agreementId(TEST_AGREEMENT_ID).build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        ListAgreementsResponse listAgreementsResponse =
                ListAgreementsResponse.builder().agreements(listedAgreement).build();
        doReturn(listAgreementsResponse).when(client).listAgreements(any(ListAgreementsRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        List<ResourceModel> testModels = response.getResourceModels();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();

        ResourceModel testListedModel = testModels.get(0);

        assertThat(testListedModel).hasFieldOrPropertyWithValue("arn", TEST_ARN);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("agreementId", TEST_AGREEMENT_ID);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("localProfileId", TEST_LOCAL_PROFILE);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("partnerProfileId", TEST_PARTNER_PROFILE);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("serverId", TEST_SERVER_ID);
        assertThat(testListedModel).hasFieldOrPropertyWithValue("status", TEST_STATUS);

        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).listAgreements(any(ListAgreementsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).listAgreements(any(ListAgreementsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).listAgreements(any(ListAgreementsRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
