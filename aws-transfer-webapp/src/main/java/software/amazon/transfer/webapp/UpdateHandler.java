package software.amazon.transfer.webapp;

import static software.amazon.transfer.webapp.translators.TagHelper.setDesiredTags;

import java.util.Base64;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationResponse;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppIdentityCenterConfig;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppIdentityProviderDetails;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppResponse;
import software.amazon.awssdk.services.transfer.model.WebAppUnits;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        final ResourceModel previousModel = request.getPreviousResourceState();
        final ResourceModel newModel = request.getDesiredResourceState();

        if (newModel.getIdentityProviderDetails() == null) {
            newModel.setIdentityProviderDetails(previousModel.getIdentityProviderDetails());
        }
        if (newModel.getAccessEndpoint() == null) {
            newModel.setAccessEndpoint(previousModel.getAccessEndpoint());
        }
        // Preserve WebAppUnits
        if (newModel.getWebAppUnits() == null) {
            newModel.setWebAppUnits(previousModel.getWebAppUnits());
        }
        if (newModel.getWebAppCustomization() == null && previousModel.getWebAppCustomization() != null) {
            newModel.setWebAppCustomization(previousModel.getWebAppCustomization());
        }

        Translator.ensureWebAppIdInModel(newModel);

        setDesiredTags(request, newModel);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> updateWebApp(proxy, proxyClient, clientRequestToken, progress))
                .then(progress -> {
                    // Only update customization if it has exists in the model
                    if (progress.getResourceModel().getWebAppCustomization() != null) {
                        return updateWebAppCustomization(proxy, proxyClient, clientRequestToken, progress);
                    }
                    return progress;
                })
                .then(progress -> addTags(progress, request, newModel, proxy, proxyClient, callbackContext))
                .then(progress -> removeTags(progress, request, newModel, proxy, proxyClient, callbackContext))
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateWebApp(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            ProgressEvent<ResourceModel, CallbackContext> progress) {
        return proxy.initiate(
                        "AWS-Transfer-Web-App::Update",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(this::translateToUpdateRequest)
                .makeServiceCall(this::updateWebApp)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private UpdateWebAppResponse updateWebApp(UpdateWebAppRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            UpdateWebAppResponse response = client.injectCredentialsAndInvokeV2(request, transferClient::updateWebApp);
            log("has successfully been updated.", request.webAppId());
            return response;
        }
    }

    private UpdateWebAppRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateWebAppRequest.builder()
                .webAppId(model.getWebAppId())
                .identityProviderDetails(UpdateWebAppIdentityProviderDetails.builder()
                        .identityCenterConfig(UpdateWebAppIdentityCenterConfig.builder()
                                .role(model.getIdentityProviderDetails().getRole())
                                .build())
                        .build())
                .accessEndpoint(model.getAccessEndpoint())
                .webAppUnits(WebAppUnits.builder()
                        .provisioned(model.getWebAppUnits().getProvisioned())
                        .build())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateWebAppCustomization(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            ProgressEvent<ResourceModel, CallbackContext> progress) {
        return proxy.initiate(
                        "AWS-Transfer-Web-App::UpdateCustomization",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(this::translateToUpdateCustomizationRequest)
                .makeServiceCall(this::updateWebAppCustomization)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private UpdateWebAppCustomizationRequest translateToUpdateCustomizationRequest(final ResourceModel model) {
        return UpdateWebAppCustomizationRequest.builder()
                .webAppId(model.getWebAppId())
                .title(model.getWebAppCustomization().getTitle())
                .logoFile(
                        model.getWebAppCustomization().getLogoFile() != null
                                ? convertFileToSdkBytes(
                                        model.getWebAppCustomization().getLogoFile())
                                : null)
                .faviconFile(
                        model.getWebAppCustomization().getFaviconFile() != null
                                ? convertFileToSdkBytes(
                                        model.getWebAppCustomization().getFaviconFile())
                                : null)
                .build();
    }

    private UpdateWebAppCustomizationResponse updateWebAppCustomization(
            UpdateWebAppCustomizationRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            UpdateWebAppCustomizationResponse response =
                    client.injectCredentialsAndInvokeV2(request, transferClient::updateWebAppCustomization);
            log("customization has successfully been updated.", request.webAppId());
            return response;
        }
    }

    private SdkBytes convertFileToSdkBytes(String fileData) {
        if (fileData == null) {
            return null;
        }
        // Decode Base64 string to byte array, then convert to SdkBytes
        return SdkBytes.fromByteArray(Base64.getDecoder().decode(fileData));
    }
}
