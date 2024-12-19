package software.amazon.transfer.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static software.amazon.transfer.agreement.AbstractTestBase.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UpdateAgreementRequest;
import software.amazon.awssdk.services.transfer.model.UpdateAgreementResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    private MockableBaseHandler<CallbackContext> handler;

    @Override
    MockableBaseHandler<CallbackContext> getHandler() {
        return handler;
    }

    @BeforeEach
    public void setupTestData() {
        handler = new UpdateHandler();
    }

    @ParameterizedTest
    @MethodSource("provideDirectoryOptions")
    public void handleRequest_SimpleSuccess(String baseDirectory, CustomDirectories customDirectories) {
        ResourceModel model = ResourceModel.builder()
                .serverId(TEST_SERVER_ID)
                .agreementId(TEST_AGREEMENT_ID)
                .baseDirectory(baseDirectory)
                .description(TEST_DESCRIPTION_2)
                .customDirectories(customDirectories)
                .build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        UpdateAgreementResponse updateAgreementRequest =
                UpdateAgreementResponse.builder().agreementId(TEST_AGREEMENT_ID).build();
        doReturn(updateAgreementRequest).when(client).updateAgreement(any(UpdateAgreementRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        ResourceModel testModel = response.getResourceModel();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(testModel).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(testModel).hasFieldOrPropertyWithValue("baseDirectory", baseDirectory);
        assertThat(testModel).hasFieldOrPropertyWithValue("description", TEST_DESCRIPTION_2);
        assertThat(testModel).hasFieldOrPropertyWithValue("customDirectories", customDirectories);
        verify(client, times(1)).updateAgreement(any(UpdateAgreementRequest.class));
    }

    private static Stream<Arguments> provideDirectoryOptions() {
        return Stream.of(
                Arguments.of(TEST_BASE_DIRECTORY_2, null),
                Arguments.of(null, Converter.CustomDirectoriesConverter.fromSdk(getCustomDirectories())));
    }

    @Test
    public void handleRequest_AddTagInvoked() {
        Set<Tag> desiredTags = TEST_TAG_MAP.entrySet().stream()
                .map(tag ->
                        Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toSet());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getTags()).isEqualTo(desiredTags);
        verify(client, times(1)).tagResource(any(TagResourceRequest.class));
        verify(client, times(1)).updateAgreement(any(UpdateAgreementRequest.class));
    }

    @Test
    public void handleRequest_RemoveTagInvoked() {
        Set<Tag> systemTags = SYSTEM_TAG_MAP.entrySet().stream()
                .map(tag ->
                        Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toSet());

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = callHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getTags()).isEqualTo(systemTags);
        verify(client, times(1)).untagResource(any(UntagResourceRequest.class));
        verify(client, times(1)).updateAgreement(any(UpdateAgreementRequest.class));
    }

    @Test
    public void handleRequest_InvalidRequestExceptionFailed() {
        doThrow(InvalidRequestException.class).when(client).updateAgreement(any(UpdateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_InternalServiceErrorExceptionFailed() {
        doThrow(InternalServiceErrorException.class).when(client).updateAgreement(any(UpdateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        assertThrows(CfnServiceInternalErrorException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_ResourceNotFoundExceptionFailed() {
        doThrow(ResourceNotFoundException.class).when(client).updateAgreement(any(UpdateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        assertThrows(CfnNotFoundException.class, () -> callHandler(request));
    }

    @Test
    public void handleRequest_TransferExceptionFailed() {
        doThrow(TransferException.class).when(client).updateAgreement(any(UpdateAgreementRequest.class));

        ResourceModel model = ResourceModel.builder().build();

        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(RESOURCE_TAG_MAP)
                .systemTags(SYSTEM_TAG_MAP)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> callHandler(request));
    }
}
