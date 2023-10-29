package software.amazon.transfer.user;

import static software.amazon.transfer.user.translators.Translator.translateToSdkPosixProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.HomeDirectoryType;
import software.amazon.awssdk.services.transfer.model.UpdateUserRequest;
import software.amazon.awssdk.services.transfer.model.UpdateUserResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;

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
        final ResourceModel oldModel = request.getPreviousResourceState();
        final ResourceModel newModel = request.getDesiredResourceState();

        Translator.ensureServerIdAndUserNameInModel(oldModel);
        Translator.ensureServerIdAndUserNameInModel(newModel);

        final List<String> keysToDelete = translateToSShPublicKeyBodies(oldModel);
        final List<String> requestedKeys = translateToSShPublicKeyBodies(newModel);

        // Here we prune the list of current keys to eliminate
        // existing keys by checking the body for matches.
        // What is left should be deleted to complete the update.
        // The truly new keys are added to the list.
        final List<String> keysToAdd = new ArrayList<>();
        for (String key : requestedKeys) {
            if (!keysToDelete.remove(key)) {
                keysToAdd.add(key);
            }
        }

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> updateUser(proxy, proxyClient, clientRequestToken, progress))
                .then(
                        progress ->
                                deleteSshPublicKeys(
                                        proxy,
                                        proxyClient,
                                        clientRequestToken,
                                        UPDATE,
                                        keysToDelete,
                                        progress))
                .then(
                        progress ->
                                importSshPublicKeys(
                                        proxy,
                                        proxyClient,
                                        clientRequestToken,
                                        UPDATE,
                                        keysToAdd,
                                        progress))
                .then(
                        progress ->
                                addTags(
                                        progress,
                                        request,
                                        newModel,
                                        proxy,
                                        proxyClient,
                                        callbackContext))
                .then(
                        progress ->
                                removeTags(
                                        progress,
                                        request,
                                        newModel,
                                        proxy,
                                        proxyClient,
                                        callbackContext))
                .then(
                        progress ->
                                new ReadHandler()
                                        .handleRequest(
                                                proxy,
                                                request,
                                                callbackContext,
                                                proxyClient,
                                                logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUser(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            ProgressEvent<ResourceModel, CallbackContext> progress) {
        return proxy.initiate(
                        "AWS-Transfer-User::Update",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(this::translateToUpdateRequest)
                .makeServiceCall(this::updateUser)
                .handleError(
                        (ignored, exception, client, model, context) ->
                                handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private UpdateUserResponse updateUser(
            UpdateUserRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            UpdateUserResponse response =
                    client.injectCredentialsAndInvokeV2(request, transferClient::updateUser);
            log(
                    "has successfully been updated.",
                    userIdentifier(request.serverId(), request.userName()));
            return response;
        }
    }

    private UpdateUserRequest translateToUpdateRequest(final ResourceModel model) {
        HomeDirectoryType homeDirectoryType =
                HomeDirectoryType.valueOf(
                        Optional.ofNullable(model.getHomeDirectoryType())
                                .orElse(HomeDirectoryType.PATH.name()));

        return UpdateUserRequest.builder()
                .serverId(model.getServerId())
                .userName(model.getUserName())
                .policy(model.getPolicy())
                .role(model.getRole())
                .homeDirectoryType(homeDirectoryType)
                .homeDirectory(model.getHomeDirectory())
                .homeDirectoryMappings(
                        Translator.translateToSdkHomeDirectoryMappings(
                                model.getHomeDirectoryMappings()))
                .posixProfile(translateToSdkPosixProfile(model.getPosixProfile()))
                .build();
    }
}
