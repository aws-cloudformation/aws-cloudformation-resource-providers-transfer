package software.amazon.transfer.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static software.amazon.transfer.agreement.AbstractTestBase.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.CustomDirectoriesType;
import software.amazon.awssdk.services.transfer.model.DescribeAgreementRequest;
import software.amazon.awssdk.services.transfer.model.DescribeAgreementResponse;
import software.amazon.awssdk.services.transfer.model.DescribedAgreement;
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

    @ParameterizedTest
    @MethodSource("provideDirectoryOptions")
    public void handleRequest_SimpleSuccess(String baseDirectory, CustomDirectoriesType customDirectoriesType) {
        ResourceModel model = ResourceModel.builder()
                .serverId(TEST_SERVER_ID)
                .agreementId(TEST_AGREEMENT_ID)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        DescribeAgreementResponse describeAgreementResponse = DescribeAgreementResponse.builder()
                .agreement(DescribedAgreement.builder()
                        .baseDirectory(baseDirectory)
                        .description(TEST_DESCRIPTION)
                        .status(TEST_STATUS)
                        .enforceMessageSigning(TEST_ENFORCE_MESSAGE_SIGNING)
                        .preserveFilename(TEST_PRESERVE_FILENAME)
                        .customDirectories(customDirectoriesType)
                        .build())
                .build();
        doReturn(describeAgreementResponse).when(client).describeAgreement(any(DescribeAgreementRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);
        ResourceModel testModel = response.getResourceModel();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).hasFieldOrPropertyWithValue("baseDirectory", baseDirectory);
        assertThat(testModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION);
        assertThat(testModel).hasFieldOrPropertyWithValue("status", TEST_STATUS);
        assertThat(testModel).hasFieldOrPropertyWithValue("enforceMessageSigning", TEST_ENFORCE_MESSAGE_SIGNING);
        assertThat(testModel).hasFieldOrPropertyWithValue("preserveFilename", TEST_PRESERVE_FILENAME);
        assertThat(testModel)
                .hasFieldOrPropertyWithValue(
                        "customDirectories", Converter.CustomDirectoriesConverter.fromSdk(customDirectoriesType));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    private static Stream<Arguments> provideDirectoryOptions() {
        return Stream.of(Arguments.of(TEST_BASE_DIRECTORY, null), Arguments.of(null, getCustomDirectories()));
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).describeAgreement(any(DescribeAgreementRequest.class));

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
                .describeAgreement(any(DescribeAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceNotFoundExceptionFailed() {
        doThrow(ResourceNotFoundException.class).when(client).describeAgreement(any(DescribeAgreementRequest.class));

        ResourceModel model = ResourceModel.builder()
                .agreementId(TEST_AGREEMENT_ID)
                .serverId(TEST_SERVER_ID)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).describeAgreement(any(DescribeAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
