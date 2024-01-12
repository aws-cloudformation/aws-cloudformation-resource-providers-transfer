package software.amazon.transfer.user;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DeleteUserRequest;
import software.amazon.awssdk.services.transfer.model.DeleteUserResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;

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

        Translator.ensureServerIdAndUserNameInModel(newModel);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> proxy.initiate(
                                "AWS-Transfer-User::Delete",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(DeleteHandler::translateToDeleteRequest)
                        .makeServiceCall(this::deleteUser)
                        .handleError((ignored, exception, client, model, context) ->
                                handleError(DELETE, exception, model, context, clientRequestToken))
                        .progress())
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private static DeleteUserRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteUserRequest.builder()
                .serverId(model.getServerId())
                .userName(model.getUserName())
                .build();
    }

    private DeleteUserResponse deleteUser(DeleteUserRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DeleteUserResponse response = client.injectCredentialsAndInvokeV2(request, transferClient::deleteUser);
            log("successfully deleted.", userIdentifier(request.serverId(), request.userName()));
            return response;
        }
    }
}
