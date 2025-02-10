package software.amazon.transfer.webapp;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DeleteWebAppRequest;
import software.amazon.awssdk.services.transfer.model.DeleteWebAppResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        final ResourceModel newModel = request.getDesiredResourceState();
        Translator.ensureWebAppIdInModel(newModel);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> proxy.initiate(
                                "AWS-Transfer-Web-App::Delete",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(DeleteHandler::translateToDeleteRequest)
                        .makeServiceCall(this::deleteWebApp)
                        .handleError((ignored, exception, client, model, context) ->
                                handleError(DELETE, exception, model, context, clientRequestToken))
                        .progress())
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private static DeleteWebAppRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteWebAppRequest.builder().webAppId(model.getWebAppId()).build();
    }

    private DeleteWebAppResponse deleteWebApp(DeleteWebAppRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DeleteWebAppResponse response = client.injectCredentialsAndInvokeV2(request, transferClient::deleteWebApp);
            log("successfully deleted.", request.webAppId());
            return response;
        }
    }
}
