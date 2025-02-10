package software.amazon.transfer.webapp;

import static software.amazon.transfer.webapp.translators.Translator.translateFromSdkTags;

import java.util.Base64;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationResponse;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppResponse;
import software.amazon.awssdk.services.transfer.model.DescribedWebApp;
import software.amazon.awssdk.services.transfer.model.DescribedWebAppCustomization;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();

        final ResourceModel model = request.getDesiredResourceState();
        Translator.ensureWebAppIdInModel(model);

        return proxy.initiate("AWS-Transfer-Web-App::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToReadRequest)
                .makeServiceCall(this::readWebApp)
                .handleError((ignored, exception, client, resourceModel, context) ->
                        handleError(READ, exception, resourceModel, context, clientRequestToken))
                .done(awsResponse -> proxy.initiate(
                                "AWS-Transfer-Web-App::ReadCustomization", proxyClient, model, callbackContext)
                        .translateToServiceRequest(this::translateToReadCustomizationRequest)
                        .makeServiceCall(this::readWebAppCustomization)
                        .handleError((ignored, exception, client, resourceModel, context) ->
                                handleError(READ, exception, resourceModel, context, clientRequestToken))
                        .done(customizationResponse -> ProgressEvent.defaultSuccessHandler(
                                translateFromReadResponse(awsResponse, customizationResponse))));
    }

    private ResourceModel translateFromReadResponse(
            final DescribeWebAppResponse webAppResponse,
            final DescribeWebAppCustomizationResponse customizationResponse) {

        DescribedWebApp webApp = webAppResponse.webApp();
        ResourceModel model = new ResourceModel();
        model.setArn(webApp.arn());
        model.setWebAppId(webApp.webAppId());

        IdentityProviderDetails identityProviderDetails = new IdentityProviderDetails();
        identityProviderDetails.setApplicationArn(
                webApp.describedIdentityProviderDetails().identityCenterConfig().applicationArn());
        identityProviderDetails.setInstanceArn(
                webApp.describedIdentityProviderDetails().identityCenterConfig().instanceArn());
        identityProviderDetails.setRole(
                webApp.describedIdentityProviderDetails().identityCenterConfig().role());
        model.setIdentityProviderDetails(identityProviderDetails);

        model.setAccessEndpoint(webApp.accessEndpoint());

        WebAppUnits webAppUnits = new WebAppUnits();
        webAppUnits.setProvisioned(webApp.webAppUnits().provisioned());
        model.setWebAppUnits(webAppUnits);

        model.setTags(translateFromSdkTags(webApp.tags()));

        // Only add customization if response exists
        if (customizationResponse != null && customizationResponse.webAppCustomization() != null) {
            DescribedWebAppCustomization webAppCustomization = customizationResponse.webAppCustomization();
            WebAppCustomization customization = new WebAppCustomization();
            customization.setTitle(webAppCustomization.title());
            customization.setLogoFile(convertToBase64String(webAppCustomization.logoFile()));
            customization.setFaviconFile(convertToBase64String(webAppCustomization.faviconFile()));
            model.setWebAppCustomization(customization);
        }

        return model;
    }

    private DescribeWebAppRequest translateToReadRequest(final ResourceModel model) {
        return DescribeWebAppRequest.builder().webAppId(model.getWebAppId()).build();
    }

    private DescribeWebAppCustomizationRequest translateToReadCustomizationRequest(final ResourceModel model) {
        return DescribeWebAppCustomizationRequest.builder()
                .webAppId(model.getWebAppId())
                .build();
    }

    protected DescribeWebAppResponse readWebApp(DescribeWebAppRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DescribeWebAppResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::describeWebApp);
            log("has been read successfully.", awsRequest.webAppId());
            return awsResponse;
        }
    }

    protected DescribeWebAppCustomizationResponse readWebAppCustomization(
            DescribeWebAppCustomizationRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DescribeWebAppCustomizationResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::describeWebAppCustomization);
            log("has been read successfully.", awsRequest.webAppId());
            return awsResponse;
        } catch (ResourceNotFoundException e) {
            log("No web app customization to add", awsRequest.webAppId());
            return null;
        }
    }

    private String convertToBase64String(SdkBytes bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes.asByteArray());
    }
}
