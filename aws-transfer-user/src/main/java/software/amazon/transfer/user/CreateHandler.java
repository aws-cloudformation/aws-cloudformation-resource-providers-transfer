package software.amazon.transfer.user;

import static software.amazon.transfer.user.translators.Translator.translateToSdkHomeDirectoryMappings;
import static software.amazon.transfer.user.translators.Translator.translateToSdkPosixProfile;
import static software.amazon.transfer.user.translators.Translator.translateToSdkTags;

import java.util.List;
import java.util.Optional;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateUserRequest;
import software.amazon.awssdk.services.transfer.model.CreateUserResponse;
import software.amazon.awssdk.services.transfer.model.HomeDirectoryType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;

public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        final ResourceModel newModel = request.getDesiredResourceState();

        // Needed for tag updates
        final String userArn = Translator.generateUserArn(request);
        newModel.setArn(userArn);

        final List<String> keysToAdd = translateToSShPublicKeyBodies(newModel);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> createUser(proxy, proxyClient, clientRequestToken, progress))
                .then(progress ->
                        importSshPublicKeys(proxy, proxyClient, clientRequestToken, CREATE, keysToAdd, progress))
                .then(progress -> addTags(progress, request, newModel, proxy, proxyClient, callbackContext))
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createUser(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            ProgressEvent<ResourceModel, CallbackContext> progress) {
        return proxy.initiate(
                        "AWS-Transfer-User::Create",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(CreateHandler::translateToCreateRequest)
                .makeServiceCall(this::createUser)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(CREATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private static CreateUserRequest translateToCreateRequest(final ResourceModel model) {
        String homeDirectoryType =
                Optional.ofNullable(model.getHomeDirectoryType()).orElse(HomeDirectoryType.PATH.name());

        return CreateUserRequest.builder()
                .serverId(model.getServerId())
                .userName(model.getUserName())
                .policy(model.getPolicy())
                .role(model.getRole())
                .homeDirectoryType(homeDirectoryType)
                .homeDirectory(model.getHomeDirectory())
                .homeDirectoryMappings(translateToSdkHomeDirectoryMappings(model.getHomeDirectoryMappings()))
                .posixProfile(translateToSdkPosixProfile(model.getPosixProfile()))
                .tags(translateToSdkTags(model.getTags()))
                .build();
    }

    private CreateUserResponse createUser(CreateUserRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            CreateUserResponse response = client.injectCredentialsAndInvokeV2(request, transferClient::createUser);
            log("user created successfully", userIdentifier(request.serverId(), request.userName()));
            return response;
        }
    }
}
