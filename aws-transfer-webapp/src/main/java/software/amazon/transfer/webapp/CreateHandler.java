package software.amazon.transfer.webapp;

import static software.amazon.transfer.webapp.translators.TagHelper.setDesiredTags;

import java.util.Base64;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateWebAppRequest;
import software.amazon.awssdk.services.transfer.model.CreateWebAppResponse;
import software.amazon.awssdk.services.transfer.model.IdentityCenterConfig;
import software.amazon.awssdk.services.transfer.model.Tag;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationResponse;
import software.amazon.awssdk.services.transfer.model.WebAppIdentityProviderDetails;
import software.amazon.awssdk.services.transfer.model.WebAppUnits;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.WebAppArn;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        ResourceModel newModel = request.getDesiredResourceState();

        setDesiredTags(request, newModel);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> proxy.initiate(
                                "AwS-Transfer-Web-App::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(this::translateToCreateRequest)
                        .makeServiceCall(this::createWebApp)
                        .stabilize((ignored, response, client, model, context) ->
                                stabilizeAfterCreate(request, response, client, model, context))
                        .handleError((ignored, exception, client, model, context) ->
                                handleError(CREATE, exception, model, context, clientRequestToken))
                        .progress())
                .then(progress -> {
                    // Only proceed with customization update if WebAppCustomization exists
                    if (progress.getResourceModel().getWebAppCustomization() != null) {
                        return proxy.initiate(
                                        "AWS-Transfer-Web-App::UpdateCustomization",
                                        proxyClient,
                                        progress.getResourceModel(),
                                        progress.getCallbackContext())
                                .translateToServiceRequest(this::translateToUpdateRequest)
                                .makeServiceCall(this::updateWebAppCustomization)
                                .handleError((ignored, exception, client, model, context) ->
                                        handleError(UPDATE, exception, model, context, clientRequestToken))
                                .progress();
                    }
                    return progress;
                })
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateWebAppRequest translateToCreateRequest(final ResourceModel model) {
        return CreateWebAppRequest.builder()
                .accessEndpoint(model.getAccessEndpoint())
                .identityProviderDetails(WebAppIdentityProviderDetails.builder()
                        .identityCenterConfig(IdentityCenterConfig.builder()
                                .instanceArn(model.getIdentityProviderDetails().getInstanceArn())
                                .role(model.getIdentityProviderDetails().getRole())
                                .build())
                        .build())
                .webAppUnits(WebAppUnits.builder()
                        .provisioned(
                                model.getWebAppUnits() != null
                                        ? model.getWebAppUnits().getProvisioned()
                                        : 1)
                        .build())
                .tags(
                        model.getTags() == null
                                ? null
                                : model.getTags().stream()
                                        .map(tag -> Tag.builder()
                                                .key(tag.getKey())
                                                .value(tag.getValue())
                                                .build())
                                        .collect(Collectors.toList()))
                .build();
    }

    private CreateWebAppResponse createWebApp(CreateWebAppRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            CreateWebAppResponse response =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::createWebApp);
            log("web app created successfully", response.webAppId());
            return response;
        }
    }

    private boolean stabilizeAfterCreate(
            ResourceHandlerRequest<ResourceModel> request,
            CreateWebAppResponse awsResponse,
            ProxyClient<TransferClient> ignored1,
            ResourceModel model,
            CallbackContext ignored2) {
        String webAppId = awsResponse.webAppId();
        Region region = RegionUtils.getRegion(request.getRegion());
        WebAppArn webAppArn = new WebAppArn(region, request.getAwsAccountId(), webAppId);
        model.setArn(webAppArn.getArn());
        model.setWebAppId(webAppId);

        return true;
    }

    private UpdateWebAppCustomizationRequest translateToUpdateRequest(final ResourceModel model) {
        if (StringUtils.isBlank(model.getWebAppId())) {
            WebAppArn webappArn = WebAppArn.fromString(model.getArn());
            model.setWebAppId(webappArn.getWebAppId());
        }

        return UpdateWebAppCustomizationRequest.builder()
                .webAppId(model.getWebAppId())
                .title(model.getWebAppCustomization().getTitle())
                .logoFile(convertFileToSdkBytes(model.getWebAppCustomization().getLogoFile()))
                .faviconFile(
                        convertFileToSdkBytes(model.getWebAppCustomization().getFaviconFile()))
                .build();
    }

    private UpdateWebAppCustomizationResponse updateWebAppCustomization(
            UpdateWebAppCustomizationRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            UpdateWebAppCustomizationResponse response =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::updateWebAppCustomization);
            log("web app customization updated successfully", response.webAppId());
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
