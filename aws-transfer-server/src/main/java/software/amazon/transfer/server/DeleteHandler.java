package software.amazon.transfer.server;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DeleteServerRequest;
import software.amazon.awssdk.services.transfer.model.DeleteServerResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.Translator;

public class DeleteHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        Translator.ensureServerIdInModel(request.getDesiredResourceState());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(
                        progress ->
                                proxy.initiate(
                                                "AWS-Transfer-Server::Delete",
                                                proxyClient,
                                                progress.getResourceModel(),
                                                progress.getCallbackContext())
                                        .translateToServiceRequest(
                                                DeleteHandler::translateToDeleteRequest)
                                        .makeServiceCall(this::deleteServer)
                                        .handleError(
                                                (ignored,
                                                        exception,
                                                        proxyClient1,
                                                        model,
                                                        callbackContext1) ->
                                                        handleError(
                                                                DELETE,
                                                                exception,
                                                                model,
                                                                callbackContext1,
                                                                clientRequestToken))
                                        .progress())
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private static DeleteServerRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteServerRequest.builder().serverId(model.getServerId()).build();
    }

    private DeleteServerResponse deleteServer(
            DeleteServerRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DeleteServerResponse response =
                    client.injectCredentialsAndInvokeV2(request, transferClient::deleteServer);
            log("has been successfully deleted.", request.serverId());
            return response;
        }
    }
}
