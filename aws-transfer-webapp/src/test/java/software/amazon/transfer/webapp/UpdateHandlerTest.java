package software.amazon.transfer.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Stubber;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppResponse;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationResponse;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    final UpdateHandler handler = new UpdateHandler();

    @Test
    public void handleRequest_SimpleSuccess() {
        ResourceModel model = simpleWebAppModel();

        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(model)
                .desiredResourceState(model)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);
        assertThat(response.getResourceModel()).isEqualTo(model);

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
    }

    @Test
    public void handleRequest_NullIdentityProviderDetails() {
        ResourceModel model = simpleWebAppModel();
        ResourceModel newModel = simpleWebAppModel();
        newModel.setIdentityProviderDetails(null);

        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(model)
                .desiredResourceState(newModel)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);
        assertThat(response.getResourceModel().getIdentityProviderDetails())
                .isEqualTo(model.getIdentityProviderDetails());

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
    }

    @Test
    public void handleRequest_NullWebAppCustomization() {
        ResourceModel model = fullyLoadedWebAppModel();
        model.setWebAppId(TEST_WEB_APP_ID);
        ResourceModel newModel = fullyLoadedWebAppModel();
        newModel.setWebAppId(TEST_WEB_APP_ID);
        newModel.setWebAppCustomization(null);

        final ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(model)
                .desiredResourceState(newModel)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);
        assertThat(response.getResourceModel().getWebAppCustomization()).isEqualTo(model.getWebAppCustomization());

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
    }

    @Test
    public void handleRequest_SimpleUpdate_Tagging_Failed() {
        ResourceModel model = simpleWebAppModel();
        ResourceModel newModel = simpleWebAppModel();
        newModel.setTags(Translator.translateTagMapToTagList(EXTRA_MODEL_TAGS));

        ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(model)
                .desiredResourceState(newModel)
                .desiredResourceTags(EXTRA_MODEL_TAGS)
                .build();

        AwsServiceException ex = AwsServiceException.builder()
                .awsErrorDetails(
                        AwsErrorDetails.builder().errorCode("AccessDenied").build())
                .build();
        doThrow(ex).when(client).tagResource(any(TagResourceRequest.class));

        updateWebAppAndAssertStatus(request, OperationStatus.FAILED);

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
        verify(client, atLeastOnce()).tagResource(any(TagResourceRequest.class));

        request = requestBuilder()
                .previousResourceState(newModel)
                .desiredResourceState(model)
                .previousResourceTags(EXTRA_MODEL_TAGS)
                .build();
        doThrow(ex).when(client).untagResource(any(UntagResourceRequest.class));

        updateWebAppAndAssertStatus(request, OperationStatus.FAILED);

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
        verify(client, atLeastOnce()).untagResource(any(UntagResourceRequest.class));
    }

    @Test
    public void handleRequest_VerifyAddingAndRemovingTags() {
        Tag tag1 = Tag.builder().key("key1").value("value1").build();
        Tag tag2 = Tag.builder().key("key2").value("value2").build();
        Tag tag3 = Tag.builder().key("key3").value("value3").build();

        ResourceModel currentModel = simpleWebAppModel();
        ResourceModel desiredModel = simpleWebAppModel();

        desiredModel.setTags(List.of(tag1, tag2, tag3));

        ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);

        assertThat(response.getResourceModel()).isEqualTo(desiredModel);

        currentModel.setTags(List.of(tag1, tag2, tag3));
        desiredModel.setTags(List.of());

        request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        response = updateWebAppAndAssertSuccess(request);

        currentModel.setTags(List.of(tag1, tag3));
        desiredModel.setTags(List.of(tag1, tag2, tag3));

        request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        response = updateWebAppAndAssertSuccess(request);

        currentModel.setTags(List.of(tag1, tag2, tag3));
        desiredModel.setTags(List.of(tag1, tag3));

        request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        response = updateWebAppAndAssertSuccess(request);

        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        verify(client, times(4)).updateWebApp(any(UpdateWebAppRequest.class));
    }

    @Test
    public void handleRequest_VerifyChangingWebAppUnits() {
        ResourceModel currentModel = simpleWebAppModel();
        ResourceModel desiredModel = simpleWebAppModel();

        desiredModel.setWebAppUnits(WebAppUnits.builder().provisioned(3).build());

        ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);

        assertThat(response.getResourceModel()).isEqualTo(desiredModel);

        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
    }

    @Test
    public void handleRequest_VerifyChangingWebAppCustomization() {
        ResourceModel currentModel = simpleWebAppModel();
        ResourceModel desiredModel = simpleWebAppModel();

        desiredModel.setWebAppCustomization(WebAppCustomization.builder()
                .title("new title")
                .logoFile(loadTestFile("logo.jpeg"))
                .faviconFile(null)
                .build());

        ResourceHandlerRequest<ResourceModel> request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = updateWebAppAndAssertSuccess(request);

        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
        verify(client, atLeastOnce()).updateWebAppCustomization(any(UpdateWebAppCustomizationRequest.class));

        desiredModel.setWebAppCustomization(WebAppCustomization.builder()
                .title("new title")
                .logoFile(null)
                .faviconFile(loadTestFile("favicon.jpeg"))
                .build());

        request = requestBuilder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        response = updateWebAppAndAssertSuccess(request);

        assertThat(response.getResourceModel()).isEqualTo(desiredModel);
        verify(client, atLeastOnce()).updateWebApp(any(UpdateWebAppRequest.class));
        verify(client, atLeastOnce()).updateWebAppCustomization(any(UpdateWebAppCustomizationRequest.class));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateWebAppAndAssertSuccess(
            ResourceHandlerRequest<ResourceModel> request) {
        return updateWebAppAndAssertStatus(request, OperationStatus.SUCCESS);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateWebAppAndAssertStatus(
            ResourceHandlerRequest<ResourceModel> request, OperationStatus status) {
        // Create a copy to avoid the update handler mutating the original.
        request = request.toBuilder()
                .previousResourceState(
                        request.getPreviousResourceState().toBuilder().build())
                .desiredResourceState(
                        request.getDesiredResourceState().toBuilder().build())
                .build();

        ResourceModel currentModel = request.getPreviousResourceState();
        ResourceModel desiredModel = request.getDesiredResourceState();

        setupUpdateWebAppResponse(null);

        if (status == OperationStatus.SUCCESS) {
            DescribeWebAppResponse describeResponse;
            if (desiredModel.getIdentityProviderDetails() == null) {
                describeResponse = describeWebAppResponseFromModel(currentModel);
            } else {
                describeResponse = describeWebAppResponseFromModel(desiredModel);
            }
            doReturn(describeResponse).when(client).describeWebApp(any(DescribeWebAppRequest.class));
        }

        // Only setup customization response if the model has customization
        if (desiredModel.getWebAppCustomization() != null) {
            setupUpdateWebAppCustomizationResponse(null);

            doReturn(describeWebAppCustomizationResponseFromModel(desiredModel))
                    .when(client)
                    .describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));
        } else if (currentModel.getWebAppCustomization() != null) {
            setupUpdateWebAppCustomizationResponse(null);

            doReturn(describeWebAppCustomizationResponseFromModel(currentModel))
                    .when(client)
                    .describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));
        }

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(status);

        return response;
    }

    private void setupUpdateWebAppResponse(Exception ex) {
        UpdateWebAppResponse response =
                UpdateWebAppResponse.builder().webAppId(TEST_WEB_APP_ID).build();

        Stubber stubber;
        if (ex != null) {
            stubber = doThrow(ex).doReturn(response);
        } else {
            stubber = doReturn(response);
        }

        stubber.when(client).updateWebApp(any(UpdateWebAppRequest.class));
    }

    private void setupUpdateWebAppCustomizationResponse(Exception ex) {
        UpdateWebAppCustomizationResponse response = UpdateWebAppCustomizationResponse.builder()
                .webAppId(TEST_WEB_APP_ID)
                .build();

        Stubber stubber;
        if (ex != null) {
            stubber = doThrow(ex).doReturn(response);
        } else {
            stubber = doReturn(response);
        }

        stubber.when(client).updateWebAppCustomization(any(UpdateWebAppCustomizationRequest.class));
    }
}
